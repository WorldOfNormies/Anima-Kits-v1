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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/** Read-only display of a kit's contents. */
public class KitDisplayGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;
    private static final int ITEMS_AREA = 45;

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final Kit kit;
    private Inventory inventory;

    public KitDisplayGui(AnimaKitsPlugin plugin, Player player, Kit kit) {
        this.plugin = plugin;
        this.player = player;
        this.kit = kit;
    }

    public void open() {
        String titleRaw = plugin.getConfig().getString("gui.kit-display-title",
                "<gradient:#54DAF4:#545EB6><bold>Viewing: {kit}</bold></gradient>")
                .replace("{kit}", kit.getPlainName());
        Component title = MM.deserialize(titleRaw);

        inventory = Bukkit.createInventory(null, INV_SIZE, title);

        // Populate items (read-only clones)
        List<ItemStack> items = kit.getItems();
        for (int i = 0; i < Math.min(items.size(), ITEMS_AREA); i++) {
            inventory.setItem(i, items.get(i).clone());
        }

        // Border
        Material borderMat = parseMaterial(
                plugin.getConfig().getString("gui.border-material", "BLACK_STAINED_GLASS_PANE"));
        for (int i = ITEMS_AREA; i < INV_SIZE; i++) {
            inventory.setItem(i, GuiItem.border(borderMat));
        }
        inventory.setItem(49, GuiItem.make(Material.BOOK,
                MM.deserialize("<yellow>Viewing: <white>" + kit.getPlainName() + "</white></yellow>"),
                List.of(MM.deserialize("<gray>This is a read-only preview.</gray>"))));

        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().equals(inventory)) event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        HandlerList.unregisterAll(this);
    }

    private Material parseMaterial(String name) {
        try { return Material.valueOf(name.toUpperCase()); }
        catch (Exception e) { return Material.BLACK_STAINED_GLASS_PANE; }
    }
}
