package com.worldofnormies.animakits.rank.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.rank.Rank;
import com.worldofnormies.animakits.rank.manager.RankManager;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AnimaRanksMainGUI – Admin rank browser.
 * Gold/amber theme to distinguish from the kits GUI (blue/purple).
 *
 * Left-click  → view rank detail
 * Right-click → open rank editor (coming: AnimaRankEditorGUI)
 * Shift+Left  → promote/demote a player
 * Shift+Right → delete rank (with confirm)
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
            "<gradient:#FFD700:#FF8C00:#FF4500><bold>⋆༺✦ Anima Ranks ✦༻⋆</bold></gradient>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
    }

    private void populate() {
        inventory.clear();
        List<Rank> allRanks = plugin.getRankManager().getAllRanks();
        int totalPages = Math.max(1, (int) Math.ceil(allRanks.size() / (double) RANKS_PER_PAGE));
        page = Math.min(page, totalPages - 1);

        // ── Border: gold/amber alternating glass ──────────────────
        Material[] borderPattern = {
            Material.GOLD_BLOCK,              Material.YELLOW_STAINED_GLASS_PANE, Material.AIR,
            Material.YELLOW_STAINED_GLASS_PANE, Material.AIR,                    Material.YELLOW_STAINED_GLASS_PANE,
            Material.AIR,                       Material.YELLOW_STAINED_GLASS_PANE, Material.GOLD_BLOCK,

            Material.YELLOW_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
            Material.AIR, Material.AIR, Material.AIR,          Material.YELLOW_STAINED_GLASS_PANE,

            Material.GOLD_BLOCK, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
            Material.AIR, Material.AIR, Material.AIR,          Material.GOLD_BLOCK,

            Material.YELLOW_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
            Material.AIR, Material.AIR, Material.AIR,          Material.YELLOW_STAINED_GLASS_PANE,

            Material.GOLD_BLOCK, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
            Material.AIR, Material.AIR, Material.AIR,          Material.GOLD_BLOCK,

            Material.AIR,                       Material.YELLOW_STAINED_GLASS_PANE, Material.AIR,
            Material.YELLOW_STAINED_GLASS_PANE, Material.GOLD_BLOCK,               Material.YELLOW_STAINED_GLASS_PANE,
            Material.AIR,                       Material.YELLOW_STAINED_GLASS_PANE, Material.AIR
        };
        for (int i = 0; i < INV_SIZE; i++) {
            if (borderPattern[i] != Material.AIR) inventory.setItem(i, makePane(borderPattern[i]));
        }

        // ── Rank icons ────────────────────────────────────────────
        int from = page * RANKS_PER_PAGE;
        int to   = Math.min(from + RANKS_PER_PAGE, allRanks.size());

        for (int i = 0; i < (to - from); i++) {
            Rank rank = allRanks.get(from + i);
            Component name = MM.deserialize(
                rank.getDisplayName().isBlank() ? rank.getId() : rank.getDisplayName());

            List<Component> lore = new ArrayList<>();
            lore.add(MM.deserialize("<gradient:#FFD700:#FF8C00><bold>── Rank Info ──</bold></gradient>"));
            lore.add(Component.empty());
            lore.add(MM.deserialize("<dark_gray>ID:        </dark_gray><yellow>" + rank.getId() + "</yellow>"));
            lore.add(MM.deserialize("<dark_gray>Hierarchy: </dark_gray><white>#" + rank.getHierarchy() + "</white>"));
            if (!rank.getPrefix().isBlank())
                lore.add(MM.deserialize("<dark_gray>Prefix:    </dark_gray>" + rank.getPrefix() + "<reset>"));
            if (!rank.getSuffix().isBlank())
                lore.add(MM.deserialize("<dark_gray>Suffix:    </dark_gray>" + rank.getSuffix() + "<reset>"));
            if (!rank.getChatColor().isBlank())
                lore.add(MM.deserialize("<dark_gray>Chat:      </dark_gray>" + rank.getChatColor() + "Sample Text</reset>"));
            lore.add(Component.empty());
            lore.add(MM.deserialize("<dark_gray>Rankable:  </dark_gray>" + (rank.isRankable() ? "<green>Yes</green>" : "<red>No</red>")));
            if (rank.isRankable()) {
                lore.add(MM.deserialize("<dark_gray>Playtime:  </dark_gray><aqua>" + RankManager.formatPlaytime(rank.getRankupPlaytime()) + "</aqua>"));
                lore.add(MM.deserialize("<dark_gray>Price:     </dark_gray><gold>$" + rank.getRankupPrice() + "</gold>"));
            }
            lore.add(MM.deserialize("<dark_gray>Buyable:   </dark_gray>" + (rank.isBuyable() ? "<green>Yes ($" + rank.getBuyPrice() + ")</green>" : "<red>No</red>")));
            lore.add(MM.deserialize("<dark_gray>Perms:     </dark_gray><white>" + rank.getPermissions().size() + " nodes</white>"));
            lore.add(Component.empty());

            if (isAdmin) {
                lore.add(MM.deserialize("<gradient:#FFD700:#FFA500>⬡ <bold>Left-click</bold></gradient><gray> → View Details</gray>"));
                lore.add(MM.deserialize("<gradient:#4FC3F7:#1565C0>✎ <bold>Right-click</bold></gradient><gray> → Edit Rank</gray>"));
                lore.add(MM.deserialize("<gradient:#EF9A9A:#B71C1C>✘ <bold>Shift+Right</bold></gradient><gray> → Delete Rank</gray>"));
            } else {
                lore.add(MM.deserialize("<gradient:#FFD700:#FFA500>⬡ <bold>Left-click</bold></gradient><gray> → View Details</gray>"));
                if (rank.isRankable())
                    lore.add(MM.deserialize("<gradient:#A5D6A7:#2E7D32>▲ <bold>Right-click</bold></gradient><gray> → RankUp Info</gray>"));
                if (rank.isBuyable())
                    lore.add(MM.deserialize("<gradient:#FFD700:#FF8C00>$ <bold>Shift+Left</bold></gradient><gray> → Buy Rank</gray>"));
            }

            // Choose icon based on hierarchy
            Material icon = rankIconMaterial(rank.getHierarchy());
            inventory.setItem(CONTENT_SLOTS[i], makeItem(icon, name, lore));
        }

        // ── Top bar ───────────────────────────────────────────────
        // Slot 2: List all ranks in chat
        List<Component> listLore = new ArrayList<>();
        listLore.add(MM.deserialize("<gold>⬡ Left-click</gold><gray> → List all ranks in chat</gray>"));
        inventory.setItem(2, makeItem(Material.WRITABLE_BOOK,
            MM.deserialize("<gradient:#FFD700:#FF8C00><bold>📋 Ranks Registry</bold></gradient>"), listLore));

        // Slot 4: Stats / create
        List<Component> statsLore = new ArrayList<>();
        statsLore.add(MM.deserialize("<gradient:#FFD700:#FF8C00><bold>✦ Rank System Overview</bold></gradient>"));
        statsLore.add(Component.empty());
        statsLore.add(MM.deserialize("<dark_gray>Total Ranks: <white>" + allRanks.size() + "</white>"));
        statsLore.add(MM.deserialize("<dark_gray>Page: <white>" + (page + 1) + "/" + totalPages + "</white>"));
        if (isAdmin) {
            statsLore.add(Component.empty());
            statsLore.add(MM.deserialize("<gradient:#A5D6A7:#2E7D32>✦ <bold>Right-click</bold></gradient><gray> → Create new rank</gray>"));
        }
        inventory.setItem(4, makeItem(Material.NETHER_STAR,
            MM.deserialize("<gradient:#FFD700:#FF4500><bold>✦ Rank Statistics</bold></gradient>"), statsLore));

        // ── Bottom bar ────────────────────────────────────────────
        inventory.setItem(45, makeItem(Material.BARRIER,
            MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Close</bold></gradient>"),
            List.of(MM.deserialize("<gray>Close the Ranks panel.</gray>"))));

        inventory.setItem(47, makeItem(Material.RED_STAINED_GLASS_PANE,
            MM.deserialize("<gradient:#FF6060:#CC0000><bold>« Previous</bold></gradient>"),
            List.of(MM.deserialize("<gray>Page <white>" + page + "/" + totalPages + "</white></gray>"))));

        inventory.setItem(49, makeItem(Material.GOLD_INGOT,
            MM.deserialize("<gradient:#FFD700:#FF8C00><bold>✦ Rank Panel</bold></gradient>"),
            List.of(MM.deserialize("<gray>Anima Ranks v1</gray>"))));

        inventory.setItem(51, makeItem(Material.LIME_STAINED_GLASS_PANE,
            MM.deserialize("<gradient:#44FF88:#00AA44><bold>Next »</bold></gradient>"),
            List.of(MM.deserialize("<gray>Page <white>" + (page + 2) + "/" + totalPages + "</white></gray>"))));

        inventory.setItem(53, makeItem(Material.EMERALD,
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
        if (slot == 47) { if (page > 0) { page--; refresh(); } return; }
        if (slot == 51) {
            List<Rank> all = plugin.getRankManager().getAllRanks();
            if ((page + 1) * RANKS_PER_PAGE < all.size()) { page++; refresh(); }
            return;
        }
        if (slot == 53) { refresh(); return; }

        // Slot 2: list in chat
        if (slot == 2 && click == ClickType.LEFT) {
            player.closeInventory();
            List<Rank> all = plugin.getRankManager().getAllRanks();
            player.sendMessage(MM.deserialize("<gradient:#FFD700:#FF8C00><bold>⋆ Anima Ranks – All Ranks ⋆</bold></gradient>"));
            for (Rank r : all) {
                long remaining = -1;
                UUID uuid = player.getUniqueId();
                long expiry = plugin.getRankManager().getRankExpiry(uuid);
                if (expiry > 0) remaining = expiry - Instant.now().getEpochSecond();
                String exp = remaining < 0 ? "Permanent" : RankManager.formatPlaytime(remaining);
                player.sendMessage(MM.deserialize(
                    "<gray>#" + r.getHierarchy() + " </gray>"
                    + (r.getDisplayName().isBlank() ? r.getId() : r.getDisplayName())
                    + " <dark_gray>| Rankable: <white>" + r.isRankable()
                    + "</white> | Buyable: <white>" + r.isBuyable() + "</white></dark_gray>"));
            }
            return;
        }

        // Slot 4: create new rank (admin only)
        if (slot == 4 && click == ClickType.RIGHT && isAdmin) {
            String newId = "rank_" + (plugin.getRankManager().getAllRanks().size() + 1);
            int hierarchy = plugin.getRankManager().getAllRanks().size();
            plugin.getRankManager().createRank(newId, newId, hierarchy);
            refresh();
            player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Created rank <white>" + newId + "</white>. Use /anima rank to configure it.</gradient>"));
            return;
        }

        // Content slot clicks
        for (int i = 0; i < CONTENT_SLOTS.length; i++) {
            if (slot != CONTENT_SLOTS[i]) continue;
            List<Rank> all = plugin.getRankManager().getAllRanks();
            int idx = page * RANKS_PER_PAGE + i;
            if (idx >= all.size()) return;
            Rank rank = all.get(idx);

            if (click == ClickType.LEFT) {
                // Show rank details in chat
                player.closeInventory();
                sendRankDetail(rank);
            } else if (click == ClickType.RIGHT && isAdmin) {
                player.sendMessage(MM.deserialize("<gray>Use <white>/anima rank set ...</white> to edit <yellow>" + rank.getId() + "</yellow> via commands.</gray>"));
            } else if (click == ClickType.SHIFT_RIGHT && isAdmin) {
                // Delete
                plugin.getRankManager().deleteRank(rank.getId());
                player.sendMessage(MM.deserialize("<gradient:#FF4444:#CC0000>✘ Rank <white>" + rank.getId() + "</white> deleted.</gradient>"));
                refresh();
            } else if (click == ClickType.RIGHT && !isAdmin && rank.isRankable()) {
                player.closeInventory();
                sendRankupInfo(rank);
            } else if (click == ClickType.SHIFT_LEFT && !isAdmin && rank.isBuyable()) {
                player.closeInventory();
                player.sendMessage(MM.deserialize("<gold>Use <white>/anima rank rankup</white> or <white>/anima rank buy " + rank.getId() + "</white> to purchase.</gold>"));
            }
            break;
        }
    }

    private void sendRankDetail(Rank rank) {
        player.sendMessage(MM.deserialize("<gradient:#FFD700:#FF8C00><bold>⋆ Rank: " + (rank.getDisplayName().isBlank() ? rank.getId() : rank.getDisplayName()) + " ⋆</bold></gradient>"));
        player.sendMessage(MM.deserialize("<dark_gray>ID:        </dark_gray><yellow>" + rank.getId() + "</yellow>"));
        player.sendMessage(MM.deserialize("<dark_gray>Hierarchy: </dark_gray><white>#" + rank.getHierarchy() + "</white>"));
        if (!rank.getPrefix().isBlank())   player.sendMessage(MM.deserialize("<dark_gray>Prefix:    </dark_gray>" + rank.getPrefix() + "<reset>"));
        if (!rank.getSuffix().isBlank())   player.sendMessage(MM.deserialize("<dark_gray>Suffix:    </dark_gray>" + rank.getSuffix() + "<reset>"));
        if (!rank.getChatColor().isBlank()) player.sendMessage(MM.deserialize("<dark_gray>Chat Color: </dark_gray>" + rank.getChatColor() + "Preview</reset>"));
        if (!rank.getColorName().isBlank()) player.sendMessage(MM.deserialize("<dark_gray>Name Color: </dark_gray>" + rank.getColorName() + "Preview</reset>"));
        player.sendMessage(MM.deserialize("<dark_gray>Rankable:  </dark_gray>" + (rank.isRankable() ? "<green>Yes</green>" : "<red>No</red>")));
        if (rank.isRankable()) {
            player.sendMessage(MM.deserialize("<dark_gray>  Playtime required: </dark_gray><aqua>" + RankManager.formatPlaytime(rank.getRankupPlaytime()) + "</aqua>"));
            player.sendMessage(MM.deserialize("<dark_gray>  Price:             </dark_gray><gold>$" + rank.getRankupPrice() + "</gold>"));
        }
        player.sendMessage(MM.deserialize("<dark_gray>Buyable:   </dark_gray>" + (rank.isBuyable() ? "<green>Yes</green>" : "<red>No</red>")));
        if (rank.isBuyable()) {
            player.sendMessage(MM.deserialize("<dark_gray>  Price:    </dark_gray><gold>$" + rank.getBuyPrice() + "</gold>"));
            player.sendMessage(MM.deserialize("<dark_gray>  Duration: </dark_gray><white>" + (rank.getBuyDuration() < 0 ? "Permanent" : RankManager.formatPlaytime(rank.getBuyDuration())) + "</white>"));
        }
        player.sendMessage(MM.deserialize("<dark_gray>Permissions: </dark_gray><white>" + rank.getPermissions().size() + " nodes</white>"));
        rank.getPermissions().forEach((node, val) ->
            player.sendMessage(MM.deserialize("  <dark_gray>┃ </dark_gray><yellow>" + node + "</yellow> <dark_gray>→</dark_gray> " + (val ? "<green>true</green>" : "<red>false</red>"))));
    }

    private void sendRankupInfo(Rank rank) {
        UUID uuid = player.getUniqueId();
        long pt = plugin.getRankManager().getPlaytime(uuid);
        boolean meetsPlaytime = pt >= rank.getRankupPlaytime();
        player.sendMessage(MM.deserialize("<gradient:#FFD700:#FF8C00><bold>RankUp Info: " + rank.getId() + "</bold></gradient>"));
        player.sendMessage(MM.deserialize("<dark_gray>Your Playtime:    </dark_gray><aqua>" + RankManager.formatPlaytime(pt) + "</aqua>"));
        player.sendMessage(MM.deserialize("<dark_gray>Required Playtime: </dark_gray><aqua>" + RankManager.formatPlaytime(rank.getRankupPlaytime()) + "</aqua> " + (meetsPlaytime ? "<green>✔</green>" : "<red>✘</red>")));
        player.sendMessage(MM.deserialize("<dark_gray>Price: </dark_gray><gold>$" + rank.getRankupPrice() + "</gold>"));
        if (meetsPlaytime)
            player.sendMessage(MM.deserialize("<green>Use <white>/anima rank rankup</white> to rank up!</green>"));
        else
            player.sendMessage(MM.deserialize("<red>You don't meet the playtime requirement yet.</red>"));
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

    // ── Helpers ───────────────────────────────────────────────────

    private Material rankIconMaterial(int hierarchy) {
        return switch (hierarchy) {
            case 0  -> Material.NETHER_STAR;
            case 1  -> Material.DIAMOND;
            case 2  -> Material.EMERALD;
            case 3  -> Material.GOLD_INGOT;
            case 4  -> Material.IRON_INGOT;
            case 5  -> Material.COPPER_INGOT;
            default -> Material.STONE;
        };
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
