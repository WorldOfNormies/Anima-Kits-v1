package com.worldofnormies.animakits.shop.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.AnimaInventoryHolder;
import com.worldofnormies.animakits.util.ColorUtil;
import com.worldofnormies.animakits.suffix.gui.AnimaSuffixGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AnimaShopGUI {

    private final AnimaKitsPlugin plugin;
    private final Inventory inventory;

    public AnimaShopGUI(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(new AnimaInventoryHolder("shop", null), 27, ColorUtil.parse("<gradient:#FFD700:#FFFFFF:#FFD700><bold>⋆Ἲ⸸ [ ANIMA SHOP ] ⸸Ἳ⋆</bold></gradient>"));
        populate();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.setItem(10, createItem(Material.EXPERIENCE_BOTTLE, "&b&lBUY RANKS", "&7Upgrade your spiritual level."));
        inventory.setItem(12, createItem(Material.CHEST, "&6&lBUY KITS", "&7Permanent access to premium kits."));
        inventory.setItem(14, createItem(Material.NAME_TAG, "&d&lBUY SUFFIXES", "&7Pick a pre-generated suffix."));
        inventory.setItem(16, createItem(Material.WHITE_BED, "&a&lBUY PERKS", "&7Extra homes, EChest rows, etc."));
    }

    private ItemStack createItem(Material mat, String name, String loreLine) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.colorize(name));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.colorize(loreLine));
        lore.add("");
        lore.add(ColorUtil.colorize("&eClick to browse!"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void handleAction(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.NAME_TAG) {
            new AnimaSuffixGUI(plugin).open(player);
        } else if (item.getType() == Material.WHITE_BED) {
            new AnimaPerkShopGUI(plugin).open(player);
        } else if (item.getType() == Material.EXPERIENCE_BOTTLE) {
            new AnimaRankShopGUI(plugin).open(player);
        } else if (item.getType() == Material.CHEST) {
            new AnimaKitShopGUI(plugin).open(player);
        }
    }
}
