package com.worldofnormies.animakits.echest;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animaranks.Rank;
import com.worldofnormies.animakits.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EChestManager {
    private final AnimaKitsPlugin plugin;
    private final File folder;
    private final Map<UUID, Inventory> inventories = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> extraRows = new ConcurrentHashMap<>();

    public EChestManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "echests");
        if (!folder.exists()) folder.mkdirs();
    }

    public int getVisibleRows(UUID uuid) {
        Rank rank = plugin.getRankManager().getPlayerRank(uuid);
        int base = rank != null ? rank.getEchestRows() : 1;
        return Math.min(6, base + extraRows.getOrDefault(uuid, 0));
    }

    public void addExtraRow(UUID uuid, int count) {
        extraRows.merge(uuid, count, Integer::sum);
        savePlayer(uuid);
    }

    public void openEChest(org.bukkit.entity.Player player) {
        UUID uuid = player.getUniqueId();
        int rows = getVisibleRows(uuid);
        Inventory inv = Bukkit.createInventory(player, rows * 9, ColorUtil.parse("<gradient:#3060FF:#FFFFFF:#FF3030><bold>⋆Ἲ⸸ [ ANIMA ECHEST ] ⸸Ἳ⋆</bold></gradient>"));

        Inventory saved = loadInventory(uuid);
        if (saved != null) {
            for (int i = 0; i < Math.min(inv.getSize(), saved.getSize()); i++) {
                inv.setItem(i, saved.getItem(i));
            }
        }

        inventories.put(uuid, inv);
        player.openInventory(inv);
    }

    private Inventory loadInventory(UUID uuid) {
        File file = new File(folder, uuid.toString() + ".yml");
        if (!file.exists()) return null;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        extraRows.put(uuid, config.getInt("extra-rows", 0));

        List<?> list = config.getList("items");
        if (list == null) return null;

        Inventory inv = Bukkit.createInventory(null, 54, "temp");
        int i = 0;
        for (Object obj : list) {
            if (i >= 54) break;
            if (obj instanceof ItemStack is) inv.setItem(i, is);
            i++;
        }
        return inv;
    }

    public void savePlayer(UUID uuid) {
        Inventory inv = inventories.get(uuid);
        int extra = extraRows.getOrDefault(uuid, 0);

        File file = new File(folder, uuid.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        config.set("extra-rows", extra);

        if (inv != null) {
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < inv.getSize(); i++) {
                items.add(inv.getItem(i));
            }
            config.set("items", items);
        } else {
            // If not currently open, we might want to keep what's on disk or update extra-rows
            FileConfiguration existing = YamlConfiguration.loadConfiguration(file);
            config.set("items", existing.getList("items"));
        }

        try { config.save(file); } catch (IOException e) {
            plugin.getLogger().severe("Could not save EChest for " + uuid);
        }
    }
}
