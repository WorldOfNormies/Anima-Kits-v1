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

import java.util.ArrayList;
import java.util.List;

/**
 * KitEditorGui – multi-page kit content and metadata editor.
 */
public class KitEditorGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final Kit kit;
    private int page;
    private Inventory inventory;
    private boolean closing = false;

    private final int[] itemSlots = {
        9,  10, 11, 12, 13, 14, 15,
        18, 19, 20, 21, 22, 23, 24,
        27, 28, 29, 30, 31, 32, 33,
        36, 37, 38, 39, 40, 41, 42
    };

    public KitEditorGui(AnimaKitsPlugin plugin, Player player, Kit kit, int page) {
        this.plugin = plugin;
        this.player = player;
        this.kit = kit;
        this.page = page;
    }

    public void open() {
        Component title = MM.deserialize("[ New Kit Edit ]");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();

        ItemStack black = GuiItem.border(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack white = GuiItem.border(Material.WHITE_STAINED_GLASS_PANE);

        for (int s : new int[]{0, 1, 3, 5, 7, 8}) {
            inventory.setItem(s, black);
        }

        for (int s : new int[]{8, 16, 17, 25, 26, 34, 35, 43}) {
            inventory.setItem(s, white);
        }

        for (int s : new int[]{45, 47, 48, 50, 51}) {
            inventory.setItem(s, black);
        }

        // Slot 2 (Index 1): Rename Kit
        List<Component> nameTagLore = List.of(
            MM.deserialize("<gray>Current Display Name:</gray> " + kit.getRawName()),
            Component.empty(),
            MM.deserialize("<gray>Clicking this closes the UI to accept chat input.</gray>"),
            MM.deserialize("<gray>You can use <gold><bold>&lt;gradient:#Hex:#Hex&gt;</bold></gold> properties</gray>"),
            MM.deserialize("<gray>while naming your kits to give it a neat look!</gray>")
        );
        inventory.setItem(1, GuiItem.make(Material.NAME_TAG, MM.deserialize("<gradient:#FFD700:#FF8C00><bold>✎ Modify Kit Name</bold></gradient>"), nameTagLore));

        // Slot 4 (Index 3): Icon Customization
        List<Component> chestLore = List.of(
            MM.deserialize("<gray>Current Selector Material: <white>" + kit.getIconMaterial().name() + "</white></gray>"),
            Component.empty(),
            MM.deserialize("<gray>Hold any alternative item stack block inside your</gray>"),
            MM.deserialize("<gray>main inventory hand, then click here to save it as</gray>"),
            MM.deserialize("<gray>the primary menu display icon representing this kit.</gray>")
        );
        inventory.setItem(3, GuiItem.make(kit.getIconMaterial(), MM.deserialize("<gradient:#00FFFF:#4682B4><bold>⬛ Change Menu Icon</bold></gradient>"), chestLore));

        // Slot 6 (Index 5): Add Lore Line
        List<Component> bookLore = List.of(
            MM.deserialize("<gray>Current Configured Lines: <white>" + kit.getLore().size() + "</white></gray>"),
            Component.empty(),
            MM.deserialize("<gray>Appends an extra detailed line description to the icon lore.</gray>"),
            MM.deserialize("<gray>Supports standard <gold><bold>&lt;gradient:#Hex:#Hex&gt;</bold></gold> systems</gray>"),
            MM.deserialize("<gray>to maintain high-quality theme formats throughout menus.</gray>")
        );
        inventory.setItem(5, GuiItem.make(Material.WRITABLE_BOOK, MM.deserialize("<gradient:#DA70D6:#8A2BE2><bold>✎ Append Lore Descriptions</bold></gradient>"), bookLore));

        List<ItemStack> allItems = kit.getItems();
        int from = page * itemSlots.length;
        for (int i = 0; i < itemSlots.length; i++) {
            int idx = from + i;
            if (idx < allItems.size() && allItems.get(idx) != null) {
                inventory.setItem(itemSlots[i], allItems.get(idx).clone());
            }
        }

        // Slot 45 (Index 44): Cancel
        List<Component> cancelLore = List.of(
            MM.deserialize("<gray>Left-Click to exit to browser without adding modifications.</gray>"),
            MM.deserialize("<red><bold>Shift + Right-Click</bold></red> <gray>safely purges and destroys this kit completely.</gray>")
        );
        inventory.setItem(44, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<gradient:#8B0000:#FF0000><bold>✘ Cancel Changes</bold></gradient>"), cancelLore));

        // Slot 47 (Index 46): Previous Page
        if (page > 0) {
            List<Component> prevLore = List.of(MM.deserialize("<gray>Flips back to page <white>" + page + "</white> storage grid items.</gray>"));
            inventory.setItem(46, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<gradient:#8B0000:#FF8C00><bold>« Previous Page</bold></gradient>"), prevLore));
        } else {
            inventory.setItem(46, black);
        }

        // Slot 53 (Index 52): Next Page
        if (hasMoreItems()) {
            List<Component> nextLore = List.of(MM.deserialize("<gray>Advances to page <white>" + (page + 2) + "</white> to display more contents.</gray>"));
            inventory.setItem(52, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<gradient:#32CD32:#ADFF2F><bold>Next Page »</bold></gradient>"), nextLore));
        } else {
            inventory.setItem(52, black);
        }

        // Slot 54 (Index 53): Apply Changes
        List<Component> applyLore = List.of(
            MM.deserialize("<gray>Saves modified layout placements, metadata attributes,</gray>"),
            MM.deserialize("<gray>and returns selection to the centralized kit browser.</gray>")
        );
        inventory.setItem(53, GuiItem.make(Material.LIME_BUNDLE, MM.deserialize("<gradient:#006400:#32CD32><bold>✔ Apply & Save Changes</bold></gradient>"), applyLore));
    }

    private boolean hasMoreItems() {
        return kit.getItems().size() > (page + 1) * itemSlots.length;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

        int slot = event.getRawSlot();

        boolean isItemSlot = false;
        for (int s : itemSlots) {
            if (s == slot) {
                isItemSlot = true;
                break;
            }
        }

        if (isItemSlot || slot >= INV_SIZE) {
            return;
        }

        event.setCancelled(true);

        if (slot == 44) {
            if (event.isShiftClick() && event.isRightClick()) {
                saveCurrentPage();
                closing = true;
                HandlerList.unregisterAll(this);
                Bukkit.getScheduler().runTask(plugin, () -> new ConfirmDeleteGui(plugin, player, kit).open());
                return;
            }
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            plugin.getKitManager().refreshAllBrowsersSafe();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () -> new KitBrowserGui(plugin, player).open());

        } else if (slot == 1) { 
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();

            ChatInputSession.sendRenamePrompt(plugin, player);

            final Kit theKit = kit;
            final int thePage = page;
            new ChatInputSession(plugin, player,
                    newName -> {
                        theKit.setRawName(newName);
                        plugin.getKitManager().saveKits();
                        plugin.getKitManager().refreshAllBrowsersSafe();
                        new KitEditorGui(plugin, player, theKit, thePage).open();
                    },
                    () -> new KitEditorGui(plugin, player, theKit, thePage).open()
            ).await();

        } else if (slot == 3) { 
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() != Material.AIR) {
                kit.setIconMaterial(hand.getType());
                plugin.getKitManager().saveKits();
                saveCurrentPage();
                populate(); 
            } else {
                player.sendMessage(MM.deserialize("<gradient:#8B0000:#FF0000><bold>You must hold an item stack in your main hand to apply an icon!</bold></gradient>"));
            }

        } else if (slot == 5) { 
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();

            ChatInputSession.sendLorePrompt(plugin, player);

            final Kit theKit = kit;
            final int thePage = page;
            new ChatInputSession(plugin, player,
                    loreLine -> {
                        theKit.addLoreLine(loreLine);
                        plugin.getKitManager().saveKits();
                        new KitEditorGui(plugin, player, theKit, thePage).open();
                    },
                    () -> new KitEditorGui(plugin, player, theKit, thePage).open()
            ).await();

        } else if (slot == 46 && page > 0) { 
            saveCurrentPage();
            page--;
            populate();

        } else if (slot == 52 && hasMoreItems()) { 
            saveCurrentPage();
            page++;
            populate();

        } else if (slot == 53) { 
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            plugin.getKitManager().refreshAllBrowsersSafe();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () -> new KitBrowserGui(plugin, player).open());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getPlayer().equals(player)) return;
        if (closing) return;

        saveCurrentPage();
        plugin.getKitManager().saveKits();
        plugin.getKitManager().refreshAllBrowsersSafe();
        HandlerList.unregisterAll(this);
    }

    private void saveCurrentPage() {
        List<ItemStack> allItems = new ArrayList<>(kit.getItems());
        int itemsPerPage = itemSlots.length;
        int from = page * itemsPerPage;

        while (allItems.size() < from + itemsPerPage) {
            allItems.add(null);
        }

        for (int i = 0; i < itemsPerPage; i++) {
            ItemStack item = inventory.getItem(itemSlots[i]);
            if (item != null && item.getType() != Material.AIR) {
                allItems.set(from + i, item.clone());
            } else {
                allItems.set(from + i, null);
            }
        }

        while (!allItems.isEmpty() && allItems.get(allItems.size() - 1) == null) {
            allItems.remove(allItems.size() - 1);
        }

        kit.clearItems();
        for (ItemStack is : allItems) {
            if (is != null && is.getType() != Material.AIR) {
                kit.addItem(is);
            } else {
                kit.addItem(new ItemStack(Material.AIR));
            }
        }

        List<ItemStack> kitItems = kit.getItems();
        while (!kitItems.isEmpty() && kitItems.get(kitItems.size() - 1).getType() == Material.AIR) {
            List<ItemStack> temp = new ArrayList<>(kitItems);
            temp.remove(temp.size() - 1);
            kit.clearItems();
            for (ItemStack ts : temp) {
                kit.addItem(ts);
            }
            kitItems = kit.getItems();
        }
    }
}
