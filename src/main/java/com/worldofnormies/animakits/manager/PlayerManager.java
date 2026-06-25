package com.worldofnormies.animakits.manager;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlayerManager – handles persistence of player-specific data like kit cooldowns and single-claim history.
 */ 
public class PlayerManager {

    private final AnimaKitsPlugin plugin;
    private final File file;
    private FileConfiguration config;

    /**
     * Data structure:
     * playerUUID -> {
     *   "cooldowns" -> { kitUUID -> expiryTimestamp },
     *   "claims" -> [ kitUUID1, kitUUID2, ... ]
     * }
     */
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();

    public PlayerManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
    }

    public void load() {
        players.clear();
        if (!file.exists()) return;
        config = YamlConfiguration.loadConfiguration(file);

        for (String uuidStr : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection section = config.getConfigurationSection(uuidStr);
                if (section == null) continue;

                PlayerData data = new PlayerData();

                ConfigurationSection cdSection = section.getConfigurationSection("cooldowns");
                if (cdSection != null) {
                    for (String kitUuidStr : cdSection.getKeys(false)) {
                        data.cooldowns.put(UUID.fromString(kitUuidStr), cdSection.getLong(kitUuidStr));
                    }
                }

                List<String> claimList = section.getStringList("claims");
                for (String kitUuidStr : claimList) {
                    data.claims.add(UUID.fromString(kitUuidStr));
                }

                players.put(uuid, data);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void save() {
        config = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
            String path = entry.getKey().toString();
            PlayerData data = entry.getValue();

            if (!data.cooldowns.isEmpty()) {
                for (Map.Entry<UUID, Long> cdEntry : data.cooldowns.entrySet()) {
                    config.set(path + ".cooldowns." + cdEntry.getKey().toString(), cdEntry.getValue());
                }
            }

            if (!data.claims.isEmpty()) {
                List<String> claimStrings = new ArrayList<>();
                for (UUID kitId : data.claims) {
                    claimStrings.add(kitId.toString());
                }
                config.set(path + ".claims", claimStrings);
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save players.yml: " + e.getMessage());
        }
    }

    public long getRemainingCooldown(UUID player, UUID kitId) {
        PlayerData data = players.get(player);
        if (data == null) return 0;
        Long expiry = data.cooldowns.get(kitId);
        if (expiry == null) return 0;
        long remaining = expiry - Instant.now().getEpochSecond();
        if (remaining <= 0) {
            data.cooldowns.remove(kitId);
            return 0;
        }
        return remaining;
    }

    public void setCooldown(UUID player, UUID kitId, long seconds) {
        if (seconds <= 0) {
            PlayerData data = players.get(player);
            if (data != null) data.cooldowns.remove(kitId);
            return;
        }
        players.computeIfAbsent(player, k -> new PlayerData())
               .cooldowns.put(kitId, Instant.now().getEpochSecond() + seconds);
        save();
    }

    public boolean hasClaimed(UUID player, UUID kitId) {
        PlayerData data = players.get(player);
        return data != null && data.claims.contains(kitId);
    }

    public void markClaimed(UUID player, UUID kitId) {
        players.computeIfAbsent(player, k -> new PlayerData()).claims.add(kitId);
        save();
    }

    public void unmarkClaimed(UUID player, UUID kitId) {
        PlayerData data = players.get(player);
        if (data != null) {
            data.claims.remove(kitId);
            save();
        }
    }

    private static class PlayerData {
        final Map<UUID, Long> cooldowns = new HashMap<>();
        final Set<UUID> claims = new HashSet<>();
    }
}
