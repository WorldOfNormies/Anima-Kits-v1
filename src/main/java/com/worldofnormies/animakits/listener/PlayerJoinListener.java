package com.worldofnormies.animakits.listener;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.kit.Kit;
import com.worldofnormies.animakits.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles two things on player join:
 *
 * 1. Applies stored global permissions (from @a selector grants) to the joining player
 *    so they receive any kit claim permissions that were set server-wide.
 *
 * 2. Gives the configured OnJoinNew starter kit to players who have NEVER joined before.
 *    This is a direct item give — completely separate from the claim/cooldown system.
 *    The kit is given once per player per configured join-kit. If the join-kit is
 *    changed later, players who already received the old one are unaffected.
 */
public class PlayerJoinListener implements Listener {

    private final AnimaKitsPlugin plugin;

    public PlayerJoinListener(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load per-player permissions
        plugin.getPermissionManager().loadPlayer(player.getUniqueId());
        plugin.getHomeManager().loadPlayer(player.getUniqueId());

        // 1. Apply global permissions (covers @a grants made while player was offline/new)
        plugin.getPermissionManager().applyGlobalsToPlayer(player.getUniqueId());

        // 2. First-join starter kit
        String joinKitName = plugin.getConfig().getString("join-kit", "");
        if (joinKitName == null || joinKitName.isBlank()) return;

        Kit kit = plugin.getKitManager().getKitByPlainName(joinKitName);
        if (kit == null) {
            plugin.getLogger().warning("[AnimaKits] OnJoinNew kit '" + joinKitName + "' not found — check your config.yml.");
            return;
        }

        // Only give if this player has never received this specific join kit before
        if (plugin.getPlayerManager().hasReceivedJoinKit(player.getUniqueId(), kit.getId())) return;

        // Give the items — no claim record written, no cooldown set
        for (ItemStack item : kit.getItems()) {
            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item.clone());
                if (!remaining.isEmpty()) {
                    for (ItemStack leftover : remaining.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                    }
                }
            }
        }

        // Mark as received so they never get it again on future joins
        plugin.getPlayerManager().markJoinKitReceived(player.getUniqueId(), kit.getId());

        // Notify the player
        MessageUtil.sendMsg(player, "join-kit-received", Map.of("kit", kit.getPlainName()));
    }
}
