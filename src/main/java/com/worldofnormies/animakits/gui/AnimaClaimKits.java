package com.worldofnormies.animakits.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.kit.Kit;
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
import java.util.UUID;

/**
 * AnimaClaimKits – Shows items inside a specific kit to a player.
 *
 * Layout (54 slots):
 *   Slots 0-43  → kit items (display-only, not takeable)
 *   Slots 44-53 → bottom bar
 *     44 = Green Bundle  → Claim this kit
 *     45 = black pane
 *     46 = black pane
 *     47 = Red Glass Pane → « Previous page
 *     48 = black pane
 *     49 = Ender Chest   → Kit info (cooldown, permission, status)
 *     50 = black pane
 *     51 = Lime Glass Pane → Next page »
 *     52 = black pane
 *     53 = Red Bundle    → Back to Claim Main GUI
 *
 * All gradient button names + lores.
 */
public class AnimaClaimKits implements Listener {

    private static final MiniMessage MM        = MiniMessage.miniMessage();
    private static final int         INV_SIZE  = 54;
    private static final int         ITEMS_PER_PAGE = 44; // slots 0-43

    private final AnimaKitsPlugin plugin;
    private final Player          player;
    private final Kit             kit;
    private final boolean         isAdmin;
    private int                   page = 0;
    private Inventory             inventory;

    public AnimaClaimKits(AnimaKitsPlugin plugin, Player player, Kit kit, boolean isAdmin) {
        this.plugin  = plugin;
        this.player  = player;
        this.kit     = kit;
        this.isAdmin = isAdmin;
    }

    public void open() {
        Component title = MM.deserialize(
            "<gradient:#FF3030:#FFFFFF:#3060FF><bold>⋆༺⸸ " + kit.getPlainName() + " ⸸༻⋆</bold></gradient>");
        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();
        List<ItemStack> items = kit.getItems();
        int totalPages = Math.max(1, (int) Math.ceil(items.size() / (double) ITEMS_PER_PAGE));
        page = Math.min(page, totalPages - 1);

        int from = page * ITEMS_PER_PAGE;
        int to   = Math.min(from + ITEMS_PER_PAGE, items.size());

        // Place kit items (display only)
        for (int i = 0; i < (to - from); i++) {
            ItemStack item = items.get(from + i);
            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                inventory.setItem(i, item.clone());
            }
        }

        // ── Bottom bar ────────────────────────────────────────────────
        ItemStack blackPane = GuiItem.border(Material.BLACK_STAINED_GLASS_PANE);
        for (int s = 44; s < 54; s++) inventory.setItem(s, blackPane);

        UUID uuid         = player.getUniqueId();
        boolean hasPerm   = player.hasPermission("anima.kits.claim." + kit.getPlainName());
        long remaining    = plugin.getPlayerManager().getRemainingCooldown(uuid, kit.getId());
        boolean claimed   = kit.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, kit.getId());
        boolean canClaim  = hasPerm && remaining <= 0 && !claimed;

        // Slot 44 – Green Bundle = Claim
        List<Component> claimLore = new ArrayList<>();
        if (!hasPerm) {
            claimLore.add(MM.deserialize("<red>✘ You do not have permission to claim this kit.</red>"));
        } else if (claimed) {
            claimLore.add(MM.deserialize("<red>✘ You have already claimed this kit (single-claim).</red>"));
        } else if (remaining > 0) {
            claimLore.add(MM.deserialize("<gold>⏱ Cooldown: <white>" + formatTime(remaining) + "</white> remaining.</gold>"));
        } else {
            claimLore.add(MM.deserialize("<green>✔ Ready to claim!</green>"));
        }

        inventory.setItem(44, GuiItem.make(Material.LIME_BUNDLE,
            MM.deserialize("<gradient:#44FF88:#00CC55><bold>✔ Claim Kit</bold></gradient>"),
            claimLore));

        // Slot 47 – Red Glass Pane = Previous page
        inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE,
            MM.deserialize("<gradient:#FF6060:#CC0000><bold>« Previous Page</bold></gradient>"),
            List.of(MM.deserialize("<gray>Page <white>" + page + "/" + totalPages + "</white></gray>"))));

        // Slot 49 – Ender Chest = Kit info
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(MM.deserialize("<gradient:#54DAF4:#545EB6><bold>── Kit Details ──</bold></gradient>"));
        infoLore.add(Component.empty());
        infoLore.add(MM.deserialize("<gray>Permission: " +
            (hasPerm ? "<green>✔ Granted</green>" : "<red>✘ Not Granted</red>") + "</gray>"));
        infoLore.add(MM.deserialize("<gray>Cooldown: <white>" +
            (kit.getCooldown() == 0 ? "<green>None</green>" : kit.getCooldown() + "s</white>") + "</gray>"));
        if (remaining > 0) {
            infoLore.add(MM.deserialize("<gray>Remaining: <gold>" + formatTime(remaining) + "</gold></gray>"));
        }
        infoLore.add(MM.deserialize("<gray>Single Claim: <white>" +
            (kit.isSingleClaim() ? "<red>Yes</red>" : "<green>No</green>") + "</white></gray>"));
        if (kit.isSingleClaim()) {
            infoLore.add(MM.deserialize("<gray>Already Claimed: <white>" +
                (claimed ? "<red>Yes</red>" : "<green>No</green>") + "</white></gray>"));
        }
        infoLore.add(Component.empty());
        infoLore.add(MM.deserialize("<gray>Items: <white>" + items.size() + "</white> total</gray>"));
        infoLore.add(MM.deserialize("<gray>Page: <white>" + (page + 1) + "/" + totalPages + "</white></gray>"));

        inventory.setItem(49, GuiItem.make(Material.ENDER_CHEST,
            MM.deserialize("<gradient:#CC88FF:#6600CC><bold>✦ Kit Information</bold></gradient>"),
            infoLore));

        // Slot 51 – Lime Glass Pane = Next page
        inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE,
            MM.deserialize("<gradient:#44FF88:#00AA44><bold>Next Page »</bold></gradient>"),
            List.of(MM.deserialize("<gray>Page <white>" + (page + 2) + "/" + totalPages + "</white></gray>"))));

        // Slot 53 – Red Bundle = Back
        inventory.setItem(53, GuiItem.make(Material.RED_BUNDLE,
            MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Back</bold></gradient>"),
            List.of(MM.deserialize("<gray>Return to the Claim Kits menu.</gray>"))));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot == 44) {
            // Claim kit
            UUID uuid      = player.getUniqueId();
            boolean perm   = player.hasPermission("anima.kits.claim." + kit.getPlainName());
            long remaining = plugin.getPlayerManager().getRemainingCooldown(uuid, kit.getId());
            boolean claimed= kit.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, kit.getId());

            if (!perm) {
                player.sendMessage(MM.deserialize(
                    "<gradient:#FF4444:#CC0000>✘ You don't have permission to claim <white>" +
                    kit.getPlainName() + "</white>.</gradient>"));
                return;
            }
            if (claimed) {
                player.sendMessage(MM.deserialize(
                    "<gradient:#FF4444:#CC0000>✘ You have already claimed <white>" +
                    kit.getPlainName() + "</white> (single-claim only).</gradient>"));
                return;
            }
            if (remaining > 0) {
                player.sendMessage(MM.deserialize(
                    "<gold>⏱ You must wait <white>" + formatTime(remaining) +
                    "</white> before claiming <white>" + kit.getPlainName() + "</white> again.</gold>"));
                return;
            }

            // Actually give the kit
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
            player.sendMessage(MM.deserialize(
                "<gradient:#44FF88:#00CC55>✔ You claimed <white>" +
                kit.getPlainName() + "</white>!</gradient>"));
            populate(); // refresh to show updated status
            return;
        }

        if (slot == 47 && page > 0) {
            page--;
            populate();
            return;
        }

        if (slot == 51) {
            List<ItemStack> items = kit.getItems();
            if ((page + 1) * ITEMS_PER_PAGE < items.size()) {
                page++;
                populate();
            }
            return;
        }

        if (slot == 53) {
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (isAdmin) new AnimaKitsMainGUI(plugin, player).open();
                else         new AnimaClaimMainGUI(plugin, player).open();
            });
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