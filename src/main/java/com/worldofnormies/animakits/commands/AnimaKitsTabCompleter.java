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
 * AnimaKitsTabCompleter – provides context-aware tab completion for /anima.
 *
 * Completion tree:
 *   /anima kits
 *   /anima kits display      <kit>
 *   /anima kits edit         add|remove|rename   <kit>   [new-name]
 *   /anima kits lore         add|edit|remove     <kit>   [line#]  [text]
 *   /anima kits open         <kit>
 *   /anima kits clonekit     <kit>               <new-name>
 *   /anima kits give         <player>            <kit>    <amount>
 *   /anima kits giveall      <kit>               <amount>
 *   /anima kits reload
 *   /anima kits help
 *   /anima kits permission   add|remove|show
 *     permission add         <perm>              <player>  <duration>
 *     permission remove      <perm>              <player>
 *     permission show        <player>
 */
public class AnimaKitsTabCompleter implements TabCompleter {

    // ── Known sub-command tokens ───────────────────────────────────

    private static final List<String> SUB1 = List.of("kits", "kit");

    private static final List<String> SUB2 = List.of(
            "display", "edit", "lore", "open", "clonekit",
            "give", "giveall", "reload", "help", "permission");

    private static final List<String> EDIT_ACTIONS   = List.of("add", "remove", "rename");
    private static final List<String> LORE_ACTIONS   = List.of("add", "edit", "remove");
    private static final List<String> PERM_ACTIONS   = List.of("add", "remove", "show");

    private static final List<String> AMOUNTS        = List.of("1", "2", "3", "5", "10", "64");
    private static final List<String> DURATIONS      = List.of("-1", "30s", "1m", "5m", "30m",
            "1h", "6h", "12h", "1d", "7d", "30d");

    private static final List<String> KNOWN_PERMS = List.of(
            "anima.kits.use", "anima.kits.display", "anima.kits.edit.add",
            "anima.kits.edit.remove", "anima.kits.edit.rename",
            "anima.kits.lore.add", "anima.kits.lore.edit", "anima.kits.lore.remove",
            "anima.kits.open", "anima.kits.clonekit", "anima.kits.give",
            "anima.kits.giveall", "anima.kits.reload", "anima.kits.help",
            "anima.kits.permission.add", "anima.kits.permission.remove",
            "anima.kits.permission.show", "anima.kits.*");

    // ── Fields ────────────────────────────────────────────────────

    private final AnimaKitsPlugin plugin;

    public AnimaKitsTabCompleter(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Entry point ───────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        // args[0] = "kits" | "kit"
        if (args.length == 1) return filter(args[0], SUB1);
        if (args.length == 2) return filter(args[1], SUB2);

        String sub2 = args[1].toLowerCase();
        return switch (sub2) {
            case "display"    -> completeDisplay(args);
            case "edit"       -> completeEdit(args);
            case "lore"       -> completeLore(args);
            case "open"       -> args.length == 3 ? kitNames(args[2]) : List.of();
            case "clonekit"   -> completeCloneKit(args);
            case "give"       -> completeGive(args);
            case "giveall"    -> completeGiveAll(args);
            case "permission" -> completePermission(args);
            default           -> List.of();
        };
    }

    // ── Sub-command completors ─────────────────────────────────────

    /** /anima kits display <kit> */
    private List<String> completeDisplay(String[] args) {
        if (args.length == 3) return kitNames(args[2]);
        return List.of();
    }

    /** /anima kits edit <add|remove|rename> <kit> [new-name] */
    private List<String> completeEdit(String[] args) {
        if (args.length == 3) return filter(args[2], EDIT_ACTIONS);
        String action = args[2].toLowerCase();
        return switch (action) {
            case "remove", "rename" -> args.length == 4 ? kitNames(args[3]) : List.of();
            case "add"              -> List.of(); // free text: new kit name
            default                 -> List.of();
        };
    }

    /** /anima kits lore <add|edit|remove> <kit> [line#] [text] */
    private List<String> completeLore(String[] args) {
        if (args.length == 3) return filter(args[2], LORE_ACTIONS);
        String action = args[2].toLowerCase();
        if (args.length == 4) return kitNames(args[3]);
        if (args.length == 5 && !action.equals("add")) return loreLineNumbers(args[3]);
        return List.of();
    }

    /** /anima kits clonekit <kit> <new-name> */
    private List<String> completeCloneKit(String[] args) {
        if (args.length == 3) return kitNames(args[2]);
        return List.of(); // new name: free text
    }

    /** /anima kits give <player> <kit> <amount> */
    private List<String> completeGive(String[] args) {
        if (args.length == 3) return onlinePlayers(args[2]);
        if (args.length == 4) return kitNames(args[3]);
        if (args.length == 5) return filter(args[4], AMOUNTS);
        return List.of();
    }

    /** /anima kits giveall <kit> <amount> */
    private List<String> completeGiveAll(String[] args) {
        if (args.length == 3) return kitNames(args[2]);
        if (args.length == 4) return filter(args[3], AMOUNTS);
        return List.of();
    }

    /**
     * /anima kits permission <add|remove|show>
     *   add    <perm> <player> <duration>
     *   remove <perm> <player>
     *   show   <player>
     */
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
            default -> List.of();
        };
    }

    // ── Utility helpers ───────────────────────────────────────────

    /** All known kit IDs that start with {@code partial}. */
    private List<String> kitNames(String partial) {
        KitManager km = plugin.getKitManager();
        return filter(partial, km.getKitIds());
    }

    /** Online player names that start with {@code partial}. */
    private List<String> onlinePlayers(String partial) {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            names.add(p.getName());
        }
        return filter(partial, names);
    }

    /**
     * Build lore line number suggestions (1-based) for the given kit name.
     */
    private List<String> loreLineNumbers(String kitId) {
        var kit = plugin.getKitManager().getKitByName(kitId);
        if (kit == null) return List.of();
        List<String> nums = new ArrayList<>();
        for (int i = 1; i <= kit.getLore().size(); i++) {
            nums.add(String.valueOf(i));
        }
        return nums;
    }

    /**
     * Case-insensitive prefix filter using Bukkit's {@link StringUtil}.
     */
    private List<String> filter(String partial, List<String> options) {
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(partial, options, result);
        Collections.sort(result);
        return result;
    }
}
