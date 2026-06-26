package com.worldofnormies.animaranks;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.util.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class RankListener implements Listener {
    private final AnimaKitsPlugin plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public RankListener(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updateDisplayName(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String rankId = plugin.getRankManager().getPlayerRank(player.getUniqueId());
        if (rankId == null) return;
        Rank rank = plugin.getRankManager().getRank(rankId);
        if (rank == null) return;

        Component prefix = ColorUtil.parse(rank.getPrefix());
        Component suffix = ColorUtil.parse(rank.getSuffix());
        Component nameColor = ColorUtil.parse(rank.getNameColor());
        Component chatColor = ColorUtil.parse(rank.getChatColor());

        Component nameComp = nameColor.append(Component.text(player.getName()));

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component finalMsg = prefix
                .append(nameComp)
                .append(suffix)
                .append(Component.text(": "))
                .append(chatColor.append(message));
            return finalMsg;
        });
    }

    private void updateDisplayName(Player player) {
        String rankId = plugin.getRankManager().getPlayerRank(player.getUniqueId());
        if (rankId == null) return;
        Rank rank = plugin.getRankManager().getRank(rankId);
        if (rank == null) return;

        Component prefix = ColorUtil.parse(rank.getPrefix());
        Component suffix = ColorUtil.parse(rank.getSuffix());
        Component nameColor = ColorUtil.parse(rank.getNameColor());

        Component fullDisplayName = prefix
            .append(nameColor.append(Component.text(player.getName())))
            .append(suffix);

        player.displayName(fullDisplayName);
        player.playerListName(fullDisplayName);
    }
}
