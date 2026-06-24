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

    // Bottom bar slot assignments
    private static final int SLOT_RED_BUNDLE    = 45;
    private static final int SLOT_NAME_TAG      = 48;
    private static final int SLOT_CHEST         = 49;
    private static final int SLOT_BOOK          = 50;
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
        // Title always shows kit name; shows PAGE N only when there are multiple pages
        String kitDisplayName = kit.getRawName();
        String pageSuffix     = totalPages() > 1 ? " PAGE " + (page + 1) : "";
        String titleRaw       = "[ " + kitDisplayName + " ]" + pageSuffix;
        Component title       = MM.deserialize(
                "<gradient:#54DAF4:#545EB6><bold>" + titleRaw + "</bold></gradient>");

        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    // ── Populate ──────────────────────────────────────────────────

    private void populate() {
        for (int i = 0; i < INV_SIZE; i++) inventory.setItem(i, null);

        // Item area – slots 0-44
        List<ItemStack> allItems = kit.getItems();
        int from = page * ITEMS_AREA;
        for (int i = 0; i < ITEMS_AREA; i++) {
            int idx = from + i;
            if (idx < allItems.size() && allItems.get(idx) != null) {
                inventory.setItem(i, allItems.get(idx).clone());
            }
        }

        // Filler panes
        ItemStack black = makePaneBlack();
        for (int s : new int[]{46, 47, 51}) inventory.setItem(s, black);

        // Slot 45 – Red Bundle → Cancel / return to browser
        inventory.setItem(SLOT_RED_BUNDLE, makeBundle(
                Material.RED_BUNDLE,
                MM.deserialize("<red><bold>✘ Cancel / Return to Kits Menu</bold></red>"),
                List.of(MM.deserialize("<gray>Return to the Kits Main Menu.</gray>"),
                        MM.deserialize("<gray>Page changes will be saved.</gray>"))));

        // Slot 48 – Name Tag → Rename
        inventory.setItem(SLOT_NAME_TAG, GuiItem.make(
                Material.NAME_TAG,
                MM.deserialize("<yellow><bold>✎ Rename Kit</bold></yellow>"),
                List.of(
                        MM.deserialize("<gray>Current: <white>" + kit.getRawName() + "</white></gray>"),
                        Component.empty(),
                        MM.deserialize("<gray>Click to rename this kit.</gray>"),
                        MM.deserialize("<gray>A chat prompt will guide you.</gray>"))));

        // Slot 49 – Kit icon (shows the actual current icon material)
        Material iconMat = kit.getIconMaterial();
        inventory.setItem(SLOT_CHEST, GuiItem.make(
                iconMat,
                MM.deserialize("<aqua><bold>⬛ Kit Icon</bold></aqua>"),
                List.of(
                        MM.deserialize("<gray>Hold an item in your <white>main hand</white></gray>"),
                        MM.deserialize("<gray>and click to set it as the kit icon.</gray>"),
                        Component.empty(),
                        MM.deserialize("<gray>Current icon: <white>" + iconMat.name() + "</white></gray>"))));

        // Slot 50 – Book & Quill → Add lore line
        inventory.setItem(SLOT_BOOK, GuiItem.make(
                Material.WRITABLE_BOOK,
                MM.deserialize("<light_purple><bold>✎ Add Lore Line</bold></light_purple>"),
                List.of(
                        MM.deserialize("<gray>Click to add a new lore line.</gray>"),
                        MM.deserialize("<gray>Supports gradient & hex colours.</gray>"),
                        Component.empty(),
                        MM.deserialize("<gray>Current lore lines: <white>" + kit.getLore().size() + "</white></gray>"))));

        // Slot 52 – Prev page (black pane on page 0)
        if (page > 0) {
            inventory.setItem(SLOT_PREV_PAGE, makePaneColoured(
                    Material.RED_STAINED_GLASS_PANE,
                    MM.deserialize("<red><bold>« Page " + page + "</bold></red>"),
                    List.of(MM.deserialize("<gray>Go back to page <white>" + page + "</white>.</gray>"))));
        } else {
            inventory.setItem(SLOT_PREV_PAGE, black);
        }

        // Slot 53 – Next page pane OR Lime Bundle (apply & exit on last/only page)
        int tp = totalPages();
        if (page < tp - 1) {
            inventory.setItem(SLOT_NEXT_OR_APPLY, makePaneColoured(
                    Material.LIME_STAINED_GLASS_PANE,
                    MM.deserialize("<green><bold>Page " + (page + 2) + " »</bold></green>"),
                    List.of(MM.deserialize("<gray>Go to page <white>" + (page + 2) + "</white>.</gray>"))));
        } else {
            inventory.setItem(SLOT_NEXT_OR_APPLY, makeBundle(
                    Material.LIME_BUNDLE,
                    MM.deserialize("<green><bold>✔ Apply & Return to Kits Menu</bold></green>"),
                    List.of(MM.deserialize("<gray>Save all changes and go back.</gray>"))));
        }
    }

    // ── Events ────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player))   return;

        int slot = event.getRawSlot();

        // Allow free drag-and-drop in the item area (slots 0-44)
        if (slot >= 0 && slot < ITEMS_AREA) {
            return; // let vanilla handle it
        }

        event.setCancelled(true);

        if (slot == SLOT_RED_BUNDLE) {
            // Save + return to browser
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            plugin.getKitManager().refreshAllBrowsersSafe();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () -> new KitBrowserGui(plugin, player).open());

        } else if (slot == SLOT_NAME_TAG) {
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

        } else if (slot == SLOT_CHEST) {
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

        } else if (slot == SLOT_BOOK) {
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

        } else if (slot == SLOT_PREV_PAGE && page > 0) {
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () ->
                    new KitEditorGui(plugin, player, kit, page - 1).open());

        } else if (slot == SLOT_NEXT_OR_APPLY) {
            saveCurrentPage();
            int tp = totalPages();
            if (page < tp - 1) {
                // Navigate to next existing page
                closing = true;
                HandlerList.unregisterAll(this);
                Bukkit.getScheduler().runTask(plugin, () ->
                        new KitEditorGui(plugin, player, kit, page + 1).open());
            } else {
                // Last page – apply and return to browser
                plugin.getKitManager().saveKits();
                plugin.getKitManager().refreshAllBrowsersSafe();
                closing = true;
                HandlerList.unregisterAll(this);
                Bukkit.getScheduler().runTask(plugin, () ->
                        new KitBrowserGui(plugin, player).open());
            }
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
        int from = page * ITEMS_AREA;

        // Expand to fit this page
        while (allItems.size() < from + ITEMS_AREA) allItems.add(null);

        for (int i = 0; i < ITEMS_AREA; i++) {
            ItemStack item = inventory.getItem(i);
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
            kit.addItem(is != null ? is : new ItemStack(Material.AIR));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    /**
     * Total number of pages: at least 1; one extra page is added when the
     * current item count exactly fills a page (to allow adding more items).
     */
    private int totalPages() {
        int size = kit.getItems().size();
        if (size == 0) return 1;
        return (int) Math.ceil(size / (double) ITEMS_AREA);
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