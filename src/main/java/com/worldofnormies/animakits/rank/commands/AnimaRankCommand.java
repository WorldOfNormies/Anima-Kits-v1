package com.worldofnormies.animakits.rank.commands;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.rank.Rank;
import com.worldofnormies.animakits.rank.gui.AnimaRanksMainGUI;
import com.worldofnormies.animakits.rank.manager.RankManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * AnimaRankCommand – handles all /anima rank [...] subcommands.
 *
 * Subcommands:
 *   /anima rank                           → open GUI (players) or admin GUI (staff)
 *   /anima rank add <id> <hierarchy>      → create rank
 *   /anima rank remove <id>               → delete rank
 *   /anima rank list                      → list all ranks in chat
 *   /anima rank set prefix <id> <value>   → set prefix
 *   /anima rank set suffix <id> <value>   → set suffix
 *   /anima rank set colorname <id> <val>  → set name color
 *   /anima rank set chatcolor <id> <val>  → set chat color
 *   /anima rank clear <prefix|suffix|colorname|chatcolor> <id>
 *   /anima rank set add <player> <id> <duration|-1>  → assign rank
 *   /anima rank set remove <player>                  → unassign rank
 *   /anima rank promote <player> <rankId>            → set player rank directly
 *   /anima rank isrankable <id> <true|false> <playtime> <price>
 *   /anima rank rankup [rankId]           → player ranks up
 *   /anima rank isbuyable <id> <true|false> <price> <duration>
 *   /anima rank permissions <id> <node> <true|false>
 *   /anima rank <id> list                 → show players with that rank (chat)
 */
public class AnimaRankCommand {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin plugin;

    public AnimaRankCommand(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // args[0] = "rank" (already routed)
        // so real sub is args[1]

        // /anima rank  → open GUI
        if (args.length == 1) {
            if (!(sender instanceof Player player)) { err(sender, "Only players can open the GUI."); return true; }
            boolean admin = sender.hasPermission("anima.ranks.*") || sender.hasPermission("anima.ranks.admin");
            new AnimaRanksMainGUI(plugin, player, admin).open();
            return true;
        }

        String sub = args[1].toLowerCase();

        return switch (sub) {
            case "add"          -> handleAdd(sender, args);
            case "remove"       -> handleRemove(sender, args);
            case "list"         -> handleList(sender, args);
            case "set"          -> handleSet(sender, args);
            case "clear"        -> handleClear(sender, args);
            case "promote"      -> handlePromote(sender, args);
            case "isrankable"   -> handleIsRankable(sender, args);
            case "rankup"       -> handleRankup(sender, args);
            case "isbuyable"    -> handleIsBuyable(sender, args);
            case "permissions"  -> handlePermissions(sender, args);
            default             -> handleRankNameSub(sender, args);
        };
    }

    // ══════════════════════════════════════════════════════════════
    //  /anima rank add <id> <hierarchy>
    // ══════════════════════════════════════════════════════════════

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.add") && !sender.hasPermission("anima.ranks.*")) {
            return noPerm(sender);
        }
        if (args.length < 4) return usage(sender, "/anima rank add <id> <hierarchy>");
        String id = args[2].toLowerCase();
        int hierarchy;
        try { hierarchy = Integer.parseInt(args[3]); }
        catch (NumberFormatException e) { return err(sender, "Hierarchy must be a number (0 = highest)."); }

        if (plugin.getRankManager().rankExists(id)) {
            return err(sender, "A rank with ID <white>" + id + "</white> already exists.");
        }
        plugin.getRankManager().createRank(id, id, hierarchy);
        ok(sender, "Rank <yellow>" + id + "</yellow> created at hierarchy <white>#" + hierarchy + "</white>.");
        ok(sender, "Configure it with <white>/anima rank set prefix " + id + " ...</white>");
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  /anima rank remove <id>
    // ══════════════════════════════════════════════════════════════

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.delete") && !sender.hasPermission("anima.ranks.*")) {
            return noPerm(sender);
        }
        if (args.length < 3) return usage(sender, "/anima rank remove <id>");
        String id = args[2].toLowerCase();
        if (!plugin.getRankManager().rankExists(id)) return err(sender, "No rank found: <white>" + id + "</white>");
        plugin.getRankManager().deleteRank(id);
        send(sender, "<newline><gradient:#FF4B4B:#FF8585><bold>  ✕  Rank Deleted</bold></gradient>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Rank <white><bold>" + id + "</bold></white> has been permanently removed.</gray><newline>");
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  /anima rank list
    // ══════════════════════════════════════════════════════════════

    private boolean handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.list") && !sender.hasPermission("anima.ranks.*")) {
            return noPerm(sender);
        }
        List<Rank> all = plugin.getRankManager().getAllRanks();
        sender.sendMessage(MM.deserialize("<gradient:#FFD700:#FF8C00><bold>⋆ Anima Ranks (" + all.size() + ") ⋆</bold></gradient>"));
        for (Rank r : all) {
            sender.sendMessage(MM.deserialize(
                "<dark_gray>  ┃  </dark_gray><gradient:#FFD700:#FF8C00>➔</gradient> "
                + "<yellow>#" + r.getHierarchy() + "</yellow> "
                + (r.getDisplayName().isBlank() ? r.getId() : r.getDisplayName())
                + " <dark_gray>[" + r.getId() + "]</dark_gray>"
                + (r.isRankable() ? " <green>[Rankable]</green>" : "")
                + (r.isBuyable() ? " <gold>[Buyable]</gold>" : "")));
        }
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  /anima rank set ...
    // ══════════════════════════════════════════════════════════════

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.set") && !sender.hasPermission("anima.ranks.*")) {
            return noPerm(sender);
        }
        if (args.length < 3) return usage(sender,
            "/anima rank set <prefix|suffix|colorname|chatcolor|add|remove> ...");

        String action = args[2].toLowerCase();

        return switch (action) {
            case "prefix"    -> handleSetCosmetic(sender, args, "prefix");
            case "suffix"    -> handleSetCosmetic(sender, args, "suffix");
            case "colorname" -> handleSetCosmetic(sender, args, "colorname");
            case "chatcolor" -> handleSetCosmetic(sender, args, "chatcolor");
            case "add"       -> handleSetAdd(sender, args);
            case "remove"    -> handleSetRemove(sender, args);
            default          -> usage(sender, "/anima rank set <prefix|suffix|colorname|chatcolor|add|remove> ...");
        	};
	}

    	/** /anima rank set prefix <id> <value...> */
	private boolean handleSetCosmetic(CommandSender sender, String[] args, String field) {
   	// args: [anima, rank, set, prefix, <id>, <value...>]
  		if (args.length < 5) return usage(sender, "/anima rank set " + field + " <rankId> <value>");
    		String id = args[3].toLowerCase();
    		Rank rank = plugin.getRankManager().getRank(id);
    		if (rank == null) return err(sender, "No rank found: <white>" + id + "</white>");
    		String value = joinArgs(args, 4);
    
   	switch (field) 
		{
		case "prefix"    -> rank.setPrefix(value);
        		case "suffix"    -> rank.setSuffix(value);
      		case "colorname" -> rank.setColorName(value);
      		case "chatcolor" -> rank.setChatColor(value);
    		}
    
    	plugin.getRankManager().saveRanks();
    
   	// Create visual example showing the actual color/text
   	String example = "";
   		 switch (field) {
        			case "colorname" -> {
            			// Show rank name with the color applied
           			example = " <dark_gray>[ <reset>" + value + rank.getName() + "<dark_gray> ]</dark_gray>";
        			}
        			case "chatcolor" -> {
           			// Show example chat message with the color
           			example = " <dark_gray>[ <reset>" + value + "Player Message Example<dark_gray> ]</dark_gray>";
      			}
        		case "prefix" -> {
            		// Show the prefix as it will appear
            		example = " <dark_gray>[ <reset>" + value + "<dark_gray> ]</dark_gray>";
        		}
        			case "suffix" -> {
            			// Show the suffix as it will appear
            			example = " <dark_gray>[ <reset>" + value + "<dark_gray> ]</dark_gray>";
        			}
	}
    
    		ok(sender, "Rank <yellow>" + id + "</yellow> " + field + " updated:" + example);
    		return true;
	}


    /** /anima rank set add <player> <rankId> <duration|-1> */
    private boolean handleSetAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.assign") && !sender.hasPermission("anima.ranks.*")) return noPerm(sender);
        // args: [anima, rank, set, add, <player>, <rankId>, <duration>]
        if (args.length < 6) return usage(sender, "/anima rank set add <player> <rankId> <duration|-1>");
        Player target = Bukkit.getPlayer(args[4]);
        if (target == null) return err(sender, "Player not found: <white>" + args[4] + "</white>");
        String rankId = args[5].toLowerCase();
        if (!plugin.getRankManager().rankExists(rankId)) return err(sender, "No rank found: <white>" + rankId + "</white>");
        long duration = -1;
        if (args.length >= 7) {
            try { duration = parseDuration(args[6]); }
            catch (IllegalArgumentException e) { return err(sender, "Invalid duration: <white>" + args[6] + "</white>"); }
        }
        plugin.getRankManager().setPlayerRank(target.getUniqueId(), rankId, duration);
        String durStr = duration < 0 ? "Permanent" : RankManager.formatPlaytime(duration);
        send(sender, "<newline><gradient:#54DAF4:#545EB6><bold>  ✓  Rank Assigned</bold></gradient>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Player: <white><bold>" + target.getName() + "</bold></white></gray>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Rank:   <yellow><bold>" + rankId + "</bold></yellow></gray>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Duration: <white><bold>" + durStr + "</bold></white></gray><newline>");
        target.sendMessage(MM.deserialize("<gradient:#FFD700:#FF8C00><bold>✦ You have been given the rank: " + rankId + "</bold></gradient>"));
        return true;
    }

    /** /anima rank set remove <player> */
    private boolean handleSetRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.assign") && !sender.hasPermission("anima.ranks.*")) return noPerm(sender);
        if (args.length < 4) return usage(sender, "/anima rank set remove <player>");
        Player target = Bukkit.getPlayer(args[3]);
        if (target == null) return err(sender, "Player not found: <white>" + args[3] + "</white>");
        plugin.getRankManager().removePlayerRank(target.getUniqueId());
        ok(sender, "Rank removed from <white>" + target.getName() + "</white>.");
        target.sendMessage(MM.deserialize("<red>Your rank has been removed.</red>"));
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  /anima rank clear <field> <id>
    // ══════════════════════════════════════════════════════════════

    private boolean handleClear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.set") && !sender.hasPermission("anima.ranks.*")) return noPerm(sender);
        if (args.length < 4) return usage(sender, "/anima rank clear <prefix|suffix|colorname|chatcolor> <rankId>");
        String field = args[2].toLowerCase();
        String id    = args[3].toLowerCase();
        Rank rank = plugin.getRankManager().getRank(id);
        if (rank == null) return err(sender, "No rank found: <white>" + id + "</white>");
        switch (field) {
            case "prefix"    -> rank.setPrefix("");
            case "suffix"    -> rank.setSuffix("");
            case "colorname" -> rank.setColorName("");
            case "chatcolor" -> rank.setChatColor("");
            default -> { return usage(sender, "/anima rank clear <prefix|suffix|colorname|chatcolor> <rankId>"); }
        }
        plugin.getRankManager().saveRanks();
        ok(sender, "Cleared <white>" + field + "</white> on rank <yellow>" + id + "</yellow>.");
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  /anima rank promote <player> <rankId>
    // ══════════════════════════════════════════════════════════════

    private boolean handlePromote(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.promote") && !sender.hasPermission("anima.ranks.*")) return noPerm(sender);
        if (args.length < 4) return usage(sender, "/anima rank promote <player> <rankId>");
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) return err(sender, "Player not found: <white>" + args[2] + "</white>");
        String rankId = args[3].toLowerCase();
        if (!plugin.getRankManager().rankExists(rankId)) return err(sender, "No rank found: <white>" + rankId + "</white>");
        plugin.getRankManager().setPlayerRank(target.getUniqueId(), rankId, -1);
        send(sender, "<newline><gradient:#54DAF4:#545EB6><bold>  ✓  Player Promoted</bold></gradient>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Player: <white>" + target.getName() + "</white></gray>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>New Rank: <yellow>" + rankId + "</yellow></gray><newline>");
        target.sendMessage(MM.deserialize("<gradient:#FFD700:#FF8C00><bold>⬆ You have been promoted to: <white>" + rankId + "</white>!</bold></gradient>"));
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  /anima rank isrankable <id> <true|false> <playtime> <price>
    // ══════════════════════════════════════════════════════════════

    private boolean handleIsRankable(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.set") && !sender.hasPermission("anima.ranks.*")) return noPerm(sender);
        if (args.length < 6) return usage(sender, "/anima rank isrankable <rankId> <true|false> <requiredPlaytime> <price>");
        String id = args[2].toLowerCase();
        Rank rank = plugin.getRankManager().getRank(id);
        if (rank == null) return err(sender, "No rank found: <white>" + id + "</white>");
        boolean val = Boolean.parseBoolean(args[3]);
        long playtime;
        double price;
        try { playtime = parseDuration(args[4]); price = Double.parseDouble(args[5]); }
        catch (Exception e) { return err(sender, "Usage: /anima rank isrankable <id> <true|false> <playtime e.g. 1h30m> <price>"); }
        rank.setRankable(val);
        rank.setRankupPlaytime(playtime);
        rank.setRankupPrice(price);
        plugin.getRankManager().saveRanks();
        send(sender, "<newline><gradient:#54DAF4:#545EB6><bold>  ✓  Rankable Updated</bold></gradient>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Rank:     <yellow>" + id + "</yellow></gray>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Rankable: <white>" + val + "</white></gray>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Playtime: <aqua>" + RankManager.formatPlaytime(playtime) + "</aqua></gray>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Price:    <gold>$" + price + "</gold></gray><newline>");
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  /anima rank rankup [rankId]
    // ══════════════════════════════════════════════════════════════

    private boolean handleRankup(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return err(sender, "Only players can rank up.");
        if (!player.hasPermission("anima.ranks.rankup") && !player.hasPermission("anima.ranks.*")) return noPerm(sender);

        UUID uuid = player.getUniqueId();
        Rank target;

        if (args.length >= 3) {
            target = plugin.getRankManager().getRank(args[2].toLowerCase());
            if (target == null) return err(sender, "No rank found: <white>" + args[2] + "</white>");
        } else {
            target = plugin.getRankManager().getNextRankUp(uuid);
            if (target == null) {
                return err(sender, "You have no rank to rank up from, or you're already at the top.");
            }
        }

        if (!target.isRankable()) return err(sender, "Rank <white>" + target.getId() + "</white> is not rankable.");

        long pt = plugin.getRankManager().getPlaytime(uuid);
        if (pt < target.getRankupPlaytime()) {
            return err(sender, "You need <aqua>" + RankManager.formatPlaytime(target.getRankupPlaytime())
                + "</aqua> playtime. You have <aqua>" + RankManager.formatPlaytime(pt) + "</aqua>.");
        }

        // Economy check placeholder – integrate with Vault if present
        if (target.getRankupPrice() > 0) {
            sender.sendMessage(MM.deserialize("<gold>Note: Economy integration (Vault) required for price check. Price: $" + target.getRankupPrice() + "</gold>"));
            // TODO: Vault Economy.withdrawPlayer(player, target.getRankupPrice())
        }

        plugin.getRankManager().setPlayerRank(uuid, target.getId(), -1);
        send(sender, "<newline><gradient:#FFD700:#FF8C00><bold>  ▲  Rank Up!</bold></gradient>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>You ranked up to: <yellow><bold>" + target.getId() + "</bold></yellow>!</gray><newline>");
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  /anima rank isbuyable <id> <true|false> <price> <duration|-1>
    // ══════════════════════════════════════════════════════════════

    private boolean handleIsBuyable(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.set") && !sender.hasPermission("anima.ranks.*")) return noPerm(sender);
        if (args.length < 6) return usage(sender, "/anima rank isbuyable <rankId> <true|false> <price> <duration|-1>");
        String id = args[2].toLowerCase();
        Rank rank = plugin.getRankManager().getRank(id);
        if (rank == null) return err(sender, "No rank found: <white>" + id + "</white>");
        boolean val = Boolean.parseBoolean(args[3]);
        double price;
        long duration;
        try { price = Double.parseDouble(args[4]); duration = parseDuration(args[5]); }
        catch (Exception e) { return err(sender, "Invalid price or duration. Duration: 1d2h30m or -1 for permanent."); }
        rank.setBuyable(val);
        rank.setBuyPrice(price);
        rank.setBuyDuration(duration);
        plugin.getRankManager().saveRanks();
        String durStr = duration < 0 ? "Permanent" : RankManager.formatPlaytime(duration);
        send(sender, "<newline><gradient:#54DAF4:#545EB6><bold>  ✓  Buyable Updated</bold></gradient>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Rank:     <yellow>" + id + "</yellow></gray>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Buyable:  <white>" + val + "</white></gray>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Price:    <gold>$" + price + "</gold></gray>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Duration: <white>" + durStr + "</white></gray><newline>");
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  /anima rank permissions <rankId> <node> <true|false>
    // ══════════════════════════════════════════════════════════════

    private boolean handlePermissions(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.permissions") && !sender.hasPermission("anima.ranks.*")) return noPerm(sender);
        if (args.length < 3) return usage(sender, "/anima rank permissions <rankId> [node] [true|false]");

        String id = args[2].toLowerCase();
        Rank rank = plugin.getRankManager().getRank(id);
        if (rank == null) return err(sender, "No rank found: <white>" + id + "</white>");

        // List mode
        if (args.length == 3) {
            sender.sendMessage(MM.deserialize("<gradient:#FFD700:#FF8C00><bold>Permissions for rank: " + id + "</bold></gradient>"));
            if (rank.getPermissions().isEmpty()) {
                sender.sendMessage(MM.deserialize("<dark_gray>  ┃ <gray>No permissions set.</gray></dark_gray>"));
            } else {
                rank.getPermissions().forEach((node, val) ->
                    sender.sendMessage(MM.deserialize(
                        "<dark_gray>  ┃  </dark_gray><yellow>" + node + "</yellow> <dark_gray>→</dark_gray> " +
                        (val ? "<green>true</green>" : "<red>false</red>"))));
            }
            // Also list all server permissions as hint
            sender.sendMessage(MM.deserialize("<dark_gray>Tip: /anima rank permissions " + id + " <node> <true|false> to add/update.</dark_gray>"));
            return true;
        }

        if (args.length < 5) return usage(sender, "/anima rank permissions <rankId> <node> <true|false>");
        String node = args[3];
        boolean val = Boolean.parseBoolean(args[4]);
        rank.setPermission(node, val);
        plugin.getRankManager().saveRanks();
        ok(sender, "Rank <yellow>" + id + "</yellow>: node <white>" + node + "</white> → " + (val ? "<green>true</green>" : "<red>false</red>"));
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  /anima rank <rankName> list  → list players with this rank
    // ══════════════════════════════════════════════════════════════

    private boolean handleRankNameSub(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.ranks.list") && !sender.hasPermission("anima.ranks.*")) return noPerm(sender);
        String rankId = args[1].toLowerCase();
        Rank rank = plugin.getRankManager().getRank(rankId);
        if (rank == null) return err(sender, "Unknown subcommand or rank: <white>" + args[1] + "</white>. Try /anima rank list");

        // /anima rank <id> list
        if (args.length >= 3 && args[2].equalsIgnoreCase("list")) {
            sender.sendMessage(MM.deserialize("<gradient:#FFD700:#FF8C00><bold>Players with rank: " + rankId + "</bold></gradient>"));
            int count = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                String r = plugin.getRankManager().getPlayerRankId(p.getUniqueId());
                if (rankId.equalsIgnoreCase(r)) {
                    long pt = plugin.getRankManager().getPlaytime(p.getUniqueId());
                    sender.sendMessage(MM.deserialize(
                        "<dark_gray>  ┃ </dark_gray><white>" + p.getName() + "</white> <dark_gray>· Playtime: <aqua>" + RankManager.formatPlaytime(pt) + "</aqua></dark_gray>"));
                    count++;
                }
            }
            if (count == 0) sender.sendMessage(MM.deserialize("<dark_gray>  ┃ <gray>No online players found with this rank.</gray></dark_gray>"));
            sender.sendMessage(MM.deserialize("<dark_gray>  ┃ <gray>Note: Shows online players only.</gray></dark_gray>"));
            return true;
        }

        // Just /anima rank <id> – show detail
        sendRankDetail(sender, rank);
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════

    private void sendRankDetail(CommandSender sender, Rank rank) {
        sender.sendMessage(MM.deserialize("<gradient:#FFD700:#FF8C00><bold>⋆ Rank: " + rank.getId() + " ⋆</bold></gradient>"));
        sender.sendMessage(MM.deserialize("<dark_gray>  ┃ ID:         </dark_gray><yellow>" + rank.getId() + "</yellow>"));
        sender.sendMessage(MM.deserialize("<dark_gray>  ┃ Hierarchy:  </dark_gray><white>#" + rank.getHierarchy() + "</white>"));
        sender.sendMessage(MM.deserialize("<dark_gray>  ┃ Display:    </dark_gray>" + rank.getDisplayName()));
        if (!rank.getPrefix().isBlank())    sender.sendMessage(MM.deserialize("<dark_gray>  ┃ Prefix:     </dark_gray>" + rank.getPrefix() + "<reset>"));
        if (!rank.getSuffix().isBlank())    sender.sendMessage(MM.deserialize("<dark_gray>  ┃ Suffix:     </dark_gray>" + rank.getSuffix() + "<reset>"));
        if (!rank.getChatColor().isBlank()) sender.sendMessage(MM.deserialize("<dark_gray>  ┃ Chat Color: </dark_gray>" + rank.getChatColor() + "Preview<reset>"));
        if (!rank.getColorName().isBlank()) sender.sendMessage(MM.deserialize("<dark_gray>  ┃ Name Color: </dark_gray>" + rank.getColorName() + "Preview<reset>"));
        sender.sendMessage(MM.deserialize("<dark_gray>  ┃ Rankable:   </dark_gray>" + (rank.isRankable() ? "<green>Yes</green> <dark_gray>· playtime: <aqua>" + RankManager.formatPlaytime(rank.getRankupPlaytime()) + "</aqua> price: <gold>$" + rank.getRankupPrice() + "</gold></dark_gray>" : "<red>No</red>")));
        sender.sendMessage(MM.deserialize("<dark_gray>  ┃ Buyable:    </dark_gray>" + (rank.isBuyable() ? "<green>Yes</green> <dark_gray>· price: <gold>$" + rank.getBuyPrice() + "</gold> duration: <white>" + (rank.getBuyDuration() < 0 ? "Permanent" : RankManager.formatPlaytime(rank.getBuyDuration())) + "</white></dark_gray>" : "<red>No</red>")));
        sender.sendMessage(MM.deserialize("<dark_gray>  ┃ Permissions: </dark_gray><white>" + rank.getPermissions().size() + " nodes</white>"));
    }

    /**
     * Parses duration strings like "1d2h30m", "3600", "30s", "-1" (permanent).
     */
    public static long parseDuration(String input) {
        if (input == null) throw new IllegalArgumentException("null");
        if (input.equals("-1")) return -1;
        // Pure number = seconds
        try { return Long.parseLong(input); } catch (NumberFormatException ignored) {}
        long total = 0;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d+)([dhms])").matcher(input.toLowerCase());
        boolean found = false;
        while (m.find()) {
            found = true;
            long num = Long.parseLong(m.group(1));
            total += switch (m.group(2)) {
                case "d" -> num * 86400;
                case "h" -> num * 3600;
                case "m" -> num * 60;
                case "s" -> num;
                default  -> 0;
            };
        }
        if (!found) throw new IllegalArgumentException("Bad duration: " + input);
        return total;
    }

    private String joinArgs(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) { if (i > from) sb.append(' '); sb.append(args[i]); }
        return sb.toString();
    }

    private void send(CommandSender s, String msg) { s.sendMessage(MM.deserialize(msg)); }
    private boolean ok(CommandSender s, String msg) {
        s.sendMessage(MM.deserialize("<newline><gradient:#54DAF4:#545EB6><bold>  ✓  Done</bold></gradient><newline><dark_gray>  ┃</dark_gray> <gray>" + msg + "</gray><newline>"));
        return true;
    }
    private boolean err(CommandSender s, String msg) {
        s.sendMessage(MM.deserialize("<newline><gradient:#FF4B4B:#FF8585><bold>  ✕  Error</bold></gradient><newline><dark_gray>  ┃</dark_gray> <gray>" + msg + "</gray><newline>"));
        return true;
    }
    private boolean noPerm(CommandSender s) { return err(s, "You do not have permission to do this."); }
    private boolean usage(CommandSender s, String usage) {
        s.sendMessage(MM.deserialize("<newline><gradient:#FF4B4B:#FF8585><bold>  ✕  Wrong Usage</bold></gradient><newline><dark_gray>  ┃  </dark_gray><gradient:#54DAF4:#545EB6>" + usage + "</gradient><newline>"));
        return true;
    }
}
