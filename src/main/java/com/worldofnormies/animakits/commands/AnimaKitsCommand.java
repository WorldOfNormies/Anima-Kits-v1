package com.worldofnormies.animakits.commands;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.AnimaClaimMainGUI;
import com.worldofnormies.animakits.gui.AnimaKitsMainGUI;
import com.worldofnormies.animakits.kit.Kit;
import com.worldofnormies.animakits.manager.PermissionManager;
import com.worldofnormies.animakits.util.MessageUtil;
import com.worldofnormies.animakits.util.TimeUtil;
import com.worldofnormies.animaitemedit.ItemEditCommand;
import com.worldofnormies.animaranks.commands.AnimaRankCommand;
import com.worldofnormies.animaeconomy.AnimaEconomyCommand;
import com.worldofnormies.animakits.home.Home;
import com.worldofnormies.animakits.home.gui.AnimaHomeGUI;
import com.worldofnormies.animakits.rtp.gui.AnimaRtpGUI;
import com.worldofnormies.animakits.shop.gui.AnimaShopGUI;
import com.worldofnormies.animakits.perk.GlowPerk;
import com.worldofnormies.animakits.util.ColorUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.*;

/**
 * AnimaKitsCommand – handles all /anima kits [...] and /anima itemedit [...] subcommands.
 */
public class AnimaKitsCommand implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final List<String> VALID_PERMS = Arrays.asList(
            "anima.kits.*",
            "anima.kits.use",
            "anima.kits.add",
            "anima.kits.delete",
            "anima.kits.rename",
            "anima.kits.lore.add",
            "anima.kits.lore.edit",
            "anima.kits.lore.remove",
            "anima.kits.clonekit",
            "anima.kits.give",
            "anima.kits.giveall",
            "anima.kits.reload",
            "anima.kits.help",
            "anima.kits.permission.add",
            "anima.kits.permission.remove",
            "anima.kits.permission.show",
            "anima.kits.permission.claim",
            "anima.kits.permission.claimfree",
            "anima.kits.claim",
            "anima.kits.claim.*",
            "anima.kits.list",
            "anima.kits.setcooldown",
            "anima.kits.singleclaim",
            "anima.kits.onjoinnew"
    );

    private final AnimaKitsPlugin plugin;
    private final ItemEditCommand itemEditCommand;
    private final AnimaRankCommand rankCommand;
    private final AnimaEconomyCommand economyCommand;

    public AnimaKitsCommand(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.itemEditCommand = new ItemEditCommand(plugin);
        this.rankCommand     = new AnimaRankCommand(plugin);
        this.economyCommand  = new AnimaEconomyCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();

        // Handle "?" usage helper
        if (args.length > 0 && args[args.length - 1].equals("?")) {
            String sub = args[0].toLowerCase();
            showUsageHelper(sender, sub);
            return true;
        }

        if (!name.equals("anima")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = switch (name) {
                case "kits" -> "kits";
                case "itemedit" -> "itemedit";
                case "ranks" -> "rank";
                case "echo" -> "eco";
                case "feed" -> "feed";
                case "repair" -> "itemedit"; // maps to /anima itemedit repair
                case "ender" -> "echest";
                case "home" -> "homes";
                case "astore" -> "shop";
                case "claims" -> "kits"; // maps to /anima kits claim
                case "permissions" -> "permission"; // might need custom handling
                case "rtp" -> "rtp";
                default -> name;
            };

            // Custom handling for specific shortcuts
            if (name.equals("rtp")) {
                return handleRtp(sender, args);
            } else if (name.equals("repair")) {
                newArgs = new String[args.length + 2];
                newArgs[0] = "itemedit";
                newArgs[1] = "repair";
                System.arraycopy(args, 0, newArgs, 2, args.length);
            } else if (name.equals("claims")) {
                newArgs = new String[args.length + 2];
                newArgs[0] = "kits";
                newArgs[1] = "claim";
                System.arraycopy(args, 0, newArgs, 2, args.length);
            } else {
                System.arraycopy(args, 0, newArgs, 1, args.length);
            }
            return onCommand(sender, command, "anima", newArgs);
        }

        if (args.length == 0) {
            showMainUsage(sender);
            return true;
        }

        // /anima itemedit <sub> ...
        if (args[0].equalsIgnoreCase("itemedit")) {
            return itemEditCommand.onCommand(sender, command, label, args);
        }

        // /anima rank [...]
        if (args[0].equalsIgnoreCase("rank") || args[0].equalsIgnoreCase("ranks")) {
            return rankCommand.onCommand(sender, command, label, args);
        }

        // /anima eco [...]
        if (args[0].equalsIgnoreCase("eco") || args[0].equalsIgnoreCase("economy")) {
            return economyCommand.onCommand(sender, command, label, args);
        }

        // /anima homes [...]
        if (args[0].equalsIgnoreCase("homes") || args[0].equalsIgnoreCase("home")) {
            return handleHomes(sender, args);
        }

        // /anima rtp
        if (args[0].equalsIgnoreCase("rtp")) {
            return handleRtp(sender, args);
        }

        // /anima echest
        if (args[0].equalsIgnoreCase("echest")) {
            return handleEChest(sender);
        }

        // /anima shop
        if (args[0].equalsIgnoreCase("shop")) {
            return handleShop(sender);
        }

        // /anima glow
        if (args[0].equalsIgnoreCase("glow")) {
            return handleGlow(sender, args);
        }

        // /anima suffix
        if (args[0].equalsIgnoreCase("suffix")) {
            return handleSuffix(sender, args);
        }

        // /anima toggle scoreboard
        if (args[0].equalsIgnoreCase("toggle") && args.length >= 2 && args[1].equalsIgnoreCase("scoreboard")) {
            return handleToggleScoreboard(sender);
        }

        // /anima feed
        if (args[0].equalsIgnoreCase("feed")) {
            return handleFeed(sender);
        }

        if (!args[0].equalsIgnoreCase("kits")) {
            showMainUsage(sender);
            return true;
        }

        if (args.length == 1) {
            return handleDefaultGUIRouting(sender);
        }

        String sub = args[1].toLowerCase();
        return switch (sub) {
            case "claim"         -> handleClaim(sender, args);
            case "add"           -> handleAdd(sender, args);
            case "delete"        -> handleDelete(sender, args);
            case "rename"        -> handleRename(sender, args);
            case "lore"          -> handleLore(sender, args);
            case "clonekit"      -> handleClone(sender, args);
            case "give"          -> handleGive(sender, args);
            case "giveall"       -> handleGiveAll(sender, args);
            case "reload"        -> handleReload(sender);
            case "help"          -> handleHelp(sender, args);
            case "permission"    -> handlePermission(sender, args);
            case "list"          -> handleList(sender);
            case "setcooldown"   -> handleSetCooldown(sender, args);
            case "singleclaim"   -> handleSingleClaim(sender, args);
            case "onjoinnew"     -> handleOnJoinNew(sender, args);
            default              -> { showMainUsage(sender); yield true; }
        };
    }

    private boolean handleDefaultGUIRouting(CommandSender sender) {
        if (!requirePlayer(sender)) return true;
        Player player = (Player) sender;

        if (player.hasPermission("anima.kits.add") || player.hasPermission("anima.kits.*")) {
            new AnimaKitsMainGUI(plugin, player).open();
            return true;
        }

        if (player.hasPermission("anima.kits.use") || player.hasPermission("anima.kits.claim")) {
            new AnimaClaimMainGUI(plugin, player).open();
            return true;
        }

        MessageUtil.sendMsg(sender, "no-permission");
        return true;
    }

    private List<Player> resolveTargets(CommandSender sender, String input) {
        List<Player> targets = new ArrayList<>();
        if (input.startsWith("@")) {
            try {
                for (Entity entity : Bukkit.selectEntities(sender, input)) {
                    if (entity instanceof Player p) targets.add(p);
                }
            } catch (IllegalArgumentException e) {
                MessageUtil.err(sender, "Invalid target selector syntax: " + input);
                return null;
            }
            if (targets.isEmpty()) {
                MessageUtil.err(sender, "Selector '" + input + "' found no matching online players.");
                return null;
            }
        } else {
            Player target = Bukkit.getPlayerExact(input);
            if (target == null) {
                MessageUtil.sendMsg(sender, "player-not-found", Map.of("player", input));
                return null;
            }
            targets.add(target);
        }
        return targets;
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.add")) return true;
        String rawName = args.length < 3 ? "anima new kit" : joinArgs(args, 2);
        if (rawName.isEmpty()) { MessageUtil.sendMsg(sender, "name-empty"); return true; }
        if (rawName.length() > 64) { MessageUtil.sendMsg(sender, "name-too-long"); return true; }
        if (plugin.getKitManager().kitExists(rawName)) {
            MessageUtil.sendMsg(sender, "kit-already-exists", Map.of("kit", rawName)); return true;
        }
        Kit kit = plugin.getKitManager().createKit(rawName);
        MessageUtil.sendMsg(sender, "kit-created", Map.of("kit", kit.getPlainName()));
        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.delete")) return true;
        if (args.length < 3) { usage(sender, "/anima kits delete <kit>"); return true; }
        String name = joinArgs(args, 2);
        if (!plugin.getKitManager().deleteKit(name)) {
            MessageUtil.sendMsg(sender, "kit-not-found", Map.of("kit", name)); return true;
        }
        MessageUtil.sendMsg(sender, "kit-deleted", Map.of("kit", name));
        return true;
    }

    private boolean handleRename(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.rename")) return true;
        if (args.length < 4) { usage(sender, "/anima kits rename <kit> <new-name>"); return true; }
        List<String> kitIds = plugin.getKitManager().getKitIds();
        String oldName = null, newRawName = null;
        for (int split = args.length - 1; split >= 3; split--) {
            String candidate = joinArgsRange(args, 2, split);
            String remaining = joinArgs(args, split);
            if (!remaining.isEmpty() && kitIds.stream().anyMatch(id -> id.equalsIgnoreCase(candidate))) {
                oldName = candidate; newRawName = remaining; break;
            }
        }
        if (oldName == null) { oldName = args[2]; newRawName = joinArgs(args, 3); }
        if (newRawName.isEmpty()) { MessageUtil.sendMsg(sender, "name-empty"); return true; }
        if (newRawName.length() > 64) { MessageUtil.sendMsg(sender, "name-too-long"); return true; }
        if (!plugin.getKitManager().renameKit(oldName, newRawName)) {
            MessageUtil.sendMsg(sender, "kit-not-found", Map.of("kit", oldName)); return true;
        }
        MessageUtil.sendMsg(sender, "kit-renamed", Map.of("old", oldName, "new", newRawName));
        return true;
    }

    private boolean handleLore(CommandSender sender, String[] args) {
        if (args.length < 3) { usage(sender, "/anima kits lore <add|edit|remove> <kit> [args]"); return true; }
        return switch (args[2].toLowerCase()) {
            case "add"    -> handleLoreAdd(sender, args);
            case "edit"   -> handleLoreEdit(sender, args);
            case "remove" -> handleLoreRemove(sender, args);
            default       -> { usage(sender, "/anima kits lore <add|edit|remove>"); yield true; }
        };
    }

    private boolean handleLoreAdd(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.lore.add")) return true;
        if (args.length < 5) { usage(sender, "/anima kits lore add <kit> <text…>"); return true; }
        List<String> kitIds = plugin.getKitManager().getKitIds();
        String kitName = null, line = null;
        for (int split = args.length - 1; split >= 4; split--) {
            String candidate = joinArgsRange(args, 3, split);
            String remaining = joinArgs(args, split);
            if (!remaining.isEmpty() && kitIds.stream().anyMatch(id -> id.equalsIgnoreCase(candidate))) {
                kitName = candidate; line = remaining; break;
            }
        }
        if (kitName == null) { kitName = args[3]; line = joinArgs(args, 4); }
        if (!plugin.getKitManager().addLore(kitName, line)) {
            MessageUtil.sendMsg(sender, "kit-not-found", Map.of("kit", kitName)); return true;
        }
        MessageUtil.sendMsg(sender, "lore-added", Map.of("kit", kitName));
        return true;
    }

    private boolean handleLoreEdit(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.lore.edit")) return true;
        if (args.length < 6) { usage(sender, "/anima kits lore edit <kit> <line#> <text…>"); return true; }
        List<String> kitIds = plugin.getKitManager().getKitIds();
        String kitName = null; int lineNum = -1; String text = null;
        outer:
        for (int split = args.length - 2; split >= 4; split--) {
            String candidate = joinArgsRange(args, 3, split);
            if (kitIds.stream().anyMatch(id -> id.equalsIgnoreCase(candidate))) {
                try {
                    lineNum = Integer.parseInt(args[split]);
                    text = joinArgs(args, split + 1);
                    if (!text.isEmpty()) { kitName = candidate; break outer; }
                } catch (NumberFormatException ignored) {}
            }
        }
        if (kitName == null) { kitName = args[3]; try { lineNum = Integer.parseInt(args[4]); } catch (NumberFormatException e) { lineNum = -1; } text = joinArgs(args, 5); }
        Kit kit = requireKit(sender, kitName);
        if (kit == null) return true;
        if (lineNum < 1 || lineNum > kit.getLore().size()) {
            MessageUtil.sendMsg(sender, "lore-invalid-line", Map.of("max", String.valueOf(kit.getLore().size()))); return true;
        }
        plugin.getKitManager().editLore(kitName, lineNum - 1, text);
        MessageUtil.sendMsg(sender, "lore-edited", Map.of("kit", kitName, "line", String.valueOf(lineNum)));
        return true;
    }

    private boolean handleLoreRemove(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.lore.remove")) return true;
        if (args.length < 5) { usage(sender, "/anima kits lore remove <kit> <line#>"); return true; }
        String lineArg = args[args.length - 1];
        String kitName = joinArgsRange(args, 3, args.length - 1);
        Kit kit = requireKit(sender, kitName);
        if (kit == null) return true;
        int lineNum;
        try { lineNum = Integer.parseInt(lineArg); } catch (NumberFormatException e) {
            MessageUtil.sendMsg(sender, "lore-invalid-line", Map.of("max", String.valueOf(kit.getLore().size()))); return true;
        }
        if (!plugin.getKitManager().removeLore(kitName, lineNum - 1)) {
            MessageUtil.sendMsg(sender, "lore-invalid-line", Map.of("max", String.valueOf(kit.getLore().size()))); return true;
        }
        MessageUtil.sendMsg(sender, "lore-removed", Map.of("kit", kitName, "line", String.valueOf(lineNum)));
        return true;
    }

    private boolean handleClone(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.clonekit")) return true;
        if (args.length < 4) { usage(sender, "/anima kits clonekit <kit> <new-name>"); return true; }
        List<String> kitIds = plugin.getKitManager().getKitIds();
        String srcName = null, newName = null;
        for (int split = args.length - 1; split >= 3; split--) {
            String candidate = joinArgsRange(args, 2, split);
            String remaining = joinArgs(args, split);
            if (!remaining.isEmpty() && kitIds.stream().anyMatch(id -> id.equalsIgnoreCase(candidate))) {
                srcName = candidate; newName = remaining; break;
            }
        }
        if (srcName == null) { srcName = args[2]; newName = joinArgs(args, 3); }
        if (plugin.getKitManager().kitExists(newName)) {
            MessageUtil.sendMsg(sender, "kit-already-exists", Map.of("kit", newName)); return true;
        }
        Kit clone = plugin.getKitManager().cloneKit(srcName, newName);
        if (clone == null) {
            MessageUtil.sendMsg(sender, "kit-not-found", Map.of("kit", srcName)); return true;
        }
        MessageUtil.sendMsg(sender, "kit-cloned", Map.of("old", srcName, "new", newName));
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.give")) return true;
        if (args.length < 5) { usage(sender, "/anima kits give <player/selector> <kit> <amount>"); return true; }
        List<Player> targets = resolveTargets(sender, args[2]);
        if (targets == null) return true;
        String amountArg = args[args.length - 1];
        String kitName = joinArgsRange(args, 3, args.length - 1);
        Kit kit = requireKit(sender, kitName);
        if (kit == null) return true;
        int amount = parseAmount(sender, amountArg);
        if (amount < 1) return true;
        for (Player target : targets) {
            giveKit(target, kit, amount);
            MessageUtil.sendMsg(target, "kit-received", Map.of("kit", kit.getPlainName(), "amount", String.valueOf(amount)));
        }
        if (targets.size() == 1) {
            MessageUtil.sendMsg(sender, "kit-given", Map.of("kit", kit.getPlainName(), "player", targets.get(0).getName(), "amount", String.valueOf(amount)));
        } else {
            MessageUtil.sendMsg(sender, "kit-given-all", Map.of("kit", kit.getPlainName(), "amount", String.valueOf(amount), "count", String.valueOf(targets.size())));
        }
        return true;
    }

    private boolean handleGiveAll(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.giveall")) return true;
        if (args.length < 4) { usage(sender, "/anima kits giveall <kit> <amount>"); return true; }
        String amountArg = args[args.length - 1];
        String kitName = joinArgsRange(args, 2, args.length - 1);
        Kit kit = requireKit(sender, kitName);
        if (kit == null) return true;
        int amount = parseAmount(sender, amountArg);
        if (amount < 1) return true;
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        for (Player p : online) {
            giveKit(p, kit, amount);
            MessageUtil.sendMsg(p, "kit-received", Map.of("kit", kit.getPlainName(), "amount", String.valueOf(amount)));
        }
        MessageUtil.sendMsg(sender, "kit-given-all", Map.of("kit", kit.getPlainName(), "amount", String.valueOf(amount), "count", String.valueOf(online.size())));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!requirePerm(sender, "anima.kits.reload")) return true;
        plugin.reload();
        MessageUtil.sendMsg(sender, "reload");
        return true;
    }

    private boolean handleHelp(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.use")) return true;
        int page = 1;
        if (args.length >= 3) {
            try { page = Integer.parseInt(args[2]); } catch (NumberFormatException ignored) {}
        }

        if (sender.hasPermission("anima.kits.add") || sender.hasPermission("anima.kits.*")) {
            if (page == 1) {
                MessageUtil.send(sender, MM.deserialize("""
                        <gradient:#54DAF4:#545EB6><bold>━━━━━━━ Anima Admin Help (1/2) ━━━━━━━</bold></gradient>
                        <yellow>/kits</yellow> <gray>· Open kit builder</gray>
                        <yellow>/kits add <name></yellow> <gray>· Create kit</gray>
                        <yellow>/kits delete <kit></yellow> <gray>· Delete kit</gray>
                        <yellow>/kits rename <kit> <new></yellow> <gray>· Rename kit</gray>
                        <yellow>/claims [kit]</yellow> <gray>· Claim kits</gray>
                        <yellow>/ranks</yellow> <gray>· Rank management</gray>
                        <yellow>/permissions</yellow> <gray>· Permission management</gray>
                        <yellow>/anima reload</yellow> <gray>· Reload plugin</gray>
                        <gray>Type /anima help 2 for more...</gray>
                        """));
            } else {
                MessageUtil.send(sender, MM.deserialize("""
                        <gradient:#54DAF4:#545EB6><bold>━━━━━━━ Anima Admin Help (2/2) ━━━━━━━</bold></gradient>
                        <yellow>/itemedit <prefix|suffix|rename|lore|enchant|unbreakable|gloweffect|repair></yellow>
                        <yellow>/echo <balance|pay|withdraw|admin></yellow>
                        <yellow>/rtp</yellow> <gray>· Random teleport</gray>
                        <yellow>/home [set]</yellow> <gray>· Home management</gray>
                        <yellow>/ender</yellow> <gray>· Open e-chest</gray>
                        <yellow>/astore</yellow> <gray>· Open shop</gray>
                        <yellow>/glow</yellow> <gray>· Toggle glow</gray>
                        <yellow>/feed</yellow> <gray>· Feed yourself</gray>
                        """));
            }
        } else {
            MessageUtil.send(sender, MM.deserialize("""
                    <gradient:#54DAF4:#545EB6><bold>━━━━━━━ Anima Player Help ━━━━━━━</bold></gradient>
                    <yellow>/kits</yellow> <gray>· Open kit browser</gray>
                    <yellow>/claims [kit]</yellow> <gray>· Claim items</gray>
                    <yellow>/rtp</yellow> <gray>· Random teleport</gray>
                    <yellow>/home [set]</yellow> <gray>· Home management</gray>
                    <yellow>/ender</yellow> <gray>· Open e-chest</gray>
                    <yellow>/astore</yellow> <gray>· Open shop</gray>
                    <yellow>/glow</yellow> <gray>· Toggle glow</gray>
                    <yellow>/feed</yellow> <gray>· Feed yourself</gray>
                    <gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>
                    """));
        }
        return true;
    }

    private boolean handlePermission(CommandSender sender, String[] args) {
        if (args.length < 3) { usage(sender, "/anima kits permission <add|remove|show|claim|claimfree> [args]"); return true; }
        return switch (args[2].toLowerCase()) {
            case "add"       -> handlePermAdd(sender, args);
            case "remove"    -> handlePermRemove(sender, args);
            case "show"      -> handlePermShow(sender, args);
            case "claim"     -> handlePermClaim(sender, args);
            case "claimfree" -> handlePermClaimFree(sender, args);
            default          -> { usage(sender, "/anima kits permission <add|remove|show|claim|claimfree>"); yield true; }
        };
    }

    private boolean handlePermAdd(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.add")) return true;
        if (args.length < 6) { usage(sender, "/anima kits permission add <perm> <player/selector> <time|-1>"); return true; }
        String perm = args[3];
        if (!VALID_PERMS.contains(perm)) { MessageUtil.sendMsg(sender, "perm-invalid", Map.of("perm", perm)); return true; }
        List<Player> targets = resolveTargets(sender, args[4]);
        if (targets == null) return true;
        long durationSecs = TimeUtil.parseDuration(args[5]);
        String durStr = TimeUtil.formatDuration(durationSecs);
        if (targets.size() == 1) {
            plugin.getPermissionManager().grant(targets.get(0).getUniqueId(), perm, durationSecs);
            MessageUtil.sendMsg(sender, "perm-granted", Map.of("perm", perm, "player", targets.get(0).getName(), "duration", durStr));
        } else {
            for (Player target : targets) plugin.getPermissionManager().grant(target.getUniqueId(), perm, durationSecs);
            MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>✓ Granted</bold></gradient> <gray>|</gray> <yellow>" + perm + "</yellow> <gray>→</gray> <white>" + targets.size() + " players</white> <gray>" + durStr + "</gray>"));
        }
        return true;
    }

    private boolean handlePermRemove(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.remove")) return true;
        if (args.length < 5) { usage(sender, "/anima kits permission remove <perm> <player/selector>"); return true; }
        String perm = args[3];
        List<Player> targets = resolveTargets(sender, args[4]);
        if (targets == null) return true;
        if (targets.size() == 1) {
            Player target = targets.get(0);
            if (!plugin.getPermissionManager().revoke(target.getUniqueId(), perm)) {
                MessageUtil.err(sender, target.getName() + " does not have " + perm);
            } else {
                MessageUtil.sendMsg(sender, "perm-revoked", Map.of("perm", perm, "player", target.getName()));
            }
        } else {
            int revoked = 0;
            for (Player target : targets) if (plugin.getPermissionManager().revoke(target.getUniqueId(), perm)) revoked++;
            MessageUtil.send(sender, MM.deserialize("<gradient:#FF4B4B:#FF8585><bold>✕ Revoked</bold></gradient> <gray>|</gray> <yellow>" + perm + "</yellow> <gray>→</gray> <white>" + revoked + " / " + targets.size() + " players</white>"));
        }
        return true;
    }

    private boolean handlePermShow(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.show")) return true;
        if (args.length < 4) { usage(sender, "/anima kits permission show <player>"); return true; }
        List<Player> targets = resolveTargets(sender, args[3]);
        if (targets == null) return true;
        if (targets.size() > 1) { MessageUtil.err(sender, "Permission show only works on a single player."); return true; }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(plugin.getConfig().getString("general.date-format", "dd/MM/yyyy HH:mm")).withZone(ZoneId.systemDefault());
        Player target = targets.get(0);
        Map<String, Long> perms = plugin.getPermissionManager().getAll(target.getUniqueId());
        MessageUtil.sendMsg(sender, "perm-show-header", Map.of("player", target.getName()));
        if (perms.isEmpty()) {
            MessageUtil.sendMsg(sender, "perm-show-empty");
        } else {
            for (Map.Entry<String, Long> e : perms.entrySet()) {
                String expires = e.getValue() == -1 ? "never" : fmt.format(Instant.ofEpochSecond(e.getValue()));
                MessageUtil.sendMsg(sender, "perm-show-entry", Map.of("perm", e.getKey(), "expires", expires));
            }
        }
        return true;
    }

    private boolean handlePermClaim(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.claim")) return true;
        if (args.length < 6) { usage(sender, "/anima kits permission claim <player/selector> <kit> <true|false> [duration]"); return true; }
        List<Player> targets = resolveTargets(sender, args[3]);
        if (targets == null) return true;
        int boolIdx = -1; String durationArg = null;
        String lastArg = args[args.length - 1], secondLast = args.length >= 2 ? args[args.length - 2] : "";
        if ((secondLast.equalsIgnoreCase("true") || secondLast.equalsIgnoreCase("false")) && !lastArg.equalsIgnoreCase("true") && !lastArg.equalsIgnoreCase("false")) { boolIdx = args.length - 2; durationArg = lastArg; }
        else if (lastArg.equalsIgnoreCase("true") || lastArg.equalsIgnoreCase("false")) { boolIdx = args.length - 1; }
        if (boolIdx < 5) { usage(sender, "/anima kits permission claim <player/selector> <kit> <true|false> [duration]"); return true; }
        String kitNameRaw = joinArgsRange(args, 4, boolIdx);
        Kit kit = requireKit(sender, kitNameRaw);
        if (kit == null) return true;
        boolean grant = Boolean.parseBoolean(args[boolIdx]);
        String kitName = kit.getPlainName(), permNode = PermissionManager.CLAIM_PREFIX + kitName;
        long durationSecs = (durationArg != null) ? TimeUtil.parseDuration(durationArg) : -1L;
        String durStr = TimeUtil.formatDuration(durationSecs);
        boolean isSelector = args[3].startsWith("@");
        for (Player target : targets) {
            if (grant) plugin.getPermissionManager().grantClaimPermission(target.getUniqueId(), kitName, durationSecs);
            else plugin.getPermissionManager().revokeClaimPermission(target.getUniqueId(), kitName);
        }
        if (targets.size() == 1 && !isSelector) {
            if (grant) MessageUtil.sendMsg(sender, "perm-granted", Map.of("perm", permNode, "player", targets.get(0).getName(), "duration", durStr));
            else MessageUtil.sendMsg(sender, "perm-revoked", Map.of("perm", permNode, "player", targets.get(0).getName()));
        } else {
            if (grant) MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>✓ Granted</bold></gradient> <gray>|</gray> <yellow>" + permNode + "</yellow> <gray>→</gray> <white>" + targets.size() + " players</white> <gray>" + durStr + "</gray>"));
            else MessageUtil.send(sender, MM.deserialize("<gradient:#FF4B4B:#FF8585><bold>✕ Revoked</bold></gradient> <gray>|</gray> <yellow>" + permNode + "</yellow> <gray>→</gray> <white>" + targets.size() + " players</white>"));
        }
        if (isSelector) {
            if (grant) { plugin.getPermissionManager().grantGlobal(permNode, durationSecs); MessageUtil.send(sender, MM.deserialize("<gray><italic>Global grant stored — new players joining will automatically receive this permission.</italic></gray>")); }
            else plugin.getPermissionManager().revokeGlobal(permNode);
        }
        return true;
    }

    private boolean handlePermClaimFree(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.claimfree")) return true;
        if (args.length < 6) { usage(sender, "/anima kits permission claimfree <player/selector> <kit> <true|false> [duration]"); return true; }
        List<Player> targets = resolveTargets(sender, args[3]);
        if (targets == null) return true;
        int boolIdx = -1; String durationArg = null;
        String lastArg = args[args.length - 1], secondLast = args.length >= 2 ? args[args.length - 2] : "";
        if ((secondLast.equalsIgnoreCase("true") || secondLast.equalsIgnoreCase("false")) && !lastArg.equalsIgnoreCase("true") && !lastArg.equalsIgnoreCase("false")) { boolIdx = args.length - 2; durationArg = lastArg; }
        else if (lastArg.equalsIgnoreCase("true") || lastArg.equalsIgnoreCase("false")) { boolIdx = args.length - 1; }
        if (boolIdx < 5) { usage(sender, "/anima kits permission claimfree <player/selector> <kit> <true|false> [duration]"); return true; }
        String kitNameRaw = joinArgsRange(args, 4, boolIdx);
        Kit kit = requireKit(sender, kitNameRaw);
        if (kit == null) return true;
        boolean grant = Boolean.parseBoolean(args[boolIdx]);
        String kitName = kit.getPlainName(), permNode = "anima.kits.claimfree." + kitName;
        long durationSecs = (durationArg != null) ? TimeUtil.parseDuration(durationArg) : -1L;
        String durStr = TimeUtil.formatDuration(durationSecs);
        boolean isSelector = args[3].startsWith("@");
        for (Player target : targets) {
            if (grant) plugin.getPermissionManager().grant(target.getUniqueId(), permNode, durationSecs);
            else plugin.getPermissionManager().revoke(target.getUniqueId(), permNode);
        }
        if (targets.size() == 1 && !isSelector) {
            if (grant) MessageUtil.sendMsg(sender, "perm-granted", Map.of("perm", permNode, "player", targets.get(0).getName(), "duration", durStr));
            else MessageUtil.sendMsg(sender, "perm-revoked", Map.of("perm", permNode, "player", targets.get(0).getName()));
        } else {
            if (grant) MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>✓ Granted</bold></gradient> <gray>|</gray> <yellow>" + permNode + "</yellow> <gray>→</gray> <white>" + targets.size() + " players</white> <gray>" + durStr + "</gray>"));
            else MessageUtil.send(sender, MM.deserialize("<gradient:#FF4B4B:#FF8585><bold>✕ Revoked</bold></gradient> <gray>|</gray> <yellow>" + permNode + "</yellow> <gray>→</gray> <white>" + targets.size() + " players</white>"));
        }
        if (isSelector) {
            if (grant) { plugin.getPermissionManager().grantGlobal(permNode, durationSecs); MessageUtil.send(sender, MM.deserialize("<gray><italic>Global grant stored — new players joining will automatically receive this permission.</italic></gray>")); }
            else plugin.getPermissionManager().revokeGlobal(permNode);
        }
        return true;
    }

    private boolean handleOnJoinNew(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.onjoinnew")) return true;
        if (args.length < 3) {
            String current = plugin.getConfig().getString("join-kit", "");
            if (current == null || current.isBlank()) MessageUtil.send(sender, MM.deserialize("<gray>No OnJoinNew starter kit is currently configured.</gray>"));
            else MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>OnJoinNew Kit:</bold></gradient> <white>" + current + "</white>"));
            return true;
        }
        String input = joinArgs(args, 2);
        if (input.equalsIgnoreCase("clear") || input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off")) {
            plugin.getConfig().set("join-kit", ""); plugin.saveConfig();
            MessageUtil.send(sender, MM.deserialize("<gradient:#FF4B4B:#FF8585><bold>✕ OnJoinNew:</bold></gradient> <gray>Starter kit on first join has been disabled.</gray>"));
            return true;
        }
        Kit kit = requireKit(sender, input);
        if (kit == null) return true;
        plugin.getConfig().set("join-kit", kit.getPlainName()); plugin.saveConfig();
        MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>✓ OnJoinNew:</bold></gradient> <gray>New players will receive kit</gray> <white>" + kit.getPlainName() + "</white> <gray>on their first join.</gray>"));
        return true;
    }

    private boolean handleClaim(CommandSender sender, String[] args) {
        if (!requirePlayer(sender)) return true;
        Player player = (Player) sender;
        if (args.length < 3) { new com.worldofnormies.animakits.gui.AnimaClaimMainGUI(plugin, player).open(); return true; }
        Kit kit = requireKit(sender, joinArgs(args, 2));
        if (kit == null) return true;

        UUID uuid = player.getUniqueId();
        var playerRank = plugin.getRankManager().getPlayerRank(uuid);

        String claimPerm = "anima.kits.claim." + kit.getPlainName();
        boolean hasBukkitPerm = player.hasPermission(claimPerm) || player.hasPermission("anima.kits.claim.*") || player.hasPermission("anima.kits.*");
        boolean hasStoredPerm = plugin.getPermissionManager().hasClaimPermission(uuid, kit.getPlainName());

        // Check if kit is attached to player's rank
        boolean hasRankPerm = playerRank != null && kit.getPlainName().equalsIgnoreCase(playerRank.getKitId());

        if (!hasBukkitPerm && !hasStoredPerm && !hasRankPerm) { MessageUtil.sendMsg(sender, "no-permission"); return true; }

        if (kit.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, kit.getId())) { MessageUtil.err(sender, "You have already claimed this kit once!"); return true; }
        long cooldown = plugin.getPlayerManager().getRemainingCooldown(uuid, kit.getId());
        if (cooldown > 0) {
            boolean hasClaimFree = player.hasPermission("anima.kits.claimfree." + kit.getPlainName()) || player.hasPermission("anima.kits.claimfree.*") || plugin.getPermissionManager().hasClaimFreePermission(uuid, kit.getPlainName());

            // Check if kit is attached to player's rank
            if (playerRank != null && kit.getPlainName().equalsIgnoreCase(playerRank.getKitId())) {
                hasClaimFree = true;
            }

            if (!hasClaimFree) { MessageUtil.err(sender, "You must wait " + TimeUtil.formatDuration(cooldown) + " more before claiming this kit again."); return true; }
        }
        giveKit(player, kit, 1);
        plugin.getPlayerManager().markClaimed(uuid, kit.getId());
        if (kit.getCooldown() > 0) plugin.getPlayerManager().setCooldown(uuid, kit.getId(), kit.getCooldown());
        MessageUtil.sendMsg(player, "kit-received", Map.of("kit", kit.getPlainName(), "amount", "1"));
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!requirePerm(sender, "anima.kits.use")) return true;
        Collection<Kit> kits = plugin.getKitManager().getAllKits();
        if (kits.isEmpty()) { MessageUtil.send(sender, MM.deserialize("<red>No kits have been created yet.</red>")); return true; }
        MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>━━━━━━━ Available Kits ━━━━━━━</bold></gradient>"));
        for (Kit kit : kits) {
            String claimPerm = "anima.kits.claim." + kit.getPlainName();
            boolean hasPerm = sender.hasPermission("anima.kits.*") || sender.hasPermission("anima.kits.claim.*") || sender.hasPermission(claimPerm);
            if (sender instanceof Player p) if (!hasPerm) hasPerm = plugin.getPermissionManager().hasClaimPermission(p.getUniqueId(), kit.getPlainName());
            if (!hasPerm) continue;
            String info = " <gray>• </gray>" + kit.getRawName() + " <gray>(ID: " + kit.getPlainName() + ")</gray>";
            if (kit.getCooldown() > 0) info += " <aqua>[" + TimeUtil.formatDuration(kit.getCooldown()) + " CD]</aqua>";
            if (kit.isSingleClaim()) info += " <red>[Once]</red>";
            MessageUtil.send(sender, MM.deserialize(info));
        }
        return true;
    }

    private boolean handleSetCooldown(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.setcooldown")) return true;
        if (args.length < 4) { usage(sender, "/anima kits setcooldown <kit> <time>  (e.g. 30s  5m  2h  1d  1d12h30m)"); return true; }
        String timeArg = args[args.length - 1];
        String kitName = joinArgsRange(args, 2, args.length - 1);
        Kit kit = requireKit(sender, kitName);
        if (kit == null) return true;
        long time = TimeUtil.parseDuration(timeArg);
        if (time == -2) { MessageUtil.sendMsg(sender, "cooldown-invalid", Map.of("input", timeArg)); return true; }
        if (time == -1) time = 0;
        kit.setCooldown(time);
        plugin.getKitManager().saveKits();
        MessageUtil.sendMsg(sender, "cooldown-set", Map.of("kit", kit.getPlainName(), "time", TimeUtil.formatDuration(time)));
        return true;
    }

    private boolean handleToggleScoreboard(CommandSender sender) {
        if (!requirePlayer(sender)) return true;
        plugin.getScoreboardManager().toggleScoreboard((Player) sender);
        MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>Scoreboard toggled!</bold></gradient>"));
        return true;
    }

    private boolean handleHomes(CommandSender sender, String[] args) {
        if (!requirePlayer(sender)) return true;
        Player player = (Player) sender;
        if (!requirePerm(sender, plugin.getPermissionManager().getCommandPermission("homes"))) return true;

        if (args.length == 1) {
            new AnimaHomeGUI(plugin, player).open(player);
            return true;
        }

        String sub = args[1].toLowerCase();
        if (sub.equals("set")) {
            if (args.length < 3) { usage(sender, "/anima homes set <name>"); return true; }
            String name = args[2];
            if (plugin.getHomeManager().addHome(player.getUniqueId(), new Home(name, player.getLocation()))) {
                player.sendMessage(ColorUtil.colorize("&aHome '&f" + name + "&a' set!"));
            } else {
                player.sendMessage(ColorUtil.colorize("&cYou have reached your home limit of &f" + plugin.getHomeManager().getHomeLimit(player.getUniqueId()) + "&c!"));
            }
            return true;
        }

        new AnimaHomeGUI(plugin, player).open(player);
        return true;
    }

    private boolean handleRtp(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("admin")) {
            if (!requirePerm(sender, "anima.admin")) return true;
            if (args.length < 5) {
                usage(sender, "/anima rtp admin <world> <radius> <cooldown>");
                return true;
            }
            String world = args[2];
            try {
                int radius = Integer.parseInt(args[3]);
                int cooldown = Integer.parseInt(args[4]);
                plugin.getRtpManager().setWorldSettings(world, radius, cooldown);
                sender.sendMessage(ColorUtil.colorize("&aRTP settings for &f" + world + " &aupdated!"));
            } catch (NumberFormatException e) {
                MessageUtil.err(sender, "Radius and cooldown must be numbers.");
            }
            return true;
        }

        if (!requirePlayer(sender)) return true;
        if (!requirePerm(sender, plugin.getPermissionManager().getCommandPermission("rtp"))) return true;
        new AnimaRtpGUI(plugin).open((Player) sender);
        return true;
    }

    private boolean handleEChest(CommandSender sender) {
        if (!requirePlayer(sender)) return true;
        if (!requirePerm(sender, plugin.getPermissionManager().getCommandPermission("echest"))) return true;
        plugin.getEChestManager().openEChest((Player) sender);
        return true;
    }

    private boolean handleShop(CommandSender sender) {
        if (!requirePlayer(sender)) return true;
        if (!requirePerm(sender, plugin.getPermissionManager().getCommandPermission("shop"))) return true;
        new AnimaShopGUI(plugin).open((Player) sender);
        return true;
    }

    private boolean handleGlow(CommandSender sender, String[] args) {
        if (!requirePlayer(sender)) return true;
        if (!requirePerm(sender, plugin.getPermissionManager().getCommandPermission("glow"))) return true;
        GlowPerk.toggleGlow(plugin, (Player) sender);
        return true;
    }

    private boolean handleFeed(CommandSender sender) {
        if (!requirePlayer(sender)) return true;
        Player player = (Player) sender;
        if (!requirePerm(sender, plugin.getPermissionManager().getCommandPermission("feed"))) return true;

        // Rank-based cooldown for feed (Default 300s)
        UUID uuid = player.getUniqueId();
        long cooldown = plugin.getPlayerManager().getRemainingCooldown(uuid, UUID.nameUUIDFromBytes("feed".getBytes()));
        if (cooldown > 0) {
            MessageUtil.err(player, "You must wait " + TimeUtil.formatDuration(cooldown) + " before feeding again.");
            return true;
        }

        player.setFoodLevel(20);
        player.setSaturation(20);
        player.sendMessage(ColorUtil.colorize("&aYou have been fed!"));
        plugin.getPlayerManager().setCooldown(uuid, UUID.nameUUIDFromBytes("feed".getBytes()), 300); // Fixed for now, can be rank-based
        return true;
    }

    private boolean handleSuffix(CommandSender sender, String[] args) {
        if (args.length < 4 || !args[1].equalsIgnoreCase("create")) {
            usage(sender, "/anima suffix create <id> <suffix>");
            return true;
        }
        if (!requirePerm(sender, "anima.admin")) return true;
        String id = args[2];
        String suffix = joinArgs(args, 3);
        plugin.getSuffixManager().addTemplate(id, suffix);
        sender.sendMessage(ColorUtil.colorize("&aTemplate '&f" + id + "&a' created!"));
        return true;
    }

    private boolean handleSingleClaim(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.singleclaim")) return true;
        if (args.length < 4) { usage(sender, "/anima kits singleclaim <kit> <true|false>"); return true; }
        String boolArg = args[args.length - 1];
        String kitName = joinArgsRange(args, 2, args.length - 1);
        Kit kit = requireKit(sender, kitName);
        if (kit == null) return true;
        boolean val = Boolean.parseBoolean(boolArg);
        kit.setSingleClaim(val);
        plugin.getKitManager().saveKits();
        MessageUtil.sendMsg(sender, "singleclaim-set", Map.of("kit", kit.getPlainName(), "value", val ? "<green>ON</green>" : "<red>OFF</red>"));
        return true;
    }

    // ── Internals ─────────────────────────────────────────────────

    private boolean requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) { MessageUtil.err(sender, "This command requires an in-game player."); return false; }
        return true;
    }

    private boolean requirePerm(CommandSender sender, String perm) {
        if (!sender.hasPermission(perm) && !sender.hasPermission("anima.kits.*")) { MessageUtil.sendMsg(sender, "no-permission"); return false; }
        return true;
    }

    private Kit requireKit(CommandSender sender, String name) {
        Kit kit = plugin.getKitManager().getKitByPlainName(name);
        if (kit == null) MessageUtil.sendMsg(sender, "kit-not-found", Map.of("kit", name));
        return kit;
    }

    private void usage(CommandSender sender, String usage) { MessageUtil.sendMsg(sender, "usage-error", Map.of("usage", usage)); }

    private void showUsageHelper(CommandSender sender, String sub) {
        switch (sub) {
            case "kits", "claim", "claims" -> MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>Guide:</bold></gradient> Use <yellow>/kits</yellow> to browse kits or <yellow>/claims [kit]</yellow> to claim."));
            case "rtp" -> MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>Guide:</bold></gradient> Use <yellow>/rtp</yellow> to open world selection."));
            case "home", "homes" -> MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>Guide:</bold></gradient> Use <yellow>/home set [name]</yellow> to set a home or <yellow>/home</yellow> to list them."));
            default -> MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>Guide:</bold></gradient> Type <yellow>/anima help</yellow> for all commands."));
        }
    }

    private void showMainUsage(CommandSender sender) {
        if (sender.hasPermission("anima.kits.add") || sender.hasPermission("anima.kits.*")) {
            MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits · Admin Commands ━━━━</bold></gradient>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>claim [kit]</white> <dark_gray>·</dark_gray> <gray>Open menu / claim a kit</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>add <name></white> <dark_gray>·</dark_gray> <gray>Create a new kit</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>delete <kit></white> <dark_gray>·</dark_gray> <gray>Delete an existing kit</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>rename <kit></white> <dark_gray>·</dark_gray> <gray>Rename a kit in chat</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>lore <add|edit|remove></white> <dark_gray>·</dark_gray> <gray>Manage kit lore</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>clonekit <kit> <new></white> <dark_gray>·</dark_gray> <gray>Clone a kit</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>give <player> <kit> [n]</white> <dark_gray>·</dark_gray> <gray>Give kit</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>giveall <kit> [n]</white> <dark_gray>·</dark_gray> <gray>Give kit to everyone</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>permission <subcmd></white> <dark_gray>·</dark_gray> <gray>Timed permission nodes</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>reload</white> <dark_gray>·</dark_gray> <gray>Reload configuration</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima itemedit <white><sub> ...</white> <dark_gray>·</dark_gray> <gray>Edit held item properties</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
        } else {
            MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits · Player Menu ━━━━</bold></gradient>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>claim [kit]</white> <dark_gray>·</dark_gray> <gray>Open the graphical kit menu</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>list</white> <dark_gray>·</dark_gray> <gray>Display all your available kits</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>help</white> <dark_gray>·</dark_gray> <gray>Display info commands</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
        }
    }

    private String joinArgs(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) { if (i > from) sb.append(' '); sb.append(args[i]); }
        return sb.toString().trim();
    }

    private String joinArgsRange(String[] args, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to && i < args.length; i++) { if (i > from) sb.append(' '); sb.append(args[i]); }
        return sb.toString().trim();
    }

    private int parseAmount(CommandSender sender, String raw) {
        try { int n = Integer.parseInt(raw); if (n < 1) throw new NumberFormatException(); return n; }
        catch (NumberFormatException e) { MessageUtil.err(sender, "Amount must be a positive integer."); return -1; }
    }


    private void giveKit(Player player, Kit kit, int times) {
        for (int t = 0; t < times; t++) {
            for (ItemStack item : kit.getItems()) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item.clone());
                    if (!remaining.isEmpty()) for (ItemStack left : remaining.values()) player.getWorld().dropItemNaturally(player.getLocation(), left);
                }
            }
        }
    }
}
