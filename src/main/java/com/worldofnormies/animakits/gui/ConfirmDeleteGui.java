package com.worldofnormies.animakits.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.kit.Kit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

/**
 * Confirmation menu displayed when a staff member attempts to permanently delete a kit.
 */
public class ConfirmDeleteGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 27;

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final Kit kit;
    private Inventory inventory;
    private boolean actionTaken = false;

    public ConfirmDeleteGui(AnimaKitsPlugin plugin, Player player, Kit kit) {
        this.plugin = plugin;
        this.player = player;
        this.kit = kit;
    }

    public void open() {
        Component title = MM.deserialize("<red><bold>Confirm Deletion</bold></red>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        // Fill background with industrial glass panels
        for (int i = 0; i < INV_SIZE; i++) {
            inventory.setItem(i, GuiItem.border(Material.GRAY_STAINED_GLASS_PANE));
        }

        // Slot 11: Absolute confirmation (Destructive Action)
        inventory.setItem(11, GuiItem.make(
            Material.RED_TERRACOTTA,
            MM.deserialize("<gradient:#8B0000:#FF0000><bold>✔ CONFIRM DELETION</bold></gradient>"),
            List.of(
                MM.deserialize("<gray>Permanently purges the kit data configuration.</gray>"),
                MM.deserialize("<red><bold>Warning: This action cannot be undone!</bold></red>")
            )
        ));

        // Slot 13: Core Target Display Info
        // FIXED: Corrected string layout and tag placements to resolve compilation errors
        inventory.setItem(13, GuiItem.make(
            Material.TNT,
            MM.deserialize("<dark_red><bold>Erase Kit?</bold></dark_red> <white><bold>" + kit.getPlainName() + "</bold></white>"),
            List.of(
                MM.deserialize("<gray>Target: <red>" + kit.getPlainName() + "</red></gray>"),
                MM.deserialize("<gray>Clicking left or right items updates path variables.</gray>")
            )
        ));

        // Slot 15: Safe fallback button (Return safely back to Editor)
        inventory.setItem(15, GuiItem.make(
            Material.GREEN_TERRACOTTA,
            MM.deserialize("<gradient:#006400:#32CD32><bold>✘ CANCEL / GO BACK</bold></gradient>"),
            List.of(
                MM.deserialize("<gray>Aborts this destructive drop sequence and</gray>"),
                MM.deserialize("<gray>returns you safely to the kit editing board.</gray>")
            )
        ));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 11) { // Confirmed Delete Sequence
            actionTaken = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();

            // Perform underlying kit unregistration tasks
            plugin.getKitManager().deleteKit(kit.getPlainName());
            plugin.getKitManager().saveKits();
            plugin.getKitManager().refreshAllBrowsersSafe();

            player.sendMessage(MM.deserialize("<gradient:#8B0000:#FF0000><bold>Successfully purged kit data configurations permanently.</bold></gradient>"));
            
            // Send back to central system selection hub
            Bukkit.getScheduler().runTask(plugin, () -> new KitBrowserGui(plugin, player).open());

        } else if (slot == 15) { // Safe abort route
            actionTaken = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();
            
            // Re-open editor session seamlessly safely
            Bukkit.getScheduler().runTask(plugin, () -> new KitEditorGui(plugin, player, kit, 0).open());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getPlayer().equals(player)) return;
        if (actionTaken) return;

        // Standard interface escape pathing routes user safely back into the editor screen instead of trapping them
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().runTask(plugin, () -> new KitEditorGui(plugin, player, kit, 0).open());
    }
}
