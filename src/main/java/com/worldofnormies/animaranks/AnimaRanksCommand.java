package com.worldofnormies.animaranks;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.util.MessageUtil;
import com.worldofnormies.animaranks.gui.AnimaRanksGUI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class AnimaRanksCommand implements CommandExecutor {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final AnimaKitsPlugin plugin;

    public AnimaRanksCommand(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // args[0] is "rank" or "ranks"
        if (args.length == 1) {
            if (sender instanceof Player p) new AnimaRanksGUI(plugin, p).open();
            else MessageUtil.err(sender, "Only players can open the GUI.");
            return true;
        }

        String sub = args[1].toLowerCase();
        return switch (sub) {
            case "add" -> handleAdd(sender, args);
            case "set" -> handleSet(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender);
            case "clear" -> handleClear(sender, args);
            case "promote" -> handlePromote(sender, args);
            case "permissions" -> handlePermissions(sender, args);
            default -> { MessageUtil.err(sender, "Unknown rank command."); yield true; }
        };
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (args.length < 4) { MessageUtil.err(sender, "/anima rank add <name> <hierarchy>"); return true; }
        String name = args[2];
        try {
            int h = Integer.parseInt(args[3]);
            Rank rank = new Rank(name, h);
            plugin.getRankManager().addRank(rank);
            MessageUtil.send(sender, MM.deserialize("<green>Rank " + name + " created with hierarchy " + h + "!</green>"));
        } catch (NumberFormatException e) {
            MessageUtil.err(sender, "Hierarchy must be a number.");
        }
        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (args.length < 4) return true;
        String type = args[2].toLowerCase();
        if (type.equals("add")) {
            // /anima rank set add <player> <rank> <time>
            if (args.length < 5) return true;
            Player target = Bukkit.getPlayer(args[3]);
            if (target == null) { MessageUtil.err(sender, "Player not found."); return true; }
            plugin.getRankManager().setPlayerRank(target.getUniqueId(), args[4]);
            MessageUtil.send(sender, MM.deserialize("<green>Set " + target.getName() + " to rank " + args[4] + "!</green>"));
            return true;
        }

        // /anima rank set <prefix|suffix|colorname|chatcolor> <rank> <value>
        if (args.length < 5) return true;
        Rank rank = plugin.getRankManager().getRank(args[3]);
        if (rank == null) { MessageUtil.err(sender, "Rank not found."); return true; }
        String val = joinArgs(args, 4);
        switch (type) {
            case "prefix" -> rank.setPrefix(val);
            case "suffix" -> rank.setSuffix(val);
            case "colorname" -> rank.setNameColor(val);
            case "chatcolor" -> rank.setChatColor(val);
        }
        plugin.getRankManager().saveRanks();
        MessageUtil.send(sender, MM.deserialize("<green>Updated " + type + " for rank " + rank.getId() + "!</green>"));
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 3) return true;
        plugin.getRankManager().deleteRank(args[2]);
        MessageUtil.send(sender, MM.deserialize("<red>Rank " + args[2] + " deleted.</red>"));
        return true;
    }

    private boolean handleList(CommandSender sender) {
        MessageUtil.send(sender, MM.deserialize("<blue>Ranks List:</blue>"));
        for (Rank r : plugin.getRankManager().getAllRanks()) {
            MessageUtil.send(sender, MM.deserialize("<gray>- " + r.getId() + " (H:" + r.getHierarchy() + ")</gray>"));
        }
        return true;
    }

    private boolean handleClear(CommandSender sender, String[] args) {
        if (args.length < 4) return true;
        Rank rank = plugin.getRankManager().getRank(args[3]);
        if (rank == null) return true;
        switch (args[2].toLowerCase()) {
            case "prefix" -> rank.setPrefix("");
            case "suffix" -> rank.setSuffix("");
            case "chatcolor" -> rank.setChatColor("");
            case "colorname" -> rank.setNameColor("");
        }
        plugin.getRankManager().saveRanks();
        MessageUtil.send(sender, MM.deserialize("<green>Cleared " + args[2] + " for rank " + rank.getId()));
        return true;
    }

    private boolean handlePromote(CommandSender sender, String[] args) {
        if (args.length < 4) return true;
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) return true;
        plugin.getRankManager().setPlayerRank(target.getUniqueId(), args[3]);
        MessageUtil.send(sender, MM.deserialize("<green>Promoted " + target.getName() + " to " + args[3]));
        return true;
    }

    private boolean handlePermissions(CommandSender sender, String[] args) {
        // /anima rank permissions <rank> <node> <true|false>
        if (args.length < 5) { MessageUtil.err(sender, "/anima rank permissions <rank> <node> <true|false>"); return true; }
        Rank rank = plugin.getRankManager().getRank(args[2]);
        if (rank == null) { MessageUtil.err(sender, "Rank not found."); return true; }
        String node = args[3];
        boolean val = Boolean.parseBoolean(args[4]);
        if (val) {
            if (!rank.getPermissions().contains(node)) rank.getPermissions().add(node);
        } else {
            rank.getPermissions().remove(node);
        }
        plugin.getRankManager().saveRanks();
        MessageUtil.send(sender, MM.deserialize("<green>Updated permission " + node + " for rank " + rank.getId()));
        return true;
    }

    private String joinArgs(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (i > from) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString().trim();
    }
}
