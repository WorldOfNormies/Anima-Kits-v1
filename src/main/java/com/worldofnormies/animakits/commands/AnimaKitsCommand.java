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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.*;

/** 
 * AnimaKitsCommand – handles all /anima kits [...] subcommands.
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
            "anima.kits.claim",
            "anima.kits.claim.*",
            "anima.kits.list",
            "anima.kits.setcooldown",
            "anima.kits.singleclaim"
    );

    private final AnimaKitsPlugin plugin;

    public AnimaKitsCommand(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        boolean isKitAlias = label.equalsIgnoreCase("kit");

        if (isKitAlias) {
            if (args.length == 0) {
                return handleOpenAdminGUI(sender);
            }
        } else {
            if (args.length == 0 || (!args[0].equalsIgnoreCase("kits") && !args[0].equalsIgnoreCase("help"))) {
                showMainUsage(sender);
                return true;
            }

            if (args[0].equalsIgnoreCase("help")) {
                return handleHelp(sender);
            }

            if (args.length == 1) {
                return handleOpenAdminGUI(sender);
            }
        }

        int offset = isKitAlias ? 0 : 1;

        String sub = args[offset].toLowerCase();
        String[] shiftedArgs = args;
        if (isKitAlias) {
            shiftedArgs = new String[args.length + 1];
            shiftedArgs[0] = "kits";
            System.arraycopy(args, 0, shiftedArgs, 1, args.length);
        }

        return switch (sub) {
            case "claim"         -> handleClaim(sender, shiftedArgs);
            case "add"           -> handleAdd(sender, shiftedArgs);
            case "delete"        -> handleDelete(sender, shiftedArgs);
            case "rename"        -> handleRename(sender, shiftedArgs);
            case "lore"          -> handleLore(sender, shiftedArgs);
            case "clonekit"      -> handleClone(sender, shiftedArgs);
            case "give"          -> handleGive(sender, shiftedArgs);
            case "giveall"       -> handleGiveAll(sender, shiftedArgs);
            case "reload"        -> handleReload(sender);
            case "help"          -> handleHelp(sender);
            case "permission"    -> handlePermission(sender, shiftedArgs);
            case "list"          -> handleList(sender);
            case "setcooldown"   -> handleSetCooldown(sender, shiftedArgs);
            case "singleclaim"   -> handleSingleClaim(sender, shiftedArgs);
            default              -> { showMainUsage(sender); yield true; }
        };
    }

    private boolean handleOpenAdminGUI(CommandSender sender) {
        if (!requirePlayer(sender)) return true;
        if (!requirePerm(sender, "anima.kits.use")) return true;
        Player player = (Player) sender;
        if (player.hasPermission("anima.kits.add") || player.hasPermission("anima.kits.admin")) {
            new AnimaKitsMainGUI(plugin, player).open();
        } else {
            new AnimaClaimMainGUI(plugin, player).open();
        }
        return true;
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
        String name = args[2];
        if (!plugin.getKitManager().deleteKit(name)) {
            MessageUtil.sendMsg(sender, "kit-not-found", Map.of("kit", name)); return true;
        }
        MessageUtil.sendMsg(sender, "kit-deleted", Map.of("kit", name));
        return true;
    }

    private boolean handleRename(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.rename")) return true;
        if (args.length < 4) { usage(sender, "/anima kits rename <kit> <new-name>"); return true; }
        String oldName   = args[2];
        String newRawName = joinArgs(args, 3);
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

    private boolean handleReload(CommandSender sender) {
        if (!requirePerm(sender, "anima.kits.reload")) return true;
        plugin.reload();
        MessageUtil.sendMsg(sender, "reload");
        return true;
    }

    private boolean handleHelp(CommandSender sender) {
        List<String> lines = new ArrayList<>();
        lines.add("<gradient:#AA00FF:#FF6AFF:#FFFFFF:#FF6AFF:#AA00FF><bold>⋆༺⸸ Anima Kits Help ⸸༻⋆</bold></gradient>");
        lines.add("<gray>Available commands based on your permissions:</gray>");
        lines.add("");

        if (sender.hasPermission("anima.kits.use")) {
            lines.add("<gradient:#FFD700:#FFA500>⬡ /anima kits</gradient> <dark_gray>»</dark_gray> <white>Open kit menu</white>");
            lines.add("<gradient:#FFD700:#FFA500>⬡ /anima kits claim [kit]</gradient> <dark_gray>»</dark_gray> <white>Claim a kit</white>");
        }

        if (sender.hasPermission("anima.kits.add")) {
            lines.add("<gradient:#4FC3F7:#1565C0>✦ /anima kits add <name></gradient> <dark_gray>»</dark_gray> <white>Create a kit</white>");
        }
        if (sender.hasPermission("anima.kits.delete")) {
            lines.add("<gradient:#4FC3F7:#1565C0>✦ /anima kits delete <kit></gradient> <dark_gray>»</dark_gray> <white>Delete a kit</white>");
        }
        if (sender.hasPermission("anima.kits.rename")) {
            lines.add("<gradient:#4FC3F7:#1565C0>✦ /anima kits rename <kit> <new></gradient> <dark_gray>»</dark_gray> <white>Rename a kit</white>");
        }
        if (sender.hasPermission("anima.kits.clonekit")) {
            lines.add("<gradient:#4FC3F7:#1565C0>✦ /anima kits clonekit <kit> <new></gradient> <dark_gray>»</dark_gray> <white>Clone a kit</white>");
        }
        if (sender.hasPermission("anima.kits.give")) {
            lines.add("<gradient:#A5D6A7:#2E7D32>✦ /anima kits give <player> <kit> [n]</gradient> <dark_gray>»</dark_gray> <white>Give kit</white>");
        }
        if (sender.hasPermission("anima.kits.reload")) {
            lines.add("<gradient:#EF9A9A:#B71C1C>✦ /anima kits reload</gradient> <dark_gray>»</dark_gray> <white>Reload plugin</white>");
        }

        lines.add("");
        lines.add("<gradient:#AA00FF:#FF6AFF:#FFFFFF:#FF6AFF:#AA00FF><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>");

        for (String line : lines) {
            MessageUtil.send(sender, MM.deserialize(line));
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // /anima kits permission ...
    // -----------------------------------------------------------------------

    private boolean handlePermission(CommandSender sender, String[] args) {
        if (args.length < 3) {
            usage(sender, "/anima kits permission <add|remove|show|claim> [args]");
            return true;
        }
        return switch (args[2].toLowerCase()) {
            case "add"    -> handlePermAdd(sender, args);
            case "remove" -> handlePermRemove(sender, args);
            case "show"   -> handlePermShow(sender, args);
            case "claim"  -> handlePermClaim(sender, args);
            default       -> { usage(sender, "/anima kits permission <add|remove|show|claim>"); yield true; }
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

    /**
     * /anima kits permission claim &lt;player&gt; &lt;kit&gt; &lt;true|false&gt; [duration]
     *
     * <ul>
     *   <li>true  – grant  anima.kits.claim.&lt;kit&gt; to the player</li>
     *   <li>false – revoke anima.kits.claim.&lt;kit&gt; from the player</li>
     * </ul>
     *
     * Optional [duration] follows the same format as permission add: {@code 30m}, {@code 2h},
     * {@code 7d}, a raw number of seconds, or {@code -1} for permanent (default when omitted).
     */
    private boolean handlePermClaim(CommandSender sender, String[] args) {
        if (!requirePerm(sender, "anima.kits.permission.add")) return true;
        // args: 0=kits 1=permission 2=claim 3=<player> 4=<kit> 5=<true|false> [6=duration]
        if (args.length < 6) {
            usage(sender, "/anima kits permission claim <player> <kit> <true|false> [duration]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[3]);
        if (target == null) {
            MessageUtil.sendMsg(sender, "player-not-found", Map.of("player", args[3]));
            return true;
        }

        Kit kit = requireKit(sender, args[4]);
        if (kit == null) return true;

        boolean grant = Boolean.parseBoolean(args[5]);
        if (!args[5].equalsIgnoreCase("true") && !args[5].equalsIgnoreCase("false")) {
            usage(sender, "/anima kits permission claim <player> <kit> <true|false> [duration]");
            return true;
        }

        String kitName = kit.getPlainName();
        String permNode = PermissionManager.CLAIM_PREFIX + kitName;

        if (grant) {
            long durationSecs = (args.length >= 7) ? parseDuration(args[6]) : -1L;
            plugin.getPermissionManager().grantClaimPermission(target.getUniqueId(), kitName, durationSecs);
            String durStr = durationSecs == -1 ? " permanently" : " for " + args[6];
            MessageUtil.sendMsg(sender, "perm-granted",
                    Map.of("perm", permNode, "player", target.getName(), "duration", durStr));
        } else {
            if (!plugin.getPermissionManager().revokeClaimPermission(target.getUniqueId(), kitName)) {
                MessageUtil.err(sender, target.getName() + " does not have " + permNode);
                return true;
            }
            MessageUtil.sendMsg(sender, "perm-revoked",
                    Map.of("perm", permNode, "player", target.getName()));
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Claim handler
    // -----------------------------------------------------------------------

    private boolean handleClaim(CommandSender sender, String[] args) {
        if (!requirePlayer(sender)) return true;
        Player player = (Player) sender;

        if (args.length < 3) {
            new AnimaClaimMainGUI(plugin, player).open();
            return true;
        }

        Kit kit = requireKit(sender, args[2]);
        if (kit == null) return true;

        // Check claim permission: Bukkit perm OR stored per-kit perm
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

    // -----------------------------------------------------------------------
    // Remaining handlers
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

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
        MessageUtil.send(sender, MM.deserialize("<gradient:#FF4444:#CC0000><bold>✘ Invalid Usage!</bold></gradient> <gray>Type <white>/anima help</white> for a list of commands.</gray>"));
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

    private long parseDuration(String raw) {
        if (raw.equals("-1")) return -1;
        long multiplier = 1;
        String stripped = raw;
        if (raw.endsWith("m"))      { multiplier = 60;    stripped = raw.substring(0, raw.length() - 1); }
        else if (raw.endsWith("h")) { multiplier = 3600;  stripped = raw.substring(0, raw.length() - 1); }
        else if (raw.endsWith("d")) { multiplier = 86400; stripped = raw.substring(0, raw.length() - 1); }
        try { return Long.parseLong(stripped) * multiplier; }
        catch (NumberFormatException e) { return 3600; }
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