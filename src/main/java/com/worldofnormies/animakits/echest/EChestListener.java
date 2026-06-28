package com.worldofnormies.animakits.echest;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class EChestListener implements Listener {
    private final AnimaKitsPlugin plugin;

    public EChestListener(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().contains("ANIMA ECHEST")) {
            plugin.getEChestManager().savePlayer(event.getPlayer().getUniqueId());
        }
    }
}
