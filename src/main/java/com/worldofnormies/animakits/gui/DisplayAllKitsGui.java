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
 * DisplayAllKitsGui – renamed from "Viewing All Kit GUI".
 * Display name: "Displaying All Kit GUI" with a neat light purple to orange bold letter name.
 * Sorts kits: claimable (with glow) first, then divider, then locked.
 */
public class DisplayAllKitsGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;
    private static final int KITS_AREA = 45;

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

        // Available slots for kits (remaining from decoration)
        int[] kitSlots = {
            9, 10, 11, 12, 13, 14, 15,
            18, 19, 20, 21, 22, 23, 24,
            27, 28, 29, 30, 31, 32, 33,
            36, 37, 38, 39, 40, 41, 42
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

        // Decoration & Navigation
        ItemStack blackPane = GuiItem.border(Material.BLACK_STAINED_GLASS_PANE);
        // Black: [0, 1, 2, 3, 5, 6, 7, 8, 46, 47, 48, 49, 51, 52, 54] -> 0,1,2,4,5,6,7,45,46,47,48,50,51,53 (Skipped 3, 8, 44, 49, 52)
        int[] blackSlots = {0, 1, 2, 4, 5, 6, 7, 45, 47, 48, 50, 51, 53};
        for (int slot : blackSlots) {
            if (slot < INV_SIZE) inventory.setItem(slot, blackPane);
        }

        // White glass pane: Slot = [9, 17, 18, 26, 27, 35, 36, 44, 50] -> 8,16,17,25,26,34,35,43,49
        ItemStack whitePane = GuiItem.border(Material.WHITE_STAINED_GLASS_PANE);
        for (int slot : new int[]{8,16,17,25,26,34,35,43,49}) {
            if (slot < INV_SIZE) inventory.setItem(slot, whitePane);
        }

        // Ender Chest: Slot = [4] -> Index 3
        inventory.setItem(3, GuiItem.make(Material.ENDER_CHEST, MM.deserialize("<light_purple><bold>Kit Statistics</bold></light_purple>"),
                List.of(MM.deserialize("<gray>Total Kits: <white>" + allKits.size() + "</white></gray>"),
                        MM.deserialize("<gray>Page: <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        // Red Bundle: Slot = [45] -> Index 44 (Return to main menu)
        inventory.setItem(44, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<red><bold>✘ Return to Kits Menu</bold></red>")));

        // Red Glass pane: Slot = [47] -> Index 46 (Prev Page)
        if (page > 0) {
            inventory.setItem(46, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<red>« Previous Page</red>")));
        } else {
            inventory.setItem(46, blackPane);
        }

        // Lime Glass pane: Slot = [53] -> Index 52 (Next Page)
        if (to < displayList.size()) {
            inventory.setItem(52, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<green>Next Page »</green>")));
        } else {
            inventory.setItem(52, blackPane);
        }
    }

    private ItemStack createKitIcon(Kit kit, boolean canClaim) {
        Component name = ColorUtil.parse(kit.getRawName());
        List<Component> lore = new ArrayList<>();
        for (String l : kit.getLore()) lore.add(ColorUtil.parse(l));
        lore.add(Component.empty());
        lore.add(MM.deserialize("<gradient:#54DAF4:#545EB6>☚ <bold>Left Mouse Click To View</bold></gradient>"));
        lore.add(MM.deserialize("<gradient:#FFB700:#FF8000>☛ <bold>Right Mouse Click To Claim</bold></gradient>"));

        // Cooldown info
        lore.add(MM.deserialize("<gray>Kit Has A Cooldown Time Of: <gold><bold>" + formatTime(kit.getCooldown()) + "</bold></gold></gray>"));
        // TODO: Time left to claim
        lore.add(MM.deserialize("<gray>This Kit Can Be Claimed Only Once = " + (kit.isSingleClaim() ? "<red>True</red>" : "<green>False</green>") + "</gray>"));

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

        if (slot == 44) { player.closeInventory(); return; }
        if (slot == 46 && page > 0) { page--; populate(); return; }
        if (slot == 52) {
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            // ... need to recalculate displayList size for proper pagination check
            page++; populate();
            return;
        }

        // Check if it's a kit slot
        int[] kitSlots = {
            9, 10, 11, 12, 13, 14, 15,
            18, 19, 20, 21, 22, 23, 24,
            27, 28, 29, 30, 31, 32, 33,
            36, 37, 38, 39, 40, 41, 42
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
            player.sendMessage(MM.deserialize("<red>You do not have permission to claim this kit!</red>"));
            return;
        }
        // TODO: Check cooldown

        // Give items
        for (ItemStack item : kit.getItems()) {
            if (item != null && item.getType() != Material.AIR) {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(item.clone());
                for (ItemStack rem : remaining.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), rem);
                }
            }
        }
        player.sendMessage(MM.deserialize("<green>You have claimed the " + kit.getPlainName() + " kit!</green>"));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) HandlerList.unregisterAll(this);
    }
}
