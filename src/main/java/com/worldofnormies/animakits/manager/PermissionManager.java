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
 */
public class PermissionManager {

    private final AnimaKitsPlugin plugin;
    private final File file;
    private FileConfiguration config;

    /** playerUUID → permNode → expiryEpochSecond (-1 = permanent) */
    private final Map<UUID, Map<String, Long>> data = new HashMap<>();

    /** Global permissions for @a (persists for new players) */
    private final Map<String, Long> globalData = new HashMap<>();

    public PermissionManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "permissions.yml");
    }

    public void load() {
        data.clear();
        globalData.clear();
        if (!file.exists()) return;
        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            if (key.equalsIgnoreCase("global")) {
                var section = config.getConfigurationSection(key);
                if (section != null) {
                    for (String perm : section.getKeys(false)) {
                        globalData.put(perm, section.getLong(perm));
                    }
                }
                continue;
            }
            try {
                UUID uuid = UUID.fromString(key);
                Map<String, Long> perms = new HashMap<>();
                var section = config.getConfigurationSection(key);
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
        for (var entry : globalData.entrySet()) {
            config.set("global." + entry.getKey(), entry.getValue());
        }
        try { config.save(file); } catch (IOException e) {
            plugin.getLogger().severe("Could not save permissions.yml: " + e.getMessage());
        }
    }

    public void grant(UUID player, String perm, long durationSeconds) {
        long expiry = durationSeconds == -1 ? -1 : Instant.now().getEpochSecond() + durationSeconds;
        data.computeIfAbsent(player, k -> new HashMap<>()).put(perm, expiry);
        save();
    }

    public void grantGlobal(String perm, long durationSeconds) {
        long expiry = durationSeconds == -1 ? -1 : Instant.now().getEpochSecond() + durationSeconds;
        globalData.put(perm, expiry);
        save();
    }

    public boolean revoke(UUID player, String perm) {
        Map<String, Long> perms = data.get(player);
        if (perms == null || !perms.containsKey(perm)) return false;
        perms.remove(perm);
        save();
        return true;
    }

    public boolean revokeGlobal(String perm) {
        if (!globalData.containsKey(perm)) return false;
        globalData.remove(perm);
        save();
        return true;
    }

    public boolean has(UUID player, String perm) {
        // Check global first
        Long globalExpiry = globalData.get(perm);
        if (globalExpiry != null) {
            if (globalExpiry == -1 || Instant.now().getEpochSecond() <= globalExpiry) return true;
            else {
                globalData.remove(perm);
                save();
            }
        }

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
        Map<String, Long> perms = data.getOrDefault(player, Collections.emptyMap());
        long now = Instant.now().getEpochSecond();
        perms.entrySet().removeIf(e -> e.getValue() != -1 && now > e.getValue());
        return Collections.unmodifiableMap(perms);
    }
}
