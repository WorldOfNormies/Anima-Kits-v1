package com.worldofnormies.animaranks;

import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnimaRankTabCompleter {
    private static final List<String> RANK_MAIN = Arrays.asList("add", "remove", "set", "clear", "promote", "rankup", "list", "isrankable", "isbuyable");
    private static final List<String> RANK_SET = Arrays.asList("prefix", "suffix", "colorname", "chatcolor");

    public static List<String> complete(RankManager rankManager, String[] args) {
        if (args.length == 1) return filter(args[0], List.of("rank", "ranks"));
        if (args.length == 2 && args[0].equalsIgnoreCase("rank")) return filter(args[1], RANK_MAIN);

        if (args.length == 3 && args[0].equalsIgnoreCase("rank")) {
            String sub = args[1].toLowerCase();
            if (sub.equals("set") || sub.equals("remove")) {
                List<String> rankNames = new ArrayList<>();
                for (Rank r : rankManager.getAllRanks()) rankNames.add(r.getName());
                return filter(args[2], rankNames);
            }
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
            return filter(args[3], RANK_SET);
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
