package com.worldofnormies.animakits.home;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animaranks.Rank;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HomeManager {
    private final AnimaKitsPlugin plugin;
    private final File folder;
    private final Map<UUID, Map<String, Home>> playerHomes = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> extraHomeSlots = new ConcurrentHashMap<>();

    public HomeManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "homes");
        if (!folder.exists()) folder.mkdirs();
    }

    public void loadPlayer(UUID uuid) {
        File file = new File(folder, uuid.toString() + ".yml");
        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        extraHomeSlots.put(uuid, config.getInt("extra-slots", 0));

        ConfigurationSection homeSec = config.getConfigurationSection("homes");
        if (homeSec != null) {
            Map<String, Home> homes = new HashMap<>();
            for (String name : homeSec.getKeys(false)) {
                UUID worldUuid = UUID.fromString(homeSec.getString(name + ".world"));
                double x = homeSec.getDouble(name + ".x");
                double y = homeSec.getDouble(name + ".y");
                double z = homeSec.getDouble(name + ".z");
                float yaw = (float) homeSec.getDouble(name + ".yaw");
                float pitch = (float) homeSec.getDouble(name + ".pitch");
                homes.put(name.toLowerCase(), new Home(name, worldUuid, x, y, z, yaw, pitch));
            }
            playerHomes.put(uuid, homes);
        }
    }

    public void savePlayer(UUID uuid) {
        Map<String, Home> homes = playerHomes.get(uuid);
        int extra = extraHomeSlots.getOrDefault(uuid, 0);

        if ((homes == null || homes.isEmpty()) && extra == 0) {
            File file = new File(folder, uuid.toString() + ".yml");
            if (file.exists()) file.delete();
            return;
        }

        File file = new File(folder, uuid.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        config.set("extra-slots", extra);

        if (homes != null) {
            for (Home home : homes.values()) {
                String path = "homes." + home.getName();
                config.set(path + ".world", home.getWorldUuid().toString());
                config.set(path + ".x", home.getX());
                config.set(path + ".y", home.getY());
                config.set(path + ".z", home.getZ());
                config.set(path + ".yaw", home.getYaw());
                config.set(path + ".pitch", home.getPitch());
            }
        }

        try { config.save(file); } catch (IOException e) {
            plugin.getLogger().severe("Could not save homes for " + uuid);
        }
    }

    public int getHomeLimit(UUID uuid) {
        Rank rank = plugin.getRankManager().getPlayerRank(uuid);
        int base = rank != null ? rank.getHomeLimit() : 2;
        return base + extraHomeSlots.getOrDefault(uuid, 0);
    }

    public void addExtraHomeSlot(UUID uuid, int count) {
        extraHomeSlots.merge(uuid, count, Integer::sum);
        savePlayer(uuid);
    }

    public Collection<Home> getHomes(UUID uuid) {
        Map<String, Home> homes = playerHomes.get(uuid);
        return homes != null ? homes.values() : Collections.emptyList();
    }

    public Home getHome(UUID uuid, String name) {
        Map<String, Home> homes = playerHomes.get(uuid);
        return homes != null ? homes.get(name.toLowerCase()) : null;
    }

    public boolean addHome(UUID uuid, Home home) {
        if (getHomes(uuid).size() >= getHomeLimit(uuid)) return false;
        playerHomes.computeIfAbsent(uuid, k -> new HashMap<>()).put(home.getName().toLowerCase(), home);
        savePlayer(uuid);
        return true;
    }

    public boolean removeHome(UUID uuid, String name) {
        Map<String, Home> homes = playerHomes.get(uuid);
        if (homes == null || homes.remove(name.toLowerCase()) == null) return false;
        savePlayer(uuid);
        return true;
    }
}
