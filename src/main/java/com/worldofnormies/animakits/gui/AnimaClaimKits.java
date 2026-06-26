package com.worldofnormies.animakits.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.kit.Kit;
import com.worldofnormies.animaitemedit.ItemEditUtil;
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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * AnimaClaimKits – Displays items inside a specific kit to a player.
 *
 * Items are shown with their full lore plus Unbreakable / Glow badges
 * automatically appended when those properties are set on the item.
 */
public class AnimaClaimKits implements Listener {

    private static final MiniMessage MM        = MiniMessage.miniMessage();
    private static final int         INV_SIZE  = 54;

    private static final int[] CONTENT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    private static final int ITEMS_PER_PAGE = CONTENT_SLOTS.length; // 28

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
            "<gradient:#FF3030:#FFFFFF:#3060FF><bold>⋆༺⸸ </bold></gradient>")
            .append(com.worldofnormies.animakits.util.ColorUtil.parse(kit.getRawName()))
            .append(MM.deserialize("<gradient:#FF3030:#FFFFFF:#3060FF><bold> ⸸༻⋆</bold></gradient>"));
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

        // ── 1. Border ──
        Material[] borderPattern = new Material[INV_SIZE];
        Arrays.fill(borderPattern, Material.AIR);
        for (int i = 0; i < 9; i++) {
            borderPattern[i]      = (i % 2 == 0) ? Material.LIME_STAINED_GLASS_PANE : Material.GREEN_STAINED_GLASS_PANE;
            borderPattern[45 + i] = (i % 2 == 0) ? Material.LIME_STAINED_GLASS_PANE : Material.GREEN_STAINED_GLASS_PANE;
        }
        for (int i = 1; i < 5; i++) {
            borderPattern[i * 9]     = (i % 2 == 0) ? Material.GREEN_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE;
            borderPattern[i * 9 + 8] = (i % 2 == 0) ? Material.LIME_STAINED_GLASS_PANE : Material.GREEN_STAINED_GLASS_PANE;
        }
        int[] actionSlots = {45, 47, 49, 51, 53};
        for (int s : actionSlots) borderPattern[s] = Material.AIR;
        for (int i = 0; i < INV_SIZE; i++) {
            if (borderPattern[i] != Material.AIR) inventory.setItem(i, makePane(borderPattern[i]));
        }

        // ── 2. Item Content with hover badges ──
        int from = page * ITEMS_PER_PAGE;
        int to   = Math.min(from + ITEMS_PER_PAGE, items.size());
        for (int i = 0; i < (to - from); i++) {
            ItemStack item = items.get(from + i);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(CONTENT_SLOTS[i], wrapItemForDisplay(item.clone()));
            }
        }

        // ── 3. Button Bar ──
        UUID uuid      = player.getUniqueId();
        boolean hasPerm = player.hasPermission("anima.kits.claim." + kit.getPlainName());
        long remaining  = plugin.getPlayerManager().getRemainingCooldown(uuid, kit.getId());
        boolean claimed = kit.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, kit.getId());

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
        inventory.setItem(45, GuiItem.make(Material.LIME_BUNDLE,
            MM.deserialize("<gradient:#44FF88:#00CC55><bold>✔ Claim Kit</bold></gradient>"), claimLore));

        inventory.setItem(47, GuiItem.make(Material.RED_STAINED_GLASS_PANE,
            MM.deserialize("<gradient:#FF6060:#CC0000><bold>« Previous Page</bold></gradient>"),
            List.of(MM.deserialize("<gray>Page <white>" + page + "/" + totalPages + "</white></gray>"))));

        // Kit info
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(MM.deserialize("<gradient:#54DAF4:#545EB6><bold>── Kit Details ──</bold></gradient>"));
        infoLore.add(Component.empty());
        infoLore.add(MM.deserialize("<gray>Permission: " + (hasPerm ? "<green>✔ Granted</green>" : "<red>✘ Not Granted</red>") + "</gray>"));
        infoLore.add(MM.deserialize("<gray>Cooldown: <white>" + (kit.getCooldown() == 0 ? "<green>None</green>" : kit.getCooldown() + "s</white>") + "</gray>"));
        if (remaining > 0) infoLore.add(MM.deserialize("<gray>Remaining: <gold>" + formatTime(remaining) + "</gold></gray>"));
        infoLore.add(MM.deserialize("<gray>Single Claim: <white>" + (kit.isSingleClaim() ? "<red>Yes</red>" : "<green>No</green>") + "</white></gray>"));
        if (kit.isSingleClaim()) infoLore.add(MM.deserialize("<gray>Already Claimed: <white>" + (claimed ? "<red>Yes</red>" : "<green>No</green>") + "</white></gray>"));
        infoLore.add(Component.empty());
        infoLore.add(MM.deserialize("<gray>Items: <white>" + items.size() + "</white> total</gray>"));
        infoLore.add(MM.deserialize("<gray>Page: <white>" + (page + 1) + "/" + totalPages + "</white></gray>"));
        inventory.setItem(49, GuiItem.make(Material.ENDER_CHEST,
            MM.deserialize("<gradient:#CC88FF:#6600CC><bold>✦ Kit Information</bold></gradient>"), infoLore));

        inventory.setItem(51, GuiItem.make(Material.LIME_STAINED_GLASS_PANE,
            MM.deserialize("<gradient:#44FF88:#00AA44><bold>Next Page »</bold></gradient>"),
            List.of(MM.deserialize("<gray>Page <white>" + (page + 2) + "/" + totalPages + "</white></gray>"))));

        inventory.setItem(53, GuiItem.make(Material.RED_BUNDLE,
            MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Back</bold></gradient>"),
            List.of(MM.deserialize("<gray>Return to the Claim Kits menu.</gray>"))));
    }

    /**
     * Wraps a kit item for display in the claim GUI — appends Unbreakable and Glow
     * status badges to the item's existing lore so players can see item properties
     * on hover without modifying the real item data.
     */
    private ItemStack wrapItemForDisplay(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return item;
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;

        List<Component> lore = new ArrayList<>(meta.lore() != null ? meta.lore() : List.of());

        boolean isUnbreakable = meta.isUnbreakable();
        boolean hasGlow = meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);

        boolean badgeAdded = false;

        if (isUnbreakable) {
            if (!badgeAdded) { lore.add(Component.empty()); badgeAdded = true; }
            // Light purple → magenta gradient for Infinite Durability
            lore.add(MM.deserialize("<gradient:#DA70D6:#FF00FF:#9400D3><bold>[Infinite Durability]</bold></gradient>"));
        }

        if (hasGlow) {
            if (!badgeAdded) { lore.add(Component.empty()); badgeAdded = true; }
            // Multi-gradient for Glow: On
            lore.add(MM.deserialize("<gradient:#FF00FF:#00FFFF:#FF69B4><bold>[Glow: On]</bold></gradient>"));
        }

        meta.lore(lore);
        copy.setItemMeta(meta);
        return copy;
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
            if (event.getClick().isShiftClick()) event.setCancelled(true);
            return;
        }

        if (slot == 45) {
            UUID uuid      = player.getUniqueId();
            boolean perm   = player.hasPermission("anima.kits.claim." + kit.getPlainName());
            long remaining = plugin.getPlayerManager().getRemainingCooldown(uuid, kit.getId());
            boolean claimed= kit.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, kit.getId());

            if (!perm) {
                player.sendMessage(MM.deserialize("<gradient:#FF4444:#CC0000>✘ You don't have permission to claim <white>" + kit.getPlainName() + "</white>.</gradient>"));
                return;
            }
            if (claimed) {
                player.sendMessage(MM.deserialize("<gradient:#FF4444:#CC0000>✘ You have already claimed <white>" + kit.getPlainName() + "</white> (single-claim only).</gradient>"));
                return;
            }
            if (remaining > 0) {
                player.sendMessage(MM.deserialize("<gold>⏱ You must wait <white>" + formatTime(remaining) + "</white> before claiming <white>" + kit.getPlainName() + "</white> again.</gold>"));
                return;
            }
            for (ItemStack item : kit.getItems()) {
                if (item != null && item.getType() != Material.AIR) {
                    java.util.HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
                    for (ItemStack drop : leftover.values()) player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
            plugin.getPlayerManager().markClaimed(uuid, kit.getId());
            if (kit.getCooldown() > 0) plugin.getPlayerManager().setCooldown(uuid, kit.getId(), kit.getCooldown());
            player.sendMessage(MM.deserialize("<gradient:#44FF88:#00CC55>✔ You claimed <white>" + kit.getPlainName() + "</white>!</gradient>"));
            populate();
            return;
        }

        if (slot == 47 && page > 0) { page--; populate(); return; }

        if (slot == 51) {
            List<ItemStack> items = kit.getItems();
            if ((page + 1) * ITEMS_PER_PAGE < items.size()) { page++; populate(); }
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
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inventory)) event.setCancelled(true);
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

    private ItemStack makePane(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.displayName(Component.space()); item.setItemMeta(meta); }
        return item;
    }
}
