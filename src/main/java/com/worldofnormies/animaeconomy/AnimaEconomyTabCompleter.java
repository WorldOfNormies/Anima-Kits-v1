package com.worldofnormies.animaeconomy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnimaEconomyTabCompleter {

    private static final List<String> ECON_SUBS = Arrays.asList("set", "add", "take");
    private static final List<String> MAIN_ECON_SUBS = Arrays.asList("balance", "pay", "withdraw", "economy", "tokens");

    public static List<String> complete(String[] args) {
        if (args.length == 1) {
            return filter(args[0], MAIN_ECON_SUBS);
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "balance" -> {
                if (args.length == 2) return null; // Player names
            }
            case "pay" -> {
                if (args.length == 2) return null; // Player names
            }
            case "economy", "tokens" -> {
                if (args.length == 2) return filter(args[1], ECON_SUBS);
                if (args.length == 3) return null; // Player names
            }
        }

        return Collections.emptyList();
    }

    private static List<String> filter(String partial, List<String> options) {
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(partial, options, result);
        Collections.sort(result);
        return result;
    }
}
