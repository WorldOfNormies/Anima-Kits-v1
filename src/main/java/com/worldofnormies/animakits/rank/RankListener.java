package com.worldofnormies.animakits.rank;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * RankListener – hooks into player events to:
 *  - Apply rank permissions on join
 *  - Track playtime (start/end session)
 *  - Check rank expiry on join
 *  - Apply chat prefix/suffix/color formatting
 */
public class RankListener implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin plugin;

    public RankListener(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getRankManager().onPlayerJoin(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getRankManager().onPlayerQuit(player);
    }

    /**
     * Applies rank prefix, suffix, and chat color to chat messages.
     * Uses legacy AsyncPlayerChatEvent – works on all Paper/Spigot 1.21.
     *
     * Format: [prefix] PlayerName[suffix]: chatcolor message
     *
     * Note: For full MiniMessage chat support (gradients in chat), a dedicated
     * chat plugin or Paper's Adventure chat events are recommended.
     * This applies the rank's chat color and prefix/suffix as legacy color codes
     * via ColorUtil for broad compatibility.
     */
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        var rm = plugin.getRankManager();
        var rank = rm.getPlayerRank(player.getUniqueId());
        if (rank == null) return;

        String prefix    = rank.getPrefix().isBlank()    ? "" : com.worldofnormies.animakits.util.ColorUtil.colorize(rank.getPrefix()) + " ";
        String suffix    = rank.getSuffix().isBlank()    ? "" : " " + com.worldofnormies.animakits.util.ColorUtil.colorize(rank.getSuffix());
        String nameColor = rank.getColorName().isBlank() ? "" : com.worldofnormies.animakits.util.ColorUtil.colorize(rank.getColorName());
        String chatColor = rank.getChatColor().isBlank() ? "" : com.worldofnormies.animakits.util.ColorUtil.colorize(rank.getChatColor());

        // Build format: <prefix><namecolor>name</reset><suffix>: <chatcolor>message
        String format = prefix + nameColor + "%s" + org.bukkit.ChatColor.RESET + suffix
                + org.bukkit.ChatColor.GRAY + ": " + chatColor + "%s";
        event.setFormat(format);
    }
}
