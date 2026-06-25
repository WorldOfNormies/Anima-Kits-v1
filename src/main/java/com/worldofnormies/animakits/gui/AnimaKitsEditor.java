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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * AnimaKitsEditor – multi-page kit editor.
 *
 * Row 1 (slots 0-8) = control bar (locked).
 * Rows 2-5 (slots 9-44) = editable item area (drag freely).
 * Row 6 (slots 45-53) = bottom navigation bar.
 *
 * Control bar layout:
 *   0 = Name Tag   → Rename kit (chat input)
 *   2 = Kit Icon   → Set icon (item in hand)
 *   4 = Clock      → Set cooldown (chat input)
 *   6 = Book&Quill → Add lore line (chat input)
 *   8 = Barrier    → Toggle single-claim
 *
 * Bottom bar layout:
 *   44 = Red Bundle       → Cancel / back to main
 *   47 = Red Glass Pane   → « Previous page
 *   51 = Lime Glass Pane  → Next page »
 *   53 = Lime Bundle      → Apply & Save
 */
public class AnimaKitsEditor implements Listener {

    private static final MiniMessage MM       = MiniMessage.miniMessage();
    private static final int         INV_SIZE = 54;

    // Editable item area: rows 2-5
    private static final int EDIT_START = 9;
    private static final int EDIT_END   = 45; // exclusive
    private static final int ITEMS_PER_PAGE = EDIT_END - EDIT_START; // 36

    // Control bar slots
    private static final int SLOT_NAME_TAG    = 0;
    private static final int SLOT_ICON        = 2;
    private static final int SLOT_COOLDOWN    = 4;
    private static final int SLOT_BOOK        = 6;
    private static final int SLOT_SINGLE      = 8;

    // Bottom bar slots
    private static final int SLOT_RED_BUNDLE  = 44;
    private static final int SLOT_PREV_PAGE   = 47;
    private static final int SLOT_NEXT_PAGE   = 51;
    private static final int SLOT_GREEN_BUNDLE= 53;

    private final AnimaKitsPlugin plugin;
    private final Player          player;
    private final Kit             kit;
    private int                   page;
    private Inventory             inventory;
    private boolean               closing = false;

    public AnimaKitsEditor(AnimaKitsPlugin plugin, Player player, Kit kit, int page) {
        this.plugin = plugin;
        this.player = player;
        this.kit    = kit;
        this.page   = page;
    }

    public void open() {
        Component title = MM.deserialize(
                "<gradient:#FFD700:#FF8C00:#FF4500><bold>[ </bold></gradient>" +
                "<gradient:#FF6AFF:#AA00FF><bold>" + kit.getPlainName() + " Edit</bold></gradient>" +
                "<gradient:#FFD700:#FF8C00:#FF4500><bold> ] PAGE " + (page + 1) + "</bold></gradient>");

        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();

        // ── Control bar (row 1) ──────────────────────────────────────
        ItemStack black = paneBlack();
        for (int i = 0; i < 9; i++) inventory.setItem(i, black);

        inventory.setItem(SLOT_NAME_TAG, GuiItem.make(
                Material.NAME_TAG,
                MM.deserialize("<gradient:#FFD700:#FFA500><bold>✎ Rename Kit</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Current name: <white>" + kit.getPlainName() + "</white></gray>"),
                    Component.empty(),
                    MM.deserialize("<gradient:#FFD700:#FFA500>⬡ Click</gradient><gray> to set a new name.</gray>"),
                    MM.deserialize("<dark_gray>Type <white>//cancel</white> to abort.</dark_gray>")
                )));

        inventory.setItem(SLOT_ICON, GuiItem.make(
                kit.getIconMaterial(),
                MM.deserialize("<gradient:#4FC3F7:#1565C0><bold>⬛ Kit Icon</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Current: <white>" + kit.getIconMaterial().name() + "</white></gray>"),
                    Component.empty(),
                    MM.deserialize("<gradient:#4FC3F7:#1565C0>⬡ Click</gradient><gray> while holding an item to set as icon.</gray>")
                )));

        inventory.setItem(SLOT_COOLDOWN, GuiItem.make(
                Material.CLOCK,
                MM.deserialize("<gradient:#CE93D8:#6A1B9A><bold>⏱ Set Cooldown</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Current: <white>" +
                        (kit.getCooldown() == 0 ? "None" : kit.getCooldown() + "s") + "</white></gray>"),
                    Component.empty(),
                    MM.deserialize("<gradient:#CE93D8:#6A1B9A>⬡ Click</gradient><gray> to set cooldown (e.g. <white>300</white> for 5 min).</gray>"),
                    MM.deserialize("<dark_gray>Use <white>0</white> for no cooldown.</dark_gray>")
                )));

        inventory.setItem(SLOT_BOOK, GuiItem.make(
                Material.WRITABLE_BOOK,
                MM.deserialize("<gradient:#A5D6A7:#2E7D32><bold>✎ Add Lore Line</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Lore lines: <white>" + kit.getLore().size() + "</white></gray>"),
                    Component.empty(),
                    MM.deserialize("<gradient:#A5D6A7:#2E7D32>⬡ Click</gradient><gray> to add a lore line.</gray>"),
                    MM.deserialize("<dark_gray>Supports MiniMessage formatting.</dark_gray>")
                )));

        inventory.setItem(SLOT_SINGLE, GuiItem.make(
                kit.isSingleClaim() ? Material.BARRIER : Material.LIME_CONCRETE,
                kit.isSingleClaim()
                    ? MM.deserialize("<gradient:#EF9A9A:#B71C1C><bold>⊗ Single-Claim: ON</bold></gradient>")
                    : MM.deserialize("<gradient:#A5D6A7:#2E7D32><bold>⊕ Single-Claim: OFF</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Players can only claim this kit <white>once</white>.</gray>"),
                    Component.empty(),
                    MM.deserialize("<gradient:#CE93D8:#6A1B9A>⬡ Click</gradient><gray> to toggle.</gray>")
                )));

        // ── Editable item area (rows 2-5) ────────────────────────────
        List<ItemStack> allItems = kit.getItems();
        int from = page * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int idx = from + i;
            if (idx < allItems.size() && allItems.get(idx) != null
                    && allItems.get(idx).getType() != org.bukkit.Material.AIR) {
                inventory.setItem(EDIT_START + i, allItems.get(idx).clone());
            }
            // leave empty slots truly empty so players can drag items in freely
        }

        // ── Bottom bar ───────────────────────────────────────────────
        ItemStack purplePane = panePurple();
        for (int s = 45; s < 54; s++) inventory.setItem(s, purplePane);

        inventory.setItem(SLOT_RED_BUNDLE, GuiItem.make(Material.RED_BUNDLE,
                MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Cancel</bold></gradient>"),
                List.of(MM.deserialize("<gray>Return to the main kits menu without saving.</gray>"))));

        inventory.setItem(SLOT_PREV_PAGE, GuiItem.make(Material.RED_STAINED_GLASS_PANE,
                MM.deserialize("<gradient:#FF6060:#CC0000><bold>« Previous Page</bold></gradient>"),
                List.of(MM.deserialize("<gray>Page <white>" + page + "/" + Math.max(1,
                        (int) Math.ceil(allItems.size() / (double) ITEMS_PER_PAGE)) + "</white></gray>"))));

        inventory.setItem(SLOT_NEXT_PAGE, GuiItem.make(Material.LIME_STAINED_GLASS_PANE,
                MM.deserialize("<gradient:#44FF88:#00AA44><bold>Next Page »</bold></gradient>"),
                List.of(MM.deserialize("<gray>Add more items on the next page.</gray>"))));

        inventory.setItem(SLOT_GREEN_BUNDLE, GuiItem.make(Material.LIME_BUNDLE,
                MM.deserialize("<gradient:#44FF88:#00CC55><bold>✔ Apply & Save</bold></gradient>"),
                List.of(MM.deserialize("<gray>Save all changes and return to main menu.</gray>"))));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

        int slot = event.getRawSlot();

        // ── Allow full interaction in edit area ──────────────────────
        if (slot >= EDIT_START && slot < EDIT_END) {
            // Let vanilla handle it – player can place, take, swap freely
            return;
        }

        // ── Prevent moving items from player inv into control/bottom bar ──
        // But allow clicks originating from player inventory that target empty edit slots
        // (handled above by the early return). Everything else in the GUI is locked.
        event.setCancelled(true);

        // ── Control bar ──────────────────────────────────────────────
        if (slot == SLOT_ICON) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() != Material.AIR) {
                kit.setIconMaterial(hand.getType());
                populate();
            }
            return;
        }

        if (slot == SLOT_SINGLE) {
            kit.setSingleClaim(!kit.isSingleClaim());
            populate();
            return;
        }

        // ── Chat-input actions ───────────────────────────────────────
        if (slot == SLOT_NAME_TAG) {
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();
            ChatInputSession.sendRenamePrompt(plugin, player);
            new ChatInputSession(plugin, player,
                    newName -> {
                        kit.setRawName(newName);
                        plugin.getKitManager().saveKits();
                        new AnimaKitsEditor(plugin, player, kit, page).open();
                    },
                    () -> new AnimaKitsEditor(plugin, player, kit, page).open()
            ).await();
            return;
        }

        if (slot == SLOT_COOLDOWN) {
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();
            player.sendMessage(MM.deserialize(
                "<gradient:#CE93D8:#6A1B9A><bold>⏱ Set Cooldown</bold></gradient>\n" +
                "<gray>Type the cooldown in <white>seconds</white> (e.g. <yellow>300</yellow> = 5 min).\n" +
                "Type <white>0</white> for no cooldown. Type <white>//cancel</white> to abort.</gray>"));
            new ChatInputSession(plugin, player,
                    input -> {
                        if (input.equalsIgnoreCase("//cancel")) {
                            new AnimaKitsEditor(plugin, player, kit, page).open();
                            return;
                        }
                        try {
                            long secs = Long.parseLong(input.trim());
                            kit.setCooldown(Math.max(0, secs));
                            plugin.getKitManager().saveKits();
                            player.sendMessage(MM.deserialize(
                                "<gradient:#44FF88:#00CC55>✔ Cooldown set to <white>" + kit.getCooldown() + "s</white>.</gradient>"));
                        } catch (NumberFormatException e) {
                            player.sendMessage(MM.deserialize("<red>✘ Invalid number. Cooldown unchanged.</red>"));
                        }
                        new AnimaKitsEditor(plugin, player, kit, page).open();
                    },
                    () -> new AnimaKitsEditor(plugin, player, kit, page).open()
            ).await();
            return;
        }

        if (slot == SLOT_BOOK) {
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();
            ChatInputSession.sendLorePrompt(plugin, player);
            new ChatInputSession(plugin, player,
                    line -> {
                        kit.addLoreLine(line);
                        new AnimaKitsEditor(plugin, player, kit, page).open();
                    },
                    () -> new AnimaKitsEditor(plugin, player, kit, page).open()
            ).await();
            return;
        }

        // ── Bottom bar ───────────────────────────────────────────────
        if (slot == SLOT_RED_BUNDLE) {
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () -> new AnimaKitsMainGUI(plugin, player).open());
            return;
        }

        if (slot == SLOT_GREEN_BUNDLE) {
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            plugin.getKitManager().refreshAllBrowsersSafe();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () -> new AnimaKitsMainGUI(plugin, player).open());
            return;
        }

        if (slot == SLOT_PREV_PAGE && page > 0) {
            saveCurrentPage();
            page--;
            open();
            return;
        }

        if (slot == SLOT_NEXT_PAGE) {
            saveCurrentPage();
            page++;
            open();
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        // Allow drags that only touch the edit area
        for (int slot : event.getRawSlots()) {
            if (slot < EDIT_START || slot >= EDIT_END) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (closing) return;
        saveCurrentPage();
        plugin.getKitManager().saveKits();
        HandlerList.unregisterAll(this);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private void saveCurrentPage() {
        List<ItemStack> allItems = new ArrayList<>(kit.getItems());
        int from = page * ITEMS_PER_PAGE;

        while (allItems.size() < from + ITEMS_PER_PAGE) allItems.add(null);

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            ItemStack item = inventory.getItem(EDIT_START + i);
            allItems.set(from + i,
                    (item != null && item.getType() != Material.AIR) ? item.clone() : null);
        }

        kit.clearItems();
        for (ItemStack is : allItems) {
            kit.addItem(is != null ? is : new ItemStack(Material.AIR));
        }
    }

    private ItemStack paneBlack() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.displayName(Component.space()); item.setItemMeta(meta); }
        return item;
    }

    private ItemStack panePurple() {
        ItemStack item = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.displayName(Component.space()); item.setItemMeta(meta); }
        return item;
    }
}