package com.worldofnormies.animakits.shop.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.AnimaInventoryHolder;
import com.worldofnormies.animakits.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AnimaPerkShopGUI {
    private final AnimaKitsPlugin plugin;
    private final Inventory inventory;

    public AnimaPerkShopGUI(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(new AnimaInventoryHolder("shop_perks", null), 27, ColorUtil.parse("<gradient:#32CD32:#FFFFFF:#32CD32><bold>⋆Ἲ⸸ [ BUY PERKS ] ⸸Ἳ⋆</bold></gradient>"));
        populate();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.setItem(11, createItem(Material.WHITE_BED, "&a&lBUY HOME SLOT", "&7Cost: &f5,000 Ani", "&7Adds 1 extra home slot permanently."));
        inventory.setItem(15, createItem(Material.CHEST, "&6&lBUY ECHEST ROW", "&7Cost: &f10,000 Ani", "&7Adds 1 extra Ender Chest row."));
    }

    private ItemStack createItem(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.colorize(name));
        List<String> lore = new ArrayList<>();
        for (String line : loreLines) lore.add(ColorUtil.colorize(line));
        lore.add("");
        lore.add(ColorUtil.colorize("&eClick to buy!"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void handleAction(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.WHITE_BED) {
            if (plugin.getEconomyManager().withdraw(player.getUniqueId(), "ani", 5000)) {
                plugin.getHomeManager().addExtraHomeSlot(player.getUniqueId(), 1);
                player.sendMessage(ColorUtil.colorize("&aYou purchased an extra home slot!"));
            } else {
                player.sendMessage(ColorUtil.colorize("&cYou need 5,000 Ani for a home slot."));
            }
        } else if (item.getType() == Material.CHEST) {
            if (plugin.getEconomyManager().withdraw(player.getUniqueId(), "ani", 10000)) {
                plugin.getEChestManager().addExtraRow(player.getUniqueId(), 1);
                player.sendMessage(ColorUtil.colorize("&aYou purchased an extra Ender Chest row!"));
            } else {
                player.sendMessage(ColorUtil.colorize("&cYou need 10,000 Ani for an EChest row."));
            }
        }
    }
}
