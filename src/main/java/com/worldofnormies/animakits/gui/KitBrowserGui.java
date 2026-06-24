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

    private static final int INV_SIZE = 54;

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
        // Red (#FF0000) -> Light Gray (#D3D3D3) -> Blue (#0000FF) bold title
        Component title = MM.deserialize("<gradient:#FF0000:#D3D3D3:#0000FF><bold>Anima Kits</bold></gradient>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
    }

    private void populate() {
        inventory.clear();
        List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());

        // Available slots within inside grid layout (rows 1-4, skipping side borders)
        int[] kitSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int totalPages = Math.max(1, (int) Math.ceil(allKits.size() / (double) kitSlots.length));
        page = Math.min(page, totalPages - 1);

        int from = page * kitSlots.length;
        int to   = Math.min(from + kitSlots.length, allKits.size());
        List<Kit> pageKits = allKits.subList(from, to);

        // --- BACKGROUND DECORATION ---
        ItemStack blackPane = GuiItem.border(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack whitePane = GuiItem.border(Material.WHITE_STAINED_GLASS_PANE);

        // Top Row (0-8, except slot 4 which holds the Ender Chest)
        for (int s : new int[]{0, 1, 2, 3, 5, 6, 7, 8}) {
            inventory.setItem(s, blackPane);
        }

        // Side Borders (White Glass)
        for (int s : new int[]{9, 18, 27, 36, 17, 26, 35, 44}) {
            inventory.setItem(s, whitePane);
        }

        // Bottom Row Fillers
        for (int s : new int[]{46, 48, 49, 50, 52}) {
            inventory.setItem(s, blackPane);
        }

        // --- KIT ICONS POPULATION ---
        for (int i = 0; i < pageKits.size(); i++) {
            Kit kit = pageKits.get(i);
            Component name = ColorUtil.parse(kit.getRawName());
            List<Component> lore = new ArrayList<>();

            // Group 1: Open / Edit
            lore.add(MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold>Left Click</bold></gradient> <gray><bold>→</bold> Open</gray>"));
            lore.add(MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold>Shift Left Click</bold></gradient> <gray><bold>→</bold> Edit Kit</gray>"));
            
            // Separation Break
            lore.add(Component.empty());

            // Group 2: Give / Clone
            lore.add(MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold>Right Click</bold></gradient> <gray><bold>→</bold> Give (Self)</gray>"));
            lore.add(MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold>Shift Right Click</bold></gradient> <gray><bold>→</bold> Clone Kit</gray>"));
            
            // Separation Break
            lore.add(Component.empty());

            // Group 3: Delete / Admin Give Actions
            lore.add(MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold>Middle Mouse</bold></gradient> <gray><bold>→</bold> Delete This Kit</gray>"));
            lore.add(MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold>Shift Middle Mouse</bold></gradient> <gray><bold>→</bold> Give or Giveall</gray>"));

            ItemStack icon = GuiItem.make(kit.getIconMaterial(), name, lore);
            inventory.setItem(kitSlots[i], icon);
        }

        // --- INTERACTIVE BUTTONS & SPECIAL ITEMS ---

        // Slot 4: Ender Chest (Kit Statistics)
        inventory.setItem(4, GuiItem.make(Material.ENDER_CHEST, MM.deserialize("<light_purple><bold>Kit Statistics</bold></light_purple>"),
                List.of(MM.deserialize("<gray>Total Kits: <white>" + allKits.size() + "</white></gray>"),
                        MM.deserialize("<gray>Page: <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        // Empty Status Handling Slot
        if (allKits.isEmpty()) {
            inventory.setItem(13, GuiItem.make(Material.LIGHT_GRAY_STAINED_GLASS_PANE, MM.deserialize("<gray>No Kits Created Yet</gray>")));
        }

        // Slot 45: Return/Close Button (Dark blood red to red bold gradient)
        inventory.setItem(45, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<gradient:#8B0000:#FF0000><bold>✘ Close</bold></gradient>")));

        // Slot 47: Previous Page (Dark blood red to orange bold gradient)
        if (page > 0) {
            inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<gradient:#8B0000:#FF8C00><bold>« Previous Page</bold></gradient>")));
        } else {
            inventory.setItem(47, blackPane);
        }

        // Slot 51: Next Page (Lime green to yellowish-green bold gradient)
        if (to < allKits.size()) {
            inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<gradient:#32CD32:#ADFF2F><bold>Next Page »</bold></gradient>")));
        } else {
            inventory.setItem(51, blackPane);
        }

        // Slot 53: Refresh Button (Replaced by Black Pane per instruction)
        inventory.setItem(53, blackPane);
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
            int[] kitSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
            };
            if ((page + 1) * kitSlots.length < allKits.size()) {
                page++; refresh();
            }
            return;
        }
        if (slot == 53) { refresh(); return; }

        // Kit grid detection map
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
            List<Kit> kits = new ArrayList<>(plugin.getKitManager().getAllKits());
            int idx = page * kitSlots.length + kitIdx;
            if (idx >= kits.size()) return;
            Kit kit = kits.get(idx);

            if (event.isShiftClick()) {
                if (event.isLeftClick()) {
                    new KitEditorGui(plugin, player, kit, 0).open();
                } else if (event.isRightClick()) {
                    Kit cloned = plugin.getKitManager().cloneKit(kit.getPlainName(), kit.getPlainName() + " Copy");
                    player.sendMessage(MM.deserialize("<green>Kit cloned successfully!</green>"));
                    refresh();
                }
            } else if (event.getClick().name().contains("MIDDLE")) {
                if (event.isShiftClick()) {
                    triggerGivePrompt(kit);
                } else {
                    new ConfirmDeleteGui(plugin, player, kit).open();
                }
            } else if (event.isLeftClick()) {
                new ViewingSpecificKitGui(plugin, player, kit).open();
            } else if (event.isRightClick()) {
                giveKit(player, kit);
                
                Component prefix = MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold>Kit </bold></gradient>");
                Component kitName = ColorUtil.parse(kit.getRawName()).toBuilder().bold(true).build();
                Component suffix = MM.deserialize("<gradient:#301934:#8A2BE2:#FFFFE0><bold> given to you.</bold></gradient>");
                player.sendMessage(prefix.append(kitName).append(suffix));
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
}
