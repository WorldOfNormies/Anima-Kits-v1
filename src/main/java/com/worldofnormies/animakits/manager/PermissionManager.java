package com.worldofnormies.animakits.manager;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * PermissionManager – stores timed/permanent per-player AnimaKits permissions.
 * Expiry of -1 means permanent.
 *
 * Per-kit claim permissions follow the node pattern:
 * anima.kits.claim.<kitName>
 * Custom bypass cooldown permissions follow the node pattern:
 * anima.kits.claimfree.<kitName>
 *
 * Use {@link #grantClaimPermission} / {@link #revokeClaimPermission} / {@link #hasClaimPermission}
 * as convenience wrappers for those specific nodes.
 *
 * GLOBAL PERMISSIONS: Use {@link #grantGlobal} to grant a permission to ALL players,
 * including ones who haven't joined yet. These are applied on PlayerJoinEvent via
 * {@link #applyGlobalsToPlayer(UUID)}. This is what powers @a / @e selector grants
 * so new players automatically receive the same permission when they first join.
 */
public class PermissionManager {

    /** Prefix used for per-kit claim permission nodes stored in this manager. */
    public static final String CLAIM_PREFIX = "anima.kits.claim.";

    /** Special UUID key used in permissions.yml to store global (all-player) permissions. */
    private static final UUID GLOBAL_KEY = new UUID(0, 0);

    private final AnimaKitsPlugin plugin;
    private final File file;
    private FileConfiguration config;

    /** playerUUID → permNode → expiryEpochSecond (-1 = permanent) */
    private final Map<UUID, Map<String, Long>> data = new HashMap<>();

    public PermissionManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "permissions.yml");
    }

    public void load() {
        data.clear();
        if (!file.exists()) return;
        config = YamlConfiguration.loadConfiguration(file);
        for (String uuidStr : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Long> perms = new HashMap<>();
                var section = config.getConfigurationSection(uuidStr);
                if (section != null) {
                    for (String perm : section.getKeys(false)) {
                        perms.put(perm, section.getLong(perm));
                    }
                }
                data.put(uuid, perms);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void save() {
        config = new YamlConfiguration();
        for (var entry : data.entrySet()) {
            for (var permEntry : entry.getValue().entrySet()) {
                config.set(entry.getKey() + "." + permEntry.getKey(), permEntry.getValue());
            }
        }
        try { config.save(file); } catch (IOException e) {
            plugin.getLogger().severe("Could not save permissions.yml: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Generic perm API
    // -----------------------------------------------------------------------

    public void grant(UUID player, String perm, long durationSeconds) {
        long expiry = durationSeconds == -1 ? -1 : Instant.now().getEpochSecond() + durationSeconds;
        data.computeIfAbsent(player, k -> new HashMap<>()).put(perm, expiry);
        save();
    }

    public boolean revoke(UUID player, String perm) {
        Map<String, Long> perms = data.get(player);
        if (perms == null || !perms.containsKey(perm)) return false;
        perms.remove(perm);
        save();
        return true;
    }

    public boolean has(UUID player, String perm) {
        Map<String, Long> perms = data.get(player);
        if (perms == null) return false;
        Long expiry = perms.get(perm);
        if (expiry == null) return false;
        if (expiry == -1) return true;
        if (Instant.now().getEpochSecond() > expiry) {
            perms.remove(perm); // lazy cleanup
            return false;
        }
        return true;
    }

    /** Returns all perm → expiry entries for a player (expired ones removed lazily). */
    public Map<String, Long> getAll(UUID player) {
        Map<String, Long> perms = data.get(player);
        if (perms == null) return Collections.emptyMap();
        long now = Instant.now().getEpochSecond();
        perms.entrySet().removeIf(e -> e.getValue() != -1 && now > e.getValue());
        return Collections.unmodifiableMap(perms);
    }

    // -----------------------------------------------------------------------
    // Global permissions (apply to ALL players, including future joiners)
    // -----------------------------------------------------------------------

    /**
     * Grant a permission globally — it will apply to every player, including ones
     * who haven't joined yet. Call {@link #applyGlobalsToPlayer(UUID)} on PlayerJoinEvent.
     */
    public void grantGlobal(String perm, long durationSeconds) {
        grant(GLOBAL_KEY, perm, durationSeconds);
    }

    /** Revoke a global permission. Does NOT remove it from players who already received it. */
    public boolean revokeGlobal(String perm) {
        return revoke(GLOBAL_KEY, perm);
    }

    /** Returns true if this perm is set globally. */
    public boolean hasGlobal(String perm) {
        return has(GLOBAL_KEY, perm);
    }

    /** Returns all current global permissions. */
    public Map<String, Long> getAllGlobals() {
        return getAll(GLOBAL_KEY);
    }

    /**
     * Apply all current global permissions to a specific player (call on PlayerJoinEvent).
     * Copies the global perm+expiry to the player's own entry so it shows in /perm show
     * and is checked normally by {@link #has}.
     */
    public void applyGlobalsToPlayer(UUID player) {
        Map<String, Long> globals = data.get(GLOBAL_KEY);
        if (globals == null || globals.isEmpty()) return;
        long now = Instant.now().getEpochSecond();
        Map<String, Long> playerPerms = data.computeIfAbsent(player, k -> new HashMap<>());
        boolean changed = false;
        for (Map.Entry<String, Long> e : globals.entrySet()) {
            // Skip expired globals
            if (e.getValue() != -1 && now > e.getValue()) continue;
            // Only set if the player doesn't already have this perm (don't overwrite a longer expiry)
            if (!playerPerms.containsKey(e.getKey())) {
                playerPerms.put(e.getKey(), e.getValue());
                changed = true;
            }
        }
        if (changed) save();
    }

    // -----------------------------------------------------------------------
    // Per-kit claim permission helpers
    // -----------------------------------------------------------------------

    /**
     * Grant a player permission to claim {@code kitName}.
     *
     * @param player          the player's UUID
     * @param kitName         plain (lower-case) kit name
     * @param durationSeconds seconds until expiry, or {@code -1} for permanent
     */
    public void grantClaimPermission(UUID player, String kitName, long durationSeconds) {
        grant(player, CLAIM_PREFIX + kitName, durationSeconds);
    }

    /**
     * Revoke a player's permission to claim {@code kitName}.
     *
     * @return {@code true} if the permission existed and was removed
     */
    public boolean revokeClaimPermission(UUID player, String kitName) {
        return revoke(player, CLAIM_PREFIX + kitName);
    }

    /**
     * Check whether a player currently holds a valid (non-expired) per-kit claim
     * permission managed by this manager.
     *
     * <p>Note: this only checks the custom permission store. Bukkit-level permissions
     * (e.g. granted by a permission plugin) are checked separately via
     * {@link org.bukkit.entity.Player#hasPermission(String)}.
     *
     * @param player  the player's UUID
     * @param kitName plain kit name
     * @return {@code true} if the stored permission is valid
     */
    public boolean hasClaimPermission(UUID player, String kitName) {
        // Wildcard stored claim covers all kits
        if (has(player, "anima.kits.claim.*")) return true;
        // Check global wildcard
        if (hasGlobal("anima.kits.claim.*")) return true;
        if (hasGlobal(CLAIM_PREFIX + kitName)) return true;
        return has(player, CLAIM_PREFIX + kitName);
    }

    /**
     * Check whether a player holds a valid permission to bypass a kit's cooldown limits.
     *
     * @param player  the player's UUID
     * @param kitName plain kit name
     * @return {@code true} if the player holds a valid cooldown bypass node
     */
    public boolean hasClaimFreePermission(UUID player, String kitName) {
        if (has(player, "anima.kits.claimfree.*")) return true;
        if (hasGlobal("anima.kits.claimfree.*")) return true;
        if (hasGlobal("anima.kits.claimfree." + kitName)) return true;
        return has(player, "anima.kits.claimfree." + kitName);
    }

    /**
     * Return all claim-permission entries for a player (nodes starting with
     * {@link #CLAIM_PREFIX}), with expired ones removed lazily.
     */
    public Map<String, Long> getAllClaimPermissions(UUID player) {
        Map<String, Long> all = getAll(player);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map.Entry<String, Long> e : all.entrySet()) {
            if (e.getKey().startsWith(CLAIM_PREFIX) || e.getKey().equals("anima.kits.claim.*")) {
                result.put(e.getKey(), e.getValue());
            }
        }
        return Collections.unmodifiableMap(result);
    }
}
