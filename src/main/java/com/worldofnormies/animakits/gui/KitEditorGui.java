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

import java.util.ArrayList;
import java.util.List;

/**
 * KitEditorGui – multi-page kit editor.
 *
 * Bottom bar (slots 45-53):
 *   45 = RED  Bundle  → Cancel / back to Kits Main Menu
 *   46 = BLACK pane   (filler)
 *   47 = BLACK pane   (filler)
 *   48 = NAME TAG     → Rename kit (chat input with gradient guide)
 *   49 = CHEST/icon   → Set kit icon from main hand
 *   50 = BOOK&QUILL   → Add lore line (chat input with gradient guide)
 *   51 = BLACK pane   (filler)
 *   52 = RED  pane    → Prev page  (hidden on page 0, black pane instead)
 *   53 = LIME pane    → Next page  OR  LIME Bundle → Apply & back (last page)
 */
public class KitEditorGui implements Listener {

    private static final MiniMessage MM         = MiniMessage.miniMessage();
    private static final int         INV_SIZE   = 54;
    private static final int         ITEMS_AREA = 45;  // slots 0-44

    // Top bar slot assignments (for controls)
    private static final int SLOT_NAME_TAG      = 2;
    private static final int SLOT_CHEST         = 4;
    private static final int SLOT_BOOK          = 6;

    // Bottom bar slot assignments
    private static final int SLOT_RED_BUNDLE    = 45;
    private static final int SLOT_PREV_PAGE     = 52;
    private static final int SLOT_NEXT_OR_APPLY = 53;

    private final AnimaKitsPlugin plugin;
    private final Player          player;
    private final Kit             kit;
    private int                   page;
    private Inventory             inventory;
    private boolean               closing = false;

    public KitEditorGui(AnimaKitsPlugin plugin, Player player, Kit kit, int page) {
        this.plugin = plugin;
        this.player = player;
        this.kit    = kit;
        this.page   = page;
    }

    // ── Open ──────────────────────────────────────────────────────

    public void open() {
        Component title = MM.deserialize("[ New Kit Edit ]");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    // ── Populate ──────────────────────────────────────────────────

    private void populate() {
        inventory.clear();

        // Decoration panes
        ItemStack black = makePaneBlack();
        // Slot = [0, 1, 3, 5, 7, 8, 46, 48, 49, 51, 52] -> Index: 0, 1, 2, 4, 6, 7, 45, 47, 48, 50, 51
        // Wait, slot 0 is 0. Slot 1 is 1. Slot 3 is 2. Slot 5 is 4. Slot 7 is 6. Slot 8 is 7.
        for (int s : new int[]{0, 1, 2, 4, 6, 7, 45, 47, 48, 50, 51}) {
            inventory.setItem(s, black);
        }

        ItemStack white = makePaneColoured(Material.WHITE_STAINED_GLASS_PANE, Component.space(), List.of());
        // Slot = [9, 17, 18, 26, 27, 35, 36, 44] -> Index: 8, 16, 17, 25, 26, 34, 35, 43
        for (int s : new int[]{8, 16, 17, 25, 26, 34, 35, 43}) {
            inventory.setItem(s, white);
        }

        // Slot 2 (Index 2) – Name Tag → Rename (Wait, user said Name Tag Slot 2, which is index 1...)
        // Let's re-read: Name Tag: Slot = [2]. Slot 1 is 0, Slot 2 is 1.
        // My index 2 is Slot 3.

        // Re-calibrating:
        // Decoration: [0, 1, 3, 5, 7, 8, 46, 48, 49, 51, 52]
        // 0->0, 1->1, 3->2, 5->4, 7->6, 8->7
        // Name Tag: 2 -> Index 1
        // Chest: 4 -> Index 3
        // Book: 6 -> Index 5
        // Red Bundle: 45 -> Index 44
        // Red Pane: 47 -> Index 46
        // Lime Pane: 53 -> Index 52
        // Green Bundle: 54 -> Index 53

        // Let's re-populate with corrected indices:
        inventory.setItem(0, black);
        inventory.setItem(2, black);
        inventory.setItem(4, black);
        inventory.setItem(6, black);
        inventory.setItem(7, black);
        inventory.setItem(45, black);
        inventory.setItem(47, black);
        inventory.setItem(48, black);
        inventory.setItem(50, black);
        inventory.setItem(51, black);

        inventory.setItem(8, white);
        inventory.setItem(16, white);
        inventory.setItem(17, white);
        inventory.setItem(25, white);
        inventory.setItem(26, white);
        inventory.setItem(34, white);
        inventory.setItem(35, white);
        inventory.setItem(43, white);

        inventory.setItem(1, GuiItem.make(
                Material.NAME_TAG,
                MM.deserialize("<yellow><bold>✎ Rename Kit</bold></yellow>"),
                List.of(MM.deserialize("<gray>Current: <white>" + kit.getRawName() + "</white></gray>"))));

        inventory.setItem(3, GuiItem.make(
                kit.getIconMaterial(),
                MM.deserialize("<aqua><bold>⬛ Kit Icon</bold></aqua>"),
                List.of(MM.deserialize("<gray>Current icon: <white>" + kit.getIconMaterial().name() + "</white></gray>"))));

        inventory.setItem(5, GuiItem.make(
                Material.WRITABLE_BOOK,
                MM.deserialize("<light_purple><bold>✎ Add Lore Line</bold></light_purple>"),
                List.of(MM.deserialize("<gray>Current lore lines: <white>" + kit.getLore().size() + "</white></gray>"))));

        // Item area: 9-16, 18-25, 27-34, 36-43 (indices)
        int[] itemSlots = {
            9, 10, 11, 12, 13, 14, 15,
            18, 19, 20, 21, 22, 23, 24,
            27, 28, 29, 30, 31, 32, 33,
            36, 37, 38, 39, 40, 41, 42
        };

        List<ItemStack> allItems = kit.getItems();
        int from = page * itemSlots.length;
        for (int i = 0; i < itemSlots.length; i++) {
            int idx = from + i;
            if (idx < allItems.size() && allItems.get(idx) != null) {
                inventory.setItem(itemSlots[i], allItems.get(idx).clone());
            }
        }

        // Navigation
        inventory.setItem(44, makeBundle(Material.RED_BUNDLE, MM.deserialize("<red><bold>✘ Cancel</bold></red>"), List.of()));

        if (page > 0) {
            inventory.setItem(46, makePaneColoured(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<red><bold>« Previous Page</bold></red>"), List.of()));
        } else {
            inventory.setItem(46, black);
        }

        if (toManyItems()) {
            inventory.setItem(52, makePaneColoured(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<green><bold>Next Page »</bold></green>"), List.of()));
        } else {
            inventory.setItem(52, black);
        }

        inventory.setItem(53, makeBundle(Material.LIME_BUNDLE, MM.deserialize("<green><bold>✔ Apply Changes</bold></green>"), List.of()));
    }

    private boolean toManyItems() {
        return kit.getItems().size() > (page + 1) * 28; // 28 is itemSlots.length
    }

    // ── Events ────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player))   return;

        int slot = event.getRawSlot();

        // Item area slots
        int[] itemSlots = {
            9, 10, 11, 12, 13, 14, 15,
            18, 19, 20, 21, 22, 23, 24,
            27, 28, 29, 30, 31, 32, 33,
            36, 37, 38, 39, 40, 41, 42
        };
        boolean isItemSlot = false;
        for (int s : itemSlots) { if (s == slot) { isItemSlot = true; break; } }

        // Allow interaction with item slots and player inventory
        if (isItemSlot || slot >= INV_SIZE) {
            return;
        }

        event.setCancelled(true);

        // Indices updated to match populate()
        // SLOT_RED_BUNDLE was 45 (slot 46 in 1-based)
        // User's template says: Red Bundle: Slot = [45] -> Index 44
        if (slot == 44) { // Cancel
            // Check for right click to delete
            if (event.isRightClick()) {
                saveCurrentPage();
                closing = true;
                HandlerList.unregisterAll(this);
                Bukkit.getScheduler().runTask(plugin, () -> new ConfirmDeleteGui(plugin, player, kit).open());
                return;
            }
            // Save + return to browser
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            plugin.getKitManager().refreshAllBrowsersSafe();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () -> new KitBrowserGui(plugin, player).open());

        } else if (slot == 1) { // Name Tag
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();

            // Show the rename prompt in chat
            ChatInputSession.sendRenamePrompt(plugin, player);

            final Kit  theKit  = kit;
            final int  thePage = page;
            new ChatInputSession(plugin, player,
                    newName -> {
                        theKit.setRawName(newName);
                        plugin.getKitManager().saveKits();
                        plugin.getKitManager().refreshAllBrowsersSafe();
                        new KitEditorGui(plugin, player, theKit, thePage).open();
                    },
                    () -> new KitEditorGui(plugin, player, theKit, thePage).open()
            ).await();

        } else if (slot == 3) { // Chest
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() != Material.AIR) {
                kit.setIconMaterial(hand.getType());
                plugin.getKitManager().saveKits();
                saveCurrentPage();
                closing = true;
                HandlerList.unregisterAll(this);
                Bukkit.getScheduler().runTask(plugin, () ->
                        new KitEditorGui(plugin, player, kit, page).open());
            }

        } else if (slot == 5) { // Book
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();

            // Show the lore prompt in chat
            ChatInputSession.sendLorePrompt(plugin, player);

            final Kit  theKit  = kit;
            final int  thePage = page;
            new ChatInputSession(plugin, player,
                    loreLine -> {
                        theKit.addLoreLine(loreLine);
                        plugin.getKitManager().saveKits();
                        new KitEditorGui(plugin, player, theKit, thePage).open();
                    },
                    () -> new KitEditorGui(plugin, player, theKit, thePage).open()
            ).await();

        } else if (slot == 46 && page > 0) { // Prev Page
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () ->
                    new KitEditorGui(plugin, player, kit, page - 1).open());

        } else if (slot == 52 && toManyItems()) { // Next Page
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () ->
                    new KitEditorGui(plugin, player, kit, page + 1).open());

        } else if (slot == 53) { // Apply
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            plugin.getKitManager().refreshAllBrowsersSafe();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () ->
                    new KitBrowserGui(plugin, player).open());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getPlayer().equals(player))       return;
        if (closing) { HandlerList.unregisterAll(this); return; }

        // Auto-save on natural close (Escape key, etc.)
        saveCurrentPage();
        plugin.getKitManager().saveKits();
        plugin.getKitManager().refreshAllBrowsersSafe();
        HandlerList.unregisterAll(this);
    }

    // ── Page save ─────────────────────────────────────────────────

    private void saveCurrentPage() {
        List<ItemStack> allItems = new ArrayList<>(kit.getItems());
        int[] itemSlots = {
            9, 10, 11, 12, 13, 14, 15,
            18, 19, 20, 21, 22, 23, 24,
            27, 28, 29, 30, 31, 32, 33,
            36, 37, 38, 39, 40, 41, 42
        };
        int itemsPerPage = itemSlots.length;
        int from = page * itemsPerPage;

        // Expand to fit this page
        while (allItems.size() < from + itemsPerPage) allItems.add(null);

        for (int i = 0; i < itemsPerPage; i++) {
            ItemStack item = inventory.getItem(itemSlots[i]);
            if (item != null && item.getType() != Material.AIR) {
                allItems.set(from + i, item.clone());
            } else {
                allItems.set(from + i, null);
            }
        }

        // Trim trailing nulls
        while (!allItems.isEmpty() && allItems.get(allItems.size() - 1) == null) {
            allItems.remove(allItems.size() - 1);
        }

        kit.clearItems();
        for (ItemStack is : allItems) {
            if (is != null && is.getType() != Material.AIR) {
                kit.addItem(is);
            } else {
                // If we want to preserve gaps, we should add AIR or keep them as null.
                // User said: "i dont think there is a difference , so yeah i guess but if player has full inventory ensure to drop items on ground at players position"
                // This suggests compressing is fine.
                // To preserve gaps in GUI, we should probably store AIR and skip it when giving.
                kit.addItem(new ItemStack(Material.AIR));
            }
        }

        // Final trim of AIR from the end of the list
        List<ItemStack> kitItems = kit.getItems();
        while (!kitItems.isEmpty() && kitItems.get(kitItems.size() - 1).getType() == Material.AIR) {
            kit.clearItems(); // Not efficient but Kit.java doesn't have removeLast
            // Re-adding everything except last
            List<ItemStack> temp = new ArrayList<>(kitItems);
            temp.remove(temp.size() - 1);
            for (ItemStack ts : temp) kit.addItem(ts);
            kitItems = kit.getItems();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    /**
     * Total number of pages: at least 1; one extra page is added when the
     * current item count exactly fills a page (to allow adding more items).
     */
    private int totalPages() {
        int size = kit.getItems().size();
        int itemsPerPage = ITEMS_AREA - 9;
        if (size == 0) return 1;
        return (int) Math.ceil(size / (double) itemsPerPage);
    }

    private ItemStack makePaneBlack() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta  meta = item.getItemMeta();
        if (meta != null) { meta.displayName(Component.space()); item.setItemMeta(meta); }
        return item;
    }

    private ItemStack makePaneColoured(Material mat, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack makeBundle(Material mat, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}