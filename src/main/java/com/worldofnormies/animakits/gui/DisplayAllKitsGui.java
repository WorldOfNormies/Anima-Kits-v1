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

/**
 * DisplayAllKitsGui
 * Display name: "Displaying All Kit GUI" with a neat light purple to orange bold letter name.
 * Sorts kits: claimable (with glow) first, then divider, then locked.
 */
public class DisplayAllKitsGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private int page = 0;
    private Inventory inventory;

    public DisplayAllKitsGui(AnimaKitsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        build();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void build() {
        Component title = MM.deserialize("<gradient:#D948FF:#FF9100><bold>Displaying All Kit GUI</bold></gradient>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
    }

    private void populate() {
        inventory.clear();
        List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
        List<Kit> claimable = new ArrayList<>();
        List<Kit> locked = new ArrayList<>();

        for (Kit k : allKits) {
            String kitPerm = "anima.kits.claim.KitName \"" + k.getPlainName() + "\"";
            if (plugin.getPermissionManager().has(player.getUniqueId(), kitPerm)) {
                claimable.add(k);
            } else {
                locked.add(k);
            }
        }

        List<Object> displayList = new ArrayList<>(claimable);
        if (!locked.isEmpty()) {
            displayList.add("DIVIDER");
            displayList.addAll(locked);
        }

        // Available inside grid slots for items (rows 1-4, skipping white glass borders)
        int[] kitSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int totalPages = Math.max(1, (int) Math.ceil(displayList.size() / (double) kitSlots.length));
        page = Math.min(page, totalPages - 1);

        int from = page * kitSlots.length;
        int to = Math.min(from + kitSlots.length, displayList.size());

        for (int i = 0; i < (to - from); i++) {
            Object obj = displayList.get(from + i);
            if (obj instanceof Kit kit) {
                boolean canClaim = claimable.contains(kit);
                inventory.setItem(kitSlots[i], createKitIcon(kit, canClaim));
            } else if (obj.equals("DIVIDER")) {
                inventory.setItem(kitSlots[i], createDivider());
            }
        }

        // --- BACKGROUND DECORATION ---
        ItemStack blackPane = GuiItem.border(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack whitePane = GuiItem.border(Material.WHITE_STAINED_GLASS_PANE);

        // Top Row (0-8, except slot 4 which is the Ender Chest)
        for (int slot : new int[]{0, 1, 2, 3, 5, 6, 7, 8}) {
            inventory.setItem(slot, blackPane);
        }

        // Side Borders (White Glass)
        for (int slot : new int[]{9, 18, 27, 36, 17, 26, 35, 44}) {
            inventory.setItem(slot, whitePane);
        }

        // Bottom Row Fillers (Placing background items first)
        for (int slot : new int[]{45, 46, 48, 49, 50, 52, 53}) {
            inventory.setItem(slot, blackPane);
        }

        // --- INTERACTIVE BUTTONS & UTILITY ITEMS ---

        // Slot 4: Ender Chest (Kit Statistics)
        inventory.setItem(4, GuiItem.make(Material.ENDER_CHEST, MM.deserialize("<light_purple><bold>Kit Statistics</bold></light_purple>"),
                List.of(MM.deserialize("<gray>Total Kits: <white>" + allKits.size() + "</white></gray>"),
                        MM.deserialize("<gray>Page: <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        // Slot 45: Red Bundle (Return to main menu)
        //inventory.setItem(45, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<gradient:#8B0000:#FF0000><bold>✘ Return to Kits Menu</bold></gradient>")));

        // Slot 47: Previous Page
        if (page > 0) {
            inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<gradient:#8B0000:#FF8C00><bold>« Previous Page</bold></gradient>")));
        } else {
            inventory.setItem(47, blackPane);
        }

        // Slot 51: Next Page
        if (to < displayList.size()) {
            inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<gradient:#32CD32:#ADFF2F><bold>Next Page »</bold></gradient>")));
        } else {
            inventory.setItem(51, blackPane);
        }

    }

    private ItemStack createKitIcon(Kit kit, boolean canClaim) {
        Component name = ColorUtil.parse(kit.getRawName());
        List<Component> lore = new ArrayList<>();
        for (String l : kit.getLore()) lore.add(ColorUtil.parse(l));
        lore.add(Component.empty());
        lore.add(MM.deserialize("<gradient:#54DAF4:#545EB6>☚ <bold>Left Mouse Click To View</bold></gradient>"));
        lore.add(MM.deserialize("<gradient:#FFB700:#FF8000>☛ <bold>Right Mouse Click To Claim</bold></gradient>"));

        // Cooldown Line
        lore.add(MM.deserialize("<white><bold>Cooldown Time:</bold></white> <gradient:#FFD700:#D3D3D3><bold>" + formatTime(kit.getCooldown()) + "</bold></gradient>"));
        
        // Single Claim Line
        lore.add(MM.deserialize("<gradient:#8A2BE2:#32CD32><bold>Single Claim?</bold></gradient> " + 
            (kit.isSingleClaim() ? "<gradient:#8B0000:#FF0000><bold>True</bold></gradient>" : "<gradient:#006400:#32CD32><bold>False</bold></gradient>")));

        return GuiItem.make(kit.getIconMaterial(), name, lore, canClaim);
    }

    private ItemStack createDivider() {
        return GuiItem.make(Material.BLACK_STAINED_GLASS_PANE,
            MM.deserialize("<bold>Locked</bold>"),
            List.of(
                MM.deserialize("<white><bold>☚</bold></white> <gradient:#A8FF78:#78FFD6><bold>Kits You Can Claim</bold></gradient>"),
                MM.deserialize("<gradient:#FF4B2B:#FF416C><bold>Kits You Can't Claim Yet</bold></gradient> <white><bold>☛</bold></white>")
            )
        );
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
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player clicker)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= INV_SIZE) return;

        if (slot == 45) { player.closeInventory(); return; }
        if (slot == 47 && page > 0) { page--; populate(); return; }
        if (slot == 51) {
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            List<Kit> claimable = new ArrayList<>();
            List<Kit> locked = new ArrayList<>();
            for (Kit k : allKits) {
                String kitPerm = "anima.kits.claim.KitName \"" + k.getPlainName() + "\"";
                if (plugin.getPermissionManager().has(player.getUniqueId(), kitPerm)) claimable.add(k);
                else locked.add(k);
            }
            List<Object> displayList = new ArrayList<>(claimable);
            if (!locked.isEmpty()) { displayList.add("DIVIDER"); displayList.addAll(locked); }

            int[] kitSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
            };

            int to = Math.min((page + 1) * kitSlots.length, displayList.size());
            if (to < displayList.size()) {
                page++;
                populate();
            }
            return;
        }

        // Check grid area for valid item slot clicks
        int[] kitSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int kitIdx = -1;
        for (int i = 0; i < kitSlots.length; i++) {
            if (kitSlots[i] == slot) { kitIdx = i; break; }
        }

        if (kitIdx != -1) {
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            List<Kit> claimable = new ArrayList<>();
            List<Kit> locked = new ArrayList<>();
            for (Kit k : allKits) {
                String kitPerm = "anima.kits.claim.KitName \"" + k.getPlainName() + "\"";
                if (plugin.getPermissionManager().has(player.getUniqueId(), kitPerm)) claimable.add(k);
                else locked.add(k);
            }
            List<Object> displayList = new ArrayList<>(claimable);
            if (!locked.isEmpty()) { displayList.add("DIVIDER"); displayList.addAll(locked); }

            int idx = page * kitSlots.length + kitIdx;
            if (idx < displayList.size()) {
                Object obj = displayList.get(idx);
                if (obj instanceof Kit kit) {
                    if (event.isLeftClick()) {
                        new ViewingSpecificKitGui(plugin, player, kit).open();
                    } else if (event.isRightClick()) {
                        handleClaim(kit);
                    }
                }
            }
        }
    }

    private void handleClaim(Kit kit) {
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
    
        // --- Dynamic Gradient Break ---
        // Part 1: "You have claimed the " (Dark Purple #301934 -> Violet #8A2BE2 -> Light Yellow #FFFFE0)
        Component prefix = MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold>You have claimed the </bold></gradient>");
        
        // Part 2: Original kit color name forced into bold
        Component kitName = ColorUtil.parse(kit.getRawName()).toBuilder().bold(true).build();
        
        // Part 3: " kit!" (Same Dark Purple -> Violet -> Light Yellow gradient)
        Component suffix = MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold> kit!</bold></gradient>");
    
        // Combine them and send
        player.sendMessage(prefix.append(kitName).append(suffix));
    }
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) HandlerList.unregisterAll(this);
    }
}
