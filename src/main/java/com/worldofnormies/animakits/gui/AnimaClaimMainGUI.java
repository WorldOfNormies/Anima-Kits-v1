package com.worldofnormies.animakits.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.kit.Kit;
import com.worldofnormies.animakits.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AnimaClaimMainGUI – Player Claim Menu.
 *
 * Sorting: Claimable first, then Black Stained Glass Pane separator, then Unclaimable.
 * Glow: Only claimable kits.
 *
 * Slots:
 * 47 = RED glass staned pane   → « Previous
 * 51 = lime GREEN stained glas panes  → Next »
 */
public class AnimaClaimMainGUI implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE = 54;

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private Inventory inventory;
    private int page = 0;

    private final int[] kitSlots = {
        0, 1, 2, 3, 4, 5, 6, 7, 8,
        9, 10, 11, 12, 13, 14, 15, 16, 17,
        18, 19, 20, 21, 22, 23, 24, 25, 26,
        27, 28, 29, 30, 31, 32, 33, 34, 35,
        36, 37, 38, 39, 40, 41, 42, 43
    };

    public AnimaClaimMainGUI(AnimaKitsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Component title = MM.deserialize("<gradient:#FF3030:#FFFFFF:#3060FF><bold>⋆༺⸸ Claim Kits  ⸸༻⋆</bold></gradient>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();
        UUID uuid = player.getUniqueId();

        List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());

        List<Kit> claimable = allKits.stream()
                .filter(k -> player.hasPermission("anima.kits.claim." + k.getPlainName()))
                .sorted(Comparator.comparing(Kit::getPlainName))
                .collect(Collectors.toList());

        List<Kit> unclaimable = allKits.stream()
                .filter(k -> !player.hasPermission("anima.kits.claim." + k.getPlainName()))
                .sorted(Comparator.comparing(Kit::getPlainName))
                .collect(Collectors.toList());

        List<Object> combined = new ArrayList<>(claimable);

        // Separator
        ItemStack separator = GuiItem.make(Material.BLACK_STAINED_GLASS_PANE,
                MM.deserialize("<dark_gray>Separator</dark_gray>"),
                List.of(MM.deserialize("<gray>left you can claim and right you cannot claim</gray>")));
        combined.add(separator);
        combined.addAll(unclaimable);

        int totalPages = Math.max(1, (int) Math.ceil(combined.size() / (double) kitSlots.length));
        page = Math.min(page, totalPages - 1);

        int from = page * kitSlots.length;
        int to = Math.min(from + kitSlots.length, combined.size());

        for (int i = 0; i < (to - from); i++) {
            Object obj = combined.get(from + i);
            if (obj instanceof Kit kit) {
                boolean canClaim = player.hasPermission("anima.kits.claim." + kit.getPlainName());
                List<Component> lore = new ArrayList<>();
                for (String line : kit.getLore()) {
                    lore.add(MM.deserialize(line));
                }
                lore.add(Component.empty());
                lore.add(MM.deserialize("<green>Left-click → View kit</green>"));
                lore.add(MM.deserialize("<gold>Right-click → Claim kit</gold>"));

                Component kitTitle = ColorUtil.parse(kit.getRawName()).decorate(TextDecoration.BOLD);
                inventory.setItem(kitSlots[i], GuiItem.make(kit.getIconMaterial(), kitTitle, lore, canClaim));
            } else if (obj instanceof ItemStack stack) {
                inventory.setItem(kitSlots[i], stack);
            }
        }

        ItemStack blackPane = GuiItem.border(Material.BLACK_STAINED_GLASS_PANE);
        for (int s = 44; s < 54; s++) {
            if (inventory.getItem(s) == null) inventory.setItem(s, blackPane);
        }

        inventory.setItem(44, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<red><bold>✘ Exit</bold></red>")));

        // 47 = RED glass staned pane   → « Previous
        // 51 = lime GREEN stained glas panes  → Next »
        inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<red><bold>« Previous</bold></red>")));
        inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<green><bold>Next »</bold></green>")));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= INV_SIZE) return;

        if (slot == 44) {
            player.closeInventory();
            return;
        }

        if (slot == 47 && page > 0) {
            page--;
            populate();
            return;
        }

        if (slot == 51) {
            // Re-calculate combined size
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            if ((page + 1) * kitSlots.length < (allKits.size() + 1)) {
                page++;
                populate();
            }
            return;
        }

        int slotIndex = -1;
        for (int i = 0; i < kitSlots.length; i++) {
            if (kitSlots[i] == slot) {
                slotIndex = i;
                break;
            }
        }

        if (slotIndex != -1) {
            int combinedIndex = (page * kitSlots.length) + slotIndex;

            // Re-generate list for index lookup
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            List<Kit> claimable = allKits.stream()
                .filter(k -> player.hasPermission("anima.kits.claim." + k.getPlainName()))
                .sorted(Comparator.comparing(Kit::getPlainName))
                .collect(Collectors.toList());

            List<Kit> unclaimable = allKits.stream()
                .filter(k -> !player.hasPermission("anima.kits.claim." + k.getPlainName()))
                .sorted(Comparator.comparing(Kit::getPlainName))
                .collect(Collectors.toList());

            List<Object> combined = new ArrayList<>(claimable);
            combined.add(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
            combined.addAll(unclaimable);

            if (combinedIndex >= combined.size()) return;
            Object obj = combined.get(combinedIndex);
            if (!(obj instanceof Kit kit)) return;

            ClickType click = event.getClick();
            if (click == ClickType.LEFT) {
                new AnimaClaimKits(plugin, player, kit, false).open();
            } else if (click == ClickType.RIGHT) {
                player.performCommand("anima kits claim " + kit.getPlainName());
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}
