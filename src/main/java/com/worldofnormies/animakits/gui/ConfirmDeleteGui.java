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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * ConfirmDeleteGui – 1-row (9-slot) GUI confirming kit deletion.
 *
 * Layout (slots 0-8):
 *   0 1 2 = BLACK pane
 *   3     = RED pane   → Cancel (go back to Kit editor)
 *   4     = BLACK pane
 *   5     = LIME pane  → Confirm delete
 *   6 7 8 = BLACK pane
 */
public class ConfirmDeleteGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final Kit kit;
    private Inventory inventory;

    // slots
    private static final int CANCEL_SLOT  = 3;
    private static final int CONFIRM_SLOT = 5;

    public ConfirmDeleteGui(AnimaKitsPlugin plugin, Player player, Kit kit) {
        this.plugin = plugin;
        this.player = player;
        this.kit    = kit;
    }

    public void open() {
        Component title = MM.deserialize(
                "<red><bold>Delete kit: </bold></red><white>" + kit.getPlainName() + "</white><red><bold>?</bold></red>");
        inventory = Bukkit.createInventory(null, 9, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        ItemStack black = pane(Material.BLACK_STAINED_GLASS_PANE,
                MM.deserialize("<dark_gray> </dark_gray>"));
        ItemStack red   = pane(Material.RED_STAINED_GLASS_PANE,
                MM.deserialize("<red><bold>✘ Cancel</bold></red>"),
                List.of(MM.deserialize("<gray>Go back – keep the kit.</gray>")));
        ItemStack lime  = pane(Material.LIME_STAINED_GLASS_PANE,
                MM.deserialize("<green><bold>✔ Confirm Delete</bold></green>"),
                List.of(MM.deserialize("<red>This action cannot be undone!</red>")));

        for (int i : new int[]{0, 1, 2, 4, 6, 7, 8}) {
            inventory.setItem(i, black);
        }
        inventory.setItem(CANCEL_SLOT,  red);
        inventory.setItem(CONFIRM_SLOT, lime);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player))   return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot == CANCEL_SLOT) {
            // Reopen the editor for this kit
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () ->
                    new KitEditorGui(plugin, player, kit, 0).open());
        } else if (slot == CONFIRM_SLOT) {
            plugin.getKitManager().deleteKit(kit.getPlainName());
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () ->
                    new KitBrowserGui(plugin, player).open());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        HandlerList.unregisterAll(this);
    }

    // ── helpers ───────────────────────────────────────────────────

    private ItemStack pane(Material mat, Component name) {
        return pane(mat, name, List.of());
    }

    private ItemStack pane(Material mat, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            if (!lore.isEmpty()) meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}