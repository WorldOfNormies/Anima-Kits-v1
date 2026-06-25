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
 * ConfirmDeleteGui – 3-row (27-slot) chest GUI confirming kit deletion.
 *
 * Layout (matches GUI_Delete_Kit.png):
 *   Row 0  [0-8]  : grey/brown filler panes
 *   Row 1  [9-17] : col 2=red cancel (slot 11), col 4=TNT display (slot 13), col 6=lime confirm (slot 15), rest filler
 *   Row 2  [18-26]: grey/brown filler panes
 */
public class ConfirmDeleteGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final Kit kit;
    private Inventory inventory;

    // Middle row slots
    private static final int CANCEL_SLOT  = 11;
    private static final int TNT_SLOT     = 13;
    private static final int CONFIRM_SLOT = 15;

    public ConfirmDeleteGui(AnimaKitsPlugin plugin, Player player, Kit kit) {
        this.plugin = plugin;
        this.player = player;
        this.kit    = kit;
    }

    public void open() {
        Component title = MM.deserialize(
                "<red><bold>Delete kit: </bold></red><white>" + kit.getPlainName() + "</white><red><bold>?</bold></red>");
        inventory = Bukkit.createInventory(null, 27, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        // Brown/grey filler panes for background (matches screenshot dark grid)
        ItemStack brownFill = pane(Material.BROWN_STAINED_GLASS_PANE,
                MM.deserialize("<dark_gray> </dark_gray>"),
                List.of());
        ItemStack grayFill  = pane(Material.GRAY_STAINED_GLASS_PANE,
                MM.deserialize("<dark_gray> </dark_gray>"),
                List.of());

        // Fill all slots with alternating pattern matching the screenshot
        for (int i = 0; i < 27; i++) {
            // Alternate brown and gray to match the checker-like look in the screenshot
            inventory.setItem(i, (i % 2 == 0) ? brownFill : grayFill);
        }

        // Red cancel button – slot 11 (left side of middle row)
        ItemStack red = pane(Material.RED_STAINED_GLASS_PANE,
                MM.deserialize("<red><bold>✘ Cancel</bold></red>"),
                List.of(MM.deserialize("<gray>Go back – keep the kit.</gray>")));
        inventory.setItem(CANCEL_SLOT, red);

        // TNT display item – slot 13 (centre of middle row)
        ItemStack tnt = new ItemStack(Material.TNT);
        ItemMeta tntMeta = tnt.getItemMeta();
        if (tntMeta != null) {
            tntMeta.displayName(MM.deserialize("<red><bold>" + kit.getPlainName() + "</bold></red>"));
            tntMeta.lore(List.of(
                    MM.deserialize("<gray>Are you sure you want to</gray>"),
                    MM.deserialize("<red>permanently delete</red> <gray>this kit?</gray>")
            ));
            tnt.setItemMeta(tntMeta);
        }
        inventory.setItem(TNT_SLOT, tnt);

        // Lime confirm button – slot 15 (right side of middle row)
        ItemStack lime = pane(Material.LIME_STAINED_GLASS_PANE,
                MM.deserialize("<green><bold>✔ Confirm Delete</bold></green>"),
                List.of(MM.deserialize("<red>This action cannot be undone!</red>")));
        inventory.setItem(CONFIRM_SLOT, lime);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player))   return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot == CANCEL_SLOT) {
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () ->
                    new AnimaKitsEditor(plugin, player, kit, 0).open());
        } else if (slot == CONFIRM_SLOT) {
            plugin.getKitManager().deleteKit(kit.getPlainName());
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () ->
                    new AnimaKitsMainGUI(plugin, player).open());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        HandlerList.unregisterAll(this);
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