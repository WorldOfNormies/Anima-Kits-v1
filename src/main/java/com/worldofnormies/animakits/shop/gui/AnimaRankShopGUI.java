package com.worldofnormies.animakits.shop.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.AnimaInventoryHolder;
import com.worldofnormies.animakits.util.ColorUtil;
import com.worldofnormies.animaranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AnimaRankShopGUI {
    private final AnimaKitsPlugin plugin;
    private final Inventory inventory;

    public AnimaRankShopGUI(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(new AnimaInventoryHolder("shop_ranks", null), 54, ColorUtil.parse("<gradient:#8A2BE2:#FFFFFF:#8A2BE2><bold>⋆Ἲ⸸ [ BUY RANKS ] ⸸Ἳ⋆</bold></gradient>"));
        populate();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void populate() {
        List<Rank> ranks = plugin.getRankManager().getAllRanks();
        int i = 10;
        for (Rank rank : ranks) {
            if (!rank.isBuyable()) continue;
            if (i > 43) break;
            if (i % 9 == 0 || i % 9 == 8) i++;

            ItemStack item = new ItemStack(rank.getIconMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ColorUtil.colorize(rank.getPrefix() + rank.getDisplayName()));
            List<String> lore = new ArrayList<>();
            String currency = rank.isBuyableWithAnimaz() ? "Animaz" : "Ani";
            lore.add(ColorUtil.colorize("&7Price: &f" + rank.getBuyPrice() + " " + currency));
            lore.add(ColorUtil.colorize("&7Duration: &f" + (rank.getBuyDuration() == -1 ? "Permanent" : (rank.getBuyDuration()/3600) + " hours")));
            lore.add("");
            lore.add(ColorUtil.colorize("&b&lRIGHTS & PERMISSIONS:"));
            lore.add(ColorUtil.colorize("&7• Homes: &f" + rank.getHomeLimit()));
            lore.add(ColorUtil.colorize("&7• EChest Rows: &f" + rank.getEchestRows()));
            lore.add(ColorUtil.colorize("&7• Repair Cooldown: &f" + (rank.getRepairCooldown() == -1 ? "Locked" : (rank.getRepairCooldown() == 0 ? "Instant" : rank.getRepairCooldown() + "s"))));
            lore.add("");
            lore.add(ColorUtil.colorize("&eClick to buy!"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(i++, item);
        }
    }

    public void handleAction(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String displayName = org.bukkit.ChatColor.stripColor(meta.getDisplayName());
        Rank targetRank = null;
        for (Rank r : plugin.getRankManager().getAllRanks()) {
            if (org.bukkit.ChatColor.stripColor(ColorUtil.colorize(r.getPrefix() + r.getDisplayName())).equals(displayName)) {
                targetRank = r;
                break;
            }
        }

        if (targetRank != null) {
            String type = targetRank.isBuyableWithAnimaz() ? "animaz" : "ani";
            if (plugin.getEconomyManager().withdraw(player.getUniqueId(), type, (long)targetRank.getBuyPrice())) {
                plugin.getRankManager().setPlayerRank(player.getUniqueId(), targetRank.getId(), targetRank.getBuyDuration());
                player.sendMessage(ColorUtil.colorize("&aYou purchased rank: &f" + targetRank.getDisplayName()));
                player.closeInventory();
            } else {
                player.sendMessage(ColorUtil.colorize("&cYou do not have enough " + (targetRank.isBuyableWithAnimaz() ? "Animaz" : "Ani") + "!"));
            }
        }
    }
}
