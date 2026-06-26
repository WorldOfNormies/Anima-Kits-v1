package com.worldofnormies.animaranks;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RankManager {
    private final AnimaKitsPlugin plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<String, Rank> ranks = new LinkedHashMap<>();
    private final Map<UUID, String> playerRanks = new HashMap<>();

    public RankManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ranks.yml");
        loadRanks();
    }

    public void loadRanks() {
        ranks.clear();
        if (!file.exists()) {
            createDefaultRanks();
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("ranks");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection rSec = section.getConfigurationSection(id);
                if (rSec == null) continue;
                Rank rank = new Rank(id, rSec.getInt("hierarchy"));
                rank.setPrefix(rSec.getString("prefix", ""));
                rank.setSuffix(rSec.getString("suffix", ""));
                rank.setNameColor(rSec.getString("nameColor", ""));
                rank.setChatColor(rSec.getString("chatColor", ""));
                rank.setRankable(rSec.getBoolean("rankable", false));
                rank.setRequiredPlaytime(rSec.getLong("requiredPlaytime", 0));
                rank.setRankupPrice(rSec.getDouble("rankupPrice", 0));
                rank.setBuyable(rSec.getBoolean("buyable", false));
                rank.setBuyPrice(rSec.getDouble("buyPrice", 0));
                rank.setBuyDuration(rSec.getLong("buyDuration", -1));
                rank.setPermissions(rSec.getStringList("permissions"));
                ranks.put(id.toLowerCase(), rank);
            }
        }

        ConfigurationSection players = config.getConfigurationSection("players");
        if (players != null) {
            for (String uuidStr : players.getKeys(false)) {
                playerRanks.put(UUID.fromString(uuidStr), players.getString(uuidStr));
            }
        }
    }

    private void createDefaultRanks() {
        Rank owner = new Rank("Owner", 0);
        owner.setPrefix("<gradient:#FF0000:#FFFF00>[OWNER]</gradient> ");
        owner.setNameColor("<gradient:#FF0000:#FFFF00><bold>");
        ranks.put("owner", owner);
        saveRanks();
    }

    public void saveRanks() {
        config = new YamlConfiguration();
        for (Rank r : ranks.values()) {
            String path = "ranks." + r.getId();
            config.set(path + ".hierarchy", r.getHierarchy());
            config.set(path + ".prefix", r.getPrefix());
            config.set(path + ".suffix", r.getSuffix());
            config.set(path + ".nameColor", r.getNameColor());
            config.set(path + ".chatColor", r.getChatColor());
            config.set(path + ".rankable", r.isRankable());
            config.set(path + ".requiredPlaytime", r.getRequiredPlaytime());
            config.set(path + ".rankupPrice", r.getRankupPrice());
            config.set(path + ".buyable", r.isBuyable());
            config.set(path + ".buyPrice", r.getBuyPrice());
            config.set(path + ".buyDuration", r.getBuyDuration());
            config.set(path + ".permissions", r.getPermissions());
        }
        for (Map.Entry<UUID, String> e : playerRanks.entrySet()) {
            config.set("players." + e.getKey().toString(), e.getValue());
        }
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public Rank getRank(String id) { return ranks.get(id.toLowerCase()); }
    public Collection<Rank> getAllRanks() { return ranks.values(); }
    public void addRank(Rank rank) { ranks.put(rank.getId().toLowerCase(), rank); saveRanks(); }
    public void deleteRank(String id) { ranks.remove(id.toLowerCase()); saveRanks(); }

    public String getPlayerRank(UUID uuid) { return playerRanks.get(uuid); }
    public void setPlayerRank(UUID uuid, String rankId) { playerRanks.put(uuid, rankId.toLowerCase()); saveRanks(); }
    public void removePlayerRank(UUID uuid) { playerRanks.remove(uuid); saveRanks(); }
}
