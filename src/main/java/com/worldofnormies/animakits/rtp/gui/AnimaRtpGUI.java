package com.worldofnormies.animakits.rtp.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AnimaRtpGUI implements Listener {

    private final AnimaKitsPlugin plugin;
    private final Inventory inventory;

    public AnimaRtpGUI(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 27, ColorUtil.parse("<gradient:#3060FF:#FFFFFF:#FF3030><bold>⋆Ἲ⸸ [ RANDOM TELEPORT ] ⸸Ἳ⋆</bold></gradient>"));
        populate();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void populate() {
        // Overworld
        inventory.setItem(11, createWorldItem(Material.GRASS_BLOCK, "&a&lOVERWORLD", "world"));
        // Nether
        inventory.setItem(13, createWorldItem(Material.NETHERRACK, "&c&lNETHER", "world_nether"));
        // End
        inventory.setItem(15, createWorldItem(Material.END_STONE, "&e&lTHE END", "world_the_end"));
    }

    private ItemStack createWorldItem(Material mat, String name, String worldName) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.colorize(name));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.colorize("&7Teleport to a random location in this world."));
        lore.add("");
        lore.add(ColorUtil.colorize("&aClick to teleport!"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        World world = null;
        if (item.getType() == Material.GRASS_BLOCK) world = Bukkit.getWorld("world");
        else if (item.getType() == Material.NETHERRACK) world = Bukkit.getWorld("world_nether");
        else if (item.getType() == Material.END_STONE) world = Bukkit.getWorld("world_the_end");

        if (world != null) {
            if (plugin.getRtpManager().canRtp(player.getUniqueId(), world.getName())) {
                plugin.getRtpManager().teleport(player, world);
                player.sendMessage(ColorUtil.colorize("&aTeleporting to a random location in &f" + world.getName() + "&a..."));
                player.closeInventory();
            } else {
                long remaining = plugin.getRtpManager().getRemainingCooldown(player.getUniqueId(), world.getName());
                player.sendMessage(ColorUtil.colorize("&cYou must wait &f" + remaining + "s &cbefore using RTP again."));
            }
        }
    }
}
