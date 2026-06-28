package com.worldofnormies.animakits.suffix.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnimaSuffixGUI implements Listener {

    private final AnimaKitsPlugin plugin;
    private final Inventory inventory;

    public AnimaSuffixGUI(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 54, ColorUtil.parse("<gradient:#FF3030:#FFFFFF:#3060FF><bold>⋆Ἲ⸸ [ SELECT SUFFIX ] ⸸Ἳ⋆</bold></gradient>"));
        populate();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void populate() {
        Map<String, String> templates = plugin.getSuffixManager().getTemplates();
        int i = 10;
        for (Map.Entry<String, String> entry : templates.entrySet()) {
            if (i > 43) break;
            if (i % 9 == 0 || i % 9 == 8) i++;

            ItemStack item = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ColorUtil.colorize(entry.getValue()));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtil.colorize("&7Template: &f" + entry.getKey()));
            lore.add("");
            lore.add(ColorUtil.colorize("&eClick to apply!"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(i++, item);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() != Material.NAME_TAG) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String suffix = ColorUtil.parseToMiniMessage(meta.getDisplayName());
        // In a real scenario, check for permission/money here

        var rank = plugin.getRankManager().getPlayerRank(player.getUniqueId());
        if (rank != null) {
            rank.setSuffix(suffix);
            plugin.getRankManager().saveRanks();
            player.sendMessage(ColorUtil.colorize("&aApplied suffix: &f" + suffix));
            player.closeInventory();
        }
    }
}
