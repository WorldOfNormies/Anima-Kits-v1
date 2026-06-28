package com.worldofnormies.animaranks.commands;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animaranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AnimaRankTabCompleter – tab completion for /anima rank [...].
 * Called from AnimaKitsTabCompleter when args[0] == "rank".
 */
public class AnimaRankTabCompleter {

    private static final List<String> RANK_SUBS = List.of(
        "add", "remove", "list", "set", "clear", "promote",
        "isrankable", "rankup", "isbuyable", "permissions"
    );

    private static final List<String> SET_SUBS = List.of(
        "prefix", "suffix", "colorname", "chatcolor", "add", "remove"
    );

    private static final List<String> CLEAR_FIELDS = List.of(
        "prefix", "suffix", "colorname", "chatcolor"
    );

    private static final List<String> BOOL_VALUES  = List.of("true", "false");
    private static final List<String> DURATIONS    = List.of("-1", "30s", "5m", "30m", "1h", "12h", "1d", "7d", "30d");
    private static final List<String> PLAYTIMES    = List.of("1h", "5h", "10h", "1d", "7d", "30d", "100d");
    private static final List<String> HIERARCHIES  = List.of("0", "1", "2", "3", "4", "5", "10", "50", "99");

    private final AnimaKitsPlugin plugin;

    public AnimaRankTabCompleter(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Entry point. args[0] = "rank", args[1] = sub, args[2+] = further.
     */
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 2) return filter(args[1], RANK_SUBS);

        String sub = args[1].toLowerCase();

        return switch (sub) {
            case "add" -> {
                // /anima rank add <id> <hierarchy>
                if (args.length == 3) yield List.of("<id>");
                if (args.length == 4) yield filter(args[3], HIERARCHIES);
                yield List.of();
            }
            case "remove" -> {
                if (args.length == 3) yield rankIds(args[2]);
                yield List.of();
            }
            case "list" -> List.of();
            case "set" -> handleSetTab(sender, args);
            case "clear" -> {
                // /anima rank clear <field> <rankId>
                if (args.length == 3) yield filter(args[2], CLEAR_FIELDS);
                if (args.length == 4) yield rankIds(args[3]);
                yield List.of();
            }
            case "promote" -> {
                // /anima rank promote <player> <rankId>
                if (args.length == 3) yield onlinePlayers(args[2]);
                if (args.length == 4) yield rankIds(args[3]);
                yield List.of();
            }
            case "isrankable" -> {
                // /anima rank isrankable <id> <true|false> <playtime> <price>
                if (args.length == 3) yield rankIds(args[2]);
                if (args.length == 4) yield filter(args[3], BOOL_VALUES);
                if (args.length == 5) yield filter(args[4], PLAYTIMES);
                if (args.length == 6) yield filter(args[5], List.of("0", "100", "500", "1000", "5000"));
                yield List.of();
            }
            case "rankup" -> {
                // /anima rank rankup [rankId]
                if (args.length == 3) yield rankIds(args[2]);
                yield List.of();
            }
            case "isbuyable" -> {
                // /anima rank isbuyable <id> <true|false> <price> <duration|-1>
                if (args.length == 3) yield rankIds(args[2]);
                if (args.length == 4) yield filter(args[3], BOOL_VALUES);
                if (args.length == 5) yield filter(args[4], List.of("0", "100", "500", "1000", "5000"));
                if (args.length == 6) yield filter(args[5], DURATIONS);
                yield List.of();
            }
            case "permissions" -> {
                // /anima rank permissions <rankId> [node] [true|false]
                if (args.length == 3) yield rankIds(args[2]);
                if (args.length == 4) yield allPermissionNodes(args[3]);
                if (args.length == 5) yield filter(args[4], BOOL_VALUES);
                yield List.of();
            }
            default -> {
                // /anima rank <rankId> list
                if (args.length == 2) yield rankIds(args[1]);
                if (args.length == 3) yield filter(args[2], List.of("list"));
                yield List.of();
            }
        };
    }

    private List<String> handleSetTab(CommandSender sender, String[] args) {
        // /anima rank set <sub> ...
        if (args.length == 3) return filter(args[2], SET_SUBS);
        String setSub = args[2].toLowerCase();
        return switch (setSub) {
            case "prefix", "suffix", "colorname", "chatcolor" -> {
                // /anima rank set <field> <rankId> <value...>
                if (args.length == 4) yield rankIds(args[3]);
                yield List.of("<MiniMessage value>");
            }
            case "add" -> {
                // /anima rank set add <player> <rankId> <duration>
                if (args.length == 4) yield onlinePlayers(args[3]);
                if (args.length == 5) yield rankIds(args[4]);
                if (args.length == 6) yield filter(args[5], DURATIONS);
                yield List.of();
            }
            case "remove" -> {
                // /anima rank set remove <player>
                if (args.length == 4) yield onlinePlayers(args[3]);
                yield List.of();
            }
            default -> List.of();
        };
    }

    private List<String> rankIds(String partial) {
        List<String> ids = new ArrayList<>();
        for (Rank r : plugin.getRankManager().getAllRanks()) ids.add(r.getId());
        return filter(partial, ids);
    }

    private List<String> onlinePlayers(String partial) {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
        return filter(partial, names);
    }

    private List<String> allPermissionNodes(String partial) {
        List<String> nodes = new ArrayList<>();
        // Collect all registered Bukkit permissions
        for (Permission p : Bukkit.getPluginManager().getPermissions()) {
            nodes.add(p.getName());
        }
        Collections.sort(nodes);
        return filter(partial, nodes);
    }

    private List<String> filter(String partial, List<String> options) {
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(partial, options, result);
        Collections.sort(result);
        return result;
    }
}
