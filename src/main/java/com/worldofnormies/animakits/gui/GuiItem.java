package com.worldofnormies.animakits.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/** Simple builder for GUI display items. */
public final class GuiItem {

    private GuiItem() {}

    public static ItemStack make(Material material, Component name) {
        return make(material, name, List.of());
    }

    public static ItemStack make(Material material, Component name, List<Component> lore) {
        return make(material, name, lore, false);
    }

    public static ItemStack make(Material material, Component name, List<Component> lore, boolean enchanted) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(lore);
            if (enchanted) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack border(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.space());
            item.setItemMeta(meta);
        }
        return item;
    }
}