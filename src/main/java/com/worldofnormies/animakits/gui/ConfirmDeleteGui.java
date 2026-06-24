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
        Component title = MM.deserialize("[Delete Kit]");
        inventory = Bukkit.createInventory(null, 27, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        ItemStack black = pane(Material.BLACK_STAINED_GLASS_PANE, MM.deserialize("<dark_gray> </dark_gray>"));
        ItemStack white = pane(Material.WHITE_STAINED_GLASS_PANE, MM.deserialize("<white> </white>"));
        ItemStack red   = pane(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<red><bold>NO</bold></red>"));
        ItemStack lime  = pane(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<green><bold>YES</bold></green>"));
        ItemStack tnt   = pane(Material.TNT,
            MM.deserialize("<dark_red><bold>Erase Kitt? " + kit.getPlainName() + "</bold></dark_red>"),
            List.of(MM.deserialize("<gray>You Are About To Erase A Kit</gray>"),
                    MM.deserialize("<gray>Once Remove It's Gone Forever</gray>")));

        // Decoration: Black [1, 5, 10, 14, 19, 23] -> Index: 0, 4, 9, 13, 18, 22
        // Decoration: White [0, 18] -> Index: 17 (Wait, Slot 0 is Index 0. Slot 18 is Index 17)
        // Correction: Black Slot 1 -> Index 0. Black Slot 5 -> Index 4. Black Slot 10 -> Index 9...
        for (int i : new int[]{0, 4, 9, 13, 18, 22}) { inventory.setItem(i, black); }
        for (int i : new int[]{17}) { inventory.setItem(i, white); } // Slot 18 is Index 17. Slot 0 was given as Index 0 above.
        inventory.setItem(0, white); // Slot 0 is Index 0.

        // TNT Slot 9 -> Index 8
        inventory.setItem(8, tnt);

        // Black [2, 3, 4, 11, 13, 20, 21, 22] -> Index: 1, 2, 3, 10, 12, 19, 20, 21
        for (int i : new int[]{1, 2, 3, 10, 12, 19, 20, 21}) { inventory.setItem(i, black); }

        // Red Slot 12 -> Index 11
        inventory.setItem(11, red);

        // Green [6, 7, 8, 15, 17, 24, 25, 26] -> Index: 5, 6, 7, 14, 16, 23, 24, 25
        ItemStack green = pane(Material.GREEN_STAINED_GLASS_PANE, Component.space());
        for (int i : new int[]{5, 6, 7, 14, 16, 23, 24, 25}) { inventory.setItem(i, green); }

        // Lime Slot 16 -> Index 15
        inventory.setItem(15, lime);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player))   return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot == 11) { // NO
            // Reopen the editor for this kit
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () ->
                    new KitEditorGui(plugin, player, kit, 0).open());
        } else if (slot == 15) { // YES
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