package com.worldofnormies.animakits.listener;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.AnimaInventoryHolder;
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
            // Handling will be delegated back to GUI classes or handled here
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof AnimaInventoryHolder) {
            event.setCancelled(true);
        }
    }
}
