package com.worldofnormies.animaranks;

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
        updatePlayerDisplayName(player);
    }

    private void updatePlayerDisplayName(Player player) {
        var rm = plugin.getRankManager();
        var rank = rm.getPlayerRank(player.getUniqueId());
        if (rank == null) return;

        String nameColor = rank.getColorName().isBlank() ? "" : com.worldofnormies.animakits.util.ColorUtil.colorize(rank.getColorName());
        String displayName = nameColor + player.getName() + org.bukkit.ChatColor.RESET;

        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);
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
     * Format: [prefix] PlayerName [suffix]: chatcolor message
     *
     * Note: Uses ColorUtil.colorize() which translates MiniMessage (including gradients)
     * into legacy Bukkit/Bungee hex codes that Spigot's AsyncPlayerChatEvent format understands.
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

        // Ensure display name is up to date
        String coloredName = nameColor + player.getName() + org.bukkit.ChatColor.RESET;

        // We use Reset after prefix and suffix to prevent color bleeding.
        // %1$s is the player name (already colored if set via setFormat, but we specify it here for clarity)
        // %2$s is the message.
        String format = prefix + org.bukkit.ChatColor.RESET + coloredName + org.bukkit.ChatColor.RESET + suffix
                + org.bukkit.ChatColor.GRAY + ": " + org.bukkit.ChatColor.RESET + chatColor + "%2$s";

        // Some plugins might override the name, so we force our format
        event.setFormat(format);
    }
}
