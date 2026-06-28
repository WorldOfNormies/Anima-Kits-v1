package com.worldofnormies.animakits.shop.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.AnimaInventoryHolder;
import com.worldofnormies.animakits.kit.Kit;
import com.worldofnormies.animakits.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AnimaKitShopGUI {
    private final AnimaKitsPlugin plugin;
    private final Inventory inventory;

    public AnimaKitShopGUI(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(new AnimaInventoryHolder("shop_kits", null), 54, ColorUtil.parse("<gradient:#FFA500:#FFFFFF:#FFA500><bold>⋆Ἲ⸸ [ BUY KITS ] ⸸Ἳ⋆</bold></gradient>"));
        populate();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void populate() {
        int i = 10;
        for (Kit kit : plugin.getKitManager().getAllKits()) {
            if (i > 43) break;
            if (i % 9 == 0 || i % 9 == 8) i++;

            ItemStack item = new ItemStack(kit.getIconMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ColorUtil.colorize("&6&l" + kit.getRawName()));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtil.colorize("&7Price: &f2,500 Ani"));
            lore.add(ColorUtil.colorize("&7Access: &fPermanent"));
            lore.add("");
            lore.add(ColorUtil.colorize("&eClick to buy!"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(i++, item);
        }
    }

    public void handleAction(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String plainName = org.bukkit.ChatColor.stripColor(meta.getDisplayName());
        Kit kit = plugin.getKitManager().getKitByPlainName(plainName);

        if (kit != null) {
            if (plugin.getEconomyManager().withdraw(player.getUniqueId(), "ani", 2500)) {
                plugin.getPermissionManager().grantClaimPermission(player.getUniqueId(), kit.getPlainName(), -1);
                player.sendMessage(ColorUtil.colorize("&aYou purchased permanent access to kit: &f" + kit.getPlainName()));
                player.closeInventory();
            } else {
                player.sendMessage(ColorUtil.colorize("&cYou do not have enough Ani (2,500 required)!"));
            }
        }
    }
}
