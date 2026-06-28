package com.worldofnormies.animaranks.manager;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animaranks.Rank;
import com.worldofnormies.animaranks.gui.AnimaRanksMainGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * RankManager – loads/saves ranks.yml and player-rank.yml.
 * Tracks playtime per player (seconds online).
 * Refreshes open AnimaRanksMainGUI instances on change.
 */
public class RankManager {

    private final AnimaKitsPlugin plugin;

    // ── Data files ─────────────────────────────────────────────────
    private final File ranksFile;
    private final File playerRankFile;
    private FileConfiguration ranksConfig;
    private FileConfiguration playerRankConfig;

    // ── In-memory ──────────────────────────────────────────────────
    /** id → Rank, insertion order preserved */
    private final Map<String, Rank> ranks = new LinkedHashMap<>();

    /** playerUUID → rankId */
    private final Map<UUID, String> playerRanks = new ConcurrentHashMap<>();

    /** playerUUID → total playtime in seconds (persisted) */
    private final Map<UUID, Long> playtime = new ConcurrentHashMap<>();

    /** playerUUID → epoch second when they logged in this session */
    private final Map<UUID, Long> sessionStart = new ConcurrentHashMap<>();

    /** playerUUID → rank expiry epoch second (-1 = permanent) */
    private final Map<UUID, Long> rankExpiry = new ConcurrentHashMap<>();

    /** Open GUI instances for live refresh */
    private final Map<UUID, AnimaRanksMainGUI> openBrowsers = new ConcurrentHashMap<>();

    public RankManager(AnimaKitsPlugin plugin) {
        this.plugin      = plugin;
        File folder = new File(plugin.getDataFolder(), "ranks");
        if (!folder.exists()) folder.mkdirs();
        this.ranksFile   = new File(folder, "ranks.yml");
        this.playerRankFile = new File(folder, "player-ranks.yml");
    }

    // ══════════════════════════════════════════════════════════════
    //  PERSISTENCE
    // ══════════════════════════════════════════════════════════════

    public void load() {
        loadRanks();
        loadPlayerRanks();
    }

    private void loadRanks() {
        ranks.clear();
        if (!ranksFile.exists()) return;
        ranksConfig = YamlConfiguration.loadConfiguration(ranksFile);
        ConfigurationSection sec = ranksConfig.getConfigurationSection("ranks");
        if (sec == null) return;

        for (String id : sec.getKeys(false)) {
            ConfigurationSection rs = sec.getConfigurationSection(id);
            if (rs == null) continue;
            String displayName = rs.getString("display-name", id);
            int    hierarchy   = rs.getInt("hierarchy", 99);
            Rank rank = new Rank(id, displayName, hierarchy);
            rank.setPrefix(rs.getString("prefix", ""));
            rank.setSuffix(rs.getString("suffix", ""));
            rank.setColorName(rs.getString("color-name", ""));
            rank.setChatColor(rs.getString("chat-color", ""));
            String matName = rs.getString("icon-material", "");
            if (!matName.isBlank()) {
                try { rank.setIconMaterial(Material.valueOf(matName.toUpperCase())); }
                catch (IllegalArgumentException ignored) {}
            }
            rank.setKitId(rs.getString("kit-id", ""));

            // Rankup
            rank.setRankable(rs.getBoolean("rankable", false));
            rank.setRankupPlaytime(rs.getLong("rankup-playtime", 0));
            rank.setRankupPrice(rs.getDouble("rankup-price", 0));

            // Perks
            rank.setHomeLimit(rs.getInt("home-limit", 2));
            rank.setRepairCooldown(rs.getInt("repair-cooldown", 0));
            rank.setCanRepairAll(rs.getBoolean("can-repair-all", false));
            rank.setRtpCooldown(rs.getInt("rtp-cooldown", 300));
            rank.setEchestRows(rs.getInt("echest-rows", 1));

            // Buyable
            rank.setBuyable(rs.getBoolean("buyable", false));
            rank.setBuyableWithAnimaz(rs.getBoolean("buyable-with-animaz", false));
            rank.setBuyPrice(rs.getDouble("buy-price", 0));
            rank.setBuyDuration(rs.getLong("buy-duration", -1));

            // Permissions
            ConfigurationSection permSec = rs.getConfigurationSection("permissions");
            if (permSec != null) {
                for (String node : permSec.getKeys(false)) {
                    rank.setPermission(node, permSec.getBoolean(node, true));
                }
            }
            ranks.put(id, rank);
        }
    }

    public void saveRanks() {
        if (ranksConfig == null) ranksConfig = new YamlConfiguration();
        ranksConfig.set("ranks", null);
        for (Rank rank : ranks.values()) {
            String p = "ranks." + rank.getId();
            ranksConfig.set(p + ".display-name", rank.getDisplayName());
            ranksConfig.set(p + ".hierarchy",    rank.getHierarchy());
            ranksConfig.set(p + ".prefix",        rank.getPrefix());
            ranksConfig.set(p + ".suffix",        rank.getSuffix());
            ranksConfig.set(p + ".color-name",    rank.getColorName());
            ranksConfig.set(p + ".chat-color",    rank.getChatColor());
            ranksConfig.set(p + ".icon-material", rank.getIconMaterial().name());
            ranksConfig.set(p + ".kit-id",        rank.getKitId());
            ranksConfig.set(p + ".rankable",      rank.isRankable());
            ranksConfig.set(p + ".rankup-playtime", rank.getRankupPlaytime());
            ranksConfig.set(p + ".rankup-price",  rank.getRankupPrice());
            ranksConfig.set(p + ".home-limit",    rank.getHomeLimit());
            ranksConfig.set(p + ".repair-cooldown", rank.getRepairCooldown());
            ranksConfig.set(p + ".can-repair-all", rank.canRepairAll());
            ranksConfig.set(p + ".rtp-cooldown",  rank.getRtpCooldown());
            ranksConfig.set(p + ".echest-rows",   rank.getEchestRows());
            ranksConfig.set(p + ".buyable",       rank.isBuyable());
            ranksConfig.set(p + ".buyable-with-animaz", rank.isBuyableWithAnimaz());
            ranksConfig.set(p + ".buy-price",     rank.getBuyPrice());
            ranksConfig.set(p + ".buy-duration",  rank.getBuyDuration());
            for (Map.Entry<String, Boolean> e : rank.getPermissions().entrySet()) {
                ranksConfig.set(p + ".permissions." + e.getKey(), e.getValue());
            }
        }
        try { ranksConfig.save(ranksFile); }
        catch (IOException e) { plugin.getLogger().severe("Could not save ranks.yml: " + e.getMessage()); }
    }

    private void loadPlayerRanks() {
        playerRanks.clear(); playtime.clear(); rankExpiry.clear();
        if (!playerRankFile.exists()) return;
        playerRankConfig = YamlConfiguration.loadConfiguration(playerRankFile);
        for (String uuidStr : playerRankConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String rankId = playerRankConfig.getString(uuidStr + ".rank", "");
                if (!rankId.isBlank()) playerRanks.put(uuid, rankId);
                playtime.put(uuid, playerRankConfig.getLong(uuidStr + ".playtime", 0));
                long expiry = playerRankConfig.getLong(uuidStr + ".expiry", -1);
                rankExpiry.put(uuid, expiry);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void savePlayerRanks() {
        playerRankConfig = new YamlConfiguration();
        Set<UUID> all = new HashSet<>();
        all.addAll(playerRanks.keySet());
        all.addAll(playtime.keySet());
        for (UUID uuid : all) {
            String uStr = uuid.toString();
            if (playerRanks.containsKey(uuid))
                playerRankConfig.set(uStr + ".rank", playerRanks.get(uuid));
            playerRankConfig.set(uStr + ".playtime", getPlaytime(uuid));
            playerRankConfig.set(uStr + ".expiry", rankExpiry.getOrDefault(uuid, -1L));
        }
        try { playerRankConfig.save(playerRankFile); }
        catch (IOException e) { plugin.getLogger().severe("Could not save player-ranks.yml: " + e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════════════
    //  RANK CRUD
    // ══════════════════════════════════════════════════════════════

    public boolean rankExists(String id) { return ranks.containsKey(id.toLowerCase()); }

    public Rank getRank(String id) { return id == null ? null : ranks.get(id.toLowerCase()); }

    /** Returns ranks sorted by hierarchy (0 = top). */
    public List<Rank> getAllRanks() {
        return ranks.values().stream()
                .sorted(Comparator.comparingInt(Rank::getHierarchy))
                .collect(Collectors.toList());
    }

    public Rank createRank(String id, String displayName, int hierarchy) {
        String key = id.toLowerCase();
        Rank rank = new Rank(key, displayName, hierarchy);
        ranks.put(key, rank);
        saveRanks();
        refreshBrowsers();
        return rank;
    }

    public boolean deleteRank(String id) {
        if (ranks.remove(id.toLowerCase()) == null) return false;
        // unassign all players with this rank
        playerRanks.entrySet().removeIf(e -> e.getValue().equalsIgnoreCase(id));
        saveRanks();
        savePlayerRanks();
        refreshBrowsers();
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  PLAYER RANK ASSIGNMENT
    // ══════════════════════════════════════════════════════════════

    public String getPlayerRankId(UUID uuid) { return playerRanks.getOrDefault(uuid, ""); }

    public Rank getPlayerRank(UUID uuid) { return getRank(getPlayerRankId(uuid)); }

    /** Returns a list of player UUIDs assigned to a specific rank. */
    public List<UUID> getPlayersWithRank(String rankId) {
        String target = rankId.toLowerCase();
        return playerRanks.entrySet().stream()
                .filter(e -> e.getValue().equalsIgnoreCase(target))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Assigns a rank to a player.
     * @param durationSeconds -1 = permanent
     */
    public void setPlayerRank(UUID uuid, String rankId, long durationSeconds) {
        playerRanks.put(uuid, rankId.toLowerCase());
        long expiry = durationSeconds <= 0 ? -1L : Instant.now().getEpochSecond() + durationSeconds;
        rankExpiry.put(uuid, expiry);
        savePlayerRanks();
        // Apply/remove permissions for online player
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            applyRankPermissions(p, rankId);
            updatePlayerDisplayName(p);
        }
        refreshBrowsers();
    }

    private void updatePlayerDisplayName(Player player) {
        Rank rank = getPlayerRank(player.getUniqueId());
        if (rank == null) return;

        String nameColor = rank.getColorName().isBlank() ? "" : com.worldofnormies.animakits.util.ColorUtil.colorize(rank.getColorName());
        String displayName = nameColor + player.getName() + org.bukkit.ChatColor.RESET;

        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);
    }

    public void removePlayerRank(UUID uuid) {
        String old = playerRanks.remove(uuid);
        rankExpiry.remove(uuid);
        savePlayerRanks();
        Player p = Bukkit.getPlayer(uuid);
        if (p != null && old != null) revokeRankPermissions(p, old);
        refreshBrowsers();
    }

    /** Check expiry and remove rank if expired. Call on join / periodically. */
    public void checkExpiry(UUID uuid) {
        Long expiry = rankExpiry.get(uuid);
        if (expiry == null || expiry == -1) return;
        if (Instant.now().getEpochSecond() >= expiry) removePlayerRank(uuid);
    }

    public long getRankExpiry(UUID uuid) { return rankExpiry.getOrDefault(uuid, -1L); }

    // ══════════════════════════════════════════════════════════════
    //  PLAYTIME
    // ══════════════════════════════════════════════════════════════

    public void startSession(UUID uuid) {
        sessionStart.put(uuid, Instant.now().getEpochSecond());
    }

    public void endSession(UUID uuid) {
        Long start = sessionStart.remove(uuid);
        if (start == null) return;
        long elapsed = Instant.now().getEpochSecond() - start;
        playtime.merge(uuid, elapsed, Long::sum);
        savePlayerRanks();
    }

    /** Returns total playtime in seconds (including current session). */
    public long getPlaytime(UUID uuid) {
        long persisted = playtime.getOrDefault(uuid, 0L);
        Long start = sessionStart.get(uuid);
        if (start != null) persisted += Instant.now().getEpochSecond() - start;
        return persisted;
    }

    // ══════════════════════════════════════════════════════════════
    //  PERMISSION APPLICATION (Bukkit attachment-based)
    // ══════════════════════════════════════════════════════════════

    public void applyRankPermissions(Player player, String rankId) {
        Rank rank = getRank(rankId);
        if (rank == null) return;
        for (Map.Entry<String, Boolean> e : rank.getPermissions().entrySet()) {
            player.addAttachment(plugin, e.getKey(), e.getValue());
        }
    }

    public void revokeRankPermissions(Player player, String rankId) {
        // Bukkit attachments are session-based; recalculate all on next join.
        // For now we just remove all and re-apply (simple approach).
        player.getEffectivePermissions().stream()
                .filter(pi -> {
                    Rank r = getRank(rankId);
                    return r != null && r.getPermissions().containsKey(pi.getPermission());
                })
                .forEach(pi -> {
                    var att = player.getEffectivePermissions().stream()
                            .filter(x -> x.getPermission().equals(pi.getPermission()))
                            .findFirst().orElse(null);
                    // Attachments from this plugin will be cleaned up on player rejoin
                });
    }

    /** Called on PlayerJoinEvent to re-apply persisted rank permissions. */
    public void onPlayerJoin(Player player) {
        checkExpiry(player.getUniqueId());
        startSession(player.getUniqueId());
        String rankId = getPlayerRankId(player.getUniqueId());
        if (!rankId.isBlank()) applyRankPermissions(player, rankId);
    }

    /** Called on PlayerQuitEvent. */
    public void onPlayerQuit(Player player) {
        endSession(player.getUniqueId());
    }

    // ══════════════════════════════════════════════════════════════
    //  RANKUP LOGIC
    // ══════════════════════════════════════════════════════════════

    /**
     * Returns the next rank a player can rank up TO (one step higher in hierarchy,
     * i.e. lower hierarchy number), or null if already at top or no rankable rank exists.
     */
    public Rank getNextRankUp(UUID uuid) {
        Rank current = getPlayerRank(uuid);
        List<Rank> sorted = getAllRanks(); // sorted by hierarchy asc (0 = top)
        if (current == null) return null;
        // next rank UP = lower hierarchy number (one step above current)
        return sorted.stream()
                .filter(r -> r.getHierarchy() < current.getHierarchy() && r.isRankable())
                .max(Comparator.comparingInt(Rank::getHierarchy)) // closest one above
                .orElse(null);
    }

    /**
     * Returns whether a player meets rankup requirements for the given rank.
     */
    public boolean meetsRankupRequirements(UUID uuid, Rank target) {
        long pt = getPlaytime(uuid);
        return pt >= target.getRankupPlaytime(); // economy check handled in command
    }

    // ══════════════════════════════════════════════════════════════
    //  GUI BROWSER TRACKING
    // ══════════════════════════════════════════════════════════════

    public void registerBrowser(Player p, AnimaRanksMainGUI gui)   { openBrowsers.put(p.getUniqueId(), gui); }
    public void unregisterBrowser(Player p)                         { openBrowsers.remove(p.getUniqueId()); }

    private void refreshBrowsers() {
        openBrowsers.values().forEach(AnimaRanksMainGUI::refresh);
    }

}
