package com.worldofnormies.animaranks.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.ChatInputSession;
import com.worldofnormies.animakits.gui.GuiItem;
import com.worldofnormies.animakits.util.TimeUtil;
import com.worldofnormies.animaranks.Rank;
import com.worldofnormies.animakits.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AnimaRankPlayerListGUI – Lists players assigned to a specific rank.
 * Theme: Purple and Magenta.
 */
public class AnimaRankPlayerListGUI implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;
    private static final int[] PLAYER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final Rank rank;
    private int page = 0;
    private Inventory inventory;
    private boolean closing = false;

    public AnimaRankPlayerListGUI(AnimaKitsPlugin plugin, Player player, Rank rank) {
        this.plugin = plugin;
        this.player = player;
        this.rank = rank;
    }

    public void open() {
        Component title = MM.deserialize("<gradient:#FF00FF:#800080><bold>Players: </bold></gradient>")
                .append(ColorUtil.parse(rank.getDisplayName().isBlank() ? rank.getId() : rank.getDisplayName()));
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();

        // ── Border Pattern: Purple and Magenta ──
        Material[] borderPattern = new Material[INV_SIZE];
        for (int i = 0; i < INV_SIZE; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                borderPattern[i] = (i % 2 == 0) ? Material.PURPLE_STAINED_GLASS_PANE : Material.MAGENTA_STAINED_GLASS_PANE;
            }
        }
        for (int i = 0; i < INV_SIZE; i++) {
            if (borderPattern[i] != null) inventory.setItem(i, GuiItem.border(borderPattern[i]));
        }

        // Get players with this rank
        List<UUID> playerUuids = plugin.getRankManager().getPlayersWithRank(rank.getId());
        int totalPages = Math.max(1, (int) Math.ceil(playerUuids.size() / (double) PLAYER_SLOTS.length));
        page = Math.min(page, totalPages - 1);

        int from = page * PLAYER_SLOTS.length;
        int to = Math.min(from + PLAYER_SLOTS.length, playerUuids.size());

        for (int i = 0; i < (to - from); i++) {
            UUID uuid = playerUuids.get(from + i);
            OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
            String name = target.getName() != null ? target.getName() : uuid.toString();

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.displayName(MM.deserialize("<gradient:#FF00FF:#800080><bold>" + name + "</bold></gradient>"));
                List<Component> lore = new ArrayList<>();
                lore.add(MM.deserialize("<gray>UUID: " + uuid + "</gray>"));
                lore.add(MM.deserialize("<gray>Playtime: <aqua>" + TimeUtil.formatDuration(plugin.getRankManager().getPlaytime(uuid)) + "</aqua></gray>"));
                lore.add(Component.empty());
                lore.add(MM.deserialize("<red>⬡ Left-Click</red><gray> to remove rank.</gray>"));
                lore.add(MM.deserialize("<yellow>⬡ Right-Click</yellow><gray> to promote to specific rank.</gray>"));
                lore.add(MM.deserialize("<green>⬡ Middle-Click</green><gray> to promote to next hierarchy.</gray>"));
                meta.lore(lore);
                head.setItemMeta(meta);
            }
            inventory.setItem(PLAYER_SLOTS[i], head);
        }

        // ── Controls ──
        inventory.setItem(45, GuiItem.make(Material.BARRIER, MM.deserialize("<red><bold>Back</bold></red>")));
        if (page > 0)
            inventory.setItem(47, GuiItem.make(Material.ARROW, MM.deserialize("<yellow>Previous Page</yellow>")));
        if (page < totalPages - 1)
            inventory.setItem(51, GuiItem.make(Material.ARROW, MM.deserialize("<yellow>Next Page</yellow>")));
        inventory.setItem(53, GuiItem.make(Material.EMERALD, MM.deserialize("<green>Refresh</green>")));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

        int slot = event.getRawSlot();
        if (slot < 0) return;
        event.setCancelled(true);

        if (slot == 45) {
            closing = true;
            HandlerList.unregisterAll(this);
            new AnimaRanksMainGUI(plugin, player, true).open();
            return;
        }
        if (slot == 47 && page > 0) { page--; populate(); return; }
        if (slot == 51) { page++; populate(); return; }
        if (slot == 53) { populate(); return; }

        for (int i = 0; i < PLAYER_SLOTS.length; i++) {
            if (slot == PLAYER_SLOTS[i]) {
                List<UUID> playerUuids = plugin.getRankManager().getPlayersWithRank(rank.getId());
                int idx = page * PLAYER_SLOTS.length + i;
                if (idx >= playerUuids.size()) return;
                UUID targetUuid = playerUuids.get(idx);
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
                String name = target.getName() != null ? target.getName() : targetUuid.toString();

                ClickType click = event.getClick();
                if (click.isLeftClick()) {
                    plugin.getRankManager().removePlayerRank(targetUuid);
                    player.sendMessage(MM.deserialize("<green>Removed rank from <white>" + name + "</white>.</green>"));
                    populate();
                } else if (click.isRightClick()) {
                    startChatInput("Enter rank ID to promote <white>" + name + "</white> to:", input -> {
                        if (plugin.getRankManager().rankExists(input)) {
                            plugin.getRankManager().setPlayerRank(targetUuid, input, -1);
                            player.sendMessage(MM.deserialize("<green>Promoted <white>" + name + "</white> to <yellow>" + input + "</yellow>.</green>"));
                        } else {
                            player.sendMessage(MM.deserialize("<red>Rank <white>" + input + "</white> does not exist.</red>"));
                        }
                        open();
                    });
                } else if (click == ClickType.MIDDLE) {
                    Rank next = plugin.getRankManager().getNextRankUp(targetUuid);
                    if (next != null) {
                        plugin.getRankManager().setPlayerRank(targetUuid, next.getId(), -1);
                        player.sendMessage(MM.deserialize("<green>Promoted <white>" + name + "</white> to next rank: <yellow>" + next.getId() + "</yellow>.</green>"));
                        populate();
                    } else {
                        player.sendMessage(MM.deserialize("<red><white>" + name + "</white> is already at the highest rankable rank or has no next rank.</red>"));
                    }
                }
                return;
            }
        }
    }

    private void startChatInput(String prompt, java.util.function.Consumer<String> callback) {
        closing = true;
        HandlerList.unregisterAll(this);
        player.closeInventory();
        player.sendMessage(MM.deserialize("<gradient:#FF00FF:#800080><bold>✎ Player Management</bold></gradient>"));
        player.sendMessage(MM.deserialize("<gray>" + prompt + "</gray>"));
        player.sendMessage(MM.deserialize("<dark_gray>Type <white>//cancel</white> to abort.</dark_gray>"));
        new ChatInputSession(plugin, player, input -> {
            if (input.equalsIgnoreCase("//cancel")) {
                open();
            } else {
                callback.accept(input);
            }
        }, this::open).await();
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inventory)) event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (closing) return;
        HandlerList.unregisterAll(this);
    }
}
