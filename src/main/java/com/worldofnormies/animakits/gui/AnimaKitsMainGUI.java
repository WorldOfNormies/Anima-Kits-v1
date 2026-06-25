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
 * AnimaKitsMainGUI – Admin Panel (opens with /anima kits).
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
        Component title = MM.deserialize("<gradient:#FF3030:#FFFFFF:#3060FF><bold>⋆༺⸸ Anima Kits ⸸༻⋆</bold></gradient>");
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
            lore.add(MM.deserialize("<yellow>Shift + Left-click</yellow> <gray>→ Give / GiveAll</gray>"));
            lore.add(MM.deserialize("<yellow>Shift + Q/Middle</yellow> <gray>→ Delete kit</gray>"));
            lore.add(MM.deserialize("<yellow>Shift + Right-click</yellow> <gray>→ Clone kit</gray>"));

            ItemStack icon = GuiItem.make(kit.getIconMaterial(), name, lore);
            inventory.setItem(i, icon);
        }

        // Bottom border
        Material borderMat = Material.BLACK_STAINED_GLASS_PANE;
        for (int i = KITS_PER_PAGE; i < INV_SIZE; i++) {
            inventory.setItem(i, GuiItem.border(borderMat));
        }

        inventory.setItem(45, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<red><bold>✘ Close</bold></red>")));

        inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<red><bold>« Previous</bold></red>")));

        inventory.setItem(49, GuiItem.make(Material.ENDER_CHEST, MM.deserialize("<light_purple><bold>Kit Statistics</bold></light_purple>"),
                List.of(MM.deserialize("<gray>Total Kits: <white>" + allKits.size() + "</white></gray>"),
                        MM.deserialize("<gray>Page: <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<green><bold>Next »</bold></green>")));

        inventory.setItem(53, GuiItem.make(Material.LIME_BUNDLE, MM.deserialize("<green><bold>Refresh</bold></green>")));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(player)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= INV_SIZE) return;

        if (slot == 45) { player.closeInventory(); return; }
        if (slot == 47) {
            if (page > 0) {
                page--; refresh();
            }
            return;
        }
        if (slot == 51) {
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            if ((page + 1) * KITS_PER_PAGE < allKits.size()) {
                page++; refresh();
            }
            return;
        }
        if (slot == 53) { refresh(); return; }

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
                Component msg = MM.deserialize("<gradient:#54DAF4:#545EB6><bold>Kit: " + kit.getPlainName() + "</bold></gradient>\n")
                        .append(MM.deserialize("<yellow>[Give to Player]</yellow> ").clickEvent(ClickEvent.suggestCommand("/anima kits give <player> \"" + kit.getPlainName() + "\" 1")))
                        .append(MM.deserialize("<green>[Give All]</green>").clickEvent(ClickEvent.suggestCommand("/anima kits giveall \"" + kit.getPlainName() + "\" 1")));
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
                                player.sendMessage(MM.deserialize("<green>Kit cloned as <white>" + newName + "</white>!</green>"));
                            } else {
                                player.sendMessage(MM.deserialize("<red>Failed to clone kit.</red>"));
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
}
