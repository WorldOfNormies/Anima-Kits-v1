package com.worldofnormies.animaeconomy;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final AnimaKitsPlugin plugin;
    private final File file;
    private FileConfiguration config;

    private final Map<UUID, Long> moneyBalances = new HashMap<>();
    private final Map<UUID, Long> coinBalances = new HashMap<>();
    private final Map<UUID, Long> xpBalances = new HashMap<>();

    public EconomyManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        File folder = new File(plugin.getDataFolder(), "economy");
        if (!folder.exists()) folder.mkdirs();
        this.file = new File(folder, "economy.yml");
    }

    public void load() {
        if (!file.exists()) {
            // plugin.saveResource("economy.yml", false); // This expects it in the root of the jar
        }
        config = YamlConfiguration.loadConfiguration(file);

        moneyBalances.clear();
        coinBalances.clear();
        xpBalances.clear();

        if (config.contains("money")) {
            for (String key : config.getConfigurationSection("money").getKeys(false)) {
                moneyBalances.put(UUID.fromString(key), config.getLong("money." + key));
            }
        }
        if (config.contains("coins")) {
            for (String key : config.getConfigurationSection("coins").getKeys(false)) {
                coinBalances.put(UUID.fromString(key), config.getLong("coins." + key));
            }
        }
        if (config.contains("xp")) {
            for (String key : config.getConfigurationSection("xp").getKeys(false)) {
                xpBalances.put(UUID.fromString(key), config.getLong("xp." + key));
            }
        }
    }

    public void save() {
        for (Map.Entry<UUID, Long> entry : moneyBalances.entrySet()) {
            config.set("money." + entry.getKey().toString(), entry.getValue());
        }
        for (Map.Entry<UUID, Long> entry : coinBalances.entrySet()) {
            config.set("coins." + entry.getKey().toString(), entry.getValue());
        }
        for (Map.Entry<UUID, Long> entry : xpBalances.entrySet()) {
            config.set("xp." + entry.getKey().toString(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save economy.yml!");
        }
    }

    public long getBalance(UUID uuid, String type) {
        return switch (type.toLowerCase()) {
            case "money", "ani" -> moneyBalances.getOrDefault(uuid, 0L);
            case "coins", "animaz" -> coinBalances.getOrDefault(uuid, 0L);
            case "xp", "experience" -> xpBalances.getOrDefault(uuid, 0L);
            default -> 0L;
        };
    }

    public void setBalance(UUID uuid, String type, long amount) {
        switch (type.toLowerCase()) {
            case "money", "ani" -> moneyBalances.put(uuid, Math.max(0, amount));
            case "coins", "animaz" -> coinBalances.put(uuid, Math.max(0, amount));
            case "xp", "experience" -> xpBalances.put(uuid, Math.max(0, amount));
        }
    }

    public void addBalance(UUID uuid, String type, long amount) {
        setBalance(uuid, type, getBalance(uuid, type) + amount);
    }

    public boolean withdraw(UUID uuid, String type, long amount) {
        long current = getBalance(uuid, type);
        if (current < amount) return false;
        setBalance(uuid, type, current - amount);
        return true;
    }
}
