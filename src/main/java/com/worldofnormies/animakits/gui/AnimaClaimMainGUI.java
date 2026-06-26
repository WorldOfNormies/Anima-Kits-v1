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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AnimaClaimMainGUI – Player-facing kit claim browser matching All Claim Kit GUI.png layout.
 */
public class AnimaClaimMainGUI implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int INV_SIZE   = 54;

    // Center area empty slots where kits go (28 slots total)
    private static final int[] KIT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    private final AnimaKitsPlugin plugin;
    private final Player          player;
    private Inventory             inventory;
    private int                   page       = 0;
    private int                   filterMode = 0; // 0=all, 1=can-claim, 2=cant-claim

    public AnimaClaimMainGUI(AnimaKitsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        // Centered bold reddish-orange to gold gradient title
        Component title = MM.deserialize(
            "        <gradient:#FF4500:#FFD700><bold>Claim GUI Menu</bold></gradient>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();
        UUID uuid = player.getUniqueId();

        // ── 1. Setup Static Border Background Pattern ──
        Material[] borderPattern = {
            Material.YELLOW_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE,
            Material.AIR, // Slot 4 placeholder for Ender Chest
            Material.ORANGE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE,

            Material.ORANGE_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.ORANGE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.YELLOW_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.ORANGE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.YELLOW_STAINED_GLASS_PANE,

            Material.AIR, Material.YELLOW_STAINED_GLASS_PANE, Material.AIR, Material.YELLOW_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.AIR, Material.YELLOW_STAINED_GLASS_PANE, Material.AIR
        };

        for (int i = 0; i < INV_SIZE; i++) {
            if (borderPattern[i] != Material.AIR) {
                inventory.setItem(i, GuiItem.border(borderPattern[i]));
            }
        }

        List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());

        List<Kit> claimable = allKits.stream()
            .filter(k -> player.hasPermission("anima.kits.claim." + k.getPlainName())
                      && plugin.getPlayerManager().getRemainingCooldown(uuid, k.getId()) <= 0
                      && !(k.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, k.getId())))
            .sorted(Comparator.comparing(Kit::getPlainName))
            .collect(Collectors.toList());

        List<Kit> unclaimable = allKits.stream()
            .filter(k -> !claimable.contains(k))
            .sorted(Comparator.comparing(Kit::getPlainName))
            .collect(Collectors.toList());

        List<Object> combined = new ArrayList<>();
        if (filterMode == 0) {
            combined.addAll(claimable);
            if (!claimable.isEmpty() && !unclaimable.isEmpty()) {
                combined.add("separator");
            }
            combined.addAll(unclaimable);
        } else if (filterMode == 1) {
            combined.addAll(claimable);
        } else {
            combined.addAll(unclaimable);
        }

        int totalPages = Math.max(1, (int) Math.ceil(combined.size() / (double) KIT_SLOTS.length));
        page = Math.min(page, totalPages - 1);

        int from = page * KIT_SLOTS.length;
        int to   = Math.min(from + KIT_SLOTS.length, combined.size());

        // Fill kit content area
        for (int i = 0; i < (to - from); i++) {
            Object obj = combined.get(from + i);
            if (obj instanceof Kit kit) {
                boolean canClaim = claimable.contains(kit);
                long remaining   = plugin.getPlayerManager().getRemainingCooldown(uuid, kit.getId());
                boolean hasPerm  = player.hasPermission("anima.kits.claim." + kit.getPlainName());
                boolean claimed  = kit.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, kit.getId());

                List<Component> lore = new ArrayList<>();
                for (String line : kit.getLore()) lore.add(MM.deserialize(line));
                if (!kit.getLore().isEmpty()) lore.add(Component.empty());

                if (!hasPerm) {
                    lore.add(MM.deserialize("<red>✘ No permission to claim.</red>"));
                } else if (claimed) {
                    lore.add(MM.deserialize("<red>✘ Already claimed (single-claim).</red>"));
                } else if (remaining > 0) {
                    lore.add(MM.deserialize("<gold>⏱ Cooldown: <white>" + formatTime(remaining) + "</white> remaining.</gold>"));
                } else {
                    lore.add(MM.deserialize("<green>✔ Ready to claim!</green>"));
                }

                lore.add(Component.empty());
                lore.add(MM.deserialize("<gradient:#44FF88:#00CC55>⬡ <bold>Left-click</bold></gradient><gray> → View Kit Contents</gray>"));
                lore.add(MM.deserialize("<gradient:#FFD700:#FFA500>✦ <bold>Right-click</bold></gradient><gray> → Claim This Kit</gray>"));

                Component kitTitle = ColorUtil.parse(kit.getRawName()).decoration(TextDecoration.BOLD, true);
                inventory.setItem(KIT_SLOTS[i], GuiItem.make(kit.getIconMaterial(), kitTitle, lore, canClaim));

            } else if ("separator".equals(obj)) {
                List<Component> sepLore = List.of(
                    MM.deserialize("<gradient:#44FF88:#00CC55>← Can Claim</gradient>   <gradient:#FF6060:#CC0000>Can't Claim →</gradient>"),
                    MM.deserialize("<dark_gray>Glowing kits can be claimed now.</dark_gray>")
                );
                inventory.setItem(KIT_SLOTS[i], GuiItem.make(Material.BLACK_STAINED_GLASS_PANE,
                    MM.deserialize("<gradient:#44FF88:#00CC55><bold>◄ Can Claim</bold></gradient><gray> | </gray><gradient:#FF6060:#CC0000><bold>Can't Claim ►</bold></gradient>"),
                    sepLore));
            }
        }

        // ── 2. Top Interactive Action Setup ──
        // Slot 4 – Ender Chest = Filter Toggle
        String filterLabel = filterMode == 0 ? "<white>All Kits</white>"
                           : filterMode == 1 ? "<green>Claimable Only</green>"
                                             : "<red>Unclaimable Only</red>";
        inventory.setItem(4, GuiItem.make(Material.ENDER_CHEST,
            MM.deserialize("<gradient:#CC88FF:#6600CC><bold>✦ Filter Kits</bold></gradient>"),
            List.of(
                MM.deserialize("<gray>Current filter: " + filterLabel + "</gray>"),
                Component.empty(),
                MM.deserialize("<gradient:#4FC3F7:#1565C0>⬡ <bold>Click</bold></gradient><gray> to cycle filter mode:</gray>"),
                MM.deserialize("<dark_gray>All → Claimable Only → Unclaimable Only → All</dark_gray>"),
                Component.empty(),
                MM.deserialize("<dark_gray>Claimable: <green>" + claimable.size() + "</green>  |  Unclaimable: <red>" + unclaimable.size() + "</red></dark_gray>")
            )));

        // ── 3. Bottom Interactive Action Setup (Row 6) ──
        // Slot 45 – Red Bundle = Close Menu
        inventory.setItem(45, GuiItem.make(Material.RED_BUNDLE,
            MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Close Menu</bold></gradient>"),
            List.of(MM.deserialize("<gray>Close the Claim Kits menu.</gray>"))));

        // Slot 47 – Red Stained Glass Pane = Previous Page
        inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE,
            MM.deserialize("<gradient:#FF6060:#CC0000><bold>« Previous Page</bold></gradient>"),
            List.of(MM.deserialize("<gray>Page <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        // Slot 51 – Lime Stained Glass Pane = Next Page
        inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE,
            MM.deserialize("<gradient:#44FF88:#00AA44><bold>Next Page »</bold></gradient>"),
            List.of(MM.deserialize("<gray>Page <white>" + (page + 1) + "/" + totalPages + "</white></gray>"))));

        // Slot 53 – Lime Bundle = Claim All Available
        int claimableCount = claimable.size();
        inventory.setItem(53, GuiItem.make(Material.LIME_BUNDLE,
            MM.deserialize("<gradient:#44FF88:#00CC55><bold>✦ Claim All Kits</bold></gradient>"),
            List.of(
                MM.deserialize("<gray>Claim all kits you are eligible for.</gray>"),
                Component.empty(),
                MM.deserialize("<dark_gray>Eligible kits: <green>" + claimableCount + "</green></dark_gray>"),
                Component.empty(),
                MM.deserialize("<gradient:#44FF88:#00CC55>⬡ <bold>Click</bold></gradient><gray> to claim all available kits.</gray>")
            )));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

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

        // Slot 45: Close
        if (slot == 45) {
            player.closeInventory();
            return;
        }

        // Slot 4: Toggle filter
        if (slot == 4) {
            filterMode = (filterMode + 1) % 3;
            page = 0;
            populate();
            return;
        }

        // Slot 47: Previous page
        if (slot == 47 && page > 0) {
            page--;
            populate();
            return;
        }

        // Slot 51: Next page
        if (slot == 51) {
            page++;
            populate();
            return;
        }

        // Slot 53: Claim All
        if (slot == 53) {
            UUID uuid       = player.getUniqueId();
            List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());
            int claimed     = 0;
            for (Kit kit : allKits) {
                if (!player.hasPermission("anima.kits.claim." + kit.getPlainName())) continue;
                if (kit.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, kit.getId())) continue;
                if (plugin.getPlayerManager().getRemainingCooldown(uuid, kit.getId()) > 0) continue;

                for (ItemStack item : kit.getItems()) {
                    if (item != null && item.getType() != org.bukkit.Material.AIR) {
                        java.util.HashMap<Integer, ItemStack> leftover =
                            player.getInventory().addItem(item.clone());
                        for (ItemStack drop : leftover.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), drop);
                        }
                    }
                }
                plugin.getPlayerManager().markClaimed(uuid, kit.getId());
                if (kit.getCooldown() > 0) {
                    plugin.getPlayerManager().setCooldown(uuid, kit.getId(), kit.getCooldown());
                }
                claimed++;
            }
            if (claimed > 0) {
                player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ Claimed <white>" + claimed + "</white> kit(s)!</gradient>"));
            } else {
                player.sendMessage(MM.deserialize("<gold>⚠ No kits available to claim right now.</gold>"));
            }
            populate();
            return;
        }

        // Process Kit items inside center panel layout
        int slotIndex = -1;
        for (int i = 0; i < KIT_SLOTS.length; i++) {
            if (KIT_SLOTS[i] == slot) { slotIndex = i; break; }
        }
        if (slotIndex == -1) return;

        int combinedIndex = page * KIT_SLOTS.length + slotIndex;

        UUID uuid = player.getUniqueId();
        List<Kit> allKits   = new ArrayList<>(plugin.getKitManager().getAllKits());
        List<Kit> claimable = allKits.stream()
            .filter(k -> player.hasPermission("anima.kits.claim." + k.getPlainName())
                      && plugin.getPlayerManager().getRemainingCooldown(uuid, k.getId()) <= 0
                      && !(k.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, k.getId())))
            .sorted(Comparator.comparing(Kit::getPlainName))
            .collect(Collectors.toList());
        List<Kit> unclaimable = allKits.stream()
            .filter(k -> !claimable.contains(k))
            .sorted(Comparator.comparing(Kit::getPlainName))
            .collect(Collectors.toList());

        List<Object> combined = new ArrayList<>();
        if (filterMode == 0) {
            combined.addAll(claimable);
            if (!claimable.isEmpty() && !unclaimable.isEmpty()) combined.add("separator");
            combined.addAll(unclaimable);
        } else if (filterMode == 1) {
            combined.addAll(claimable);
        } else {
            combined.addAll(unclaimable);
        }

        if (combinedIndex >= combined.size()) return;
        Object obj = combined.get(combinedIndex);
        if (!(obj instanceof Kit kit)) return;

        ClickType click = event.getClick();
        if (click == ClickType.LEFT) {
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () ->
                new AnimaClaimKits(plugin, player, kit, false).open());
        } else if (click == ClickType.RIGHT) {
            boolean hasPerm = player.hasPermission("anima.kits.claim." + kit.getPlainName());
            long remaining  = plugin.getPlayerManager().getRemainingCooldown(uuid, kit.getId());
            boolean claimed = kit.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, kit.getId());

            if (!hasPerm) {
                player.sendMessage(MM.deserialize("<gradient:#FF4444:#CC0000>✘ You don't have permission to claim <white>" + kit.getPlainName() + "</white>.</gradient>"));
                return;
            }
            if (claimed) {
                player.sendMessage(MM.deserialize("<gradient:#FF4444:#CC0000>✘ You have already claimed <white>" + kit.getPlainName() + "</white> (single-claim only).</gradient>"));
                return;
            }
            if (remaining > 0) {
                player.sendMessage(MM.deserialize("<gold>⏱ Wait <white>" + formatTime(remaining) + "</white> before claiming again.</gradient>"));
                return;
            }
            for (ItemStack item : kit.getItems()) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    java.util.HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
                    for (ItemStack drop : leftover.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }
            }
            plugin.getPlayerManager().markClaimed(uuid, kit.getId());
            if (kit.getCooldown() > 0)
                plugin.getPlayerManager().setCooldown(uuid, kit.getId(), kit.getCooldown());
            player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ You claimed <white>" + kit.getPlainName() + "</white>!</gradient>"));
            populate();
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
        if (event.getInventory().equals(inventory)) HandlerList.unregisterAll(this);
    }

    private String formatTime(long seconds) {
        if (seconds <= 0) return "Ready";
        long h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }
}