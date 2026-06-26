package com.worldofnormies.animaranks;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RankManager {
    private final AnimaKitsPlugin plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<String, Rank> ranks = new LinkedHashMap<>();
    private final Map<UUID, String> playerRanks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerPlaytime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastRepair = new ConcurrentHashMap<>();

    public RankManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ranks.yml");
    }

    public void load() {
        ranks.clear();
        if (!file.exists()) {
            createDefaultRank();
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("ranks");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Rank rank = new Rank(key, section.getInt(key + ".priority"));
                rank.setPrefix(section.getString(key + ".prefix", ""));
                rank.setSuffix(section.getString(key + ".suffix", ""));
                rank.setNameColor(section.getString(key + ".nameColor", ""));
                rank.setChatColor(section.getString(key + ".chatColor", ""));
                rank.getPermissions().addAll(section.getStringList(key + ".permissions"));
                rank.getKits().addAll(section.getStringList(key + ".kits"));
                rank.setRankable(section.getBoolean(key + ".isRankable", false));
                rank.setBuyable(section.getBoolean(key + ".isBuyable", false));
                rank.setPrice(section.getDouble(key + ".price", 0));
                rank.setRequiredPlaytime(section.getLong(key + ".requiredPlaytime", 0));
                ranks.put(key, rank);
            }
        }
        loadPlayerRanks();
    }

    private void createDefaultRank() {
        Rank def = new Rank("Default", 100);
        def.setPrefix("<gray>[Member]</gray> ");
        ranks.put("Default", def);
        save();
    }

    public void save() {
        config = new YamlConfiguration();
        for (Rank rank : ranks.values()) {
            String path = "ranks." + rank.getName();
            config.set(path + ".priority", rank.getPriority());
            config.set(path + ".prefix", rank.getPrefix());
            config.set(path + ".suffix", rank.getSuffix());
            config.set(path + ".nameColor", rank.getNameColor());
            config.set(path + ".chatColor", rank.getChatColor());
            config.set(path + ".permissions", rank.getPermissions());
            config.set(path + ".kits", rank.getKits());
            config.set(path + ".isRankable", rank.isRankable());
            config.set(path + ".isBuyable", rank.isBuyable());
            config.set(path + ".price", rank.getPrice());
            config.set(path + ".requiredPlaytime", rank.getRequiredPlaytime());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save ranks.yml");
        }
        savePlayerRanks();
    }

    private void loadPlayerRanks() {
        File pFile = new File(plugin.getDataFolder(), "player_ranks.yml");
        if (!pFile.exists()) return;
        FileConfiguration pConfig = YamlConfiguration.loadConfiguration(pFile);
        for (String uuidStr : pConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            playerRanks.put(uuid, pConfig.getString(uuidStr + ".rank", "Default"));
            playerPlaytime.put(uuid, pConfig.getLong(uuidStr + ".playtime", 0));
            lastRepair.put(uuid, pConfig.getLong(uuidStr + ".lastRepair", 0));
        }
    }

    private void savePlayerRanks() {
        File pFile = new File(plugin.getDataFolder(), "player_ranks.yml");
        FileConfiguration pConfig = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : playerRanks.entrySet()) {
            pConfig.set(entry.getKey().toString() + ".rank", entry.getValue());
            pConfig.set(entry.getKey().toString() + ".playtime", playerPlaytime.getOrDefault(entry.getKey(), 0L));
            pConfig.set(entry.getKey().toString() + ".lastRepair", lastRepair.getOrDefault(entry.getKey(), 0L));
        }
        try {
            pConfig.save(pFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player_ranks.yml");
        }
    }

    public Rank getRank(String name) { return ranks.get(name); }
    public Collection<Rank> getAllRanks() { return ranks.values(); }
    public void addRank(Rank rank) { ranks.put(rank.getName(), rank); }
    public void removeRank(String name) { ranks.remove(name); }

    public String getPlayerRankName(UUID uuid) { return playerRanks.getOrDefault(uuid, "Default"); }
    public Rank getPlayerRank(UUID uuid) { return getRank(getPlayerRankName(uuid)); }
    public void setPlayerRank(UUID uuid, String rankName) { playerRanks.put(uuid, rankName); }

    public long getPlaytime(UUID uuid) { return playerPlaytime.getOrDefault(uuid, 0L); }
    public void addPlaytime(UUID uuid, long seconds) {
        playerPlaytime.put(uuid, getPlaytime(uuid) + seconds);
    }

    public long getRepairCooldown(UUID uuid) {
        long last = lastRepair.getOrDefault(uuid, 0L);
        if (last == 0) return 0;
        long now = System.currentTimeMillis() / 1000;

        long cooldown = 60; // Default
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(uuid);
        if (player != null) {
            for (org.bukkit.permissions.PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
                String perm = pai.getPermission();
                if (perm.startsWith("anima.itemedit.repair.cooldown.")) {
                    try {
                        cooldown = Long.parseLong(perm.substring("anima.itemedit.repair.cooldown.".length()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        long diff = now - last;
        return diff >= cooldown ? 0 : (cooldown - diff);
    }

    public void setRepairLastUsed(UUID uuid) {
        lastRepair.put(uuid, System.currentTimeMillis() / 1000);
    }
}
