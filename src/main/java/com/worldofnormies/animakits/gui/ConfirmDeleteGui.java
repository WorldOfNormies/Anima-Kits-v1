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
 * ConfirmDeleteGui – Deletion confirmation interface matching GUI Delete Kit.png.
 */
public class ConfirmDeleteGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int          INV_SIZE = 27;

    private final AnimaKitsPlugin plugin;
    private final Player          player;
    private final Kit             kit;
    private Inventory             inventory;

    private static final int CANCEL_SLOT  = 10;
    private static final int TNT_SLOT     = 13;
    private static final int CONFIRM_SLOT = 16;

    public ConfirmDeleteGui(AnimaKitsPlugin plugin, Player player, Kit kit) {
        this.plugin = plugin;
        this.player = player;
        this.kit    = kit;
    }

    public void open() {
        // Updated layout exactly as requested: gradient completely closed before appending the kit name directly
        Component title = MM.deserialize(
                "<gradient:#4a0000:#8b0000:#ff3333><bold>Delete kit?: </bold></gradient>" + kit.getPlainName());
                
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        ItemStack brownPane = pane(Material.BROWN_STAINED_GLASS_PANE, Component.space(), List.of());
        ItemStack blackPane = pane(Material.BLACK_STAINED_GLASS_PANE, Component.space(), List.of());
        ItemStack greenPane = pane(Material.GREEN_STAINED_GLASS_PANE, Component.space(), List.of());

        // Fill out 3-section layout bands from GUI Delete Kit.png
        for (int i = 0; i < INV_SIZE; i++) {
            int col = i % 9;
            if (col <= 2) {
                inventory.setItem(i, brownPane);
            } else if (col <= 5) {
                inventory.setItem(i, blackPane);
            } else {
                inventory.setItem(i, greenPane);
            }
        }

        // Action Item Injection Overrides
        ItemStack red = pane(Material.RED_STAINED_GLASS_PANE,
                MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Cancel</bold></gradient>"),
                List.of(MM.deserialize("<gray>Go back – keep the kit.</gray>")));
        inventory.setItem(CANCEL_SLOT, red);

        ItemStack tnt = new ItemStack(Material.TNT);
        ItemMeta tntMeta = tnt.getItemMeta();
        if (tntMeta != null) {
            tntMeta.displayName(MM.deserialize("<gradient:#FF3333:#8B0000><bold>" + kit.getPlainName() + "</bold></gradient>"));
            tntMeta.lore(List.of(
                    MM.deserialize("<gray>Are you sure you want to</gray>"),
                    MM.deserialize("<gradient:#FF4444:#CC0000>permanently delete</gradient> <gray>this kit?</gray>")
            ));
            tnt.setItemMeta(tntMeta);
        }
        inventory.setItem(TNT_SLOT, tnt);

        ItemStack lime = pane(Material.LIME_STAINED_GLASS_PANE,
                MM.deserialize("<gradient:#44FF88:#00CC55><bold>✔ Confirm Delete</bold></gradient>"),
                List.of(MM.deserialize("<gradient:#FF4444:#CC0000>This action cannot be undone!</gradient>")));
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
