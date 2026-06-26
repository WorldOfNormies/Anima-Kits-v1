package com.worldofnormies.animakits.commands;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.manager.KitManager;
import com.worldofnormies.animaitemedit.ItemEditTabCompleter;
import com.worldofnormies.animaeconomy.AnimaEconomyTabCompleter;
import com.worldofnormies.animaranks.AnimaRankTabCompleter;
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
 * AnimaKitsTabCompleter – provides context-aware tab completion for /anima.
 * Handles both /anima kits [...] and /anima itemedit [...].
 */
public class AnimaKitsTabCompleter implements TabCompleter {

    private static final List<String> ROOT_SUBS = List.of("kits", "kit", "itemedit", "balance", "pay", "economy", "tokens", "rank", "ranks");
    private static final List<String> SUB1       = List.of("kits", "kit");

    private static final List<String> ADMIN_SUB2 = List.of(
            "claim", "add", "delete", "rename", "lore", "clonekit",
            "give", "giveall", "reload", "help", "permission",
            "list", "setcooldown", "singleclaim", "onjoinnew");

    private static final List<String> PLAYER_SUB2 = List.of("claim", "list", "help");

    private static final List<String> LORE_ACTIONS   = List.of("add", "edit", "remove");
    private static final List<String> PERM_ACTIONS   = List.of("add", "remove", "show", "claim", "claimfree");

    private static final List<String> AMOUNTS        = List.of("1", "2", "3", "5", "10", "64");
    private static final List<String> DURATIONS      = List.of("-1", "30s", "5m", "30m", "1h", "12h", "1d", "7d");
    private static final List<String> BOOLEAN_VALUES = List.of("true", "false");

    private static final List<String> KNOWN_PERMS = List.of(
            "anima.kits.*", "anima.kits.use", "anima.kits.add", "anima.kits.delete",
            "anima.kits.rename", "anima.kits.lore.add", "anima.kits.lore.edit",
            "anima.kits.lore.remove", "anima.kits.clonekit", "anima.kits.give",
            "anima.kits.giveall", "anima.kits.reload", "anima.kits.help",
            "anima.kits.permission.add", "anima.kits.permission.remove",
            "anima.kits.permission.show", "anima.kits.permission.claim",
            "anima.kits.permission.claimfree", "anima.kits.claim", "anima.kits.claim.*",
            "anima.kits.list", "anima.kits.setcooldown", "anima.kits.singleclaim");

    private final AnimaKitsPlugin plugin;

    public AnimaKitsTabCompleter(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(args[0], ROOT_SUBS);
        }

        // Route itemedit completions to ItemEditTabCompleter
        if (args[0].equalsIgnoreCase("itemedit")) {
            return ItemEditTabCompleter.complete(sender, args);
        }

        if (args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("pay") ||
            args[0].equalsIgnoreCase("economy") || args[0].equalsIgnoreCase("tokens")) {
            return AnimaEconomyTabCompleter.complete(args);
        }

        if (args[0].equalsIgnoreCase("rank") || args[0].equalsIgnoreCase("ranks")) {
            return AnimaRankTabCompleter.complete(plugin.getRankManager(), args);
        }

        if (args.length == 2 && SUB1.contains(args[0].toLowerCase())) {
            if (sender.hasPermission("anima.kits.add") || sender.hasPermission("anima.kits.*")) {
                return filter(args[1], ADMIN_SUB2);
            }
            return filter(args[1], PLAYER_SUB2);
        }

        if (args.length > 2 && SUB1.contains(args[0].toLowerCase())) {
            String sub2 = args[1].toLowerCase();
            return switch (sub2) {
                case "claim"        -> handleClaimTab(sender, args);
                case "delete"       -> handleMultiWordKitTab(sender, args, 2, null);
                case "setcooldown"  -> handleMultiWordKitTab(sender, args, 2, DURATIONS);
                case "singleclaim"  -> handleMultiWordKitTab(sender, args, 2, BOOLEAN_VALUES);
                case "onjoinnew"    -> handleMultiWordKitTab(sender, args, 2, null);
                case "rename"       -> args.length == 3 ? adminKitNames(sender, args[2]) : List.of();
                case "clonekit"     -> args.length == 3 ? adminKitNames(sender, args[2]) : List.of();
                case "lore"         -> handleLoreTab(sender, args);
                case "give"         -> handleGiveTab(sender, args);
                case "giveall"      -> handleGiveAllTab(sender, args);
                case "permission"   -> handlePermissionTab(sender, args);
                default             -> List.of();
            };
        }

        return List.of();
    }

    private List<String> handleClaimTab(CommandSender sender, String[] args) {
        if (args.length != 3) return List.of();
        if (!(sender instanceof Player player)) return List.of();
        List<String> rawIds = plugin.getKitManager().getKitIds();
        if (player.hasPermission("anima.kits.*") || player.hasPermission("anima.kits.claim.*")) return filter(args[2], rawIds);
        List<String> authorized = new ArrayList<>();
        for (String id : rawIds) {
            if (player.hasPermission("anima.kits.claim." + id) || plugin.getPermissionManager().hasClaimPermission(player.getUniqueId(), id)) authorized.add(id);
        }
        return filter(args[2], authorized);
    }

    private List<String> handleLoreTab(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.kits.lore.add") && !sender.hasPermission("anima.kits.*")) return List.of();
        if (args.length == 3) return filter(args[2], LORE_ACTIONS);
        String action = args[2].toLowerCase();
        if (args.length == 4) return adminKitNames(sender, args[3]);
        if (args.length == 5 && (action.equals("edit") || action.equals("remove"))) return filter(args[4], loreLineNumbers(args[3]));
        return List.of();
    }

    private List<String> handleGiveTab(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.kits.give") && !sender.hasPermission("anima.kits.*")) return List.of();
        if (args.length == 3) return targetSelectablePlayers(args[2]);
        if (args.length == 4) return adminKitNames(sender, args[3]);
        if (args.length == 5) return filter(args[4], AMOUNTS);
        return List.of();
    }

    private List<String> handleGiveAllTab(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.kits.giveall") && !sender.hasPermission("anima.kits.*")) return List.of();
        return handleMultiWordKitTab(sender, args, 2, AMOUNTS);
    }

    private List<String> handleMultiWordKitTab(CommandSender sender, String[] args, int kitArgStart, List<String> finalOptions) {
        if (!sender.hasPermission("anima.kits.*") && !sender.hasPermission("anima.kits.add")) return List.of();
        List<String> kitIds = plugin.getKitManager().getKitIds();
        String typed = joinArgsUpTo(args, kitArgStart, args.length);
        String typedMinusLast = joinArgsUpTo(args, kitArgStart, args.length - 1);
        String lastArg = args[args.length - 1];
        boolean exactMatch = kitIds.stream().anyMatch(id -> id.equalsIgnoreCase(typed));
        boolean prevExact  = !typedMinusLast.isEmpty() && kitIds.stream().anyMatch(id -> id.equalsIgnoreCase(typedMinusLast));
        if (exactMatch && finalOptions != null) return filter(lastArg, finalOptions);
        if (prevExact && finalOptions != null) return filter(lastArg, finalOptions);
        String partialTyped = typed.toLowerCase();
        List<String> nextWords = new ArrayList<>();
        for (String id : kitIds) {
            if (id.toLowerCase().startsWith(partialTyped)) {
                String[] parts = id.split(" ");
                int wordCount = typed.isEmpty() ? 0 : typed.split(" ").length;
                if (wordCount < parts.length) nextWords.add(parts[wordCount]);
            }
        }
        return new ArrayList<>(new java.util.LinkedHashSet<>(nextWords));
    }

    private List<String> handlePermissionTab(CommandSender sender, String[] args) {
        if (args.length == 3) return filter(args[2], PERM_ACTIONS);
        String action = args[2].toLowerCase();
        return switch (action) {
            case "add" -> {
                if (args.length == 4) yield filter(args[3], KNOWN_PERMS);
                if (args.length == 5) yield targetSelectablePlayers(args[4]);
                if (args.length == 6) yield filter(args[5], DURATIONS);
                yield List.of();
            }
            case "remove" -> {
                if (args.length == 4) yield filter(args[3], KNOWN_PERMS);
                if (args.length == 5) yield targetSelectablePlayers(args[4]);
                yield List.of();
            }
            case "show" -> {
                if (args.length == 4) yield targetSelectablePlayers(args[3]);
                yield List.of();
            }
            case "claim", "claimfree" -> {
                if (args.length == 4) yield targetSelectablePlayers(args[3]);
                List<String> kitIds = plugin.getKitManager().getKitIds();
                String lastArg = args[args.length - 1];
                String typedMinusLast = joinArgsUpTo(args, 4, args.length - 1);
                String typedMinustwo  = joinArgsUpTo(args, 4, args.length - 2);
                String secondLast = args.length > 5 ? args[args.length - 2] : "";
                boolean isBool = secondLast.equalsIgnoreCase("true") || secondLast.equalsIgnoreCase("false");
                if (!typedMinustwo.isEmpty() && kitIds.stream().anyMatch(id -> id.equalsIgnoreCase(typedMinustwo)) && isBool) yield filter(lastArg, DURATIONS);
                if (!typedMinusLast.isEmpty() && kitIds.stream().anyMatch(id -> id.equalsIgnoreCase(typedMinusLast))) yield filter(lastArg, BOOLEAN_VALUES);
                String partialTyped = joinArgsUpTo(args, 4, args.length).toLowerCase();
                List<String> nextWords = new ArrayList<>();
                for (String id : kitIds) {
                    if (id.toLowerCase().startsWith(partialTyped)) {
                        String[] parts = id.split(" ");
                        int wordCount = partialTyped.isEmpty() ? 0 : partialTyped.split(" ").length;
                        if (wordCount < parts.length) nextWords.add(parts[wordCount]);
                    }
                }
                yield new ArrayList<>(new java.util.LinkedHashSet<>(nextWords));
            }
            default -> List.of();
        };
    }

    private List<String> adminKitNames(CommandSender sender, String partial) {
        if (!sender.hasPermission("anima.kits.*") && !sender.hasPermission("anima.kits.add")) return List.of();
        return filter(partial, plugin.getKitManager().getKitIds());
    }

    private List<String> targetSelectablePlayers(String partial) {
        List<String> suggestions = new ArrayList<>(List.of("@a", "@p", "@r", "@e"));
        for (Player p : Bukkit.getOnlinePlayers()) suggestions.add(p.getName());
        return filter(partial, suggestions);
    }

    private List<String> loreLineNumbers(String kitId) {
        var kit = plugin.getKitManager().getKitByPlainName(kitId);
        if (kit == null) return List.of();
        List<String> nums = new ArrayList<>();
        for (int i = 1; i <= kit.getLore().size(); i++) nums.add(String.valueOf(i));
        return nums;
    }

    private List<String> filter(String partial, List<String> options) {
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(partial, options, result);
        Collections.sort(result);
        return result;
    }

    private String joinArgsUpTo(String[] args, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to && i < args.length; i++) { if (i > from) sb.append(' '); sb.append(args[i]); }
        return sb.toString().trim();
    }
}
