package com.worldofnormies.animakits.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.kit.Kit;
import com.worldofnormies.animakits.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.format.TextDecoration;
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

import java.util.ArrayList;
import java.util.List;

public class KitBrowserGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private Inventory inventory;
    private int page = 0;

    private final int[] kitSlots = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    public KitBrowserGui(AnimaKitsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Component title = MM.deserialize("<gradient:#8A2BE2:#4B0082><bold>Kit Selection Browser</bold></gradient>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    public void populate() {
        inventory.clear();
        List<Kit> kits = new ArrayList<>(plugin.getKitManager().getKits());

        ItemStack blackPane = GuiItem.border(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack whitePane = GuiItem.border(Material.WHITE_STAINED_GLASS_PANE);

        for (int s : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}) inventory.setItem(s, blackPane);
        for (int s : new int[]{9, 17, 18, 26, 27, 35, 36, 44}) inventory.setItem(s, whitePane);
        for (int s : new int[]{46, 48, 49, 50, 52}) inventory.setItem(s, blackPane);

        int totalPages = Math.max(1, (int) Math.ceil(kits.size() / (double) kitSlots.length));
        page = Math.min(page, totalPages - 1);

        int from = page * kitSlots.length;
        int to = Math.min(from + kitSlots.length, kits.size());

        for (int i = 0; i < (to - from); i++) {
            Kit kit = kits.get(from + i);
            List<Component> lore = new ArrayList<>();
            for (String line : kit.getLore()) {
                lore.add(MM.deserialize(line));
            }
            lore.add(Component.empty());
            lore.add(MM.deserialize("<yellow>▸ Left-Click to Preview Contents</yellow>"));
            if (player.hasPermission("anima.kits.admin")) {
                lore.add(MM.deserialize("<red>▸ Right-Click to Edit Configuration</red>"));
            }

            Component kitTitle = ColorUtil.parse(kit.getRawName()).decorate(TextDecoration.BOLD);
            inventory.setItem(kitSlots[i], GuiItem.make(kit.getIconMaterial(), kitTitle, lore));
        }

        inventory.setItem(45, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<gradient:#8B0000:#FF0000><bold>✘ Close Menu</bold></gradient>")));

        if (page > 0) {
            inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<gradient:#8B0000:#FF8C00><bold>« Previous Page</bold></gradient>")));
        } else {
            inventory.setItem(47, blackPane);
        }

        if (to < kits.size()) {
            inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<gradient:#32CD32:#ADFF2F><bold>Next Page »</bold></gradient>")));
        } else {
            inventory.setItem(51, blackPane);
        }

        if (player.hasPermission("anima.kits.admin")) {
            inventory.setItem(53, GuiItem.make(Material.LIME_BUNDLE, MM.deserialize("<gradient:#006400:#32CD32><bold>✚ Create New Kit</bold></gradient>")));
        } else {
            inventory.setItem(53, blackPane);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= INV_SIZE) return;

        if (slot == 45) {
            player.closeInventory();
            return;
        }

        if (slot == 47 && page > 0) {
            page--;
            populate();
            return;
        }

        if (slot == 51) {
            if ((page + 1) * kitSlots.length < plugin.getKitManager().getKits().size()) {
                page++;
                populate();
            }
            return;
        }

        if (slot == 53 && player.hasPermission("anima.kits.admin")) {
            HandlerList.unregisterAll(this);
            player.closeInventory();
            ChatInputSession.sendCreatePrompt(plugin, player);
            new ChatInputSession(plugin, player,
                name -> {
                    Kit newKit = plugin.getKitManager().createKit(name);
                    new KitEditorGui(plugin, player, newKit, 0).open();
                },
                () -> new KitBrowserGui(plugin, player).open()
            ).await();
            return;
        }

        int slotIndex = -1;
        for (int i = 0; i < kitSlots.length; i++) {
            if (kitSlots[i] == slot) {
                slotIndex = i;
                break;
            }
        }

        if (slotIndex != -1) {
            int kitIndex = (page * kitSlots.length) + slotIndex;
            List<Kit> kits = new ArrayList<>(plugin.getKitManager().getKits());
            if (kitIndex >= kits.size()) return;

            Kit kit = kits.get(kitIndex);

            if (event.isRightClick() && player.hasPermission("anima.kits.admin")) {
                HandlerList.unregisterAll(this);
                new KitEditorGui(plugin, player, kit, 0).open();
            } else {
                HandlerList.unregisterAll(this);
                new KitDisplayGui(plugin, player, kit).open();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}
