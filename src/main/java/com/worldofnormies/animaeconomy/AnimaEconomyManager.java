package com.worldofnormies.animaeconomy;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AnimaEconomyManager – handles Money, Tokens, and custom Experience.
 */
public class AnimaEconomyManager {

    private final AnimaKitsPlugin plugin;
    private final File file;
    private FileConfiguration config;

    private final Map<UUID, EconomyData> balances = new ConcurrentHashMap<>();

    public AnimaEconomyManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "economy.yml");
    }

    public void load() {
        balances.clear();
        if (!file.exists()) return;
        config = YamlConfiguration.loadConfiguration(file);

        for (String uuidStr : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                double money = config.getDouble(uuidStr + ".money", 0.0);
                long tokens = config.getLong(uuidStr + ".tokens", 0);
                long xp = config.getLong(uuidStr + ".experience", 0);

                balances.put(uuid, new EconomyData(money, tokens, xp));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void save() {
        config = new YamlConfiguration();
        for (Map.Entry<UUID, EconomyData> entry : balances.entrySet()) {
            String path = entry.getKey().toString();
            EconomyData data = entry.getValue();
            config.set(path + ".money", data.money);
            config.set(path + ".tokens", data.tokens);
            config.set(path + ".experience", data.experience);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save economy.yml: " + e.getMessage());
        }
    }

    // ── Money ──

    public double getMoney(UUID uuid) {
        return balances.getOrDefault(uuid, new EconomyData()).money;
    }

    public void setMoney(UUID uuid, double amount) {
        EconomyData data = balances.computeIfAbsent(uuid, k -> new EconomyData());
        data.money = amount;
    }

    public void addMoney(UUID uuid, double amount) {
        setMoney(uuid, getMoney(uuid) + amount);
    }

    public boolean withdrawMoney(UUID uuid, double amount) {
        double current = getMoney(uuid);
        if (current < amount) return false;
        setMoney(uuid, current - amount);
        return true;
    }

    // ── Tokens ──

    public long getTokens(UUID uuid) {
        return balances.getOrDefault(uuid, new EconomyData()).tokens;
    }

    public void setTokens(UUID uuid, long amount) {
        EconomyData data = balances.computeIfAbsent(uuid, k -> new EconomyData());
        data.tokens = amount;
    }

    public void addTokens(UUID uuid, long amount) {
        setTokens(uuid, getTokens(uuid) + amount);
    }

    public boolean withdrawTokens(UUID uuid, long amount) {
        long current = getTokens(uuid);
        if (current < amount) return false;
        setTokens(uuid, current - amount);
        return true;
    }

    // ── Experience ──

    public long getExperience(UUID uuid) {
        return balances.getOrDefault(uuid, new EconomyData()).experience;
    }

    public void setExperience(UUID uuid, long amount) {
        EconomyData data = balances.computeIfAbsent(uuid, k -> new EconomyData());
        data.experience = amount;
    }

    public void addExperience(UUID uuid, long amount) {
        setExperience(uuid, getExperience(uuid) + amount);
    }

    private static class EconomyData {
        double money;
        long tokens;
        long experience;

        EconomyData() { this(0, 0, 0); }
        EconomyData(double money, long tokens, long xp) {
            this.money = money;
            this.tokens = tokens;
            this.experience = xp;
        }
    }
}
