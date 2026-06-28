package com.worldofnormies.animaeconomy;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class EconomyListener implements Listener {

    private final AnimaKitsPlugin plugin;

    public EconomyListener(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (!item.hasItemMeta()) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(EconomyUtil.ECO_TYPE_KEY, PersistentDataType.STRING)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        String type = pdc.get(EconomyUtil.ECO_TYPE_KEY, PersistentDataType.STRING);
        Long amount = pdc.get(EconomyUtil.ECO_AMOUNT_KEY, PersistentDataType.LONG);

        if (type == null || amount == null) return;

        plugin.getEconomyManager().addBalance(player.getUniqueId(), type, amount);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        MessageUtil.sendMsg(player, "eco-claimed", Map.of("amount", String.valueOf(amount), "type", type));
    }
}
