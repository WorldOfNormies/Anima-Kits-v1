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
     * "cooldowns" -> { kitUUID -> expiryTimestamp },
     * "claims" -> [ kitUUID1, kitUUID2, ... ]
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

                List<String> joinKitList = section.getStringList("joinKitsReceived");
                for (String kitUuidStr : joinKitList) {
                    try { data.joinKitsReceived.add(UUID.fromString(kitUuidStr)); } catch (IllegalArgumentException ignored) {}
                }

                data.repairCooldown = section.getLong("repairCooldown", 0);
                data.lastRepair = section.getLong("lastRepair", 0);

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

            if (!data.joinKitsReceived.isEmpty()) {
                List<String> joinStrings = new ArrayList<>();
                for (UUID kitId : data.joinKitsReceived) {
                    joinStrings.add(kitId.toString());
                }
                config.set(path + ".joinKitsReceived", joinStrings);
            }

            if (data.repairCooldown > 0) {
                config.set(path + ".repairCooldown", data.repairCooldown);
            }
            if (data.lastRepair > 0) {
                config.set(path + ".lastRepair", data.lastRepair);
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

    /** Returns true if this player has already received the on-first-join kit for the given kit ID. */
    public boolean hasReceivedJoinKit(UUID player, UUID kitId) {
        PlayerData data = players.get(player);
        return data != null && data.joinKitsReceived.contains(kitId);
    }

    /** Mark that this player has received the on-first-join kit — will never be given again. */
    public void markJoinKitReceived(UUID player, UUID kitId) {
        players.computeIfAbsent(player, k -> new PlayerData()).joinKitsReceived.add(kitId);
        save();
    }

    public long getRepairCooldown(UUID player) {
        PlayerData data = players.get(player);
        return data != null ? data.repairCooldown : 0;
    }

    public void setRepairCooldown(UUID player, long seconds) {
        players.computeIfAbsent(player, k -> new PlayerData()).repairCooldown = seconds;
        save();
    }

    public long getLastRepair(UUID player) {
        PlayerData data = players.get(player);
        return data != null ? data.lastRepair : 0;
    }

    public void setLastRepair(UUID player, long timestamp) {
        players.computeIfAbsent(player, k -> new PlayerData()).lastRepair = timestamp;
        save();
    }

    private static class PlayerData {
        final Map<UUID, Long> cooldowns = new HashMap<>();
        final Set<UUID> claims = new HashSet<>();
        final Set<UUID> joinKitsReceived = new HashSet<>();
        long repairCooldown = 0;
        long lastRepair = 0;
    }
}
