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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * AnimaKitsEditor – multi-page kit editor.
 *
 * Slots:
 *   1 = Name Tag → Rename
 *   3 = Chest/icon → Set icon
 *   5 = Book&Quill → Add lore line
 *   44 = Red Bundle → Cancel / return
 *   46 = Red Glass (filler/prev?) - wait user said 47 is Red
 *   52 = Lime Glass (filler/next?) - wait user said 51 is Lime
 *   53 = Green Bundle → Apply
 */
public class AnimaKitsEditor implements Listener {

    private static final MiniMessage MM         = MiniMessage.miniMessage();
    private static final int         INV_SIZE   = 54;
    private static final int         ITEMS_AREA = 44;

    private static final int SLOT_NAME_TAG      = 1;
    private static final int SLOT_CHEST         = 3;
    private static final int SLOT_BOOK          = 5;

    private static final int SLOT_RED_BUNDLE    = 44;
    private static final int SLOT_PREV_PAGE     = 47;
    private static final int SLOT_NEXT_PAGE     = 51;
    private static final int SLOT_GREEN_BUNDLE  = 53;

    private final AnimaKitsPlugin plugin;
    private final Player          player;
    private final Kit             kit;
    private int                   page;
    private Inventory             inventory;
    private boolean               closing = false;

    public AnimaKitsEditor(AnimaKitsPlugin plugin, Player player, Kit kit, int page) {
        this.plugin = plugin;
        this.player = player;
        this.kit    = kit;
        this.page   = page;
    }

    public void open() {
        Component title = MM.deserialize("<gradient:#FF3030:#FFFFFF:#3060FF><bold>⋆༺⸸ Anima Kits Editor ⸸༻⋆</bold></gradient>");

        inventory = Bukkit.createInventory(null, INV_SIZE, title);
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void populate() {
        inventory.clear();

        ItemStack black = makePaneBlack();
        for (int i = 0; i < 9; i++) inventory.setItem(i, black);

        inventory.setItem(SLOT_NAME_TAG, GuiItem.make(
                Material.NAME_TAG,
                MM.deserialize("<yellow><bold>✎ Rename Kit</bold></yellow>"),
                List.of(MM.deserialize("<gray>Current: <white>" + kit.getRawName() + "</white></gray>"))));

        inventory.setItem(SLOT_CHEST, GuiItem.make(
                kit.getIconMaterial(),
                MM.deserialize("<aqua><bold>⬛ Kit Icon</bold></aqua>"),
                List.of(MM.deserialize("<gray>Click with item in hand to set icon.</gray>"))));

        inventory.setItem(SLOT_BOOK, GuiItem.make(
                Material.WRITABLE_BOOK,
                MM.deserialize("<light_purple><bold>✎ Add Lore Line</bold></light_purple>"),
                List.of(MM.deserialize("<gray>Click to add lore line.</gray>"))));

        List<ItemStack> allItems = kit.getItems();
        int from = page * (ITEMS_AREA - 9);
        for (int i = 0; i < (ITEMS_AREA - 9); i++) {
            int idx = from + i;
            if (idx < allItems.size() && allItems.get(idx) != null) {
                inventory.setItem(9 + i, allItems.get(idx).clone());
            }
        }

        for (int s = 45; s < 53; s++) inventory.setItem(s, black);

        inventory.setItem(SLOT_RED_BUNDLE, GuiItem.make(Material.RED_BUNDLE, MM.deserialize("<red><bold>✘ Cancel</bold></red>")));

        // 47 = RED glass staned pane   → « Previous
        // 51 = lime GREEN stained glas panes  → Next »
        inventory.setItem(SLOT_PREV_PAGE, GuiItem.make(Material.RED_STAINED_GLASS_PANE, MM.deserialize("<red><bold>« Previous</bold></red>")));
        inventory.setItem(SLOT_NEXT_PAGE, GuiItem.make(Material.LIME_STAINED_GLASS_PANE, MM.deserialize("<green><bold>Next »</bold></green>")));

        inventory.setItem(SLOT_GREEN_BUNDLE, GuiItem.make(Material.LIME_BUNDLE, MM.deserialize("<green><bold>✔ Apply</bold></green>")));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

        int slot = event.getRawSlot();
        if (slot >= 9 && slot < ITEMS_AREA) return;

        event.setCancelled(true);

        if (slot == SLOT_RED_BUNDLE) {
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () -> new AnimaKitsMainGUI(plugin, player).open());
        } else if (slot == SLOT_GREEN_BUNDLE) {
            saveCurrentPage();
            plugin.getKitManager().saveKits();
            plugin.getKitManager().refreshAllBrowsersSafe();
            closing = true;
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTask(plugin, () -> new AnimaKitsMainGUI(plugin, player).open());
        } else if (slot == SLOT_PREV_PAGE && page > 0) {
            saveCurrentPage();
            page--;
            populate();
        } else if (slot == SLOT_NEXT_PAGE) {
            saveCurrentPage();
            page++;
            populate();
        } else if (slot == SLOT_NAME_TAG) {
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();
            ChatInputSession.sendRenamePrompt(plugin, player);
            new ChatInputSession(plugin, player,
                    newName -> {
                        kit.setRawName(newName);
                        plugin.getKitManager().saveKits();
                        new AnimaKitsEditor(plugin, player, kit, page).open();
                    },
                    () -> new AnimaKitsEditor(plugin, player, kit, page).open()
            ).await();
        } else if (slot == SLOT_CHEST) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() != Material.AIR) {
                kit.setIconMaterial(hand.getType());
                populate();
            }
        } else if (slot == SLOT_BOOK) {
            saveCurrentPage();
            closing = true;
            HandlerList.unregisterAll(this);
            player.closeInventory();
            ChatInputSession.sendLorePrompt(plugin, player);
            new ChatInputSession(plugin, player,
                    line -> {
                        kit.addLoreLine(line);
                        new AnimaKitsEditor(plugin, player, kit, page).open();
                    },
                    () -> new AnimaKitsEditor(plugin, player, kit, page).open()
            ).await();
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (closing) return;
        saveCurrentPage();
        plugin.getKitManager().saveKits();
        HandlerList.unregisterAll(this);
    }

    private void saveCurrentPage() {
        List<ItemStack> allItems = new ArrayList<>(kit.getItems());
        int itemsPerPage = ITEMS_AREA - 9;
        int from = page * itemsPerPage;

        while (allItems.size() < from + itemsPerPage) allItems.add(null);

        for (int i = 0; i < itemsPerPage; i++) {
            ItemStack item = inventory.getItem(9 + i);
            allItems.set(from + i, (item != null && item.getType() != Material.AIR) ? item.clone() : null);
        }

        kit.clearItems();
        for (ItemStack is : allItems) kit.addItem(is != null ? is : new ItemStack(Material.AIR));
    }

    private ItemStack makePaneBlack() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.displayName(Component.space()); item.setItemMeta(meta); }
        return item;
    }
}
