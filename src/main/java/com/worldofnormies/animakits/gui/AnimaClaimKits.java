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

import java.util.List;

/**
 * AnimaClaimKits – Display items inside a kit for players.
 *
 * Slots:
 * 47 = RED glass staned pane   → « Previous
 * 51 = lime GREEN stained glas panes  → Next »
 */
public class AnimaClaimKits implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;
    private static final int ITEMS_AREA = 44;

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final Kit kit;
    private final boolean isAdmin;
    private int page = 0;
    private Inventory inventory;

    public AnimaClaimKits(AnimaKitsPlugin plugin, Player player, Kit kit, boolean isAdmin) {
        this.plugin = plugin;
        this.player = player;
        this.kit = kit;
        this.isAdmin = isAdmin;
    }

    public void open() {
        Component title = MM.deserialize("<bold>⋆༺⸸ </bold>" + kit.getPlainName() + "<bold> ⸸༻⋆</bold>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();
        List<ItemStack> items = kit.getItems();
        int totalPages = Math.max(1, (int) Math.ceil(items.size() / (double) ITEMS_AREA));
        page = Math.min(page, totalPages - 1);

        int from = page * ITEMS_AREA;
        int to = Math.min(from + ITEMS_AREA, items.size());

        for (int i = 0; i < (to - from); i++) {
            ItemStack item = items.get(from + i);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(i, item.clone());
            }
        }

        ItemStack blackPane = GuiItem.border(Material.BLACK_STAINED_GLASS_PANE);
        for (int s = 45; s < 54; s++) inventory.setItem(s, blackPane);

        inventory.setItem(44, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<red><bold>✘ Back</bold></red>")));

        // 47 = RED glass staned pane   → « Previous
        // 51 = lime GREEN stained glas panes  → Next »
        inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<red><bold>« Previous</bold></red>")));
        inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<green><bold>Next »</bold></green>")));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot == 44) {
            if (isAdmin) {
                new AnimaKitsMainGUI(plugin, player).open();
            } else {
                new AnimaClaimMainGUI(plugin, player).open();
            }
        } else if (slot == 47 && page > 0) {
            page--;
            populate();
        } else if (slot == 51) {
            if ((page + 1) * ITEMS_AREA < kit.getItems().size()) {
                page++;
                populate();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) HandlerList.unregisterAll(this);
    }
}
