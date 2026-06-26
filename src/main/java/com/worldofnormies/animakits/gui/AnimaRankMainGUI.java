package com.worldofnormies.animakits.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animaranks.Rank;
import com.worldofnormies.animaranks.RankManager;
import com.worldofnormies.animakits.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AnimaRankMainGUI implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;
    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final RankManager rankManager;
    private Inventory inventory;

    public AnimaRankMainGUI(AnimaKitsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.rankManager = plugin.getRankManager();
    }

    public void open() {
        Component title = MM.deserialize("<gradient:#FF3030:#FFFFFF:#3060FF><bold>⋆༺⸸ [ Ranks ] ⸸༻⋆</bold></gradient>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();
        // Standard border
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) { meta.displayName(Component.empty()); pane.setItemMeta(meta); }
        for (int i = 0; i < 9; i++) inventory.setItem(i, pane);
        for (int i = 45; i < 54; i++) inventory.setItem(i, pane);

        List<Rank> allRanks = new ArrayList<>(rankManager.getAllRanks());
        int slot = 9;
        for (Rank rank : allRanks) {
            if (slot >= 45) break;
            inventory.setItem(slot++, createRankItem(rank));
        }
    }

    private ItemStack createRankItem(Rank rank) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ColorUtil.parse(rank.getPrefix() + rank.getName()));
        List<Component> lore = new ArrayList<>();
        lore.add(MM.deserialize("<gray>Priority: <white>" + rank.getPriority() + "</white></gray>"));
        lore.add(MM.deserialize("<gray>Prefix: </gray>").append(ColorUtil.parse(rank.getPrefix())));
        lore.add(MM.deserialize("<gray>Suffix: </gray>").append(ColorUtil.parse(rank.getSuffix())));
        lore.add(MM.deserialize("<gray>Chat Color: </gray>").append(ColorUtil.parse(rank.getChatColor() + "Example")));
        lore.add(Component.empty());
        lore.add(MM.deserialize("<yellow>Click to Edit Rank</yellow>"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        if (event.getRawSlot() < 0 || event.getRawSlot() >= INV_SIZE) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || meta.displayName() == null) return;

        // Extract rank name from display name (rough way)
        String plainName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(meta.displayName());

        Rank found = null;
        for (Rank r : rankManager.getAllRanks()) {
            if (plainName.contains(r.getName())) {
                found = r;
                break;
            }
        }

        if (found != null) {
            player.closeInventory();
            final Rank finalRank = found;
            player.sendMessage(MM.deserialize("<yellow>Editing rank: " + finalRank.getName() + "</yellow>"));
            player.sendMessage(MM.deserialize("<gray>Type a new prefix in chat (or //cancel):</gray>"));
            new ChatInputSession(plugin, player, input -> {
                if (!input.equalsIgnoreCase("//cancel")) {
                    finalRank.setPrefix(input);
                    player.sendMessage(MM.deserialize("<green>Prefix updated!</green>"));
                }
                new AnimaRankMainGUI(plugin, player).open();
            }, () -> new AnimaRankMainGUI(plugin, player).open()).await();
        }
    }
}
