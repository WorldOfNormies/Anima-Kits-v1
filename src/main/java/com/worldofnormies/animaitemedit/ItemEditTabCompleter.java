package com.worldofnormies.animaitemedit;

import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ItemEditTabCompleter – tab completion for /anima itemedit.
 * Called from AnimaKitsTabCompleter when args[0] == "itemedit".
 *
 * args[0] = "itemedit"
 * args[1] = sub  (prefix / suffix / rename / lore / enchant / ...)
 * args[2+] = further args
 */
public final class ItemEditTabCompleter {

    private static final List<String> SUBS = List.of(
            "prefix", "suffix", "rename", "lore", "enchant",
            "unbreakable", "gloweffect", "repair"
    );
    private static final List<String> SET_REMOVE_EDIT     = List.of("set", "remove", "edit");
    private static final List<String> ADD_REMOVE_EDIT_CLEAR = List.of("add", "remove", "edit", "clear");
    private static final List<String> ADD_REMOVE_EDIT     = List.of("add", "remove", "edit");
    private static final List<String> BOOL                 = List.of("true", "false");
    private static final List<String> LEVELS               = List.of("1", "2", "3", "4", "5", "10", "50", "100", "255");

    private static final List<String> ALL_ENCHANTS;

    static {
        ALL_ENCHANTS = new ArrayList<>();
        for (Enchantment e : Enchantment.values()) {
            ALL_ENCHANTS.add(e.getKey().getKey());
        }
        Collections.sort(ALL_ENCHANTS);
    }

    private ItemEditTabCompleter() {}

    /**
     * Entry point. {@code args} comes in as the full /anima args array:
     *   args[0] = "itemedit"
     *   args[1] = sub (prefix / suffix / rename / lore / enchant / ...)
     *   args[2+] = further args
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) return filter(args[0], List.of("itemedit")); // shouldn't normally hit here
        if (args.length == 2) return filter(args[1], SUBS);

        String sub = args[1].toLowerCase();
        return switch (sub) {
            case "prefix", "suffix", "rename" -> {
                if (args.length == 3) yield filter(args[2], SET_REMOVE_EDIT);
                yield List.of(); // free-text value
            }
            case "lore" -> {
                if (args.length == 3) yield filter(args[2], ADD_REMOVE_EDIT_CLEAR);
                yield List.of();
            }
            case "enchant" -> {
                if (args.length == 3) yield filter(args[2], ADD_REMOVE_EDIT);
                if (args.length == 4) yield filter(args[3], ALL_ENCHANTS);
                if (args.length == 5) yield filter(args[4], LEVELS);
                yield List.of();
            }
            case "unbreakable", "gloweffect" -> {
                if (args.length == 3) yield filter(args[2], BOOL);
                yield List.of();
            }
            case "repair" -> List.of();
            default -> List.of();
        };
    }

    private static List<String> filter(String partial, List<String> options) {
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(partial, options, result);
        Collections.sort(result);
        return result;
    }
}
