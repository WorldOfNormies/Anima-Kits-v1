package com.worldofnormies.animaranks.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.GuiItem;
import com.worldofnormies.animaranks.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AnimaRanksGUI implements Listener {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final AnimaKitsPlugin plugin;
    private final Player player;
    private Inventory inventory;

    public AnimaRanksGUI(AnimaKitsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Component title = MM.deserialize("<gradient:#00FBFF:#007BFF><bold>⋆༺⸸ [ Ranks ] ⸸༻⋆</bold></gradient>");
        inventory = Bukkit.createInventory(null, 54, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();
        ItemStack pane = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.space());
        pane.setItemMeta(meta);

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) inventory.setItem(i, pane);
        }

        int slot = 10;
        List<Rank> sorted = new ArrayList<>(plugin.getRankManager().getAllRanks());
        sorted.sort((a, b) -> Integer.compare(a.getHierarchy(), b.getHierarchy()));

        for (Rank r : sorted) {
            while (slot % 9 == 0 || slot % 9 == 8) slot++;
            if (slot >= 45) break;

            List<Component> lore = new ArrayList<>();
            lore.add(MM.deserialize("<gray>Hierarchy: <white>" + r.getHierarchy() + "</white></gray>"));
            lore.add(MM.deserialize("<gray>Prefix: " + (r.getPrefix().isEmpty() ? "<red>None" : r.getPrefix()) + "</gray>"));
            lore.add(MM.deserialize("<gray>Suffix: " + (r.getSuffix().isEmpty() ? "<red>None" : r.getSuffix()) + "</gray>"));
            lore.add(MM.deserialize("<gray>Name Color: " + (r.getNameColor().isEmpty() ? "<red>None" : r.getNameColor()) + "Sample</gray>"));
            lore.add(MM.deserialize("<gray>Chat Color: " + (r.getChatColor().isEmpty() ? "<red>None" : r.getChatColor()) + "Sample</gray>"));

            inventory.setItem(slot++, GuiItem.make(Material.GOLD_BLOCK,
                MM.deserialize("<gradient:#00FBFF:#007BFF><bold>" + r.getId() + "</bold></gradient>"), lore));
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) HandlerList.unregisterAll(this);
    }
}
