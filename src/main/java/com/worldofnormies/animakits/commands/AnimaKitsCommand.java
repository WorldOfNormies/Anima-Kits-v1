package com.worldofnormies.animakits.commands;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.KitBrowserGui;
import com.worldofnormies.animakits.gui.KitDisplayGui;
import com.worldofnormies.animakits.gui.KitEditorGui;
import com.worldofnormies.animakits.kit.Kit;
import com.worldofnormies.animakits.manager.PermissionManager;
import com.worldofnormies.animakits.util.MessageUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.*;

/**
 * AnimaKitsCommand – handles all /anima kits [...] subcommands.
 *
 * The root command is /anima; the first required argument is always "kits".
 * i.e. /anima kits [subcommand] [args…]
 */
public class AnimaKitsCommand implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    /** Valid AnimaKits permissions for the /anima kits permission subcommand. */
    private static final List<String> VALID_PERMS = Arrays.asList(
            "anima.kits.*",
            "anima.kits.use",
            "anima.kits.display",
            "anima.kits.edit.add",
            "anima.kits.edit.remove",
            "anima.kits.edit.rename",
            "anima.kits.lore.add",
            "anima.kits.lore.edit",
            "anima.kits.lore.remove",
            "anima.kits.open",
            "anima.kits.clonekit",
            "anima.kits.give",
            "anima.kits.giveall",
            "anima.kits.reload",
            "anima.kits.help",
            "anima.kits.permission.add",
            "anima.kits.permission.remove",
            "anima.kits.permission.show"
    );

    private final AnimaKitsPlugin plugin;

    public AnimaKitsCommand(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Must start with "kits"
        if (args.length == 0 || !args[0].equalsIgnoreCase("kits")) {
            showMainUsage(sender);
            return true;
        }

        // /anima kits  → open browser
        if (args.length == 1) {
            return handleOpenBrowser(sender, args);
        }

        String sub = args[1].toLowerCase();
        return switch (sub) {
            case "display"       -> handleDisplay(sender, args);
            case "edit"          -> handleEdit(sender, args);
            case "lore"          -> handleLore(sender, args);
            case "open"          -> handleOpen(sender, args);
            case "clonekit"      -> handleClone(sender, args);
            case "give"          -> handleGive(sender, args);
            case "giveall"       -> handleGiveAll(sender, args);
            case "reload"        -> handleReload(sender);
            case "help"          -> handleHelp(sender);
            case "permission"    -> handlePermission(sender, args);
            case "claim"         -> handleClaim(sender, args);
            case "list"          -> handleList(sender);
            case "setcooldown"   -> handleSetCooldown(sender, args);
            case "singleclaim"   -> handleSingleClaim(sender, args);
            default              -> { showMainUsage(sender); yield true; }
        };
    }

    // ─────────────────────────────────────────────────────────────
    // /anima kits  (no sub = open browser)
    // ─────────────────────────────────────────────────────────────

    private boolean handleOpenBrowser(CommandSender sender, String[] args) {
        if (!requirePlayer(sender)) return true;
        if (!requirePerm(sender, "anima.kits.use")) return true;
        Player player = (Player) sender;
        new KitBrowserGui(plugin, player).open();
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // /anima kits display <kit>
    // ─────────────────────────────────────────────────────────────

    private boolean handleDisplay(CommandSender sender, String[] args) {
        if (!requirePlayer(sender)) return true;
        if (!requirePerm(sender, "anima.kits.display")) return true;
        if (args.length < 3) { usage(sender, "/anima kits display <kit>"); return true; }
        Kit kit = requireKit(sender, args[2]);
        if (kit == null) return true;
        new KitDisplayGui(plugin, (Player) sender, kit).open();
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // /anima kits edit add|remove|rename ...
    // ─────────────────────────────────────────────────────────────

    private boolean handleEdit(CommandSender sender, String[] args) {
        if (args.length < 3) { usage(sender, "/anima kits edit <add|remove|rename> [args]"); return true; }
        return switch (args[2].toLowerCase()) {
            case "add"    -> handleEditAdd(sender, args);
            case "remove" -> handleEditRemove(sender, args);
            case "rename" -> handleEditRename(sender, args);
            default       -> { usage(sender, "/anima kits edit <add|remove|rename>"); yield true; }
        };
    }

    private boolean handleEditAdd(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.edit.add")) return true;
        String rawName;
        if (args.length < 4) {
            rawName = "anima new kit";
        } else {
            rawName = joinArgs(args, 3);
        }
        if (rawName.isEmpty()) { MessageUtil.sendMsg(sender, "name-empty"); return true; }
        if (rawName.length() > 64) { MessageUtil.sendMsg(sender, "name-too-long"); return true; }
        if (plugin.getKitManager().kitExists(rawName)) {
            MessageUtil.sendMsg(sender, "kit-already-exists", Map.of("kit", rawName)); return true;
        }
        Kit kit = plugin.getKitManager().createKit(rawName);
        MessageUtil.sendMsg(sender, "kit-created", Map.of("kit", kit.getPlainName()));
        return true;
    }

    private boolean handleEditRemove(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.edit.remove")) return true;
        if (args.length < 4) { usage(sender, "/anima kits edit remove <kit>"); return true; }
        String name = args[3];
        if (!plugin.getKitManager().deleteKit(name)) {
            MessageUtil.sendMsg(sender, "kit-not-found", Map.of("kit", name)); return true;
        }
        MessageUtil.sendMsg(sender, "kit-deleted", Map.of("kit", name));
        return true;
    }

    private boolean handleEditRename(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.edit.rename")) return true;
        if (args.length < 5) {
            usage(sender, "/anima kits edit rename <kit> <new-name>");
            showNameHints(sender);
            return true;
        }
        String oldName   = args[3];
        String newRawName = joinArgs(args, 4);
        if (newRawName.isEmpty()) { MessageUtil.sendMsg(sender, "name-empty"); return true; }
        if (newRawName.length() > 64) { MessageUtil.sendMsg(sender, "name-too-long"); return true; }
        if (!plugin.getKitManager().renameKit(oldName, newRawName)) {
            MessageUtil.sendMsg(sender, "kit-not-found", Map.of("kit", oldName)); return true;
        }
        MessageUtil.sendMsg(sender, "kit-renamed", Map.of("old", oldName, "new", newRawName));
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // /anima kits lore add|edit|remove ...
    // ─────────────────────────────────────────────────────────────

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
        String kitName = args[3];
        String line = joinArgs(args, 4);
        if (!plugin.getKitManager().addLore(kitName, line)) {
            MessageUtil.sendMsg(sender, "kit-not-found", Map.of("kit", kitName)); return true;
        }
        MessageUtil.sendMsg(sender, "lore-added", Map.of("kit", kitName));
        return true;
    }

    private boolean handleLoreEdit(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.lore.edit")) return true;
        if (args.length < 6) { usage(sender, "/anima kits lore edit <kit> <line#> <text…>"); return true; }
        String kitName = args[3];
        Kit kit = requireKit(sender, kitName);
        if (kit == null) return true;
        int lineNum;
        try { lineNum = Integer.parseInt(args[4]); } catch (NumberFormatException e) {
            MessageUtil.sendMsg(sender, "lore-invalid-line", Map.of("max", String.valueOf(kit.getLore().size())));
            return true;
        }
        if (lineNum < 1 || lineNum > kit.getLore().size()) {
            MessageUtil.sendMsg(sender, "lore-invalid-line", Map.of("max", String.valueOf(kit.getLore().size())));
            return true;
        }
        String text = joinArgs(args, 5);
        plugin.getKitManager().editLore(kitName, lineNum - 1, text);
        MessageUtil.sendMsg(sender, "lore-edited", Map.of("kit", kitName, "line", String.valueOf(lineNum)));
        return true;
    }

    private boolean handleLoreRemove(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.lore.remove")) return true;
        if (args.length < 5) { usage(sender, "/anima kits lore remove <kit> <line#>"); return true; }
        String kitName = args[3];
        Kit kit = requireKit(sender, kitName);
        if (kit == null) return true;
        int lineNum;
        try { lineNum = Integer.parseInt(args[4]); } catch (NumberFormatException e) {
            MessageUtil.sendMsg(sender, "lore-invalid-line", Map.of("max", String.valueOf(kit.getLore().size())));
            return true;
        }
        if (!plugin.getKitManager().removeLore(kitName, lineNum - 1)) {
            MessageUtil.sendMsg(sender, "lore-invalid-line", Map.of("max", String.valueOf(kit.getLore().size())));
            return true;
        }
        MessageUtil.sendMsg(sender, "lore-removed", Map.of("kit", kitName, "line", String.valueOf(lineNum)));
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // /anima kits open <kit>
    // ─────────────────────────────────────────────────────────────

    private boolean handleOpen(CommandSender sender, String[] args) {
        if (!requirePlayer(sender)) return true;
        if (!requirePerm(sender, "anima.kits.open")) return true;
        if (args.length < 3) { usage(sender, "/anima kits open <kit>"); return true; }
        Kit kit = requireKit(sender, args[2]);
        if (kit == null) return true;
        new KitEditorGui(plugin, (Player) sender, kit, 0).open();
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // /anima kits clonekit <kit> <new-name>
    // ─────────────────────────────────────────────────────────────

    private boolean handleClone(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.clonekit")) return true;
        if (args.length < 4) { usage(sender, "/anima kits clonekit <kit> <new-name>"); return true; }
        String srcName = args[2];
        String newName = joinArgs(args, 3);
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

    // ─────────────────────────────────────────────────────────────
    // /anima kits give <player> <kit> <amount>
    // ─────────────────────────────────────────────────────────────

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.give")) return true;
        if (args.length < 5) { usage(sender, "/anima kits give <player> <kit> <amount>"); return true; }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) { MessageUtil.sendMsg(sender, "player-not-found", Map.of("player", args[2])); return true; }
        Kit kit = requireKit(sender, args[3]);
        if (kit == null) return true;
        int amount = parseAmount(sender, args[4]);
        if (amount < 1) return true;
        giveKit(target, kit, amount);
        MessageUtil.sendMsg(sender, "kit-given", Map.of("kit", kit.getPlainName(), "player", target.getName(), "amount", String.valueOf(amount)));
        MessageUtil.sendMsg(target, "kit-received", Map.of("kit", kit.getPlainName(), "amount", String.valueOf(amount)));
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // /anima kits giveall <kit> <amount>
    // ─────────────────────────────────────────────────────────────

    private boolean handleGiveAll(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.giveall")) return true;
        if (args.length < 4) { usage(sender, "/anima kits giveall <kit> <amount>"); return true; }
        Kit kit = requireKit(sender, args[2]);
        if (kit == null) return true;
        int amount = parseAmount(sender, args[3]);
        if (amount < 1) return true;
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        for (Player p : online) {
            giveKit(p, kit, amount);
            MessageUtil.sendMsg(p, "kit-received", Map.of("kit", kit.getPlainName(), "amount", String.valueOf(amount)));
        }
        MessageUtil.sendMsg(sender, "kit-given-all", Map.of("kit", kit.getPlainName(), "amount", String.valueOf(amount), "count", String.valueOf(online.size())));
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // /anima kits reload
    // ─────────────────────────────────────────────────────────────

    private boolean handleReload(CommandSender sender) {
        if (!requirePerm(sender, "anima.kits.reload")) return true;
        plugin.reload();
        MessageUtil.sendMsg(sender, "reload");
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // /anima kits help
    // ─────────────────────────────────────────────────────────────

    private boolean handleHelp(CommandSender sender) {
        if (!requirePerm(sender, "anima.kits.help")) return true;
        MessageUtil.send(sender, MM.deserialize("""
                <gradient:#54DAF4:#545EB6><bold>━━━━━━━ AnimaKits Help ━━━━━━━</bold></gradient>
                <yellow>/anima kits</yellow>                              <gray>Open the kit browser</gray>
                <yellow>/anima kits display <kit></yellow>          <gray>View a kit (read-only)</gray>
                <yellow>/anima kits edit add <name></yellow>         <gray>Create a new kit</gray>
                <yellow>/anima kits edit remove <kit></yellow>       <gray>Delete a kit</gray>
                <yellow>/anima kits edit rename <kit> <new></yellow> <gray>Rename a kit</gray>
                <yellow>/anima kits lore add <kit> <text></yellow>   <gray>Add a lore line</gray>
                <yellow>/anima kits lore edit <kit> <#> <text></yellow> <gray>Edit a lore line</gray>
                <yellow>/anima kits lore remove <kit> <#></yellow>  <gray>Remove a lore line</gray>
                <yellow>/anima kits open <kit></yellow>             <gray>Open kit editor GUI</gray>
                <yellow>/anima kits clonekit <kit> <new></yellow>   <gray>Clone a kit</gray>
                <yellow>/anima kits give <player> <kit> <n></yellow> <gray>Give kit to player</gray>
                <yellow>/anima kits giveall <kit> <n></yellow>      <gray>Give kit to all online</gray>
                <yellow>/anima kits permission ...</yellow>         <gray>Manage timed perms</gray>
                <yellow>/anima kits reload</yellow>                 <gray>Reload config</gray>
                <gradient:#54DAF4:#545EB6><bold></bold></gradient>
                <gradient:#54DAF4:#545EB6><bold>━━━━━━━  Gradient Font Guide  ━━━━━━━</bold></gradient>
                                                
                <gradient:#54DAF4:#545EB6><bold>Start by typing <white>"<"gradient #HEX:#HEX">"</white> without → "".</bold></gradient>
                <gradient:#54DAF4:#545EB6><bold>"The Perfered Kit Name".<white></bold></gradient>
                <gradient:#54DAF4:#545EB6><bold>and end with <white>"<"/gradient">"<white> without → "".</bold></gradient>
                                                
                <gradient:#54DAF4:#545EB6><bold>━━━━━━━  Colour & Style Guide  ━━━━━━</bold></gradient>
                                                
                <gray>Legacy codes:  <white>&a Green  &c Red  &b Aqua  &6 Gold  &l Bold  &o Italic</white></gray>
                <gradient:#54DAF4:#545EB6><bold>You can also typed them as "<"red"> MSG <"/red"> without → ""</bold></gradient> 
                <gradient:#54DAF4:#545EB6><bold>same as Bold|Italic|Underline as "<"Bold" > without → "" </"Bold">"</bold></gradient> 
                                                
                <gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━  Examples  ━━━━━━━━━━━━</bold></gradient>
                                                
                <gray>Hex colour:    <white>&#FF5500MyText</white>  → <color:#FF5500>MyText</color></gray>
                                                
                <gradient:#54DAF4:#545EB6><bold>━━━━ Color Bold Italic Underline ━━━━</bold></gradient>
                                                
                <gray>MiniMessage:   <white><red>Red</red>  <bold>Bold</bold>  <italic>Italic</italic></white></gray>                               
                <gray>Gradient:      <white><gradient:#54DAF4:#545EB6>My Kit Name</gradient></white></gray>                                             
                <gray><"Rainbow">:       <white><rainbow>Rainbow Kit</rainbow></white></gray>                                
                <gray>Example kit name: <white><gradient:#FF6B6B:#FFE66D>Fire Kit</gradient></white></gray>
                <gray>Example lore:     <white><italic><gray>A blazing hot kit!</gray></italic></white></gray>
                                                
                <gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>
                """));
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // /anima kits permission add|remove|show ...
    // ─────────────────────────────────────────────────────────────

    private boolean handlePermission(CommandSender sender, String[] args) {
        if (args.length < 3) { usage(sender, "/anima kits permission <add|remove|show> [args]"); return true; }
        return switch (args[2].toLowerCase()) {
            case "add"    -> handlePermAdd(sender, args);
            case "remove" -> handlePermRemove(sender, args);
            case "show"   -> handlePermShow(sender, args);
            default       -> { usage(sender, "/anima kits permission <add|remove|show>"); yield true; }
        };
    }

    private boolean handlePermAdd(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.add")) return true;
        if (args.length < 6) { usage(sender, "/anima kits permission add <perm> <player> <time|-1>"); return true; }
        String perm = args[3];
        if (!VALID_PERMS.contains(perm)) { MessageUtil.sendMsg(sender, "perm-invalid", Map.of("perm", perm)); return true; }
        Player target = Bukkit.getPlayerExact(args[4]);
        if (target == null) { MessageUtil.sendMsg(sender, "player-not-found", Map.of("player", args[4])); return true; }
        long durationSecs = parseDuration(args[5]);
        plugin.getPermissionManager().grant(target.getUniqueId(), perm, durationSecs);
        String durStr = durationSecs == -1 ? " permanently" : " for " + args[5];
        MessageUtil.sendMsg(sender, "perm-granted", Map.of("perm", perm, "player", target.getName(), "duration", durStr));
        return true;
    }

    private boolean handlePermRemove(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.remove")) return true;
        if (args.length < 5) { usage(sender, "/anima kits permission remove <perm> <player>"); return true; }
        String perm = args[3];
        Player target = Bukkit.getPlayerExact(args[4]);
        if (target == null) { MessageUtil.sendMsg(sender, "player-not-found", Map.of("player", args[4])); return true; }
        if (!plugin.getPermissionManager().revoke(target.getUniqueId(), perm)) {
            MessageUtil.err(sender, target.getName() + " does not have " + perm); return true;
        }
        MessageUtil.sendMsg(sender, "perm-revoked", Map.of("perm", perm, "player", target.getName()));
        return true;
    }

    private boolean handlePermShow(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.show")) return true;
        if (args.length < 4) { usage(sender, "/anima kits permission show <player>"); return true; }
        Player target = Bukkit.getPlayerExact(args[3]);
        if (target == null) { MessageUtil.sendMsg(sender, "player-not-found", Map.of("player", args[3])); return true; }
        Map<String, Long> perms = plugin.getPermissionManager().getAll(target.getUniqueId());
        MessageUtil.sendMsg(sender, "perm-show-header", Map.of("player", target.getName()));
        if (perms.isEmpty()) { MessageUtil.sendMsg(sender, "perm-show-empty"); return true; }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                plugin.getConfig().getString("general.date-format", "dd/MM/yyyy HH:mm"))
                .withZone(ZoneId.systemDefault());
        for (Map.Entry<String, Long> e : perms.entrySet()) {
            String expires = e.getValue() == -1 ? "never" : fmt.format(Instant.ofEpochSecond(e.getValue()));
            MessageUtil.sendMsg(sender, "perm-show-entry", Map.of("perm", e.getKey(), "expires", expires));
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

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
        MessageUtil.send(sender, MM.deserialize(
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>/n" +
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  USE: /anima kits  ━━━━━━━</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>/n" +
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  display</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  edit</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  lore</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  open</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  clonekit</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  give</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  giveall</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  permission</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  reload</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  help</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>/n"));
    }

    private void showNameHints(CommandSender sender) {
        MessageUtil.send(sender, MM.deserialize(
            
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  Gradient Font Guide  ━━━━━━━</bold></gradient>/n" +  
                "<gradient:#54DAF4:#545EB6><bold></bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold>Start by typing <white>'<'gradient #HEX:#HEX'>'</white> without → ''.</bold></gradient>/n" +
                "<gradient:#54DAF4:#545EB6><bold>'The Perfered Kit Name'.<white></bold></gradient>/n" +
                "<gradient:#54DAF4:#545EB6><bold>and end with <white>'<'/gradient'>'<white> without → ''.</bold></gradient>/n" + 
                "<gradient:#54DAF4:#545EB6><bold></bold></gradient>/n" +                 
                "<gradient:#54DAF4:#545EB6><bold>━━━━━━━  Colour & Style Guide  ━━━━━━</bold></gradient>/n" +
                "<gradient:#54DAF4:#545EB6><bold></bold></gradient>/n" +  
                "<gray>Legacy codes:  <white>&a Green  &c Red  &b Aqua  &6 Gold  &l Bold  &o Italic</white></gray>/n" +
                "<gradient:#54DAF4:#545EB6><bold>You can also typed them as '<'red'> MSG <'/red'> without → ''</bold></gradient>/n" +
                "<gradient:#54DAF4:#545EB6><bold>same as Bold|Italic|Underline as '<'Bold' > without → '' </'Bold'>'</bold></gradient>/n"));
    }

    private String joinArgs(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
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
     * Parse a duration string: -1 = permanent, 30m, 1h, 6h, 12h, 1d, 7d, 30d.
     * Returns seconds, or -1 for permanent.
     */
    private long parseDuration(String raw) {
        if (raw.equals("-1")) return -1;
        long multiplier = 1;
        String stripped = raw;
        if (raw.endsWith("m"))      { multiplier = 60;               stripped = raw.substring(0, raw.length() - 1); }
        else if (raw.endsWith("h")) { multiplier = 3600;             stripped = raw.substring(0, raw.length() - 1); }
        else if (raw.endsWith("d")) { multiplier = 86400;            stripped = raw.substring(0, raw.length() - 1); }
        try { return Long.parseLong(stripped) * multiplier; }
        catch (NumberFormatException e) { return 3600; } // default 1h on parse error
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

    private boolean handleClaim(CommandSender sender, String[] args) {
        if (!requirePlayer(sender)) return true;
        if (args.length < 3) { usage(sender, "/anima kits claim <kit>"); return true; }
        Player player = (Player) sender;
        Kit kit = requireKit(sender, args[2]);
        if (kit == null) return true;

        if (!requirePerm(sender, "anima.kits.claim." + kit.getPlainName())) return true;

        UUID uuid = player.getUniqueId();
        if (kit.isSingleClaim() && plugin.getPlayerManager().hasClaimed(uuid, kit.getId())) {
            MessageUtil.err(sender, "You have already claimed this kit once!");
            return true;
        }

        long cooldown = plugin.getPlayerManager().getRemainingCooldown(uuid, kit.getId());
        if (cooldown > 0) {
            MessageUtil.err(sender, "You must wait " + cooldown + " more seconds before claiming this kit again.");
            return true;
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
        if (!requirePerm(sender, "anima.kits.list")) return true;
        Collection<Kit> kits = plugin.getKitManager().getAllKits();
        if (kits.isEmpty()) {
            MessageUtil.send(sender, MM.deserialize("<red>No kits have been created yet.</red>"));
            return true;
        }
        MessageUtil.send(sender, MM.deserialize("<gradient:#54DAF4:#545EB6><bold>━━━━━━━ Available Kits ━━━━━━━</bold></gradient>"));
        for (Kit kit : kits) {
            String info = " <gray>• </gray>" + kit.getRawName() + " <gray>(ID: " + kit.getPlainName() + ")</gray>";
            if (kit.getCooldown() > 0) info += " <aqua>[" + kit.getCooldown() + "s CD]</aqua>";
            if (kit.isSingleClaim()) info += " <red>[Once]</red>";
            MessageUtil.send(sender, MM.deserialize(info));
        }
        return true;
    }

    private boolean handleSetCooldown(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.setcooldown")) return true;
        if (args.length < 4) { usage(sender, "/anima kits setcooldown <kit> <time_seconds>"); return true; }
        Kit kit = requireKit(sender, args[2]);
        if (kit == null) return true;
        try {
            long time = Long.parseLong(args[3]);
            kit.setCooldown(time);
            plugin.getKitManager().saveKits();
            MessageUtil.send(sender, MM.deserialize("<green>Cooldown for kit <white>" + kit.getPlainName() + "</white> set to <white>" + time + "</white> seconds.</green>"));
        } catch (NumberFormatException e) {
            MessageUtil.err(sender, "Cooldown must be a number (seconds).");
        }
        return true;
    }

    private boolean handleSingleClaim(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.singleclaim")) return true;
        if (args.length < 4) { usage(sender, "/anima kits singleclaim <kit> <true|false>"); return true; }
        Kit kit = requireKit(sender, args[2]);
        if (kit == null) return true;
        boolean val = Boolean.parseBoolean(args[3]);
        kit.setSingleClaim(val);
        plugin.getKitManager().saveKits();
        MessageUtil.send(sender, MM.deserialize("<green>Single claim for kit <white>" + kit.getPlainName() + "</white> set to <white>" + val + "</white>.</green>"));
        return true;
    }
}
