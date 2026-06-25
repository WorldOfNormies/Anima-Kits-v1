package com.worldofnormies.animakits.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.kit.Kit;
import com.worldofnormies.animakits.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * AnimaKitsMainGUI – Admin Panel (/anima kits) matching layout from image_3f280d.png.
 */
public class AnimaKitsMainGUI implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private int page = 0;
    private Inventory inventory;

    private static final int[] CONTENT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    private static final int KITS_PER_PAGE = CONTENT_SLOTS.length; // 28
    private static final int INV_SIZE = 54;

    public AnimaKitsMainGUI(AnimaKitsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        build();
        plugin.getKitManager().registerBrowser(player, this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    public void refresh() {
        if (inventory == null) return;
        inventory.clear();
        populate();
    }

    private void build() {
        Component title = MM.deserialize(
                "<gradient:#AA00FF:#FF6AFF:#FFFFFF:#FF6AFF:#AA00FF><bold>⋆༺⸸ Anima Kits ⸸༻⋆</bold></gradient>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
    }

    private void populate() {
        List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
        int totalPages = Math.max(1, (int) Math.ceil(allKits.size() / (double) KITS_PER_PAGE));
        page = Math.min(page, totalPages - 1);

        // ── 1. Frame Setup matching image_3f280d.png (Purple Glass Borders) ──
        Material[] borderPattern = {
            Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE, Material.AIR, Material.BLUE_STAINED_GLASS_PANE, Material.AIR, Material.BLUE_STAINED_GLASS_PANE, Material.AIR, BLUE_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.BLUE_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.BLUE_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.AIR, Material.BLUE_STAINED_GLASS_PANE, Material.AIR, Material.BLUE_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE, Material.AIR, Material.BLUE_STAINED_GLASS_PANE, Material.AIR
        };

        for (int i = 0; i < INV_SIZE; i++) {
            if (borderPattern[i] != Material.AIR) {
                inventory.setItem(i, makePane(borderPattern[i]));
            }
        }

        // ── 2. Populate Page Kits Into Bounded Window Content Matrix ──
        int from = page * KITS_PER_PAGE;
        int to   = Math.min(from + KITS_PER_PAGE, allKits.size());
        List<Kit> pageKits = allKits.subList(from, to);

        for (int i = 0; i < pageKits.size(); i++) {
            Kit kit = pageKits.get(i);
            Component name = ColorUtil.parse(kit.getRawName());
            List<Component> lore = new ArrayList<>();
            for (String l : kit.getLore()) lore.add(ColorUtil.parse(l));
            lore.add(Component.empty());
            lore.add(MM.deserialize("<gradient:#FFD700:#FFA500>⬡ <bold>Left-click</bold></gradient><gray> → View Kit Contents</gray>"));
            lore.add(MM.deserialize("<gradient:#4FC3F7:#1565C0>✎ <bold>Right-click</bold></gradient><gray> → Open Kit Editor</gray>"));
            lore.add(MM.deserialize("<gradient:#A5D6A7:#2E7D32>✦ <bold>Shift + Left</bold></gradient><gray> → Give / Give All</gray>"));
            lore.add(MM.deserialize("<gradient:#EF9A9A:#B71C1C>✘ <bold>Shift + Q / Middle</bold></gradient><gray> → Delete Kit</gray>"));
            lore.add(MM.deserialize("<gradient:#CE93D8:#6A1B9A>⎘ <bold>Shift + Right</bold></gradient><gray> → Clone Kit</gray>"));
            lore.add(Component.empty());
            lore.add(MM.deserialize("<dark_gray>Cooldown: <gray>" + (kit.getCooldown() == 0 ? "<green>None" : "<yellow>" + kit.getCooldown() + "s") + "</gray>"));
            lore.add(MM.deserialize("<dark_gray>Single Claim: <gray>" + (kit.isSingleClaim() ? "<red>Yes" : "<green>No") + "</gray>"));

            ItemStack icon = GuiItem.make(kit.getIconMaterial(), name, lore);
            inventory.setItem(CONTENT_SLOTS[i], icon);
        }

        // ── 3. Top Row Interactive Operational Elements ──

        // Slot 2 – Eye of Ender = Lists all kits and timer status overview in chat
        List<Component> eyeLore = new ArrayList<>();
        eyeLore.add(MM.deserialize("<gradient:#54DAF4:#545EB6><bold>── Kit Overview Registry ──</bold></gradient>"));
        eyeLore.add(Component.empty());
        eyeLore.add(MM.deserialize("<gradient:#FFD700:#FFA500>⬡ <bold>Left-click</bold></gradient><gray> → List all kits & timers in chat</gray>"));
        inventory.setItem(2, GuiItem.make(Material.ENDER_EYE,
                MM.deserialize("<gradient:#00FFCC:#0099AA><bold>👁 Global Kits Registry</bold></gradient>"), eyeLore));

        // Slot 4 – Ender Chest = Overall configuration totals, statistics, pages info & creation engine
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(MM.deserialize("<gradient:#CC88FF:#6600CC><bold>✦ System Administration Overview</bold></gradient>"));
        infoLore.add(Component.empty());
        infoLore.add(MM.deserialize("<dark_gray>Total Kits Configured: <white>" + allKits.size() + "</white>"));
        infoLore.add(MM.deserialize("<dark_gray>GUI Active Pages: <white>" + (page + 1) + "/" + totalPages + "</white>"));
        infoLore.add(Component.empty());
        infoLore.add(MM.deserialize("<gradient:#A5D6A7:#2E7D32>✦ <bold>Right-click</bold></gradient><gray> → Create new empty kit</gray>"));
        inventory.setItem(4, GuiItem.make(Material.ENDER_CHEST,
                MM.deserialize("<gradient:#CC88FF:#6600CC><bold>✦ Kit Performance Statistics</bold></gradient>"), infoLore));

        // Slot 7 – Bookshelf = Static menu UI Structuring info layout
        inventory.setItem(7, GuiItem.make(Material.BOOKSHELF,
                MM.deserialize("<gradient:#F5B041:#DC7633><bold>🕮 System Organizer Framework</bold></gradient>"),
                List.of(MM.deserialize("<gray>Standardized configuration interface grid template.</gray>"))));

        // ── 4. Bottom Controls Matrix ──
        inventory.setItem(45, GuiItem.make(Material.RED_BUNDLE,
                MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Close Menu</bold></gradient>"),
                List.of(MM.deserialize("<gray>Close the Anima Kits panel.</gray>"))));

        inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE,
                MM.deserialize("<gradient:#FF6060:#CC0000><bold>« Previous Page</bold></gradient>"),
                List.of(MM.deserialize("<gray>Page <white>" + page + "/" + totalPages + "</white></gray>"))));

        inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE,
                MM.deserialize("<gradient:#44FF88:#00AA44><bold>Next Page »</bold></gradient>"),
                List.of(MM.deserialize("<gray>Page <white>" + (page + 2) + "/" + totalPages + "</white></gray>"))));

        inventory.setItem(53, GuiItem.make(Material.LIME_BUNDLE,
                MM.deserialize("<gradient:#44FF88:#00CC55><bold>✔ Save & Refresh</bold></gradient>"),
                List.of(MM.deserialize("<gray>Save all kits and refresh display.</gray>"))));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(player)) return;

        int slot = event.getRawSlot();
        if (slot < 0) return;

        if (slot < INV_SIZE) {
            event.setCancelled(true);
        } else {
            if (event.getClick().isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }

        if (slot == 45) {
            player.closeInventory();
            return;
        }
        if (slot == 47) {
            if (page > 0) { page--; refresh(); }
            return;
        }
        if (slot == 51) {
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            if ((page + 1) * KITS_PER_PAGE < allKits.size()) { page++; refresh(); }
            return;
        }
        if (slot == 53) {
            plugin.getKitManager().saveKits();
            refresh();
            return;
        }

        // Slot 2: Eye of Ender logic (List all kits and timers into chat)
        if (slot == 2 && event.getClick() == ClickType.LEFT) {
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            player.closeInventory();
            player.sendMessage(MM.deserialize("<gradient:#AA00FF:#FF6AFF><bold>⋆ Anima Kits – All Kits & Timers Registry ⋆</bold></gradient>"));
            for (int idx = 0; idx < allKits.size(); idx++) {
                Kit k = allKits.get(idx);
                long cd = plugin.getPlayerManager().getRemainingCooldown(player.getUniqueId(), k.getId());
                String cdStr = cd > 0 ? formatTime(cd) : "Ready";
                player.sendMessage(MM.deserialize(
                    "<gray>#" + (idx + 1) + " </gray>"
                    + "<gradient:#FFD700:#FFA500><bold>" + k.getPlainName() + "</bold></gradient>"
                    + " <dark_gray>| CD Status: <white>" + cdStr + "</white>"
                    + " | Configured CD: <white>" + k.getCooldown() + "s</white>"
                    + " | Single Claim: <white>" + k.isSingleClaim() + "</white></dark_gray>")
                    .clickEvent(ClickEvent.runCommand("/anima kits")));
            }
            return;
        }

        // Slot 4: Ender Chest logic (Right-click handles new kit generation engine)
        if (slot == 4 && event.getClick() == ClickType.RIGHT) {
            String defaultName = "New Kit";
            int counter = 1;
            while (plugin.getKitManager().kitExists(defaultName)) {
                defaultName = "New Kit " + counter++;
            }
            plugin.getKitManager().createKit(defaultName);
            plugin.getKitManager().saveKits();
            refresh();
            player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Created new kit: <white>" + defaultName + "</white></gradient>"));
            return;
        }

        // Kit Object Selections Inside Content Window Matrix
        for (int i = 0; i < CONTENT_SLOTS.length; i++) {
            if (slot == CONTENT_SLOTS[i]) {
                List<Kit> kits = new ArrayList<>(plugin.getKitManager().getAllKits());
                int idx = page * KITS_PER_PAGE + i;
                if (idx >= kits.size()) return;
                Kit kit = kits.get(idx);

                ClickType click = event.getClick();

                if (click == ClickType.LEFT) {
                    new AnimaClaimKits(plugin, player, kit, true).open();
                } else if (click == ClickType.RIGHT) {
                    new AnimaKitsEditor(plugin, player, kit, 0).open();
                } else if (click == ClickType.SHIFT_LEFT) {
                    player.closeInventory();
                    Component msg = MM.deserialize("<gradient:#54DAF4:#545EB6><bold>Kit: " + kit.getPlainName() + "</bold></gradient>\n")
                            .append(MM.deserialize("<yellow>⬡ [Give to Player]</yellow> ")
                                    .clickEvent(ClickEvent.suggestCommand("/anima kits give <player> \"" + kit.getPlainName() + "\" 1")))
                            .append(MM.deserialize("<green>✦ [Give All]</green>")
                                    .clickEvent(ClickEvent.suggestCommand("/anima kits giveall \"" + kit.getPlainName() + "\" 1")));
                    player.sendMessage(msg);
                } else if (click == ClickType.MIDDLE || (event.getClick() == ClickType.DROP && event.isShiftClick())) {
                    new ConfirmDeleteGui(plugin, player, kit).open();
                } else if (click == ClickType.SHIFT_RIGHT) {
                    player.closeInventory();
                    ChatInputSession.sendClonePrompt(plugin, player, kit);
                    new ChatInputSession(plugin, player,
                            newName -> {
                                Kit clone = plugin.getKitManager().cloneKit(kit.getPlainName(), newName);
                                if (clone != null) {
                                    player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Kit cloned as <white>" + newName + "</white>!</gradient>"));
                                } else {
                                    player.sendMessage(MM.deserialize("<red>✘ Failed to clone kit.</red>"));
                                }
                                new AnimaKitsMainGUI(plugin, player).open();
                            },
                            () -> new AnimaKitsMainGUI(plugin, player).open()
                    ).await();
                }
                break;
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getPlayer().equals(player)) return;
        plugin.getKitManager().unregisterBrowser(player);
        HandlerList.unregisterAll(this);
    }

    private String formatTime(long seconds) {
        if (seconds <= 0) return "Ready";
        long h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    private ItemStack makePane(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.displayName(Component.space()); item.setItemMeta(meta); }
        return item;
    }
}
