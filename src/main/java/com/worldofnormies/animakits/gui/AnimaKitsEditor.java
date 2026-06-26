package com.worldofnormies.animakits.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.kit.Kit;
import com.worldofnormies.animaitemedit.ItemEditUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AnimaKitsEditor – Multi-page kit editor with enhanced item interactions.
 *
 * Name Tag (slot 2) click actions:
 *   Left-Click         → rename kit (chat input)
 *   Middle-Click       → apply kit name as PREFIX to all items
 *   Shift+Middle-Click → apply kit name as SUFFIX to all items
 *
 * Kit item slot interactions (Shift + click):
 *   Shift+Left         → rename individual item (chat input)
 *   Shift+Right        → enchant item (chat input)
 *   Shift+Middle       → cycle unbreakable+glow states
 *
 * Items in editor display their real lore + action hints on hover.
 */
public class AnimaKitsEditor implements Listener {

    private static final MiniMessage MM       = MiniMessage.miniMessage();
    private static final int         INV_SIZE = 54;

    private static final int[] EDIT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    private static final int ITEMS_PER_PAGE = EDIT_SLOTS.length; // 28

    // Control bar (Row 1)
    private static final int SLOT_PLAYER_HEAD  = 0;
    private static final int SLOT_NAME_TAG     = 2;
    private static final int SLOT_ICON_CHEST   = 4;
    private static final int SLOT_BOOK_QUILL   = 6;
    private static final int SLOT_SINGLE_CLAIM = 8;

    // Bottom bar (Row 6)
    private static final int SLOT_RED_BUNDLE   = 45;
    private static final int SLOT_PREV_PAGE    = 47;
    private static final int SLOT_COOLDOWN_CLK = 49;
    private static final int SLOT_NEXT_PAGE    = 51;
    private static final int SLOT_GREEN_BUNDLE = 53;

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
        Component title = MM.deserialize("<gradient:#FFD700:#FF8C00:#FF4500><bold>[ </bold></gradient>")
                .append(com.worldofnormies.animakits.util.ColorUtil.parse(kit.getRawName()))
                .append(MM.deserialize("<gradient:#FF6AFF:#AA00FF><bold> Edit</bold></gradient>"))
                .append(MM.deserialize("<gradient:#FFD700:#FF8C00:#FF4500><bold> ]</bold></gradient>"));

        if (page > 0) {
            title = title.append(MM.deserialize("<gradient:#FFD700:#FF8C00:#FF4500><bold> PAGE " + (page + 1) + "</bold></gradient>"));
        }

        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();

        // ── 1. Border Pattern ──
        Material[] borderPattern = {
            Material.AIR, Material.MAGENTA_STAINED_GLASS_PANE, Material.AIR, Material.MAGENTA_STAINED_GLASS_PANE, Material.AIR, Material.MAGENTA_STAINED_GLASS_PANE, Material.AIR, Material.MAGENTA_STAINED_GLASS_PANE, Material.AIR,
            Material.PURPLE_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.PURPLE_STAINED_GLASS_PANE,
            Material.MAGENTA_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.MAGENTA_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.PURPLE_STAINED_GLASS_PANE,
            Material.MAGENTA_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.MAGENTA_STAINED_GLASS_PANE,
            Material.AIR, Material.PURPLE_STAINED_GLASS_PANE, Material.AIR, Material.PURPLE_STAINED_GLASS_PANE, Material.AIR, Material.PURPLE_STAINED_GLASS_PANE, Material.AIR, Material.PURPLE_STAINED_GLASS_PANE, Material.AIR
        };

        for (int i = 0; i < INV_SIZE; i++) {
            if (borderPattern[i] != Material.AIR) {
                inventory.setItem(i, makePane(borderPattern[i]));
            }
        }

        // ── 2. Control Bar (Row 1) ──
        inventory.setItem(SLOT_PLAYER_HEAD, GuiItem.make(
                Material.PLAYER_HEAD,
                MM.deserialize("<gradient:#4FC3F7:#1565C0><bold>👤 Kit Claim & Permissions</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Claim Free: " + (kit.isClaimFree() ? "<green><bold>TRUE</bold></green>" : "<red><bold>FALSE</bold></red>") + "</gray>"),
                    MM.deserialize("<gray>Time Duration To Claim: <white>" + formatTime(kit.getClaimDuration()) + "</white></gray>"),
                    Component.empty(),
                    MM.deserialize("<gradient:#4FC3F7:#1565C0>⬡ Left-Click</gradient><gray> to enter player name or selectors (@a/@e).</gray>"),
                    MM.deserialize("<gradient:#4FC3F7:#1565C0>⬡ Right-Click</gradient><gray> to toggle Claim Free status.</gray>"),
                    MM.deserialize("<gradient:#4FC3F7:#1565C0>⬡ Middle-Click</gradient><gray> to set claim time duration.</gray>"),
                    MM.deserialize("<dark_gray>Type <white>//cancel</white> to abort input windows.</dark_gray>")
                )));

        // Name Tag – expanded with middle/shift-middle actions
        inventory.setItem(SLOT_NAME_TAG, GuiItem.make(
                Material.NAME_TAG,
                MM.deserialize("<gradient:#FFD700:#FFA500><bold>✎ Kit Name & Item Branding</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Current name: </gray>").append(com.worldofnormies.animakits.util.ColorUtil.parse(kit.getRawName())),
                    Component.empty(),
                    MM.deserialize("<gradient:#FFD700:#FFA500>⬡ Left-Click</gradient><gray> · Rename this kit.</gray>"),
                    MM.deserialize("<gradient:#AA88FF:#6600FF>⬡ Middle-Click</gradient><gray> · Add kit name as <white>PREFIX</white> to all items.</gray>"),
                    MM.deserialize("<gradient:#FF88CC:#CC0077>⬡ Shift+Middle</gradient><gray> · Add kit name as <white>SUFFIX</white> to all items.</gray>"),
                    MM.deserialize("<dark_gray>Type <white>//cancel</white> to abort.</dark_gray>")
                )));

        inventory.setItem(SLOT_ICON_CHEST, GuiItem.make(
                kit.getIconMaterial(),
                MM.deserialize("<gradient:#FFB300:#FB8C00><bold>📦 Change Kit Icon</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Current Icon Material: <white>" + kit.getIconMaterial().name() + "</white></gray>"),
                    Component.empty(),
                    MM.deserialize("<gradient:#FFB300:#FB8C00>⬡ Click</gradient><gray> while holding an item to update.</gray>")
                )));

        inventory.setItem(SLOT_BOOK_QUILL, GuiItem.make(
                Material.WRITABLE_BOOK,
                MM.deserialize("<gradient:#A5D6A7:#2E7D32><bold>✎ Edit Kit Lore</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Total lines: <white>" + kit.getLore().size() + "</white></gray>"),
                    Component.empty(),
                    MM.deserialize("<gradient:#A5D6A7:#2E7D32>⬡ Click</gradient><gray> to write a new lore line.</gray>"),
                    MM.deserialize("<dark_gray>Supports MiniMessage formatting nodes.</dark_gray>")
                )));

        inventory.setItem(SLOT_SINGLE_CLAIM, GuiItem.make(
                kit.isSingleClaim() ? Material.BARRIER : Material.LIME_CONCRETE,
                kit.isSingleClaim()
                    ? MM.deserialize("<gradient:#EF9A9A:#B71C1C><bold>⊗ Single-Claim: ON</bold></gradient>")
                    : MM.deserialize("<gradient:#A5D6A7:#2E7D32><bold>⊕ Single-Claim: OFF</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Limits claiming parameters to single use iterations.</gray>"),
                    Component.empty(),
                    MM.deserialize("<gradient:#CE93D8:#6A1B9A>⬡ Click</gradient><gray> to switch configuration.</gray>")
                )));

        // ── 3. Kit Item Slots with hover lore + action hints ──
        List<ItemStack> allItems = kit.getItems();
        int from = page * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int idx = from + i;
            if (idx < allItems.size() && allItems.get(idx) != null
                    && allItems.get(idx).getType() != org.bukkit.Material.AIR) {
                ItemStack item = allItems.get(idx).clone();
                // If glow is on, ensure enchants are visible in lore
                if (item.getItemMeta() != null && item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
                    item = addEnchantLore(item);
                }
                inventory.setItem(EDIT_SLOTS[i], wrapItemWithEditorHints(item));
            }
        }

        // ── 4. Bottom Controls ──
        inventory.setItem(SLOT_RED_BUNDLE, GuiItem.make(Material.RED_BUNDLE,
                MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Cancel</bold></gradient>"),
                List.of(MM.deserialize("<gray>Return to /anima kits without saving.</gray>"))));

        int totalPages = Math.max(1, (int) Math.ceil(allItems.size() / (double) ITEMS_PER_PAGE));
        inventory.setItem(SLOT_PREV_PAGE, GuiItem.make(Material.RED_STAINED_GLASS_PANE,
                MM.deserialize("<gradient:#FF6060:#CC0000><bold>« Previous Page</bold></gradient>"),
                List.of(MM.deserialize("<gray>Page <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        inventory.setItem(SLOT_COOLDOWN_CLK, GuiItem.make(
                Material.CLOCK,
                MM.deserialize("<gradient:#CE93D8:#6A1B9A><bold>⏱ Change Cooldown</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Current Setting: <white>" +
                        (kit.getCooldown() == 0 ? "None" : kit.getCooldown() + "s") + "</white></gray>"),
                    Component.empty(),
                    MM.deserialize("<gradient:#CE93D8:#6A1B9A>⬡ Click</gradient><gray> to adjust time limitations.</gray>")
                )));

        inventory.setItem(SLOT_NEXT_PAGE, GuiItem.make(Material.LIME_STAINED_GLASS_PANE,
                MM.deserialize("<gradient:#44FF88:#00AA44><bold>Next Page »</bold></gradient>"),
                List.of(MM.deserialize("<gray>Open additional page item layout frames.</gray>"))));

        inventory.setItem(SLOT_GREEN_BUNDLE, GuiItem.make(Material.LIME_BUNDLE,
                MM.deserialize("<gradient:#44FF88:#00CC55><bold>✔ Apply & Save</bold></gradient>"),
                List.of(MM.deserialize("<gray>Apply operational variables directly to memory file structures.</gray>"))));
    }

    /**
     * Wraps a kit item with editor interaction hints appended to its lore.
     * The original lore is preserved; hints are added below a separator.
     */
    private ItemStack wrapItemWithEditorHints(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return item;
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;

        List<Component> lore = new ArrayList<>(meta.lore() != null ? meta.lore() : List.of());

        // Unbreakable badge
        if (meta.isUnbreakable()) {
            lore.add(Component.empty());
            lore.add(MM.deserialize("<gradient:#DA70D6:#FF00FF:#9400D3><bold>[Infinite Durability]</bold></gradient>"));
        }

        // Glow badge
        boolean hasGlow = meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
        if (hasGlow) {
            lore.add(MM.deserialize("<gradient:#FF00FF:#00FFFF:#FF69B4><bold>[Glow: On]</bold></gradient>"));
        }

        // Separator + action hints
        lore.add(Component.empty());
        lore.add(MM.deserialize("<dark_gray>──── <gray>Editor Actions</gray> ────</dark_gray>"));
        lore.add(MM.deserialize("<gradient:#FFD700:#FFA500>⬡ Shift+Left</gradient><gray> · Rename this item</gray>"));
        lore.add(MM.deserialize("<gradient:#FF6060:#CC0000>⬡ Shift+Right</gradient><gray> · Enchant this item</gray>"));
        lore.add(MM.deserialize("<gradient:#AA88FF:#6600FF>⬡ Shift+Middle</gradient><gray> · Cycle Unbreakable/Glow</gray>"));
        lore.add(MM.deserialize("<dark_gray>Drag items freely to rearrange.</dark_gray>"));

        meta.lore(lore);
        copy.setItemMeta(meta);
        return copy;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

        int slot = event.getRawSlot();
        if (slot < 0) return;
        if (slot >= INV_SIZE) return;

        // Check if this is one of the editable item slots
        boolean isEditSlot = false;
        int editSlotIndex = -1;
        for (int i = 0; i < EDIT_SLOTS.length; i++) {
            if (EDIT_SLOTS[i] == slot) {
                isEditSlot = true;
                editSlotIndex = i;
                break;
            }
        }

        if (isEditSlot) {
            ClickType click = event.getClick();
            // Allow regular drag/place (non-shift non-special clicks) through for item management
            if (click.isShiftClick()) {
                event.setCancelled(true);
                handleItemSlotAction(slot, editSlotIndex, click);
            } else if (click == ClickType.MIDDLE) {
                // If it's middle but not shift, we still cancel it as it was requested to be disabled
                event.setCancelled(true);
            }
            // Allow normal left/right clicks and drags for placing/taking items
            return;
        }

        // All other slots are control slots — cancel clicks
        event.setCancelled(true);

        // ── Player Head ──
        if (slot == SLOT_PLAYER_HEAD) {
            if (!event.getClick().isShiftClick()) return;
            saveCurrentPage();
            ClickType click = event.getClick();
            if (click.isLeftClick()) {
                closing = true;
                HandlerList.unregisterAll(this);
                player.closeInventory();
                player.sendMessage(MM.deserialize(
                    "<gradient:#4FC3F7:#1565C0><bold>👤 Assign Kit Permission</bold></gradient>\n" +
                    "<gray>Type a player name or targets (<white>@a</white>, <white>@e</white>, etc.) you want to allow to claim this kit.\n" +
                    "Type <white>//cancel</white> to abort.</gray>"));
                new ChatInputSession(plugin, player,
                        input -> {
                            if (input.equalsIgnoreCase("//cancel")) {
                                new AnimaKitsEditor(plugin, player, kit, page).open();
                                return;
                            }
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                    "lp user " + input.trim() + " permission set anima.kits.claim." + kit.getPlainName());
                            player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Assigned claim node to <white>" + input.trim() + "</white>!</gradient>"));
                            new AnimaKitsEditor(plugin, player, kit, page).open();
                        },
                        () -> new AnimaKitsEditor(plugin, player, kit, page).open()
                ).await();
            } else if (click.isRightClick()) {
                kit.setClaimFree(!kit.isClaimFree());
                populate();
            } else if (click == ClickType.MIDDLE) {
                closing = true;
                HandlerList.unregisterAll(this);
                player.closeInventory();
                player.sendMessage(MM.deserialize(
                    "<gradient:#4FC3F7:#1565C0><bold>⏱ Set Claim Time Duration</bold></gradient>\n" +
                    "<gray>Formats: <white>30s · 5m · 2h · 1d · 1d12h30m · 3600</white>\nUse <white>0</white> to clear. Type <white>//cancel</white> to abort.</gray>"));
                new ChatInputSession(plugin, player,
                        input -> {
                            if (input.equalsIgnoreCase("//cancel")) { new AnimaKitsEditor(plugin, player, kit, page).open(); return; }
                            try {
                                long secs = parseDurationEditor(input);
                                if (secs == -2) throw new NumberFormatException();
                                kit.setClaimDuration(Math.max(0, secs));
                                plugin.getKitManager().saveKits();
                                player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Claim window set to <white>" + formatTime(kit.getClaimDuration()) + "</white>.</gradient>"));
                            } catch (NumberFormatException e) {
                                player.sendMessage(MM.deserialize("<red>✘ Invalid duration.</red>"));
                            }
                            new AnimaKitsEditor(plugin, player, kit, page).open();
                        },
                        () -> new AnimaKitsEditor(plugin, player, kit, page).open()
                ).await();
            }
            return;
        }

        // ── Name Tag ──
        if (slot == SLOT_NAME_TAG) {
            if (!event.getClick().isShiftClick()) return;
            saveCurrentPage();
            ClickType click = event.getClick();

            if (click == ClickType.SHIFT_LEFT) {
                // Rename kit
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

            } else if (click == ClickType.MIDDLE) {
                // Apply kit name as PREFIX to all items
                applyKitNameToAllItems(false);
                player.sendMessage(MM.deserialize(
                    "<gradient:#AA88FF:#6600FF><bold>✔ Kit name applied as prefix to all items!</bold></gradient>"));

            } else if (click == ClickType.SHIFT_RIGHT) {
                applyKitNameToAllItems(true);
                player.sendMessage(MM.deserialize(
                    "<gradient:#FF88CC:#CC0077><bold>✔ Kit name applied as suffix to all items!</bold></gradient>"));
            }
            return;
        }

        // ── Icon ──
        if (slot == SLOT_ICON_CHEST) {
            if (!event.getClick().isShiftClick()) return;
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() != Material.AIR) {
                saveCurrentPage();
                kit.setIconMaterial(hand.getType());
                populate();
            }
            return;
        }

        // ── Single Claim ──
        if (slot == SLOT_SINGLE_CLAIM) {
            if (!event.getClick().isShiftClick()) return;
            saveCurrentPage();
            kit.setSingleClaim(!kit.isSingleClaim());
            populate();
            return;
        }

        // ── Lore / Book ──
        if (slot == SLOT_BOOK_QUILL) {
            if (!event.getClick().isShiftClick()) return;
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

        // ── Cooldown ──
        if (slot == SLOT_COOLDOWN_CLK) {
            if (!event.getClick().isShiftClick()) return;
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();
            player.sendMessage(MM.deserialize(
                "<gradient:#CE93D8:#6A1B9A><bold>⏱ Set Cooldown</bold></gradient>\n" +
                "<gray>Formats: <white>30s · 5m · 2h · 1d · 1d12h30m · 3600</white>\nUse <white>0</white> to clear. Type <white>//cancel</white> to abort.</gray>"));
            new ChatInputSession(plugin, player,
                    input -> {
                        if (input.equalsIgnoreCase("//cancel")) { new AnimaKitsEditor(plugin, player, kit, page).open(); return; }
                        try {
                            long secs = Long.parseLong(input.trim());
                            kit.setCooldown(Math.max(0, secs));
                            plugin.getKitManager().saveKits();
                            player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Cooldown set to <white>" + kit.getCooldown() + "s</white>.</gradient>"));
                        } catch (NumberFormatException e) {
                            player.sendMessage(MM.deserialize("<red>✘ Invalid number.</red>"));
                        }
                        new AnimaKitsEditor(plugin, player, kit, page).open();
                    },
                    () -> new AnimaKitsEditor(plugin, player, kit, page).open()
            ).await();
            return;
        }

        // ── Cancel ──
        if (slot == SLOT_RED_BUNDLE) {
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () -> new AnimaKitsMainGUI(plugin, player).open());
            return;
        }

        // ── Save ──
        if (slot == SLOT_GREEN_BUNDLE) {
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            plugin.getKitManager().refreshAllBrowsersSafe();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () -> new AnimaKitsMainGUI(plugin, player).open());
            return;
        }

        // ── Pagination ──
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

    /**
     * Handles Shift+Left, Shift+Right, Shift+Middle on item slots in the editor grid.
     * Reads the raw kit item (not the wrapped hint version) for editing.
     */
    private void handleItemSlotAction(int slot, int editSlotIndex, ClickType click) {
        // Resolve actual kit item from kit data (not the wrapped GUI copy)
        int itemIndex = page * ITEMS_PER_PAGE + editSlotIndex;
        List<ItemStack> allItems = new ArrayList<>(kit.getItems());
        if (itemIndex >= allItems.size()) return;
        ItemStack rawItem = allItems.get(itemIndex);
        if (rawItem == null || rawItem.getType() == Material.AIR) return;

        if (click == ClickType.SHIFT_LEFT) {
            // Rename item via chat
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();
            ChatInputSession.sendRenamePrompt(plugin, player);
            new ChatInputSession(plugin, player,
                    newName -> {
                        saveItemEdit(itemIndex, ItemEditUtil.setName(getKitItem(itemIndex), newName));
                        player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Item renamed!</gradient>"));
                        new AnimaKitsEditor(plugin, player, kit, page).open();
                    },
                    () -> new AnimaKitsEditor(plugin, player, kit, page).open()
            ).await();

        } else if (click == ClickType.SHIFT_RIGHT) {
            // Enchant item via chat
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();
            player.sendMessage(MM.deserialize(
                "<gradient:#FF6060:#CC0000><bold>⚔ Enchant Item</bold></gradient>\n" +
                "<gray>Type: <white>enchantment_name level</white>  (e.g. <white>sharpness 5</white>)\n" +
                "Level 0 removes the enchantment. Type <white>//cancel</white> to abort.</gray>"));

            // Helper message with clickable enchants
            Component helper = MM.deserialize("<gradient:#4FC3F7:#1565C0><bold>[ Click to suggest enchantment ]</bold></gradient> ")
                    .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(MM.deserialize("<gray>Click to see common enchantments</gray>")))
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("sharpness unbreaking efficiency mending silk_touch fortune protection power infinity knockback looting fire_aspect"));
            player.sendMessage(helper);

            new ChatInputSession(plugin, player,
                    input -> {
                        String[] parts = input.trim().split("\\s+");
                        if (parts.length < 1) { new AnimaKitsEditor(plugin, player, kit, page).open(); return; }
                        Enchantment ench = ItemEditUtil.resolveEnchantment(parts[0]);
                        if (ench == null) {
                            player.sendMessage(MM.deserialize("<red>✘ Unknown enchantment: <white>" + parts[0] + "</white></red>"));
                            new AnimaKitsEditor(plugin, player, kit, page).open();
                            return;
                        }
                        int level = parts.length >= 2 ? parseInt(parts[1], 1) : 1;
                        saveItemEdit(itemIndex, ItemEditUtil.enchant(getKitItem(itemIndex), ench, level));
                        player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Enchantment applied!</gradient>"));
                        new AnimaKitsEditor(plugin, player, kit, page).open();
                    },
                    () -> new AnimaKitsEditor(plugin, player, kit, page).open()
            ).await();

        } else if (click == ClickType.MIDDLE) {
            // Cycle unbreakable + glow: 4 states
            // State 0: unbreakable=off, glow=off
            // State 1: unbreakable=on,  glow=on
            // State 2: unbreakable=on,  glow=off
            // State 3: unbreakable=off, glow=on
            ItemStack current = getKitItem(itemIndex);
            ItemMeta meta = current.getItemMeta();
            boolean isUnbreakable = meta != null && meta.isUnbreakable();
            boolean hasGlow = ItemEditUtil.hasGlow(current);

            ItemStack next;
            String stateMsg;
            if (!isUnbreakable && !hasGlow) {
                // → state 1: both on
                next = ItemEditUtil.setUnbreakable(ItemEditUtil.setGlow(current, true), true);
                stateMsg = "<green>Unbreakable: ON</green> <gray>+</gray> <gradient:#FF00FF:#00FFFF>Glow: ON</gradient>";
            } else if (isUnbreakable && hasGlow) {
                // → state 2: unbreakable on, glow off
                next = ItemEditUtil.setGlow(current, false);
                stateMsg = "<green>Unbreakable: ON</green> <gray>+</gray> <red>Glow: OFF</red>";
            } else if (isUnbreakable) {
                // → state 3: both off
                next = ItemEditUtil.setUnbreakable(current, false);
                stateMsg = "<red>Unbreakable: OFF</red> <gray>+</gray> <red>Glow: OFF</red>";
            } else {
                // → state 0 or glow-only edge case: glow on, unbreakable off
                next = ItemEditUtil.setGlow(current, !hasGlow);
                stateMsg = "<red>Unbreakable: OFF</red> <gray>+</gray> <gradient:#FF00FF:#00FFFF>Glow: " + (!hasGlow ? "ON" : "OFF") + "</gradient>";
            }
            saveItemEdit(itemIndex, next);
            player.sendMessage(MM.deserialize("<gradient:#AA88FF:#6600FF><bold>✔ Item state:</bold></gradient> " + stateMsg));
            populate();
        }
    }

    /**
     * Applies the kit's raw display name as a prefix or suffix to every item currently in the kit.
     */
    private void applyKitNameToAllItems(boolean asSuffix) {
        saveCurrentPage();
        List<ItemStack> allItems = new ArrayList<>(kit.getItems());
        for (int i = 0; i < allItems.size(); i++) {
            ItemStack item = allItems.get(i);
            if (item == null || item.getType() == Material.AIR) continue;
            ItemStack edited = asSuffix
                    ? ItemEditUtil.setSuffix(item, kit.getRawName())
                    : ItemEditUtil.setPrefix(item, kit.getRawName());
            allItems.set(i, edited);
        }
        kit.clearItems();
        for (ItemStack is : allItems) kit.addItem(is != null ? is : new ItemStack(Material.AIR));
        plugin.getKitManager().saveKits();
        populate();
    }

    /** Retrieve the real (non-wrapped) kit item at the given absolute index. */
    private ItemStack getKitItem(int absoluteIndex) {
        List<ItemStack> items = kit.getItems();
        if (absoluteIndex >= items.size()) return null;
        return items.get(absoluteIndex);
    }

    /** Write an edited item back into the kit at the given absolute index. */
    private void saveItemEdit(int absoluteIndex, ItemStack edited) {
        List<ItemStack> allItems = new ArrayList<>(kit.getItems());
        while (allItems.size() <= absoluteIndex) allItems.add(null);
        allItems.set(absoluteIndex, edited);
        kit.clearItems();
        for (ItemStack is : allItems) kit.addItem(is != null ? is : new ItemStack(Material.AIR));
        plugin.getKitManager().saveKits();
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        for (int slot : event.getRawSlots()) {
            if (slot >= INV_SIZE) continue;
            boolean valid = false;
            for (int editSlot : EDIT_SLOTS) {
                if (editSlot == slot) { valid = true; break; }
            }
            if (!valid) { event.setCancelled(true); return; }
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

    private void saveCurrentPage() {
        List<ItemStack> allItems = new ArrayList<>(kit.getItems());
        int from = page * ITEMS_PER_PAGE;
        while (allItems.size() < from + ITEMS_PER_PAGE) allItems.add(null);

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            ItemStack guiItem = inventory.getItem(EDIT_SLOTS[i]);
            if (guiItem != null && guiItem.getType() != Material.AIR) {
                // Strip the editor hints from the item before saving
                ItemStack clean = stripEditorHints(guiItem.clone());
                allItems.set(from + i, clean);
            } else {
                allItems.set(from + i, null);
            }
        }

        kit.clearItems();
        for (ItemStack is : allItems) kit.addItem(is != null ? is : new ItemStack(Material.AIR));
    }

    /**
     * Removes the editor-appended action-hint lore lines from an item before persisting it.
     * Detects the separator line "──── Editor Actions ────" and trims everything from it onwards,
     * also removing the empty line immediately before it (the separator spacer).
     */
    private ItemStack stripEditorHints(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return item;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.lore() == null) return item;

        List<Component> lore = new ArrayList<>(meta.lore());
        net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer plain =
                net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText();

        // Find the editor separator line
        int cutAt = -1;
        for (int i = 0; i < lore.size(); i++) {
            String text = plain.serialize(lore.get(i));
            if (text.contains("Editor Actions")) { cutAt = i; break; }
        }

        if (cutAt >= 0) {
            // Also remove empty line above separator
            int trimFrom = (cutAt > 0 && plain.serialize(lore.get(cutAt - 1)).isBlank()) ? cutAt - 1 : cutAt;
            // Remove Infinite Durability / Glow badges above that too (they are dynamic)
            while (trimFrom > 0) {
                String prev = plain.serialize(lore.get(trimFrom - 1));
                if (prev.contains("Infinite Durability") || prev.contains("Glow:") || prev.isBlank()) {
                    trimFrom--;
                } else break;
            }
            lore = lore.subList(0, trimFrom);
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // ── Helpers ──────────────────────────────────────────────────

    private String formatTime(long seconds) {
        if (seconds <= 0) return "None";
        long h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private ItemStack makePane(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.displayName(Component.space()); item.setItemMeta(meta); }
        return item;
    }

    private long parseDurationEditor(String raw) {
        if (raw == null || raw.isBlank()) return -2;
        String t = raw.trim();
        if (t.equals("-1")) return -1;
        try { return Long.parseLong(t); } catch (NumberFormatException ignored) {}
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?",
                        java.util.regex.Pattern.CASE_INSENSITIVE).matcher(t);
        if (!m.matches()) return -2;
        long days  = m.group(1) != null ? Long.parseLong(m.group(1)) : 0;
        long hours = m.group(2) != null ? Long.parseLong(m.group(2)) : 0;
        long mins  = m.group(3) != null ? Long.parseLong(m.group(3)) : 0;
        long secs  = m.group(4) != null ? Long.parseLong(m.group(4)) : 0;
        long total = days * 86400L + hours * 3600L + mins * 60L + secs;
        return total > 0 ? total : -2;
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    private ItemStack addEnchantLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        List<Component> lore = new ArrayList<>(meta.lore() != null ? meta.lore() : List.of());

        // Only add if not already present
        boolean hasEnchantHeader = false;
        for (Component c : lore) {
            if (net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(c).contains("Enchantments")) {
                hasEnchantHeader = true;
                break;
            }
        }

        if (!hasEnchantHeader && !item.getEnchantments().isEmpty()) {
            lore.add(Component.empty());
            lore.add(MM.deserialize("<gray>Enchantments:</gray>"));
            for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                String name = entry.getKey().getKey().getKey().replace('_', ' ');
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                lore.add(MM.deserialize("<aqua> • " + name + " " + entry.getValue() + "</aqua>"));
            }
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
