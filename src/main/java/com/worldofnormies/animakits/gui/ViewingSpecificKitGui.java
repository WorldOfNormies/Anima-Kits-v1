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
import java.util.Map;

/**
 * ViewingSpecificKitGui – "Viewing: {kit_name}".
 * Allows claiming the kit.
 */
public class ViewingSpecificKitGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;
    private static final int ITEMS_AREA = 45;

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final Kit kit;
    private Inventory inventory;

    public ViewingSpecificKitGui(AnimaKitsPlugin plugin, Player player, Kit kit) {
        this.plugin = plugin;
        this.player = player;
        this.kit = kit;
    }

    public void open() {
        Component title = MM.deserialize("<gradient:#54DAF4:#545EB6><bold>Viewing: " + kit.getPlainName() + "</bold></gradient>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        List<ItemStack> items = kit.getItems();
        for (int i = 0; i < Math.min(items.size(), ITEMS_AREA); i++) {
            ItemStack item = items.get(i);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(i, item.clone());
            }
        }

        // Layout based on user request (minus 1 for 0-based)
        ItemStack blackPane = GuiItem.border(Material.BLACK_STAINED_GLASS_PANE);
        for (int s : new int[]{0,1,2,3, 5,6,7,8, 45,46,47,48, 50,51}) {
            if (s < INV_SIZE) inventory.setItem(s, blackPane);
        }

        ItemStack whitePane = GuiItem.border(Material.WHITE_STAINED_GLASS_PANE);
        for (int s : new int[]{8,16,17,25,26,34,35,43,49}) {
            if (s < INV_SIZE) inventory.setItem(s, whitePane);
        }

        // Ender Chest: Slot 4 -> Index 3
        inventory.setItem(3, GuiItem.make(Material.ENDER_CHEST, MM.deserialize("<light_purple><bold>" + kit.getPlainName() + "</bold></light_purple>"),
                List.of(MM.deserialize("<gray>Items in kit: <white>" + items.size() + "</white></gray>"))));

        // Red Bundle: Slot 45 -> Index 44 (Return)
        inventory.setItem(44, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<red><bold>✘ Back to Kits</bold></red>")));

        // Clock: Slot 50 -> Index 49
        inventory.setItem(49, GuiItem.make(Material.CLOCK, MM.deserialize("<gold><bold>Cooldown Information</bold></gold>"),
                List.of(MM.deserialize("<gray>Cooldown: <gold><bold>" + kit.getCooldown() + "s</bold></gold></gray>"),
                        MM.deserialize("<gray>Single Claim: " + (kit.isSingleClaim() ? "<red>True</red>" : "<green>False</green>") + "</gray>"))));

        // Green Bundle: Slot 54 -> Index 53 (Claim)
        inventory.setItem(53, GuiItem.make(Material.LIME_BUNDLE, MM.deserialize("<green><bold>✔ Claim Kit</bold></green>")));

        // Lime Glass pane: Slot 53 -> Index 52
        inventory.setItem(52, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, Component.space()));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot == 44) {
            new DisplayAllKitsGui(plugin, player).open();
        } else if (slot == 53) {
            handleClaim();
        }
    }

    private void handleClaim() {
        String kitPerm = "anima.kits.claim.KitName \"" + kit.getPlainName() + "\"";
        if (!plugin.getPermissionManager().has(player.getUniqueId(), kitPerm)) {
            player.sendMessage(MM.deserialize("<red>You do not have permission to claim this kit!</red>"));
            return;
        }
        // TODO Check cooldown

        for (ItemStack item : kit.getItems()) {
            if (item != null && item.getType() != Material.AIR) {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(item.clone());
                for (ItemStack rem : remaining.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), rem);
                }
            }
        }
        player.sendMessage(MM.deserialize("<green>You have claimed the " + kit.getPlainName() + " kit!</green>"));
        player.closeInventory();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) HandlerList.unregisterAll(this);
    }
}
