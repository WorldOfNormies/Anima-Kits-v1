package com.worldofnormies.animakits.scoreboard;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.util.ColorUtil;
import com.worldofnormies.animakits.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {

    private final AnimaKitsPlugin plugin;
    private final Map<UUID, Scoreboard> scoreboards = new ConcurrentHashMap<>();
    private final Set<UUID> disabledPlayers = new HashSet<>();
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public ScoreboardManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 20L, 20L); // Update every second
    }

    public void toggleScoreboard(Player player) {
        if (disabledPlayers.contains(player.getUniqueId())) {
            disabledPlayers.remove(player.getUniqueId());
            updateScoreboard(player);
        } else {
            disabledPlayers.add(player.getUniqueId());
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void updateScoreboard(Player player) {
        if (disabledPlayers.contains(player.getUniqueId())) return;

        Scoreboard sb = scoreboards.computeIfAbsent(player.getUniqueId(), k -> Bukkit.getScoreboardManager().getNewScoreboard());
        Objective obj = sb.getObjective("anima_eco");
        if (obj == null) {
            obj = sb.registerNewObjective("anima_eco", Criteria.DUMMY, ColorUtil.parse("<gradient:#FF3030:#FFFFFF:#3060FF><bold>⋆ Anima ⋆</bold></gradient>"));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        List<Component> lines = new ArrayList<>();
        lines.add(Component.empty());
        lines.add(ColorUtil.parse("<gray>Name: </gray><white>" + player.getName() + "</white>"));

        var rank = plugin.getRankManager().getPlayerRank(player.getUniqueId());
        String rankName = rank != null ? rank.getDisplayName() : "None";
        lines.add(ColorUtil.parse("<gray>Rank: </gray>" + rankName));

        long playtime = plugin.getRankManager().getPlaytime(player.getUniqueId());
        lines.add(ColorUtil.parse("<gray>Playtime: </gray><aqua>" + TimeUtil.formatDuration(playtime) + "</aqua>"));

        var nextRank = plugin.getRankManager().getNextRankUp(player.getUniqueId());
        String ready = "No";
        if (nextRank != null && plugin.getRankManager().meetsRankupRequirements(player.getUniqueId(), nextRank)) {
            ready = "<green>Yes!</green>";
        }
        lines.add(ColorUtil.parse("<gray>Rankup: </gray>" + ready));

        long money = plugin.getEconomyManager().getBalance(player.getUniqueId(), "money");
        lines.add(ColorUtil.parse("<gray>Money: </gray><yellow>λ " + money + "</yellow>"));

        lines.add(Component.empty());
        lines.add(ColorUtil.parse("<gray>Pos: </gray><white>" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ() + "</white>"));
        lines.add(ColorUtil.parse("<gray>Deaths: </gray><red>" + player.getStatistic(Statistic.DEATHS) + "</red>"));
        lines.add(ColorUtil.parse("<gray>Kills: </gray><green>" + player.getStatistic(Statistic.MOB_KILLS) + "</green>"));
        lines.add(Component.empty());
        lines.add(ColorUtil.parse("<gradient:#54DAF4:#545EB6>worldofnormies.com</gradient>"));

        // Update objective
        Collections.reverse(lines);

        // Proper way with Teams to support long lines and components
        for (int i = 0; i < 15; i++) {
            String teamName = "line_" + i;
            org.bukkit.scoreboard.Team team = sb.getTeam(teamName);
            if (team == null) team = sb.registerNewTeam(teamName);

            String entry = getEntry(i);
            if (!team.hasEntry(entry)) team.addEntry(entry);

            if (i < lines.size()) {
                team.prefix(lines.get(i));
                obj.getScore(entry).setScore(i);
            } else {
                sb.resetScores(entry);
            }
        }

        if (player.getScoreboard() != sb) {
            player.setScoreboard(sb);
        }
    }

    private String getEntry(int i) {
        return "§" + Integer.toHexString(i) + "§r";
    }

    public void removePlayer(Player player) {
        scoreboards.remove(player.getUniqueId());
        disabledPlayers.remove(player.getUniqueId());
    }
}
