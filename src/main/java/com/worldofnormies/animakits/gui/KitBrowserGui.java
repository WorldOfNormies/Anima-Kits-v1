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
        String titleRaw = plugin.getConfig().getString(
                "gui.main-title", "<gradient:#54DAF4:#545EB6><bold>✦ Kit Browser ✦</bold></gradient>");
        Component title = MM.deserialize(titleRaw);

        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
    }

    private void populate() {
        List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
        int totalPages = Math.max(1, (int) Math.ceil(allKits.size() / (double) KITS_PER_PAGE));
        page = Math.min(page, totalPages - 1);

        int from = page * KITS_PER_PAGE;
        int to   = Math.min(from + KITS_PER_PAGE, allKits.size());
        List<Kit> pageKits = allKits.subList(from, to);

        // Kit icons
        for (int i = 0; i < pageKits.size(); i++) {
            Kit kit = pageKits.get(i);
            Component name = ColorUtil.parse(kit.getRawName());
            List<Component> lore = new ArrayList<>();
            for (String l : kit.getLore()) lore.add(ColorUtil.parse(l));
            lore.add(Component.empty());
            lore.add(MM.deserialize("<yellow>Left-click</yellow> <gray>→ view kit</gray>"));
            lore.add(MM.deserialize("<yellow>Right-click</yellow> <gray>→ open editor</gray>"));
            ItemStack icon = GuiItem.make(kit.getIconMaterial(), name, lore);
            inventory.setItem(i, icon);
        }

        // Bottom border
        Material borderMat = parseMaterial(
                plugin.getConfig().getString("gui.border-material", "BLACK_STAINED_GLASS_PANE"));
        for (int i = KITS_PER_PAGE; i < INV_SIZE; i++) {
            inventory.setItem(i, GuiItem.border(borderMat));
        }

        // Layout from image: 45: Red Bundle, 47: Red Pane, 49: Ender Chest, 51: Lime Pane, 53: Green Bundle

        inventory.setItem(45, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<red><bold>✘ Close</bold></red>")));

        if (page > 0) {
            inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<red>« Previous Page</red>")));
        }

        inventory.setItem(49, GuiItem.make(Material.ENDER_CHEST, MM.deserialize("<light_purple><bold>Kit Statistics</bold></light_purple>"),
                List.of(MM.deserialize("<gray>Total Kits: <white>" + allKits.size() + "</white></gray>"),
                        MM.deserialize("<gray>Page: <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        if (to < allKits.size()) {
            inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<green>Next Page »</green>")));
        }

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
        if (slot == 45) { player.closeInventory(); return; }
        if (slot == 47 && page > 0) { page--; refresh(); return; }
        if (slot == 51) {
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            if ((page + 1) * KITS_PER_PAGE < allKits.size()) {
                page++; refresh();
            }
            return;
        }
        if (slot == 53) { refresh(); return; }

        // Kit click
        if (slot < KITS_PER_PAGE) {
            List<Kit> kits = new ArrayList<>(plugin.getKitManager().getAllKits());
            int idx = page * KITS_PER_PAGE + slot;
            if (idx >= kits.size()) return;
            Kit kit = kits.get(idx);

            boolean rightClick = event.isRightClick();
            if (rightClick && player.hasPermission("anima.kits.open")) {
                // Open editor
                KitEditorGui editor = new KitEditorGui(plugin, player, kit, 0);
                editor.open();
            } else if (player.hasPermission("anima.kits.display")) {
                // Open display (read-only)
                KitDisplayGui display = new KitDisplayGui(plugin, player, kit);
                display.open();
            }
        }
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
