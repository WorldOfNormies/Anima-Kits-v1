package com.worldofnormies.animakits.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.kit.Kit;
import com.worldofnormies.animakits.util.ColorUtil;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Read-only display of a kit's contents. */
public class KitDisplayGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final Kit kit;
    private Inventory inventory;
    private int page = 0;

    public KitDisplayGui(AnimaKitsPlugin plugin, Player player, Kit kit) {
        this.plugin = plugin;
        this.player = player;
        this.kit = kit;
    }

    public void open() {
        build();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void build() {
        // Displays kit's original color name entirely forced to bold
        Component title = ColorUtil.parse(kit.getRawName()).toBuilder().bold(true).build();
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
    }

    private void populate() {
        inventory.clear();
        List<ItemStack> items = kit.getItems();

        // Inner item display grid slots (Rows 1-4, skipping white glass side borders)
        int[] kitSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int totalPages = Math.max(1, (int) Math.ceil(items.size() / (double) kitSlots.length));
        page = Math.min(page, totalPages - 1);

        int from = page * kitSlots.length;
        int to   = Math.min(from + kitSlots.length, items.size());

        // Fill inner grid slots with raw preview items
        for (int i = 0; i < (to - from); i++) {
            ItemStack item = items.get(from + i);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(kitSlots[i], item.clone());
            }
        }

        // --- BACKGROUND DECORATION ---
        ItemStack blackPane = GuiItem.border(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack whitePane = GuiItem.border(Material.WHITE_STAINED_GLASS_PANE);

        // Top Row (0-8, skipping slot 4 for the Ender Chest)
        for (int s : new int[]{0, 1, 2, 3, 5, 6, 7, 8}) {
            inventory.setItem(s, blackPane);
        }

        // Side Borders (White Glass)
        for (int s : new int[]{9, 18, 27, 36, 17, 26, 35, 44}) {
            inventory.setItem(s, whitePane);
        }

        // Bottom Row Fillers
        for (int s : new int[]{46, 48, 50, 52}) {
            inventory.setItem(s, blackPane);
        }

        // --- INTERACTIVE BUTTONS & UTILITY ITEMS ---

        // Slot 4: Ender Chest (Kit Statistics)
        inventory.setItem(4, GuiItem.make(Material.ENDER_CHEST, MM.deserialize("<light_purple><bold>Kit Statistics</bold></light_purple>"),
                List.of(MM.deserialize("<gray>Total Items: <white>" + items.size() + "</white></gray>"),
                        MM.deserialize("<gray>Page: <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        // Slot 45: Return Button (Dark blood red to red bold gradient)
        inventory.setItem(45, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<gradient:#8B0000:#FF0000><bold>✘ Return to Kits Menu</bold></gradient>")));

        // Slot 47: Previous Page (Dark blood red to orange bold gradient)
        if (page > 0) {
            inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<gradient:#8B0000:#FF8C00><bold>« Previous Page</bold></gradient>")));
        } else {
            inventory.setItem(47, blackPane);
        }

        // Slot 49: Clock (Shows Cooldown Time)
        List<Component> clockLore = new ArrayList<>();
        clockLore.add(MM.deserialize("<white><bold>Cooldown Time:</bold></white> <gradient:#FFD700:#D3D3D3><bold>" + formatTime(kit.getCooldown()) + "</bold></gradient>"));
        clockLore.add(MM.deserialize("<gradient:#8A2BE2:#32CD32><bold>Single Claim?</bold></gradient> " + 
            (kit.isSingleClaim() ? "<gradient:#8B0000:#FF0000><bold>True</bold></gradient>" : "<gradient:#006400:#32CD32><bold>False</bold></gradient>")));
        
        inventory.setItem(49, GuiItem.make(Material.CLOCK, MM.deserialize("<gradient:#FFD700:#D3D3D3><bold>Kit Information</bold></gradient>"), clockLore));

        // Slot 51: Next Page (Lime green to yellowish-green bold gradient)
        if (to < items.size()) {
            inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<gradient:#32CD32:#ADFF2F><bold>Next Page »</bold></gradient>")));
        } else {
            inventory.setItem(51, blackPane);
        }

        // Slot 53: Green Bundle (Claim Kit option)
        inventory.setItem(53, GuiItem.make(Material.LIME_BUNDLE, MM.deserialize("<gradient:#32CD32:#ADFF2F><bold>✔ Claim Kit</bold></gradient>")));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true); // Keep view strictly read-only
        if (!(event.getWhoClicked() instanceof Player clicker)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= INV_SIZE) return;

        // Navigation Actions
        if (slot == 45) { 
            player.closeInventory(); 
            return; 
        }
        if (slot == 47 && page > 0) { 
            page--; 
            populate(); 
            return; 
        }
        if (slot == 51) {
            List<ItemStack> items = kit.getItems();
            int[] kitSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
            };
            if ((page + 1) * kitSlots.length < items.size()) {
                page++; 
                populate();
            }
            return;
        }
        
        // Claim Kit Click Event Execution
        if (slot == 53) {
            handleClaim();
        }
    }

    private void handleClaim() {
        String kitPerm = "anima.kits.claim.KitName \"" + kit.getPlainName() + "\"";
        if (!plugin.getPermissionManager().has(player.getUniqueId(), kitPerm)) {
            player.sendMessage(MM.deserialize("<gradient:#8B0000:#FF0000><bold>You do not have permission to claim this kit!</bold></gradient>"));
            return;
        }

        for (ItemStack item : kit.getItems()) {
            if (item != null && item.getType() != Material.AIR) {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(item.clone());
                for (ItemStack rem : remaining.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), rem);
                }
            }
        }

        Component prefix = MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold>You have claimed the </bold></gradient>");
        Component kitName = ColorUtil.parse(kit.getRawName()).toBuilder().bold(true).build();
        Component suffix = MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold> kit!</bold></gradient>");
        player.sendMessage(prefix.append(kitName).append(suffix));
    }

    private String formatTime(long seconds) {
        if (seconds <= 0) return "None";
        long mins = seconds / 60;
        long hours = mins / 60;
        long days = hours / 24;
        if (days > 0) return days + "d";
        if (hours > 0) return hours + "h";
        if (mins > 0) return mins + "m";
        return seconds + "s";
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}
