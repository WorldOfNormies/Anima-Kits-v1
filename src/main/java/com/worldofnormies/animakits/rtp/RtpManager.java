package com.worldofnormies.animakits.rtp;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animaranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RtpManager {
    private final AnimaKitsPlugin plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<String, Integer> worldRadii = new HashMap<>();
    private final Map<String, Integer> worldCooldowns = new HashMap<>();

    public RtpManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        worldRadii.clear();
        worldCooldowns.clear();
        var config = plugin.getConfig().getConfigurationSection("rtp.worlds");
        if (config != null) {
            for (String world : config.getKeys(false)) {
                worldRadii.put(world, config.getInt(world + ".radius", 25000));
                worldCooldowns.put(world, config.getInt(world + ".cooldown", 300));
            }
        } else {
            // Defaults
            worldRadii.put("world", 25000);
            worldCooldowns.put("world", 300);
            worldRadii.put("world_nether", 25000);
            worldCooldowns.put("world_nether", 300);
            worldRadii.put("world_the_end", 25000);
            worldCooldowns.put("world_the_end", 300);
        }
    }

    public void setWorldSettings(String world, int radius, int cooldown) {
        worldRadii.put(world, radius);
        worldCooldowns.put(world, cooldown);
        plugin.getConfig().set("rtp.worlds." + world + ".radius", radius);
        plugin.getConfig().set("rtp.worlds." + world + ".cooldown", cooldown);
        plugin.saveConfig();
    }

    public long getCooldown(UUID uuid, String world) {
        Rank rank = plugin.getRankManager().getPlayerRank(uuid);
        int worldBase = worldCooldowns.getOrDefault(world, 300);
        int rankBase = rank != null ? rank.getRtpCooldown() : 300;
        return Math.min(worldBase, rankBase);
    }


    public boolean canRtp(UUID uuid, String world) {
        if (!cooldowns.containsKey(uuid)) return true;
        long last = cooldowns.get(uuid);
        return (System.currentTimeMillis() - last) / 1000 >= getCooldown(uuid, world);
    }

    public long getRemainingCooldown(UUID uuid, String world) {
        if (!cooldowns.containsKey(uuid)) return 0;
        long last = cooldowns.get(uuid);
        long diff = (System.currentTimeMillis() - last) / 1000;
        return Math.max(0, getCooldown(uuid, world) - diff);
    }

    public void teleport(Player player, World world) {
        int radius = worldRadii.getOrDefault(world.getName(), 25000);
        int x = random.nextInt(radius * 2) - radius;
        int z = random.nextInt(radius * 2) - radius;

        int y = world.getHighestBlockYAt(x, z);
        Block block = world.getBlockAt(x, y, z);

        // Simple safety check
        if (block.getType().isBurnable() || block.isLiquid()) {
            teleport(player, world); // retry
            return;
        }

        Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
        player.teleport(loc);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
