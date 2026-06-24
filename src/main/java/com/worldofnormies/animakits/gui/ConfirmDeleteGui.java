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
        // 1. Define the items
        ItemStack brown = pane(Material.BROWN_STAINED_GLASS_PANE, MM.deserialize("<dark_gray> </dark_gray>"));
        ItemStack black = pane(Material.BLACK_STAINED_GLASS_PANE, MM.deserialize("<dark_gray> </dark_gray>"));
        ItemStack green = pane(Material.GREEN_STAINED_GLASS_PANE, Component.space());
        
        ItemStack red   = pane(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<red><bold>NO</bold></red>"));
        ItemStack lime  = pane(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<green><bold>YES</bold></green>"));
        
        ItemStack tnt   = pane(Material.TNT,
            MM.deserialize("<dark_red><bold>Erase Kit?<white></white></dark_red> " <white>+ kit.getPlainName() + "</bold>"),
            List.of(MM.deserialize("<gold>You Are About To Erase A Kit</gold>"),
                    MM.deserialize("<gold>Once Removed It's Gone Forever</gold>")));
    
        // 2. Fill the Left Section (Brown backgrounds + Red button)
        for (int i : new int[]{0, 1, 2, 9, 11, 18, 19, 20}) {
            inventory.setItem(i, brown);
        }
        inventory.setItem(10, red); // Red Button
    
        // 3. Fill the Middle Section (Black backgrounds + TNT)
        for (int i : new int[]{3, 4, 5, 12, 14, 21, 22, 23}) {
            inventory.setItem(i, black);
        }
        inventory.setItem(13, tnt); // TNT Info Block
    
        // 4. Fill the Right Section (Green backgrounds + Lime button)
        for (int i : new int[]{6, 7, 8, 15, 17, 24, 25, 26}) {
            inventory.setItem(i, green);
        }
        inventory.setItem(16, lime); // Lime Button
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
