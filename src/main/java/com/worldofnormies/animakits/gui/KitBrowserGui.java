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
 * KitBrowserGui – paginated kit list.
 * Refreshes automatically when kits are added, removed or renamed.
 */
public class KitBrowserGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private int page = 0;
    private Inventory inventory;

    private static final int KITS_PER_PAGE = 45;
    private static final int INV_SIZE = 54;
    private static final int PREV_SLOT = 48;
    private static final int NEXT_SLOT = 50;

    public KitBrowserGui(AnimaKitsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        build();
        plugin.getKitManager().registerBrowser(player, this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    /** Rebuild and re-push inventory contents to all viewers (live refresh). */
    public void refresh() {
        if (inventory == null) return;
        inventory.clear();
        populate();
    }

    // ── Build ──────────────────────────────────────────────────────

    private void build() {
        Component title = MM.deserialize("[All Kits Menu]");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
    }

    private void populate() {
        inventory.clear();
        List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());

        int[] kitSlots = {
            10, 11, 12, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int totalPages = Math.max(1, (int) Math.ceil(allKits.size() / (double) kitSlots.length));
        page = Math.min(page, totalPages - 1);

        int from = page * kitSlots.length;
        int to   = Math.min(from + kitSlots.length, allKits.size());
        List<Kit> pageKits = allKits.subList(from, to);

        // Decoration panes
        ItemStack blackPane = GuiItem.border(Material.BLACK_STAINED_GLASS_PANE);
        // Slot = [0, 1, 2, 3, 5, 6, 7, 8, 46, 48, 49, 51, 52] -> Index: 0,1,2,3,5,6,7,45,47,48,50,51
        for (int s : new int[]{0,1,2,3, 5,6,7, 45,47,48,50,51}) {
            inventory.setItem(s, blackPane);
        }

        ItemStack whitePane = GuiItem.border(Material.WHITE_STAINED_GLASS_PANE);
        // Slot = [9, 17, 18, 26, 27, 35, 36, 44] -> Index: 8,16,17,25,26,34,35,43
        for (int s : new int[]{8,16,17,25,26,34,35,43}) {
            inventory.setItem(s, whitePane);
        }

        // Kit icons
        for (int i = 0; i < pageKits.size(); i++) {
            Kit kit = pageKits.get(i);
            Component name = ColorUtil.parse(kit.getRawName());
            List<Component> lore = new ArrayList<>();
            lore.add(MM.deserialize("<yellow>Left Click</yellow> <gray>→ Open</gray>"));
            lore.add(MM.deserialize("<yellow>Shift Left Click</yellow> <gray>→ Edit Kit</gray>"));
            lore.add(MM.deserialize("<yellow>Right Click</yellow> <gray>→ Give (Self)</gray>"));
            lore.add(MM.deserialize("<yellow>Shift Right Click</yellow> <gray>→ Clone Kit</gray>"));
            lore.add(MM.deserialize("<yellow>Middle Mouse</yellow> <gray>→ Delete This Kit</gray>"));
            lore.add(MM.deserialize("<yellow>Shift Middle Mouse</yellow> <gray>→ Give or Giveall</gray>"));

            ItemStack icon = GuiItem.make(kit.getIconMaterial(), name, lore);
            inventory.setItem(kitSlots[i], icon);
        }

        // Special items
        // Ender Chest: Slot = [4] -> Index 3
        inventory.setItem(3, GuiItem.make(Material.ENDER_CHEST, MM.deserialize("<light_purple><bold>Kit Statistics</bold></light_purple>"),
                List.of(MM.deserialize("<gray>Total Kits: <white>" + allKits.size() + "</white></gray>"),
                        MM.deserialize("<gray>Page: <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        // Light Gray glass pane (Empty kit slot example): Slot = [13] -> Index 12
        if (allKits.isEmpty()) {
            inventory.setItem(12, GuiItem.make(Material.LIGHT_GRAY_STAINED_GLASS_PANE, MM.deserialize("<gray>No Kits Created Yet</gray>")));
        }

        // Red Bundle (Close/Exit): Slot = [45] -> Index 44
        inventory.setItem(44, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<red><bold>✘ Close</bold></red>")));

        // Red glass pane: Slot = [47] -> Index 46
        if (page > 0) {
            inventory.setItem(46, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<red>« Previous Page</red>")));
        } else {
            inventory.setItem(46, blackPane);
        }

        // Book and Quill (Manage/Info): Slot = [50] -> Index 49
        inventory.setItem(49, GuiItem.make(Material.WRITABLE_BOOK, MM.deserialize("<aqua><bold>Manage Kits</bold></aqua>"),
                List.of(MM.deserialize("<gray>Use this menu to manage your kits.</gray>"))));

        // Lime glass pane: Slot = [53] -> Index 52
        if (to < allKits.size()) {
            inventory.setItem(52, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<green>Next Page »</green>")));
        } else {
            inventory.setItem(52, blackPane);
        }

        // Green Bundle (Next/Accept): Slot = [54] -> Index 53
        inventory.setItem(53, GuiItem.make(Material.LIME_BUNDLE, MM.deserialize("<green><bold>Refresh</bold></green>")));
    }

    // ── Events ─────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(player)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= INV_SIZE) return;

        // Pagination & Actions
        if (slot == 44) { player.closeInventory(); return; }
        if (slot == 46 && page > 0) { page--; refresh(); return; }
        if (slot == 52) {
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            if ((page + 1) * 27 < allKits.size()) { // 27 is current kitSlots.length
                page++; refresh();
            }
            return;
        }
        if (slot == 53) { refresh(); return; }

        // Kit click
        int[] kitSlots = {
            10, 11, 12, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };
        int kitIdx = -1;
        for (int i = 0; i < kitSlots.length; i++) {
            if (kitSlots[i] == slot) { kitIdx = i; break; }
        }

        if (kitIdx != -1) {
            List<Kit> kits = new ArrayList<>(plugin.getKitManager().getAllKits());
            int idx = page * kitSlots.length + kitIdx;
            if (idx >= kits.size()) return;
            Kit kit = kits.get(idx);

            if (event.isShiftClick()) {
                if (event.isLeftClick()) {
                    // Shift Left Click = Edit Kit
                    new KitEditorGui(plugin, player, kit, 0).open();
                } else if (event.isRightClick()) {
                    // Shift Right Click = Clone Kit
                    Kit cloned = plugin.getKitManager().cloneKit(kit.getPlainName(), kit.getPlainName() + " Copy");
                    player.sendMessage(MM.deserialize("<green>Kit cloned successfully!</green>"));
                    refresh();
                }
            } else if (event.getClick().name().contains("MIDDLE")) {
                if (event.isShiftClick()) {
                    // Shift Middle Mouse = Trigger Give or Giveall
                    triggerGivePrompt(kit);
                } else {
                    // Middle Mouse = Trigger Delete This Kit
                    new ConfirmDeleteGui(plugin, player, kit).open();
                }
            } else if (event.isLeftClick()) {
                // Left Click = Open
                new ViewingSpecificKitGui(plugin, player, kit).open();
            } else if (event.isRightClick()) {
                // Right Click = Give (Self)
                giveKit(player, kit);
                player.sendMessage(MM.deserialize("<green>Kit " + kit.getPlainName() + " given to you.</green>"));
            }
        }
    }

    private void giveKit(Player target, Kit kit) {
        for (ItemStack item : kit.getItems()) {
            if (item != null && item.getType() != Material.AIR) {
                Map<Integer, ItemStack> remaining = target.getInventory().addItem(item.clone());
                for (ItemStack rem : remaining.values()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), rem);
                }
            }
        }
    }

    private void triggerGivePrompt(Kit kit) {
        player.closeInventory();
        player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6><bold>Who Do You Want To Give This Kit To ?</bold></gradient>"));

        Component giveSingle = MM.deserialize("<green><bold>[ Give To Single Player ]</bold></green>")
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/anima internal-give-single \"" + kit.getPlainName() + "\""));

        Component giveAll = MM.deserialize("<aqua><bold>[ Give All ]</bold></aqua>")
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/anima internal-give-all \"" + kit.getPlainName() + "\""));

        player.sendMessage(giveSingle.append(Component.space()).append(giveAll));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getPlayer().equals(player)) return;
        plugin.getKitManager().unregisterBrowser(player);
        HandlerList.unregisterAll(this);
    }

    // ── Helpers ────────────────────────────────────────────────────

    private Material parseMaterial(String name) {
        try { return Material.valueOf(name.toUpperCase()); }
        catch (Exception e) { return Material.BLACK_STAINED_GLASS_PANE; }
    }
}
