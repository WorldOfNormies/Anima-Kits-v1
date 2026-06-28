package com.worldofnormies.animakits.listener;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.AnimaInventoryHolder;
import com.worldofnormies.animakits.home.gui.AnimaHomeGUI;
import com.worldofnormies.animakits.rtp.gui.AnimaRtpGUI;
import com.worldofnormies.animakits.shop.gui.AnimaPerkShopGUI;
import com.worldofnormies.animakits.shop.gui.AnimaRankShopGUI;
import com.worldofnormies.animakits.shop.gui.AnimaShopGUI;
import com.worldofnormies.animakits.suffix.gui.AnimaSuffixGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class MenuListener implements Listener {
    private final AnimaKitsPlugin plugin;

    public MenuListener(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof AnimaInventoryHolder holder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            switch (holder.getType()) {
                case "homes" -> new AnimaHomeGUI(plugin, player).handleAction(player, event.getCurrentItem(), event.isLeftClick(), event.isRightClick(), event.getClick() == org.bukkit.event.inventory.ClickType.MIDDLE);
                case "rtp" -> new AnimaRtpGUI(plugin).handleAction(player, event.getCurrentItem());
                case "shop" -> new AnimaShopGUI(plugin).handleAction(player, event.getCurrentItem());
                case "shop_perks" -> new AnimaPerkShopGUI(plugin).handleAction(player, event.getCurrentItem());
                case "shop_ranks" -> new AnimaRankShopGUI(plugin).handleAction(player, event.getCurrentItem());
                case "shop_kits" -> new com.worldofnormies.animakits.shop.gui.AnimaKitShopGUI(plugin).handleAction(player, event.getCurrentItem());
                case "suffix" -> new AnimaSuffixGUI(plugin).handleAction(player, event.getCurrentItem());
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof AnimaInventoryHolder) {
            event.setCancelled(true);
        }
    }
}
