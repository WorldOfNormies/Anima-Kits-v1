package com.worldofnormies.animakits.rank.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.ChatInputSession;
import com.worldofnormies.animakits.gui.GuiItem;
import com.worldofnormies.animakits.rank.Rank;
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

import java.util.ArrayList;
import java.util.List;

/**
 * AnimaRankEditor – Individual rank editor GUI.
 * Theme: Orange and Yellow.
 */
public class AnimaRankEditor implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final Rank rank;
    private Inventory inventory;
    private boolean closing = false;

    public AnimaRankEditor(AnimaKitsPlugin plugin, Player player, Rank rank) {
        this.plugin = plugin;
        this.player = player;
        this.rank = rank;
    }

    public void open() {
        Component title = MM.deserialize("<gradient:#FFA500:#FFFF00><bold>Edit Rank: </bold></gradient>")
                .append(ColorUtil.parse(rank.getDisplayName().isBlank() ? rank.getId() : rank.getDisplayName()));
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();

        // ── Border Pattern: Orange and Yellow ──
        Material[] borderPattern = new Material[INV_SIZE];
        for (int i = 0; i < INV_SIZE; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                borderPattern[i] = (i % 2 == 0) ? Material.ORANGE_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE;
            }
        }

        for (int i = 0; i < INV_SIZE; i++) {
            if (borderPattern[i] != null) {
                inventory.setItem(i, GuiItem.border(borderPattern[i]));
            }
        }

        // ── Controls ──
        // Slot 10: Rename Rank
        inventory.setItem(10, GuiItem.make(Material.NAME_TAG,
                MM.deserialize("<gradient:#FFA500:#FFFF00><bold>✎ Rename Rank</bold></gradient>"),
                List.of(
                        MM.deserialize("<gray>Current: </gray>").append(ColorUtil.parse(rank.getDisplayName())),
                        Component.empty(),
                        MM.deserialize("<yellow>⬡ Click</yellow><gray> to rename rank.</gray>")
                )));

        // Slot 12: Prefix
        inventory.setItem(12, GuiItem.make(Material.PAPER,
                MM.deserialize("<gradient:#FFA500:#FFFF00><bold>✎ Set Prefix</bold></gradient>"),
                List.of(
                        MM.deserialize("<gray>Current: </gray>").append(ColorUtil.parse(rank.getPrefix())),
                        Component.empty(),
                        MM.deserialize("<yellow>⬡ Click</yellow><gray> to set prefix.</gray>")
                )));

        // Slot 14: Suffix
        inventory.setItem(14, GuiItem.make(Material.PAPER,
                MM.deserialize("<gradient:#FFA500:#FFFF00><bold>✎ Set Suffix</bold></gradient>"),
                List.of(
                        MM.deserialize("<gray>Current: </gray>").append(ColorUtil.parse(rank.getSuffix())),
                        Component.empty(),
                        MM.deserialize("<yellow>⬡ Click</yellow><gray> to set suffix.</gray>")
                )));

        // Slot 16: Icon
        inventory.setItem(16, GuiItem.make(rank.getIconMaterial(),
                MM.deserialize("<gradient:#FFA500:#FFFF00><bold>📦 Change Icon</bold></gradient>"),
                List.of(
                        MM.deserialize("<gray>Current: <white>" + rank.getIconMaterial().name() + "</white></gray>"),
                        Component.empty(),
                        MM.deserialize("<yellow>⬡ Click</yellow><gray> while holding item to update.</gray>")
                )));

        // Slot 28: Player Name Color
        inventory.setItem(28, GuiItem.make(Material.CYAN_DYE,
                MM.deserialize("<gradient:#FFA500:#FFFF00><bold>✎ Set Name Color</bold></gradient>"),
                List.of(
                        MM.deserialize("<gray>Current: </gray>").append(ColorUtil.parse(rank.getColorName() + "Sample")),
                        Component.empty(),
                        MM.deserialize("<yellow>⬡ Click</yellow><gray> to set name color.</gray>")
                )));

        // Slot 30: Chat Color
        inventory.setItem(30, GuiItem.make(Material.LIGHT_BLUE_DYE,
                MM.deserialize("<gradient:#FFA500:#FFFF00><bold>✎ Set Chat Color</bold></gradient>"),
                List.of(
                        MM.deserialize("<gray>Current: </gray>").append(ColorUtil.parse(rank.getChatColor() + "Sample Text")),
                        Component.empty(),
                        MM.deserialize("<yellow>⬡ Click</yellow><gray> to set chat color.</gray>")
                )));

        // Slot 32: Rankable settings
        inventory.setItem(32, GuiItem.make(Material.EMERALD,
                MM.deserialize("<gradient:#FFA500:#FFFF00><bold>▲ Rankable Settings</bold></gradient>"),
                List.of(
                        MM.deserialize("<gray>Enabled: " + (rank.isRankable() ? "<green>Yes</green>" : "<red>No</red>") + "</gray>"),
                        MM.deserialize("<gray>Price: <gold>$" + rank.getRankupPrice() + "</gold></gray>"),
                        MM.deserialize("<gray>Playtime: <aqua>" + plugin.getRankManager().formatPlaytime(rank.getRankupPlaytime()) + "</aqua></gray>"),
                        Component.empty(),
                        MM.deserialize("<yellow>⬡ Left-Click</yellow><gray> to toggle.</gray>"),
                        MM.deserialize("<yellow>⬡ Right-Click</yellow><gray> to set price.</gray>"),
                        MM.deserialize("<yellow>⬡ Middle-Click</yellow><gray> to set playtime.</gray>")
                )));

        // Slot 34: Buyable settings
        inventory.setItem(34, GuiItem.make(Material.GOLD_INGOT,
                MM.deserialize("<gradient:#FFA500:#FFFF00><bold>$ Buyable Settings</bold></gradient>"),
                List.of(
                        MM.deserialize("<gray>Enabled: " + (rank.isBuyable() ? "<green>Yes</green>" : "<red>No</red>") + "</gray>"),
                        MM.deserialize("<gray>Price: <gold>$" + rank.getBuyPrice() + "</gold></gray>"),
                        MM.deserialize("<gray>Duration: <white>" + (rank.getBuyDuration() < 0 ? "Permanent" : plugin.getRankManager().formatPlaytime(rank.getBuyDuration())) + "</white></gray>"),
                        Component.empty(),
                        MM.deserialize("<yellow>⬡ Left-Click</yellow><gray> to toggle.</gray>"),
                        MM.deserialize("<yellow>⬡ Right-Click</yellow><gray> to set price.</gray>"),
                        MM.deserialize("<yellow>⬡ Middle-Click</yellow><gray> to set duration.</gray>")
                )));

        // ── Bottom Bar ──
        inventory.setItem(45, GuiItem.make(Material.RED_BUNDLE,
                MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Cancel</bold></gradient>"),
                List.of(MM.deserialize("<gray>Return without saving.</gray>"))));

        inventory.setItem(53, GuiItem.make(Material.LIME_BUNDLE,
                MM.deserialize("<gradient:#44FF88:#00CC55><bold>✔ Save & Apply</bold></gradient>"),
                List.of(MM.deserialize("<gray>Save changes to configuration.</gray>"))));
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

        if (slot == 53) {
            plugin.getRankManager().saveRanks();
            closing = true;
            HandlerList.unregisterAll(this);
            new AnimaRanksMainGUI(plugin, player, true).open();
            return;
        }

        // Rename Rank
        if (slot == 10) {
            startChatInput("Enter new display name for rank (MiniMessage supported):", input -> {
                rank.setDisplayName(input);
                open();
            });
            return;
        }

        // Set Prefix
        if (slot == 12) {
            startChatInput("Enter new prefix for rank:", input -> {
                rank.setPrefix(input);
                open();
            });
            return;
        }

        // Set Suffix
        if (slot == 14) {
            startChatInput("Enter new suffix for rank:", input -> {
                rank.setSuffix(input);
                open();
            });
            return;
        }

        // Change Icon
        if (slot == 16) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() != Material.AIR) {
                rank.setIconMaterial(hand.getType());
                populate();
            } else {
                player.sendMessage(MM.deserialize("<red>Please hold an item in your main hand to set it as the icon.</red>"));
            }
            return;
        }

        // Set Name Color
        if (slot == 28) {
            startChatInput("Enter new name color (MiniMessage tag or hex):", input -> {
                rank.setColorName(input);
                open();
            });
            return;
        }

        // Set Chat Color
        if (slot == 30) {
            startChatInput("Enter new chat color (MiniMessage tag or hex):", input -> {
                rank.setChatColor(input);
                open();
            });
            return;
        }

        // Rankable Settings
        if (slot == 32) {
            ClickType click = event.getClick();
            if (click.isLeftClick()) {
                rank.setRankable(!rank.isRankable());
                populate();
            } else if (click.isRightClick()) {
                startChatInput("Enter rankup price:", input -> {
                    try { rank.setRankupPrice(Double.parseDouble(input)); }
                    catch (NumberFormatException e) { player.sendMessage(MM.deserialize("<red>Invalid price.</red>")); }
                    open();
                });
            } else if (click == ClickType.MIDDLE) {
                startChatInput("Enter required playtime (e.g. 1d12h30m):", input -> {
                    try { rank.setRankupPlaytime(com.worldofnormies.animakits.rank.commands.AnimaRankCommand.parseDuration(input)); }
                    catch (Exception e) { player.sendMessage(MM.deserialize("<red>Invalid duration.</red>")); }
                    open();
                });
            }
            return;
        }

        // Buyable Settings
        if (slot == 34) {
            ClickType click = event.getClick();
            if (click.isLeftClick()) {
                rank.setBuyable(!rank.isBuyable());
                populate();
            } else if (click.isRightClick()) {
                startChatInput("Enter buy price:", input -> {
                    try { rank.setBuyPrice(Double.parseDouble(input)); }
                    catch (NumberFormatException e) { player.sendMessage(MM.deserialize("<red>Invalid price.</red>")); }
                    open();
                });
            } else if (click == ClickType.MIDDLE) {
                startChatInput("Enter duration (e.g. 30d, or -1 for permanent):", input -> {
                    try { rank.setBuyDuration(com.worldofnormies.animakits.rank.commands.AnimaRankCommand.parseDuration(input)); }
                    catch (Exception e) { player.sendMessage(MM.deserialize("<red>Invalid duration.</red>")); }
                    open();
                });
            }
            return;
        }
    }

    private void startChatInput(String prompt, java.util.function.Consumer<String> callback) {
        closing = true;
        HandlerList.unregisterAll(this);
        player.closeInventory();
        player.sendMessage(MM.deserialize("<gradient:#FFA500:#FFFF00><bold>✎ Rank Editor</bold></gradient>"));
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
