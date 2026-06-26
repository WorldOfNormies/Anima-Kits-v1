package com.worldofnormies.animaranks;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animaeconomy.AnimaEconomyManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AnimaRankCommand {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final AnimaKitsPlugin plugin;
    private final RankManager rankManager;
    private final AnimaEconomyManager economyManager;

    public AnimaRankCommand(AnimaKitsPlugin plugin, RankManager rankManager, AnimaEconomyManager economyManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
        this.economyManager = economyManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) return false;
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "rank" -> handleRank(sender, args);
            case "ranks" -> handleRanks(sender);
            default -> { return false; }
        }
        return true;
    }

    private void handleRanks(CommandSender sender) {
        if (!(sender instanceof Player player)) return;
        new com.worldofnormies.animakits.gui.AnimaRankMainGUI(plugin, player).open();
    }

    private void handleRank(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "add" -> {
                if (args.length < 4) { sender.sendMessage(MM.deserialize("<red>Usage: /anima rank add <name> <priority></red>")); return; }
                String name = args[2];
                int priority = Integer.parseInt(args[3]);
                rankManager.addRank(new Rank(name, priority));
                sender.sendMessage(MM.deserialize("<green>Rank " + name + " added.</green>"));
            }
            case "remove" -> {
                if (args.length < 3) { sender.sendMessage(MM.deserialize("<red>Usage: /anima rank remove <name></red>")); return; }
                rankManager.removeRank(args[2]);
                sender.sendMessage(MM.deserialize("<green>Rank removed.</green>"));
            }
            case "set" -> handleSet(sender, args);
            case "clear" -> handleClear(sender, args);
            case "isrankable" -> {
                if (args.length < 4) { sender.sendMessage(MM.deserialize("<red>Usage: /anima rank isrankable <rank> <true|false> [playtime] [price]</red>")); return; }
                Rank rank = rankManager.getRank(args[2]);
                if (rank == null) { sender.sendMessage(MM.deserialize("<red>Rank not found.</red>")); return; }
                rank.setRankable(Boolean.parseBoolean(args[3]));
                if (args.length >= 5) rank.setRequiredPlaytime(Long.parseLong(args[4]));
                if (args.length >= 6) rank.setPrice(Double.parseDouble(args[5]));
                sender.sendMessage(MM.deserialize("<green>Updated isrankable for " + rank.getName() + ".</green>"));
            }
            case "isbuyable" -> {
                if (args.length < 4) { sender.sendMessage(MM.deserialize("<red>Usage: /anima rank isbuyable <rank> <true|false> [price]</red>")); return; }
                Rank rank = rankManager.getRank(args[2]);
                if (rank == null) { sender.sendMessage(MM.deserialize("<red>Rank not found.</red>")); return; }
                rank.setBuyable(Boolean.parseBoolean(args[3]));
                if (args.length >= 5) rank.setPrice(Double.parseDouble(args[4]));
                sender.sendMessage(MM.deserialize("<green>Updated isbuyable for " + rank.getName() + ".</green>"));
            }
            case "promote" -> {
                if (args.length < 4) { sender.sendMessage(MM.deserialize("<red>Usage: /anima rank promote <player> <rank></red>")); return; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                String rankName = args[3];
                if (rankManager.getRank(rankName) == null) { sender.sendMessage(MM.deserialize("<red>Rank not found.</red>")); return; }
                rankManager.setPlayerRank(target.getUniqueId(), rankName);
                sender.sendMessage(MM.deserialize("<green>Promoted " + target.getName() + " to " + rankName + ".</green>"));
            }
            case "rankup" -> handleRankUp(sender);
            default -> sendUsage(sender);
        }
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 5) { sender.sendMessage(MM.deserialize("<red>Usage: /anima rank set <rank> <prefix|suffix|colorname|chatcolor> <value></red>")); return; }
        Rank rank = rankManager.getRank(args[2]);
        if (rank == null) { sender.sendMessage(MM.deserialize("<red>Rank not found.</red>")); return; }
        String type = args[3].toLowerCase();
        String value = joinArgs(args, 4);

        switch (type) {
            case "prefix" -> rank.setPrefix(value);
            case "suffix" -> rank.setSuffix(value);
            case "colorname" -> rank.setNameColor(value);
            case "chatcolor" -> rank.setChatColor(value);
        }
        sender.sendMessage(MM.deserialize("<green>Updated " + type + " for rank " + rank.getName() + ".</green>"));
    }

    private void handleClear(CommandSender sender, String[] args) {
        if (args.length < 4) { sender.sendMessage(MM.deserialize("<red>Usage: /anima rank clear <rank> <prefix|suffix|colorname|chatcolor></red>")); return; }
        Rank rank = rankManager.getRank(args[2]);
        if (rank == null) { sender.sendMessage(MM.deserialize("<red>Rank not found.</red>")); return; }
        String type = args[3].toLowerCase();

        switch (type) {
            case "prefix" -> rank.setPrefix("");
            case "suffix" -> rank.setSuffix("");
            case "colorname" -> rank.setNameColor("");
            case "chatcolor" -> rank.setChatColor("");
        }
        sender.sendMessage(MM.deserialize("<green>Cleared " + type + " for rank " + rank.getName() + ".</green>"));
    }

    private void handleRankUp(CommandSender sender) {
        if (!(sender instanceof Player player)) return;
        Rank current = rankManager.getPlayerRank(player.getUniqueId());
        // Simple logic for finding next rank (priority - 1)
        Rank next = null;
        for (Rank r : rankManager.getAllRanks()) {
            if (r.getPriority() == current.getPriority() - 1) {
                next = r;
                break;
            }
        }

        if (next == null || !next.isRankable()) {
            player.sendMessage(MM.deserialize("<red>No rank-up available.</red>"));
            return;
        }

        long playtime = rankManager.getPlaytime(player.getUniqueId());
        if (playtime < next.getRequiredPlaytime()) {
            player.sendMessage(MM.deserialize("<red>Insufficient playtime.</red>"));
            return;
        }

        if (economyManager.withdrawMoney(player.getUniqueId(), next.getPrice())) {
            rankManager.setPlayerRank(player.getUniqueId(), next.getName());
            player.sendMessage(MM.deserialize("<green>Ranked up to " + next.getName() + "!</green>"));
        } else {
            player.sendMessage(MM.deserialize("<red>Insufficient funds.</red>"));
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(MM.deserialize("<red>Invalid rank command usage.</red>"));
    }

    private String joinArgs(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (i > from) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
