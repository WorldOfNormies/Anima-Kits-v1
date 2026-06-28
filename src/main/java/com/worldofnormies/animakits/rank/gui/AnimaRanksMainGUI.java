package com.worldofnormies.animakits.rank.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.GuiItem;
import com.worldofnormies.animakits.rank.Rank;
import com.worldofnormies.animakits.rank.manager.RankManager;
import com.worldofnormies.animakits.util.ColorUtil;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AnimaRanksMainGUI – Main rank browser.
 * Theme: Purple and Magenta.
 */
public class AnimaRanksMainGUI implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final int[] CONTENT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    private static final int RANKS_PER_PAGE = CONTENT_SLOTS.length;
    private static final int INV_SIZE       = 54;

    private final AnimaKitsPlugin plugin;
    private final Player          player;
    private final boolean         isAdmin;
    private int                   page = 0;
    private int                   filterMode = 0; // 0=all, 1=rankable, 2=non-rankable, 3=buyable, 4=non-buyable
    private Inventory             inventory;

    public AnimaRanksMainGUI(AnimaKitsPlugin plugin, Player player, boolean isAdmin) {
        this.plugin   = plugin;
        this.player   = player;
        this.isAdmin  = isAdmin;
    }

    public void open() {
        build();
        plugin.getRankManager().registerBrowser(player, this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    public void refresh() {
        if (inventory == null) return;
        inventory.clear();
        populate();
    }

    private void build() {
        Component title = MM.deserialize(
            "<gradient:#FF00FF:#800080><bold>⋆༺✦ Anima Ranks ✦༻⋆</bold></gradient>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
    }

    private void populate() {
        inventory.clear();
        UUID uuid = player.getUniqueId();

        // ── Border: Purple and Magenta ──
        Material[] borderPattern = new Material[INV_SIZE];
        for (int i = 0; i < INV_SIZE; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                borderPattern[i] = (i % 2 == 0) ? Material.PURPLE_STAINED_GLASS_PANE : Material.MAGENTA_STAINED_GLASS_PANE;
            }
        }
        for (int i = 0; i < INV_SIZE; i++) {
            if (borderPattern[i] != null) inventory.setItem(i, GuiItem.border(borderPattern[i]));
        }

        List<Rank> allRanks = plugin.getRankManager().getAllRanks();
        List<Rank> filteredRanks;
        switch (filterMode) {
            case 1 -> filteredRanks = allRanks.stream().filter(Rank::isRankable).collect(Collectors.toList());
            case 2 -> filteredRanks = allRanks.stream().filter(r -> !r.isRankable()).collect(Collectors.toList());
            case 3 -> filteredRanks = allRanks.stream().filter(Rank::isBuyable).collect(Collectors.toList());
            case 4 -> filteredRanks = allRanks.stream().filter(r -> !r.isBuyable()).collect(Collectors.toList());
            default -> filteredRanks = allRanks;
        }

        // Split into categories for separators if in "All" mode
        List<Object> combined = new ArrayList<>();
        if (filterMode == 0) {
            List<Rank> rankable = filteredRanks.stream().filter(Rank::isRankable).collect(Collectors.toList());
            List<Rank> nonRankable = filteredRanks.stream().filter(r -> !r.isRankable()).collect(Collectors.toList());
            combined.addAll(rankable);
            if (!rankable.isEmpty() && !nonRankable.isEmpty()) {
                combined.add("separator_rankable");
            }
            combined.addAll(nonRankable);
        } else {
            combined.addAll(filteredRanks);
        }

        int totalPages = Math.max(1, (int) Math.ceil(combined.size() / (double) RANKS_PER_PAGE));
        page = Math.min(page, totalPages - 1);

        int from = page * RANKS_PER_PAGE;
        int to   = Math.min(from + RANKS_PER_PAGE, combined.size());

        for (int i = 0; i < (to - from); i++) {
            Object obj = combined.get(from + i);
            if (obj instanceof Rank rank) {
                Component name = ColorUtil.parse(rank.getDisplayName().isBlank() ? rank.getId() : rank.getDisplayName());

                List<Component> lore = new ArrayList<>();
                lore.add(MM.deserialize("<gradient:#FF00FF:#800080><bold>── Rank Info ──</bold></gradient>"));
                lore.add(Component.empty());
                lore.add(MM.deserialize("<dark_gray>ID:        </dark_gray><yellow>" + rank.getId() + "</yellow>"));
                lore.add(MM.deserialize("<dark_gray>Hierarchy: </dark_gray><white>#" + rank.getHierarchy() + "</white>"));
                if (!rank.getPrefix().isBlank())
                    lore.add(MM.deserialize("<dark_gray>Prefix:    </dark_gray>").append(ColorUtil.parse(rank.getPrefix())));
                if (!rank.getSuffix().isBlank())
                    lore.add(MM.deserialize("<dark_gray>Suffix:    </dark_gray>").append(ColorUtil.parse(rank.getSuffix())));
                lore.add(Component.empty());
                lore.add(MM.deserialize("<dark_gray>Rankable:  </dark_gray>" + (rank.isRankable() ? "<green>Yes</green>" : "<red>No</red>")));
                if (rank.isRankable()) {
                    lore.add(MM.deserialize("<dark_gray>Playtime:  </dark_gray><aqua>" + RankManager.formatPlaytime(rank.getRankupPlaytime()) + "</aqua>"));
                    lore.add(MM.deserialize("<dark_gray>Price:     </dark_gray><gold>$" + rank.getRankupPrice() + "</gold>"));
                }
                lore.add(MM.deserialize("<dark_gray>Buyable:   </dark_gray>" + (rank.isBuyable() ? "<green>Yes ($" + rank.getBuyPrice() + ")</green>" : "<red>No</red>")));
                lore.add(Component.empty());

                if (isAdmin) {
                    lore.add(MM.deserialize("<gradient:#FF00FF:#800080>⬡ <bold>Left-click</bold></gradient><gray> → View Players</gray>"));
                    lore.add(MM.deserialize("<gradient:#FFA500:#FFFF00>✎ <bold>Right-click</bold></gradient><gray> → Edit Rank</gray>"));
                    lore.add(MM.deserialize("<gradient:#FF4444:#CC0000>✘ <bold>Shift+Right</bold></gradient><gray> → Delete Rank</gray>"));
                } else {
                    lore.add(MM.deserialize("<gradient:#FF00FF:#800080>⬡ <bold>Left-click</bold></gradient><gray> → View Details</gray>"));
                    if (rank.isRankable())
                        lore.add(MM.deserialize("<gradient:#A5D6A7:#2E7D32>▲ <bold>Right-click</bold></gradient><gray> → RankUp Info</gray>"));
                }

                inventory.setItem(CONTENT_SLOTS[i], GuiItem.make(rank.getIconMaterial(), name, lore, rank.isRankable()));
            } else if ("separator_rankable".equals(obj)) {
                List<Component> sepLore = List.of(
                    MM.deserialize("<gradient:#FF00FF:#800080>← Rankable</gradient>   <gradient:#FF4444:#CC0000>Non-Rankable →</gradient>")
                );
                inventory.setItem(CONTENT_SLOTS[i], GuiItem.make(Material.PURPLE_STAINED_GLASS_PANE,
                    MM.deserialize("<gradient:#FF00FF:#800080><bold>◄ Rankable</bold></gradient><gray> | </gray><gradient:#FF4444:#CC0000><bold>Non-Rankable ►</bold></gradient>"),
                    sepLore));
            }
        }

        // ── Top Bar ──
        // Slot 4: Filter Toggle
        String filterLabel = switch (filterMode) {
            case 1 -> "<green>Rankable Only</green>";
            case 2 -> "<red>Non-Rankable Only</red>";
            case 3 -> "<gold>Buyable Only</gold>";
            case 4 -> "<yellow>Non-Buyable Only</yellow>";
            default -> "<white>All Ranks</white>";
        };
        inventory.setItem(4, GuiItem.make(Material.ENDER_CHEST,
            MM.deserialize("<gradient:#FF00FF:#800080><bold>✦ Filter Ranks</bold></gradient>"),
            List.of(
                MM.deserialize("<gray>Current filter: " + filterLabel + "</gray>"),
                Component.empty(),
                MM.deserialize("<gradient:#FF00FF:#800080>⬡ <bold>Click</bold></gradient><gray> to cycle filter mode.</gray>"),
                MM.deserialize("<dark_gray>All → Rankable → Non-Rankable → Buyable → Non-Buyable</dark_gray>")
            )));

        if (isAdmin) {
            inventory.setItem(6, GuiItem.make(Material.NETHER_STAR,
                MM.deserialize("<gradient:#A5D6A7:#2E7D32><bold>✦ Create Rank</bold></gradient>"),
                List.of(MM.deserialize("<gray>Right-click to create a new rank.</gray>"))));
        }

        // ── Bottom bar ────────────────────────────────────────────
        inventory.setItem(45, GuiItem.make(Material.BARRIER,
            MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Close</bold></gradient>"),
            List.of(MM.deserialize("<gray>Close the Ranks panel.</gray>"))));

        inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE,
            MM.deserialize("<gradient:#FF6060:#CC0000><bold>« Previous</bold></gradient>"),
            List.of(MM.deserialize("<gray>Page <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        inventory.setItem(49, GuiItem.make(Material.PURPLE_STAINED_GLASS_PANE,
            MM.deserialize("<gradient:#FF00FF:#800080><bold>✦ Rank Panel</bold></gradient>"),
            List.of(MM.deserialize("<gray>Anima Ranks v2</gray>"))));

        inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE,
            MM.deserialize("<gradient:#44FF88:#00AA44><bold>Next »</bold></gradient>"),
            List.of(MM.deserialize("<gray>Page <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        inventory.setItem(53, GuiItem.make(Material.EMERALD,
            MM.deserialize("<gradient:#44FF88:#00CC55><bold>✔ Refresh</bold></gradient>"),
            List.of(MM.deserialize("<gray>Refresh the display.</gray>"))));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(player)) return;

        int slot = event.getRawSlot();
        if (slot < 0) return;
        if (slot < INV_SIZE) event.setCancelled(true);
        else { if (event.getClick().isShiftClick()) event.setCancelled(true); return; }

        ClickType click = event.getClick();

        if (slot == 45) { player.closeInventory(); return; }
        if (slot == 4) { filterMode = (filterMode + 1) % 5; page = 0; populate(); return; }
        if (slot == 6 && click == ClickType.RIGHT && isAdmin) {
            String newId = "rank_" + (plugin.getRankManager().getAllRanks().size() + 1);
            int hierarchy = plugin.getRankManager().getAllRanks().size();
            plugin.getRankManager().createRank(newId, newId, hierarchy);
            refresh();
            player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Created rank <white>" + newId + "</white>.</gradient>"));
            return;
        }
        if (slot == 47 && page > 0) { page--; populate(); return; }
        if (slot == 51) { page++; populate(); return; }
        if (slot == 53) { populate(); return; }

        for (int i = 0; i < CONTENT_SLOTS.length; i++) {
            if (slot != CONTENT_SLOTS[i]) continue;

            // Re-calculate the list because it's dynamic
            List<Rank> allRanks = plugin.getRankManager().getAllRanks();
            List<Rank> filteredRanks;
            switch (filterMode) {
                case 1 -> filteredRanks = allRanks.stream().filter(Rank::isRankable).collect(Collectors.toList());
                case 2 -> filteredRanks = allRanks.stream().filter(r -> !r.isRankable()).collect(Collectors.toList());
                case 3 -> filteredRanks = allRanks.stream().filter(Rank::isBuyable).collect(Collectors.toList());
                case 4 -> filteredRanks = allRanks.stream().filter(r -> !r.isBuyable()).collect(Collectors.toList());
                default -> filteredRanks = allRanks;
            }

            List<Object> combined = new ArrayList<>();
            if (filterMode == 0) {
                List<Rank> rankable = filteredRanks.stream().filter(Rank::isRankable).collect(Collectors.toList());
                List<Rank> nonRankable = filteredRanks.stream().filter(r -> !r.isRankable()).collect(Collectors.toList());
                combined.addAll(rankable);
                if (!rankable.isEmpty() && !nonRankable.isEmpty()) combined.add("separator_rankable");
                combined.addAll(nonRankable);
            } else {
                combined.addAll(filteredRanks);
            }

            int idx = page * RANKS_PER_PAGE + i;
            if (idx >= combined.size()) return;
            Object obj = combined.get(idx);
            if (!(obj instanceof Rank rank)) return;

            if (isAdmin) {
                if (click == ClickType.LEFT) {
                    HandlerList.unregisterAll(this);
                    new AnimaRankPlayerListGUI(plugin, player, rank).open();
                } else if (click == ClickType.RIGHT) {
                    HandlerList.unregisterAll(this);
                    new AnimaRankEditor(plugin, player, rank).open();
                } else if (click == ClickType.SHIFT_RIGHT) {
                    plugin.getRankManager().deleteRank(rank.getId());
                    player.sendMessage(MM.deserialize("<red>Deleted rank <white>" + rank.getId() + "</white>.</red>"));
                    populate();
                }
            } else {
                if (click == ClickType.LEFT) {
                    // Show detail or something
                } else if (click == ClickType.RIGHT && rank.isRankable()) {
                    // Rankup info
                }
            }
            break;
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inventory)) event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getPlayer().equals(player)) return;
        plugin.getRankManager().unregisterBrowser(player);
        HandlerList.unregisterAll(this);
    }

    private ItemStack makePane(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.displayName(Component.space()); item.setItemMeta(meta); }
        return item;
    }

    private ItemStack makeItem(Material mat, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
