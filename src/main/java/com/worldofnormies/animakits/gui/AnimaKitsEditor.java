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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * AnimaKitsEditor – Multi-page kit editor matching Edit Kit GUI_2.png layout.
 */
public class AnimaKitsEditor implements Listener {

    private static final MiniMessage MM       = MiniMessage.miniMessage();
    private static final int         INV_SIZE = 54;

    // Center editable item bounding slots based on Edit Kit GUI_2.png side framing
    private static final int[] EDIT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    private static final int ITEMS_PER_PAGE = EDIT_SLOTS.length; // 28

    // Interactive Action Slot Configurations
    private static final int SLOT_PLAYER_HEAD  = 0;
    private static final int SLOT_NAME_TAG     = 2;
    private static final int SLOT_ICON_CHEST   = 4;
    private static final int SLOT_BOOK_QUILL   = 6;
    private static final int SLOT_SINGLE_CLAIM = 8;

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

        // ── 1. Frame / Alternating Border Glass Pattern ──
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

        // ── 2. Control Bar Interactive Configurations (Row 1) ──
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

        inventory.setItem(SLOT_NAME_TAG, GuiItem.make(
                Material.NAME_TAG,
                MM.deserialize("<gradient:#FFD700:#FFA500><bold>✎ Rename Kit</bold></gradient>"),
                List.of(
                    MM.deserialize("<gray>Current name: </gray>").append(com.worldofnormies.animakits.util.ColorUtil.parse(kit.getRawName())),
                    Component.empty(),
                    MM.deserialize("<gradient:#FFD700:#FFA500>⬡ Click</gradient><gray> to modify the text layout.</gray>"),
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

        // ── 3. Load Active Content Matrix Items ──
        List<ItemStack> allItems = kit.getItems();
        int from = page * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int idx = from + i;
            if (idx < allItems.size() && allItems.get(idx) != null
                    && allItems.get(idx).getType() != org.bukkit.Material.AIR) {
                inventory.setItem(EDIT_SLOTS[i], allItems.get(idx).clone());
            }
        }

        // ── 4. Bottom Controls Layer Setup (Row 6) ──
        inventory.setItem(SLOT_RED_BUNDLE, GuiItem.make(Material.RED_BUNDLE,
                MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Cancel</bold></gradient>"),
                List.of(MM.deserialize("<gray>Return to /anima kits without saving.</gray>"))));

        inventory.setItem(SLOT_PREV_PAGE, GuiItem.make(Material.RED_STAINED_GLASS_PANE,
                MM.deserialize("<gradient:#FF6060:#CC0000><bold>« Previous Page</bold></gradient>"),
                List.of(MM.deserialize("<gray>Page <white>" + (page + 1) + "/" + Math.max(1,
                        (int) Math.ceil(allItems.size() / (double) ITEMS_PER_PAGE)) + "</white></gray>"))));

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

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

        int slot = event.getRawSlot();
        if (slot < 0) return;
        if (slot >= INV_SIZE) return;

        for (int editSlot : EDIT_SLOTS) {
            if (editSlot == slot) return;
        }

        event.setCancelled(true);

        if (slot == SLOT_PLAYER_HEAD) {
            saveCurrentPage();
            ClickType click = event.getClick();

            if (click.isLeftClick()) {
                closing = true;
                HandlerList.unregisterAll(this);
                player.closeInventory();
                player.sendMessage(MM.deserialize(
                    "<gradient:#4FC3F7:#1565C0><bold>👤 Assign Kit Permission</bold></gradient>\n" +
                    "<gray>Type a Specific player name or targets (<white>@a</white>, <white>@e</white>, etc.) you want to allow to claim this kit.\n" +
                    "Type <white>//cancel</white> to abort.</gray>"));
                new ChatInputSession(plugin, player,
                        input -> {
                            if (input.equalsIgnoreCase("//cancel")) {
                                new AnimaKitsEditor(plugin, player, kit, page).open();
                                return;
                            }
                            String targetName = input.trim();
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + targetName + " permission set anima.kits.claim." + kit.getPlainName());
                            player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Assigned claim node permission to <white>" + targetName + "</white>!</gradient>"));
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
                    "<gray>Type the claim window duration.\n" +
                    "Formats: <white>30s · 5m · 2h · 1d · 1d12h30m · H:M:S · 3600</white>\n" +
                    "Use <white>0</white> to clear. Type <white>//cancel</white> to abort.</gray>"));
                new ChatInputSession(plugin, player,
                        input -> {
                            if (input.equalsIgnoreCase("//cancel")) {
                                new AnimaKitsEditor(plugin, player, kit, page).open();
                                return;
                            }
                            try {
                                long secs = 0;
                                if (input.contains(":")) {
                                    String[] parts = input.split(":");
                                    if (parts.length == 3) {
                                        secs = Long.parseLong(parts[0]) * 3600L + Long.parseLong(parts[1]) * 60L + Long.parseLong(parts[2]);
                                    } else if (parts.length == 2) {
                                        secs = Long.parseLong(parts[0]) * 60L + Long.parseLong(parts[1]);
                                    }
                                } else {
                                    secs = Long.parseLong(input.trim());
                                }
                                kit.setClaimDuration(Math.max(0, secs));
                                plugin.getKitManager().saveKits();
                                player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Claim window duration set to <white>" + formatTime(kit.getClaimDuration()) + "</white>.</gradient>"));
                            } catch (NumberFormatException e) {
                                player.sendMessage(MM.deserialize("<red>✘ Invalid duration layout configuration.</red>"));
                            }
                            new AnimaKitsEditor(plugin, player, kit, page).open();
                        },
                        () -> new AnimaKitsEditor(plugin, player, kit, page).open()
                ).await();
            }
            return;
        }

        if (slot == SLOT_ICON_CHEST) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() != Material.AIR) {
                saveCurrentPage();
                kit.setIconMaterial(hand.getType());
                populate();
            }
            return;
        }

        if (slot == SLOT_SINGLE_CLAIM) {
            saveCurrentPage();
            kit.setSingleClaim(!kit.isSingleClaim());
            populate();
            return;
        }

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

        if (slot == SLOT_COOLDOWN_CLK) {
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();
            player.sendMessage(MM.deserialize(
                "<gradient:#CE93D8:#6A1B9A><bold>⏱ Set Cooldown</bold></gradient>\n" +
                "<gray>Type the cooldown duration.\n" +
                "Formats: <white>30s · 5m · 2h · 1d · 1d12h30m · 3600</white>\n" +
                "Use <white>0</white> to clear. Type <white>//cancel</white> to abort.</gray>"));
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
                                "<gradient:#44FF88:#00CC55>✔ Cooldown window set to <white>" + kit.getCooldown() + "s</white>.</gradient>"));
                        } catch (NumberFormatException e) {
                            player.sendMessage(MM.deserialize("<red>✘ Invalid numerical configuration. Action terminated.</red>"));
                        }
                        new AnimaKitsEditor(plugin, player, kit, page).open();
                    },
                    () -> new AnimaKitsEditor(plugin, player, kit, page).open()
            ).await();
            return;
        }

        if (slot == SLOT_BOOK_QUILL) {
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
        for (int slot : event.getRawSlots()) {
            if (slot >= INV_SIZE) continue;

            boolean valid = false;
            for (int editSlot : EDIT_SLOTS) {
                if (editSlot == slot) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
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

    private void saveCurrentPage() {
        List<ItemStack> allItems = new ArrayList<>(kit.getItems());
        int from = page * ITEMS_PER_PAGE;

        while (allItems.size() < from + ITEMS_PER_PAGE) allItems.add(null);

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            ItemStack item = inventory.getItem(EDIT_SLOTS[i]);
            allItems.set(from + i,
                    (item != null && item.getType() != Material.AIR) ? item.clone() : null);
        }

        kit.clearItems();
        for (ItemStack is : allItems) {
            kit.addItem(is != null ? is : new ItemStack(Material.AIR));
        }
    }

    private String formatTime(long seconds) {
        if (seconds <= 0) return "None";
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private ItemStack makePane(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.displayName(Component.space()); item.setItemMeta(meta); }
        return item;
    }
    /** Parses duration string: plain seconds, -1 = permanent, compound 1d2h30m45s. Returns -2 on error. */
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

    private String formatDurationEditor(long seconds) {
        if (seconds <= 0)  return "None";
        if (seconds == -1) return "Permanent";
        long d = seconds / 86400, h = (seconds % 86400) / 3600,
             m = (seconds % 3600) / 60, s = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("d ");
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (s > 0) sb.append(s).append("s");
        return sb.toString().trim();
    }

}
