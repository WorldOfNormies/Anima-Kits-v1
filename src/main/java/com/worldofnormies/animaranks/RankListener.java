package com.worldofnormies.animaranks;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.util.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankListener implements Listener {
    private final AnimaKitsPlugin plugin;
    private final RankManager rankManager;
    private final Map<UUID, Long> joinTimes = new HashMap<>();

    public RankListener(AnimaKitsPlugin plugin, RankManager rankManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        joinTimes.put(uuid, System.currentTimeMillis());
        applyRankPermissions(event.getPlayer());
    }

    private void applyRankPermissions(org.bukkit.entity.Player player) {
        Rank rank = rankManager.getPlayerRank(player.getUniqueId());
        if (rank == null) return;
        for (String perm : rank.getPermissions()) {
            player.addAttachment(plugin, perm, true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (joinTimes.containsKey(uuid)) {
            long diff = (System.currentTimeMillis() - joinTimes.get(uuid)) / 1000;
            rankManager.addPlaytime(uuid, diff);
            joinTimes.remove(uuid);
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Rank rank = rankManager.getPlayerRank(event.getPlayer().getUniqueId());
        if (rank == null) return;

        Component prefix = ColorUtil.parse(rank.getPrefix());
        Component suffix = ColorUtil.parse(rank.getSuffix());
        Component nameColor = ColorUtil.parse(rank.getNameColor());
        Component chatColor = ColorUtil.parse(rank.getChatColor());

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component nameComp = nameColor.append(sourceDisplayName);
            Component formattedPrefix = prefix.equals(Component.empty()) ? Component.empty() : prefix.append(Component.space());
            Component formattedSuffix = suffix.equals(Component.empty()) ? Component.empty() : Component.space().append(suffix);

            return formattedPrefix
                    .append(nameComp)
                    .append(formattedSuffix)
                    .append(Component.text(": "))
                    .append(chatColor.append(message));
        });
    }
}
