package com.worldofnormies.animakits.commands;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.manager.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AnimaKitsTabCompleter – provides auto-complete handling for free kit tags and @a targets.
 */
public class AnimaKitsTabCompleter implements TabCompleter {

    private static final List<String> SUB1 = List.of("kits", "kit");

    private static final List<String> SUB2 = List.of(
            "claim", "add", "delete", "rename", "lore", "clonekit",
            "give", "giveall", "reload", "help", "permission",
            "list", "setcooldown", "singleclaim");

    private static final List<String> LORE_ACTIONS   = List.of("add", "edit", "remove");
    private static final List<String> PERM_ACTIONS   = List.of("add", "remove", "show", "claim");

    private static final List<String> AMOUNTS        = List.of("1", "2", "3", "5", "10", "64");
    private static final List<String> DURATIONS      = List.of("-1", "30s", "1m", "5m", "30m", "1h", "6h", "12h", "1d", "7d", "30d");

    private static final List<String> KNOWN_PERMS = List.of(
            "anima.kits.use", "anima.kits.add", "anima.kits.delete",
            "anima.kits.rename", "anima.kits.lore.add", "anima.kits.lore.edit",
            "anima.kits.lore.remove", "anima.kits.clonekit", "anima.kits.give",
            "anima.kits.giveall", "anima.kits.reload", "anima.kits.help",
            "anima.kits.permission.add", "anima.kits.permission.remove",
            "anima.kits.permission.show", "anima.kits.claim", "anima.kits.*");

    private final AnimaKitsPlugin plugin;

    public AnimaKitsTabCompleter(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return filter(args[0], SUB1);
        if (args[0].equalsIgnoreCase("kits")) return List.of();
        if (args.length == 2) return filter(args[1], SUB2);

        String sub2 = args[1].toLowerCase();
        return switch (sub2) {
            case "claim"       -> args.length == 3 ? kitNames(args[2]) : List.of();
            case "add"         -> List.of();
            case "delete"      -> args.length == 3 ? kitNames(args[2]) : List.of();
            case "rename"      -> args.length == 3 ? kitNames(args[2]) : List.of();
            case "lore"        -> completeLore(args);
            case "clonekit"    -> args.length == 3 ? kitNames(args[2]) : List.of();
            case "give"        -> completeGive(args);
            case "giveall"     -> completeGiveAll(args);
            case "permission"  -> completePermission(args);
            case "setcooldown" -> args.length == 3 ? kitNames(args[2]) : List.of();
            case "singleclaim" -> completeSingleClaim(args);
            default            -> List.of();
        };
    }

    private List<String> completeLore(String[] args) {
        if (args.length == 3) return filter(args[2], LORE_ACTIONS);
        String action = args[2].toLowerCase();
        if (args.length == 4) return kitNames(args[3]);
        if (args.length == 5 && !action.equals("add")) return loreLineNumbers(args[3]);
        return List.of();
    }

    private List<String> completeGive(String[] args) {
        if (args.length == 3) {
            List<String> options = new ArrayList<>(onlinePlayers(""));
            options.add("@a");
            return filter(args[2], options);
        }
        if (args.length == 4) return kitNames(args[3]);
        if (args.length == 5) return filter(args[4], AMOUNTS);
        return List.of();
    }

    private List<String> completeGiveAll(String[] args) {
        if (args.length == 3) return kitNames(args[2]);
        if (args.length == 4) return filter(args[3], AMOUNTS);
        return List.of();
    }

    private List<String> completeSingleClaim(String[] args) {
        if (args.length == 3) return kitNames(args[2]);
        if (args.length == 4) return filter(args[3], List.of("true", "false"));
        return List.of();
    }

    private List<String> completePermission(String[] args) {
        if (args.length == 3) return filter(args[2], PERM_ACTIONS);
        
        String action = args[2].toLowerCase();
        return switch (action) {
            case "add" -> {
                if (args.length == 4) yield filter(args[3], KNOWN_PERMS);
                if (args.length == 5) yield onlinePlayers(args[4]);
                if (args.length == 6) yield filter(args[5], DURATIONS);
                yield List.of();
            }
            case "remove" -> {
                if (args.length == 4) yield filter(args[3], KNOWN_PERMS);
                if (args.length == 5) yield onlinePlayers(args[4]);
                yield List.of();
            }
            case "show" -> {
                if (args.length == 4) yield onlinePlayers(args[3]);
                yield List.of();
            }
            case "claim" -> {
                if (args.length == 4) {
                    List<String> options = new ArrayList<>(onlinePlayers(""));
                    options.add("free");
                    options.add("@a");
                    return filter(args[3], options);
                }
                if (args.length == 5) yield kitNames(args[4]);
                if (args.length == 6) yield filter(args[5], List.of("true", "false"));
                if (args.length == 7) yield filter(args[6], DURATIONS);
                yield List.of();
            }
            default -> List.of();
        };
    }

    private List<String> kitNames(String partial) {
        KitManager km = plugin.getKitManager();
        return filter(partial, km.getKitIds());
    }

    private List<String> onlinePlayers(String partial) {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            names.add(p.getName());
        }
        return filter(partial, names);
    }

    private List<String> loreLineNumbers(String kitId) {
        var kit = plugin.getKitManager().getKitByName(kitId);
        if (kit == null) return List.of();
        List<String> nums = new ArrayList<>();
        for (int i = 1; i <= kit.getLore().size(); i++) {
            nums.add(String.valueOf(i));
        }
        return nums;
    }

    private List<String> filter(String partial, List<String> options) {
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(partial, options, result);
        Collections.sort(result);
        return result;
    }
}