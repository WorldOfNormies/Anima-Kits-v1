package com.worldofnormies.animakits.commands;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.AnimaClaimMainGUI;
import com.worldofnormies.animakits.gui.AnimaKitsMainGUI;
import com.worldofnormies.animakits.kit.Kit;
import com.worldofnormies.animakits.manager.PermissionManager;
import com.worldofnormies.animakits.util.MessageUtil;
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
 * AnimaKitsCommand – handles all /anima kits [...] subcommands with target selector support.
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
            "anima.kits.onjoinnew",
            "anima.kits.permission.repaircooldown"
    );

    private final AnimaKitsPlugin plugin;
    private final com.worldofnormies.animaitemedit.ItemEditCommand itemEditCommand;
    private final com.worldofnormies.animaranks.AnimaRanksCommand rankCommand;

    public AnimaKitsCommand(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.itemEditCommand = new com.worldofnormies.animaitemedit.ItemEditCommand(plugin);
        this.rankCommand = new com.worldofnormies.animaranks.AnimaRanksCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showMainUsage(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("itemedit")) {
            return itemEditCommand.onCommand(sender, command, label, args);
        }

        if (args[0].equalsIgnoreCase("rank") || args[0].equalsIgnoreCase("ranks")) {
            return rankCommand.onCommand(sender, command, label, args);
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
            case "help"          -> handleHelp(sender);
            case "permission"    -> handlePermission(sender, args);
            case "rank", "ranks" -> rankCommand.onCommand(sender, command, label, args);
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

    /**
     * Resolves raw player inputs into a list of targets. Supports standard names and selectors.
     */
    private List<Player> resolveTargets(CommandSender sender, String input) {
        List<Player> targets = new ArrayList<>();
        if (input.startsWith("@")) {
            try {
                for (Entity entity : Bukkit.selectEntities(sender, input)) {
                    if (entity instanceof Player p) {
                        targets.add(p);
                    }
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
        String rawName;
        if (args.length < 3) { rawName = "anima new kit"; } else { rawName = joinArgs(args, 2); }
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
        // The old kit name can be multi-word. We find it by scanning all kits and seeing which
        // one the typed args prefix matches, then treat remaining args as the new name.
        // Try longest possible old name first (greedy match).
        List<String> kitIds = plugin.getKitManager().getKitIds();
        String oldName = null;
        String newRawName = null;
        for (int split = args.length - 1; split >= 3; split--) {
            String candidate = joinArgsRange(args, 2, split);
            String remaining = joinArgs(args, split);
            if (!remaining.isEmpty() && kitIds.stream().anyMatch(id -> id.equalsIgnoreCase(candidate))) {
                oldName = candidate;
                newRawName = remaining;
                break;
            }
        }
        if (oldName == null) {
            // Fallback: old name = args[2], new name = rest
            oldName = args[2];
            newRawName = joinArgs(args, 3);
        }
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
        // Kit name starts at args[3]; text follows. Find kit by greedy prefix match.
        List<String> kitIds = plugin.getKitManager().getKitIds();
        String kitName = null; String line = null;
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
        // Format: lore edit <kit…> <line#> <text…>
        // Find kit by greedy match; line# is an integer, text is the rest
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
        // Last arg is the line number; everything from args[3] to second-to-last is the kit name
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
        // Source kit name can be multi-word — find it by greedy prefix match
        List<String> kitIds = plugin.getKitManager().getKitIds();
        String srcName = null; String newName = null;
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

        // Last arg is amount; everything from args[3] to second-to-last is the kit name
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
        // Last arg is amount; everything from args[2] to second-to-last is the kit name
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

    private boolean handleHelp(CommandSender sender) {
        if (!requirePerm(sender, "anima.kits.use")) return true;
        
        if (sender.hasPermission("anima.kits.add") || sender.hasPermission("anima.kits.*")) {
            MessageUtil.send(sender, MM.deserialize("""
                    <gradient:#54DAF4:#545EB6><bold>━━━━━━━ AnimaKits Admin Help ━━━━━━━</bold></gradient>
                    <yellow>/anima kits</yellow>                              <gray>Open administrative kit builder layout</gray>
                    <yellow>/anima kits add <name></yellow>              <gray>Create a new kit from scratch</gray>
                    <yellow>/anima kits delete <kit></yellow>            <gray>Delete a kit configuration</gray>
                    <yellow>/anima kits rename <kit> <new></yellow>      <gray>Rename an existing kit</gray>
                    <yellow>/anima kits claim</yellow>                       <gray>Open Claim Kits GUI</gray>
                    <yellow>/anima kits claim <kit></yellow>                 <gray>Directly claim a specific kit</gray>
                    <yellow>/anima kits lore add <kit> <text></yellow>   <gray>Add a lore line to a kit</gray>
                    <yellow>/anima kits lore edit <kit> <#> <text></yellow> <gray>Edit an existing lore line</gray>
                    <yellow>/anima kits lore remove <kit> <#></yellow>  <gray>Remove a lore line</gray>
                    <yellow>/anima kits clonekit <kit> <new></yellow>   <gray>Clone an existing kit setup</gray>
                    <yellow>/anima kits give <player/selector> <kit> <n></yellow> <gray>Give kit to target player(s) [Supports Selectors]</gray>
                    <yellow>/anima kits giveall <kit> <n></yellow>      <gray>Give kit to all online entities</gray>
                    <yellow>/anima kits setcooldown <kit> <secs></yellow><gray>Set cooldown timer on a kit</gray>
                    <yellow>/anima kits singleclaim <kit> <t|f></yellow> <gray>Toggle unique single claim status</gray>
                    <yellow>/anima kits permission ...</yellow>         <gray>Manage profile custom temporary flags</gray>
                    <yellow>/anima kits reload</yellow>                 <gray>Reload master plugin configuration</gray>
                    <gradient:#54DAF4:#545EB6><bold>━━━━ Colour & Gradient Guide ━━━━</bold></gradient>
                    <gray>Legacy codes:  <white>&a Green  &c Red  &b Aqua  &6 Gold  &l Bold</white></gray>
                    <gray>Hex colour:    <white>&#FF5500MyText</white>  → <color:#FF5500>MyText</color></gray>
                    <gray>MiniMessage:   <white><red>Red</red>  <bold>Bold</bold>  <italic>Italic</italic></white></gray>
                    <gray>Gradient:      <white><gradient:#54DAF4:#545EB6>My Kit Name</gradient></white></gray>
                    <gray>Rainbow:       <white><rainbow>Rainbow Kit</rainbow></white></gray>
                    <gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>
                    """));
        } else {
            MessageUtil.send(sender, MM.deserialize("""
                    <gradient:#54DAF4:#545EB6><bold>━━━━━━━ AnimaKits Help ━━━━━━━</bold></gradient>
                    <yellow>/anima kits</yellow>              <gray>Open the graphical kit selector browser</gray>
                    <yellow>/anima kits claim</yellow>        <gray>Open the graphical kit claim browser</gray>
                    <yellow>/anima kits claim <kit></yellow>  <gray>Claim items from a specific authorized kit</gray>
                    <yellow>/anima kits list</yellow>         <gray>List names of all currently available kits</gray>
                    <yellow>/anima kits help</yellow>         <gray>Show this support guidelines overview</gray>
                    <gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>
                    """));
        }
        return true;
    }

    private boolean handlePermission(CommandSender sender, String[] args) {
        if (args.length < 3) {
            usage(sender, "/anima kits permission <add|remove|show|claim|claimfree|repaircooldown> [args]");
            return true;
        }
        return switch (args[2].toLowerCase()) {
            case "add"       -> handlePermAdd(sender, args);
            case "remove"    -> handlePermRemove(sender, args);
            case "show"      -> handlePermShow(sender, args);
            case "claim"     -> handlePermClaim(sender, args);
            case "claimfree" -> handlePermClaimFree(sender, args);
            case "repaircooldown" -> handleRepairCooldown(sender, args);
            default          -> { usage(sender, "/anima kits permission <add|remove|show|claim|claimfree|repaircooldown>"); yield true; }
        };
    }

    private boolean handleRepairCooldown(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.repaircooldown")) return true;
        if (args.length < 5) { usage(sender, "/anima kits permission repaircooldown <player/selector> <time>"); return true; }
        List<Player> targets = resolveTargets(sender, args[3]);
        if (targets == null) return true;
        long time = parseDuration(args[4]);
        if (time == -2) { MessageUtil.err(sender, "Invalid duration: " + args[4]); return true; }
        if (time == -1) time = 0;
        for (Player target : targets) {
            plugin.getPlayerManager().setRepairCooldown(target.getUniqueId(), time);
        }
        MessageUtil.send(sender, MM.deserialize("<gradient:#44FF88:#00CC55><bold>  ✓  </bold></gradient><gray>Repair cooldown set to <white>" + formatDuration(time) + "</white> for <white>" + targets.size() + " players</white>.</gray>"));
        return true;
    }

    private boolean handlePermAdd(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.add")) return true;
        if (args.length < 6) { usage(sender, "/anima kits permission add <perm> <player/selector> <time|-1>"); return true; }
        String perm = args[3];
        if (!VALID_PERMS.contains(perm)) { MessageUtil.sendMsg(sender, "perm-invalid", Map.of("perm", perm)); return true; }

        List<Player> targets = resolveTargets(sender, args[4]);
        if (targets == null) return true;

        long durationSecs = parseDuration(args[5]);
        String durStr = durationSecs == -1 ? "Permanent" : formatDuration(durationSecs);

        if (targets.size() == 1) {
            plugin.getPermissionManager().grant(targets.get(0).getUniqueId(), perm, durationSecs);
            MessageUtil.sendMsg(sender, "perm-granted",
                    Map.of("perm", perm, "player", targets.get(0).getName(), "duration", durStr));
        } else {
            for (Player target : targets) {
                plugin.getPermissionManager().grant(target.getUniqueId(), perm, durationSecs);
            }
            MessageUtil.send(sender, MM.deserialize(
                "<gradient:#54DAF4:#545EB6><bold>✓ Granted</bold></gradient> <gray>|</gray> <yellow>" + perm + "</yellow> <gray>→</gray> <white>" + targets.size() + " players</white> <gray>" + durStr + "</gray>"));
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
            for (Player target : targets) {
                if (plugin.getPermissionManager().revoke(target.getUniqueId(), perm)) revoked++;
            }
            MessageUtil.send(sender, MM.deserialize(
                "<gradient:#FF4B4B:#FF8585><bold>✕ Revoked</bold></gradient> <gray>|</gray> <yellow>" + perm + "</yellow> <gray>→</gray> <white>" + revoked + " / " + targets.size() + " players</white>"));
        }
        return true;
    }

    private boolean handlePermShow(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.show")) return true;
        if (args.length < 4) { usage(sender, "/anima kits permission show <player>"); return true; }

        List<Player> targets = resolveTargets(sender, args[3]);
        if (targets == null) return true;

        // Permission show is a diagnostic command — only allow single player target
        if (targets.size() > 1) {
            MessageUtil.err(sender, "Permission show only works on a single player, not selectors.");
            return true;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                plugin.getConfig().getString("general.date-format", "dd/MM/yyyy HH:mm"))
                .withZone(ZoneId.systemDefault());

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
        if (args.length < 6) {
            usage(sender, "/anima kits permission claim <player/selector> <kit> <true|false> [duration]");
            return true;
        }

        List<Player> targets = resolveTargets(sender, args[3]);
        if (targets == null) return true;

        int boolIdx = -1;
        String durationArg = null;
        String lastArg = args[args.length - 1];
        String secondLast = args.length >= 2 ? args[args.length - 2] : "";
        if ((secondLast.equalsIgnoreCase("true") || secondLast.equalsIgnoreCase("false"))
                && !lastArg.equalsIgnoreCase("true") && !lastArg.equalsIgnoreCase("false")) {
            boolIdx = args.length - 2;
            durationArg = lastArg;
        } else if (lastArg.equalsIgnoreCase("true") || lastArg.equalsIgnoreCase("false")) {
            boolIdx = args.length - 1;
        }

        if (boolIdx < 5) {
            usage(sender, "/anima kits permission claim <player/selector> <kit> <true|false> [duration]");
            return true;
        }

        String kitNameRaw = joinArgsRange(args, 4, boolIdx);
        Kit kit = requireKit(sender, kitNameRaw);
        if (kit == null) return true;

        boolean grant = Boolean.parseBoolean(args[boolIdx]);
        String kitName = kit.getPlainName();
        String permNode = PermissionManager.CLAIM_PREFIX + kitName;
        long durationSecs = (durationArg != null) ? parseDuration(durationArg) : -1L;
        String durStr = durationSecs == -1 ? "Permanent" : formatDuration(durationSecs);
        boolean isSelector = args[3].startsWith("@");

        for (Player target : targets) {
            if (grant) {
                plugin.getPermissionManager().grantClaimPermission(target.getUniqueId(), kitName, durationSecs);
            } else {
                plugin.getPermissionManager().revokeClaimPermission(target.getUniqueId(), kitName);
            }
        }

        // Send a single summary message — never one line per player
        if (targets.size() == 1 && !isSelector) {
            if (grant) {
                MessageUtil.sendMsg(sender, "perm-granted",
                        Map.of("perm", permNode, "player", targets.get(0).getName(), "duration", durStr));
            } else {
                MessageUtil.sendMsg(sender, "perm-revoked",
                        Map.of("perm", permNode, "player", targets.get(0).getName()));
            }
        } else {
            if (grant) {
                MessageUtil.send(sender, MM.deserialize(
                    "<gradient:#54DAF4:#545EB6><bold>✓ Granted</bold></gradient> <gray>|</gray> <yellow>" + permNode + "</yellow> <gray>→</gray> <white>" + targets.size() + " players</white> <gray>" + durStr + "</gray>"));
            } else {
                MessageUtil.send(sender, MM.deserialize(
                    "<gradient:#FF4B4B:#FF8585><bold>✕ Revoked</bold></gradient> <gray>|</gray> <yellow>" + permNode + "</yellow> <gray>→</gray> <white>" + targets.size() + " players</white>"));
            }
        }

        // Store global entry when selector used so future joiners get it too
        if (isSelector) {
            if (grant) {
                plugin.getPermissionManager().grantGlobal(permNode, durationSecs);
                MessageUtil.send(sender, MM.deserialize(
                    "<gray><italic>Global grant stored — new players joining will automatically receive this permission.</italic></gray>"));
            } else {
                plugin.getPermissionManager().revokeGlobal(permNode);
            }
        }
        return true;
    }

    private boolean handlePermClaimFree(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.claimfree")) return true;
        if (args.length < 6) {
            usage(sender, "/anima kits permission claimfree <player/selector> <kit> <true|false> [duration]");
            return true;
        }

        List<Player> targets = resolveTargets(sender, args[3]);
        if (targets == null) return true;

        int boolIdx = -1;
        String durationArg = null;
        String lastArg = args[args.length - 1];
        String secondLast = args.length >= 2 ? args[args.length - 2] : "";
        if ((secondLast.equalsIgnoreCase("true") || secondLast.equalsIgnoreCase("false"))
                && !lastArg.equalsIgnoreCase("true") && !lastArg.equalsIgnoreCase("false")) {
            boolIdx = args.length - 2;
            durationArg = lastArg;
        } else if (lastArg.equalsIgnoreCase("true") || lastArg.equalsIgnoreCase("false")) {
            boolIdx = args.length - 1;
        }

        if (boolIdx < 5) {
            usage(sender, "/anima kits permission claimfree <player/selector> <kit> <true|false> [duration]");
            return true;
        }

        String kitNameRaw = joinArgsRange(args, 4, boolIdx);
        Kit kit = requireKit(sender, kitNameRaw);
        if (kit == null) return true;

        boolean grant = Boolean.parseBoolean(args[boolIdx]);
        String kitName = kit.getPlainName();
        String permNode = "anima.kits.claimfree." + kitName;
        long durationSecs = (durationArg != null) ? parseDuration(durationArg) : -1L;
        String durStr = durationSecs == -1 ? "Permanent" : formatDuration(durationSecs);
        boolean isSelector = args[3].startsWith("@");

        for (Player target : targets) {
            if (grant) {
                plugin.getPermissionManager().grant(target.getUniqueId(), permNode, durationSecs);
            } else {
                plugin.getPermissionManager().revoke(target.getUniqueId(), permNode);
            }
        }

        // Single summary message
        if (targets.size() == 1 && !isSelector) {
            if (grant) {
                MessageUtil.sendMsg(sender, "perm-granted",
                        Map.of("perm", permNode, "player", targets.get(0).getName(), "duration", durStr));
            } else {
                MessageUtil.sendMsg(sender, "perm-revoked",
                        Map.of("perm", permNode, "player", targets.get(0).getName()));
            }
        } else {
            if (grant) {
                MessageUtil.send(sender, MM.deserialize(
                    "<gradient:#54DAF4:#545EB6><bold>✓ Granted</bold></gradient> <gray>|</gray> <yellow>" + permNode + "</yellow> <gray>→</gray> <white>" + targets.size() + " players</white> <gray>" + durStr + "</gray>"));
            } else {
                MessageUtil.send(sender, MM.deserialize(
                    "<gradient:#FF4B4B:#FF8585><bold>✕ Revoked</bold></gradient> <gray>|</gray> <yellow>" + permNode + "</yellow> <gray>→</gray> <white>" + targets.size() + " players</white>"));
            }
        }

        if (isSelector) {
            if (grant) {
                plugin.getPermissionManager().grantGlobal(permNode, durationSecs);
                MessageUtil.send(sender, MM.deserialize(
                    "<gray><italic>Global grant stored — new players joining will automatically receive this permission.</italic></gray>"));
            } else {
                plugin.getPermissionManager().revokeGlobal(permNode);
            }
        }
        return true;
    }

    private boolean handleOnJoinNew(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.onjoinnew")) return true;

        // /anima kits onjoinnew           → show current setting
        // /anima kits onjoinnew <kit>     → set the join kit
        // /anima kits onjoinnew clear     → remove the join kit

        if (args.length < 3) {
            String current = plugin.getConfig().getString("join-kit", "");
            if (current == null || current.isBlank()) {
                MessageUtil.send(sender, MM.deserialize("<gray>No OnJoinNew starter kit is currently configured.</gray>"));
            } else {
                MessageUtil.send(sender, MM.deserialize(
                    "<gradient:#54DAF4:#545EB6><bold>OnJoinNew Kit:</bold></gradient> <white>" + current + "</white>"));
            }
            return true;
        }

        String input = joinArgs(args, 2);

        if (input.equalsIgnoreCase("clear") || input.equalsIgnoreCase("none") || input.equalsIgnoreCase("off")) {
            plugin.getConfig().set("join-kit", "");
            plugin.saveConfig();
            MessageUtil.send(sender, MM.deserialize(
                "<gradient:#FF4B4B:#FF8585><bold>✕ OnJoinNew:</bold></gradient> <gray>Starter kit on first join has been disabled.</gray>"));
            return true;
        }

        Kit kit = requireKit(sender, input);
        if (kit == null) return true;

        plugin.getConfig().set("join-kit", kit.getPlainName());
        plugin.saveConfig();
        MessageUtil.send(sender, MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>✓ OnJoinNew:</bold></gradient> <gray>New players will receive kit</gray> <white>" + kit.getPlainName() + "</white> <gray>on their first join.</gray>"));
        return true;
    }

    private boolean handleClaim(CommandSender sender, String[] args) {
        if (!requirePlayer(sender)) return true;
        Player player = (Player) sender;

        if (args.length < 3) {
            new AnimaClaimMainGUI(plugin, player).open();
            return true;
        }

        Kit kit = requireKit(sender, joinArgs(args, 2));
        if (kit == null) return true;

        String claimPerm = "anima.kits.claim." + kit.getPlainName();
        boolean hasBukkitPerm = player.hasPermission(claimPerm)
                || player.hasPermission("anima.kits.claim.*")
                || player.hasPermission("anima.kits.*");
        boolean hasStoredPerm = plugin.getPermissionManager()
                .hasClaimPermission(player.getUniqueId(), kit.getPlainName());

        if (!hasBukkitPerm && !hasStoredPerm) {
            MessageUtil.sendMsg(sender, "no-permission");
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (kit.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, kit.getId())) {
            MessageUtil.err(sender, "You have already claimed this kit once!");
            return true;
        }

        long cooldown = plugin.getPlayerManager().getRemainingCooldown(uuid, kit.getId());
        if (cooldown > 0) {
            boolean hasClaimFree = player.hasPermission("anima.kits.claimfree." + kit.getPlainName())
                    || player.hasPermission("anima.kits.claimfree.*")
                    || plugin.getPermissionManager().hasClaimFreePermission(uuid, kit.getPlainName());
            
            if (!hasClaimFree) {
                MessageUtil.err(sender, "You must wait " + cooldown + " more seconds before claiming this kit again.");
                return true;
            }
        }

        giveKit(player, kit, 1);
        plugin.getPlayerManager().markClaimed(uuid, kit.getId());
        if (kit.getCooldown() > 0) {
            plugin.getPlayerManager().setCooldown(uuid, kit.getId(), kit.getCooldown());
        }
        MessageUtil.sendMsg(player, "kit-received", Map.of("kit", kit.getPlainName(), "amount", "1"));
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!requirePerm(sender, "anima.kits.use")) return true;
        Collection<Kit> kits = plugin.getKitManager().getAllKits();
        if (kits.isEmpty()) {
            MessageUtil.send(sender, MM.deserialize("<red>No kits have been created yet.</red>"));
            return true;
        }
        MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>━━━━━━━ Available Kits ━━━━━━━</bold></gradient>"));
        for (Kit kit : kits) {
            String claimPerm = "anima.kits.claim." + kit.getPlainName();
            boolean hasPerm = sender.hasPermission("anima.kits.*") 
                    || sender.hasPermission("anima.kits.claim.*") 
                    || sender.hasPermission(claimPerm);
            
            if (sender instanceof Player p) {
                if (!hasPerm) {
                    hasPerm = plugin.getPermissionManager().hasClaimPermission(p.getUniqueId(), kit.getPlainName());
                }
            }

            if (!hasPerm) continue;

            String info = " <gray>• </gray>" + kit.getRawName() + " <gray>(ID: " + kit.getPlainName() + ")</gray>";
            if (kit.getCooldown() > 0) info += " <aqua>[" + kit.getCooldown() + "s CD]</aqua>";
            if (kit.isSingleClaim()) info += " <red>[Once]</red>";
            MessageUtil.send(sender, MM.deserialize(info));
        }
        return true;
    }

    private boolean handleSetCooldown(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.setcooldown")) return true;
        if (args.length < 4) {
            usage(sender, "/anima kits setcooldown <kit> <time>  (e.g. 30s  5m  2h  1d  1d12h30m)");
            return true;
        }
        String timeArg = args[args.length - 1];
        String kitName = joinArgsRange(args, 2, args.length - 1);
        Kit kit = requireKit(sender, kitName);
        if (kit == null) return true;
        long time = parseDuration(timeArg);
        if (time == -2) {
            MessageUtil.sendMsg(sender, "cooldown-invalid", Map.of("input", timeArg));
            return true;
        }
        if (time == -1) time = 0; // permanent makes no sense for cooldown — treat as clear
        kit.setCooldown(time);
        plugin.getKitManager().saveKits();
        MessageUtil.sendMsg(sender, "cooldown-set", Map.of(
                "kit",  kit.getPlainName(),
                "time", time == 0 ? "None" : formatDuration(time)));
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
        MessageUtil.sendMsg(sender, "singleclaim-set", Map.of(
                "kit",   kit.getPlainName(),
                "value", val ? "<green>ON</green>" : "<red>OFF</red>"));
        return true;
    }

    private boolean requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtil.err(sender, "This command requires an in-game player.");
            return false;
        }
        return true;
    }

    private boolean requirePerm(CommandSender sender, String perm) {
        if (!sender.hasPermission(perm) && !sender.hasPermission("anima.kits.*")) {
            MessageUtil.sendMsg(sender, "no-permission");
            return false;
        }
        return true;
    }

    private Kit requireKit(CommandSender sender, String name) {
        Kit kit = plugin.getKitManager().getKitByPlainName(name);
        if (kit == null) {
            MessageUtil.sendMsg(sender, "kit-not-found", Map.of("kit", name));
        }
        return kit;
    }

    private void usage(CommandSender sender, String usage) {
        MessageUtil.sendMsg(sender, "usage-error", Map.of("usage", usage));
    }

    private void showMainUsage(CommandSender sender) {
        if (sender.hasPermission("anima.kits.add") || sender.hasPermission("anima.kits.*")) {
            MessageUtil.send(sender, MM.deserialize(
                "<gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits · Admin Commands ━━━━</bold></gradient>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>claim [kit]</white> <dark_gray>·</dark_gray> <gray>Open menu / claim a kit</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>add <name></white> <dark_gray>·</dark_gray> <gray>Create a new kit</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>delete <kit></white> <dark_gray>·</dark_gray> <gray>Delete an existing kit</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>rename <kit></white> <dark_gray>·</dark_gray> <gray>Rename a kit in chat</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>lore <add|edit|remove></white> <dark_gray>·</dark_gray> <gray>Manage kit lore</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>clonekit <kit> <new></white> <dark_gray>·</dark_gray> <gray>Clone a kit</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>give <player/selector> <kit> [n]</white> <dark_gray>·</dark_gray> <gray>Give kit [Supports Target Selectors]</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>giveall <kit> [n]</white> <dark_gray>·</dark_gray> <gray>Give kit to everyone</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>permission <subcmd></white> <dark_gray>·</dark_gray> <gray>Timed nodes engine [Supports Selectors]</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>reload</white> <dark_gray>·</dark_gray> <gray>Reload configuration parameters</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>help</white> <dark_gray>·</dark_gray> <gray>Show help configuration layout</gray>"));
            MessageUtil.send(sender, MM.deserialize(
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
        } else {
            MessageUtil.send(sender, MM.deserialize(
                "<gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits · Player Menu ━━━━</bold></gradient>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>claim [kit]</white> <dark_gray>·</dark_gray> <gray>Open the graphical kit menu</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>list</white> <dark_gray>·</dark_gray> <gray>Display all your available kits</gray>"));
            MessageUtil.send(sender, MM.deserialize("<gray>➔ </gray><yellow>/anima kits <white>help</white> <dark_gray>·</dark_gray> <gray>Display info commands</gray>"));
            MessageUtil.send(sender, MM.deserialize(
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
        }
    }

    private String joinArgs(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (i > from) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString().trim();
    }

    /** Join args[from] up to (but not including) args[to]. */
    private String joinArgsRange(String[] args, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to && i < args.length; i++) {
            if (i > from) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString().trim();
    }

    private int parseAmount(CommandSender sender, String raw) {
        try {
            int n = Integer.parseInt(raw);
            if (n < 1) throw new NumberFormatException();
            return n;
        } catch (NumberFormatException e) {
            MessageUtil.err(sender, "Amount must be a positive integer.");
            return -1;
        }
    }

    /**
     * Parses a duration string into total seconds.
     * Supported formats (case-insensitive, combinable):
     *   plain integer  → seconds       e.g. "3600"
     *   -1             → permanent
     *   1d             → 1 day
     *   2h             → 2 hours
     *   30m            → 30 minutes
     *   45s            → 45 seconds
     *   1d2h30m45s     → compound
     * Returns -1 for permanent, -2 if the input is unparseable.
     */
    private long parseDuration(String raw) {
        if (raw == null || raw.isBlank()) return -2;
        String trimmed = raw.trim();
        if (trimmed.equals("-1")) return -1;

        // Plain integer → raw seconds
        try { return Long.parseLong(trimmed); } catch (NumberFormatException ignored) {}

        // Compound format: optional digits followed by d/h/m/s units
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?",
                        java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(trimmed);

        if (!m.matches()) return -2;

        long days    = m.group(1) != null ? Long.parseLong(m.group(1)) : 0;
        long hours   = m.group(2) != null ? Long.parseLong(m.group(2)) : 0;
        long minutes = m.group(3) != null ? Long.parseLong(m.group(3)) : 0;
        long seconds = m.group(4) != null ? Long.parseLong(m.group(4)) : 0;
        long total   = days * 86400L + hours * 3600L + minutes * 60L + seconds;
        return total > 0 ? total : -2;
    }

    /** Human-readable representation of a duration in seconds. */
    private String formatDuration(long seconds) {
        if (seconds <= 0)  return "None";
        if (seconds == -1) return "Permanent";
        long d = seconds / 86400;
        long h = (seconds % 86400) / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("d ");
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (s > 0) sb.append(s).append("s");
        return sb.toString().trim();
    }

    private void giveKit(Player player, Kit kit, int times) {
        for (int t = 0; t < times; t++) {
            for (ItemStack item : kit.getItems()) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item.clone());
                    if (!remaining.isEmpty()) {
                        for (ItemStack left : remaining.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), left);
                        }
                    }
                }
            }
        }
    }
}
