package com.worldofnormies.animakits.home.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.AnimaInventoryHolder;
import com.worldofnormies.animakits.home.Home;
import com.worldofnormies.animakits.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnimaHomeGUI {

    private final AnimaKitsPlugin plugin;
    private final UUID playerUuid;
    private final Inventory inventory;

    public AnimaHomeGUI(AnimaKitsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.playerUuid = player.getUniqueId();
        this.inventory = Bukkit.createInventory(new AnimaInventoryHolder("homes", playerUuid), 54, ColorUtil.parse("<gradient:#3060FF:#FFFFFF:#FF3030><bold>⋆Ἲ⸸ [ YOUR HOMES ] ⸸Ἳ⋆</bold></gradient>"));
        populate();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();

        // Border
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        bm.setDisplayName(" ");
        border.setItemMeta(bm);
        for (int i = 0; i < 9; i++) inventory.setItem(i, border);
        for (int i = 45; i < 54; i++) inventory.setItem(i, border);
        inventory.setItem(9, border); inventory.setItem(17, border);
        inventory.setItem(18, border); inventory.setItem(26, border);
        inventory.setItem(27, border); inventory.setItem(35, border);
        inventory.setItem(36, border); inventory.setItem(44, border);

        List<Home> homes = new ArrayList<>(plugin.getHomeManager().getHomes(playerUuid));
        int[] slots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};

        for (int i = 0; i < homes.size() && i < slots.length; i++) {
            Home home = homes.get(i);
            ItemStack item = new ItemStack(Material.WHITE_BED);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ColorUtil.colorize("&b&l" + home.getName()));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtil.colorize("&7World: &f" + (Bukkit.getWorld(home.getWorldUuid()) != null ? Bukkit.getWorld(home.getWorldUuid()).getName() : "Unknown")));
            lore.add(ColorUtil.colorize("&7X: &f" + (int)home.getX() + " &7Y: &f" + (int)home.getY() + " &7Z: &f" + (int)home.getZ()));
            lore.add("");
            lore.add(ColorUtil.colorize("&aLeft-Click to teleport"));
            lore.add(ColorUtil.colorize("&cRight-Click to delete"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(slots[i], item);
        }
    }

    public void handleAction(Player player, ItemStack item, boolean leftClick, boolean rightClick) {
        if (item == null || item.getType() == Material.AIR || item.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String homeName = org.bukkit.ChatColor.stripColor(meta.getDisplayName());

        if (leftClick) {
            Home home = plugin.getHomeManager().getHome(playerUuid, homeName);
            if (home != null && home.getLocation() != null) {
                player.teleport(home.getLocation());
                player.sendMessage(ColorUtil.colorize("&aTeleported to home: &f" + homeName));
                player.closeInventory();
            }
        } else if (rightClick) {
            if (plugin.getHomeManager().removeHome(playerUuid, homeName)) {
                player.sendMessage(ColorUtil.colorize("&cRemoved home: &f" + homeName));
                populate();
            }
        }
    }
}
