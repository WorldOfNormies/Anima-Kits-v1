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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * AnimaKitsMainGUI – Admin Panel (/anima kits).
 *
 * Layout (54-slot, rows 1-5 = kits, row 6 = controls):
 *   45 = Red Bundle        → Close
 *   46 = Black pane (filler)
 *   47 = Red Glass Pane    → « Previous
 *   48 = Black pane
 *   49 = Eye of Ender      → List kits in chat
 *   50 = Black pane
 *   51 = Lime Glass Pane   → Next »
 *   52 = Black pane
 *   53 = Lime Bundle       → Save / Refresh
 *
 * Middle slot 49 also hosts Ender Chest for "Create Kit" on right-click.
 * Kit clicks:
 *   Left          → View kit contents
 *   Right         → Open editor
 *   Shift+Left    → Give / GiveAll chat buttons
 *   Middle / Drop → Delete confirm GUI
 *   Shift+Right   → Clone kit (chat input)
 */
public class AnimaKitsMainGUI implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private int page = 0;
    private Inventory inventory;

    private static final int KITS_PER_PAGE = 45;
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
            // ── fancy gradient lore ──
            lore.add(MM.deserialize(
                "<gradient:#FFD700:#FFA500>⬡ <bold>Left-click</bold></gradient><gray> → View Kit Contents</gray>"));
            lore.add(MM.deserialize(
                "<gradient:#4FC3F7:#1565C0>✎ <bold>Right-click</bold></gradient><gray> → Open Kit Editor</gray>"));
            lore.add(MM.deserialize(
                "<gradient:#A5D6A7:#2E7D32>✦ <bold>Shift + Left</bold></gradient><gray> → Give / Give All</gray>"));
            lore.add(MM.deserialize(
                "<gradient:#EF9A9A:#B71C1C>✘ <bold>Shift + Q / Middle</bold></gradient><gray> → Delete Kit</gray>"));
            lore.add(MM.deserialize(
                "<gradient:#CE93D8:#6A1B9A>⎘ <bold>Shift + Right</bold></gradient><gray> → Clone Kit</gray>"));
            lore.add(Component.empty());
            lore.add(MM.deserialize("<dark_gray>Cooldown: <gray>" +
                    (kit.getCooldown() == 0 ? "<green>None" : "<yellow>" + kit.getCooldown() + "s") + "</gray>"));
            lore.add(MM.deserialize("<dark_gray>Single Claim: <gray>" +
                    (kit.isSingleClaim() ? "<red>Yes" : "<green>No") + "</gray>"));

            ItemStack icon = GuiItem.make(kit.getIconMaterial(), name, lore);
            inventory.setItem(i, icon);
        }

        // Bottom border
        for (int i = KITS_PER_PAGE; i < INV_SIZE; i++) {
            inventory.setItem(i, GuiItem.border(Material.PURPLE_STAINED_GLASS_PANE));
        }

        // slot 45 – Red Bundle = Close
        inventory.setItem(45, GuiItem.make(Material.RED_BUNDLE,
                MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Close Menu</bold></gradient>"),
                List.of(MM.deserialize("<gray>Close the Anima Kits panel.</gray>"))));

        // slot 47 – Previous
        inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE,
                MM.deserialize("<gradient:#FF6060:#CC0000><bold>« Previous Page</bold></gradient>"),
                List.of(MM.deserialize("<gray>Page <white>" + page + "/" + totalPages + "</white></gray>"))));

        // slot 49 – Eye of Ender = List + Ender Chest = create new kit
        inventory.setItem(49, GuiItem.make(Material.ENDER_CHEST,
                MM.deserialize("<gradient:#CC88FF:#6600CC><bold>✦ Kit Management</bold></gradient>"),
                List.of(
                    MM.deserialize("<gradient:#FFD700:#FFA500>⬡ <bold>Left-click</bold></gradient><gray> → List all kits in chat</gray>"),
                    MM.deserialize("<gradient:#A5D6A7:#2E7D32>✦ <bold>Right-click</bold></gradient><gray> → Create new kit</gray>"),
                    Component.empty(),
                    MM.deserialize("<dark_gray>Total Kits: <white>" + allKits.size() + "</white>"),
                    MM.deserialize("<dark_gray>Page: <white>" + (page + 1) + "/" + totalPages + "</white>")
                )));

        // slot 51 – Next
        inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE,
                MM.deserialize("<gradient:#44FF88:#00AA44><bold>Next Page »</bold></gradient>"),
                List.of(MM.deserialize("<gray>Page <white>" + (page + 2) + "/" + totalPages + "</white></gray>"))));

        // slot 53 – Lime Bundle = Save/Refresh
        inventory.setItem(53, GuiItem.make(Material.LIME_BUNDLE,
                MM.deserialize("<gradient:#44FF88:#00CC55><bold>✔ Save & Refresh</bold></gradient>"),
                List.of(MM.deserialize("<gray>Save all kits and refresh display.</gray>"))));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(player)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= INV_SIZE) return;

        if (slot == 45) {
            player.closeInventory();
            return;
        }
        if (slot == 47) {
            if (page > 0) { page--; refresh(); }
            return;
        }
        if (slot == 49) {
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            if (event.getClick() == ClickType.LEFT) {
                // List all kits in chat
                player.closeInventory();
                player.sendMessage(MM.deserialize(
                    "<gradient:#AA00FF:#FF6AFF><bold>⋆ Anima Kits – All Kits ⋆</bold></gradient>"));
                for (int idx = 0; idx < allKits.size(); idx++) {
                    Kit k = allKits.get(idx);
                    long cd = plugin.getPlayerManager().getRemainingCooldown(player.getUniqueId(), k.getId());
                    String cdStr = cd > 0 ? formatTime(cd) : "Ready";
                    player.sendMessage(MM.deserialize(
                        "<gray>#" + (idx + 1) + " </gray>"
                        + "<gradient:#FFD700:#FFA500><bold>" + k.getPlainName() + "</bold></gradient>"
                        + " <dark_gray>| CD: <white>" + cdStr + "</white>"
                        + " | SC: <white>" + k.isSingleClaim() + "</white></dark_gray>")
                        .clickEvent(ClickEvent.runCommand("/anima kits")));
                }
            } else if (event.getClick() == ClickType.RIGHT) {
                // Create new kit with default name
                String defaultName = "New Kit";
                int counter = 1;
                while (plugin.getKitManager().kitExists(defaultName)) {
                    defaultName = "New Kit " + counter++;
                }
                plugin.getKitManager().createKit(defaultName);
                plugin.getKitManager().saveKits();
                refresh();
                player.sendMessage(MM.deserialize(
                    "<gradient:#44FF88:#00CC55>✔ Created new kit: <white>" + defaultName + "</white></gradient>"));
            }
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

        if (slot < KITS_PER_PAGE) {
            List<Kit> kits = new ArrayList<>(plugin.getKitManager().getAllKits());
            int idx = page * KITS_PER_PAGE + slot;
            if (idx >= kits.size()) return;
            Kit kit = kits.get(idx);

            ClickType click = event.getClick();

            if (click == ClickType.LEFT) {
                new AnimaClaimKits(plugin, player, kit, true).open();
            } else if (click == ClickType.RIGHT) {
                new AnimaKitsEditor(plugin, player, kit, 0).open();
            } else if (click == ClickType.SHIFT_LEFT) {
                player.closeInventory();
                Component msg = MM.deserialize(
                        "<gradient:#54DAF4:#545EB6><bold>Kit: " + kit.getPlainName() + "</bold></gradient>\n")
                        .append(MM.deserialize("<yellow>⬡ [Give to Player]</yellow> ")
                                .clickEvent(ClickEvent.suggestCommand(
                                        "/anima kits give <player> \"" + kit.getPlainName() + "\" 1")))
                        .append(MM.deserialize("<green>✦ [Give All]</green>")
                                .clickEvent(ClickEvent.suggestCommand(
                                        "/anima kits giveall \"" + kit.getPlainName() + "\" 1")));
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
                                player.sendMessage(MM.deserialize(
                                    "<gradient:#44FF88:#00CC55>✔ Kit cloned as <white>" + newName + "</white>!</gradient>"));
                            } else {
                                player.sendMessage(MM.deserialize("<red>✘ Failed to clone kit.</red>"));
                            }
                            new AnimaKitsMainGUI(plugin, player).open();
                        },
                        () -> new AnimaKitsMainGUI(plugin, player).open()
                ).await();
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

    private String formatTime(long seconds) {
        if (seconds <= 0) return "Ready";
        long h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }
}