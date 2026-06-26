package com.worldofnormies.animaitemedit;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * ItemEditCommand – handles all /anima itemedit [...] subcommands.
 * Delegates item manipulation to {@link ItemEditUtil}.
 *
 * args[0] = "itemedit"
 * args[1] = sub (prefix / suffix / rename / lore / enchant / unbreakable / gloweffect / repair)
 * args[2+] = action / value
 */
public class ItemEditCommand {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin plugin;

    public ItemEditCommand(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MM.deserialize("<red>Only players can use /anima itemedit.</red>"));
            return true;
        }

        if (!player.hasPermission("anima.itemedit.use") && !player.hasPermission("anima.itemedit.*") && !player.hasPermission("anima.ranks.admin")) {
            player.sendMessage(MM.deserialize("<red>✘ You do not have permission to use item editing.</red>"));
            return true;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType().isAir()) {
            player.sendMessage(MM.deserialize("<red>✘ You must be holding an item to edit it.</red>"));
            return true;
        }

        if (args.length < 2) {
            sendUsage(player);
            return true;
        }

        String sub = args[1].toLowerCase();

        boolean isAdmin = player.hasPermission("anima.itemedit.*") || player.hasPermission("anima.ranks.admin");

        switch (sub) {
            case "prefix" -> {
                if (args.length < 3) { sendSubUsage(player, "prefix <set|remove> [value]"); return true; }
                String action = args[2].toLowerCase();
                if (action.equals("remove")) {
                    player.getInventory().setItemInMainHand(ItemEditUtil.removePrefix(held));
                    player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Prefix removed.</gradient>"));
                } else if (action.equals("set") || action.equals("edit")) {
                    if (args.length < 4) { sendSubUsage(player, "prefix set <value>"); return true; }
                    String value = joinArgs(args, 3);
                    player.getInventory().setItemInMainHand(ItemEditUtil.setPrefix(held, value));
                    player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Prefix set to: </gradient>" + value));
                } else {
                    sendSubUsage(player, "prefix <set|remove> [value]");
                }
            }
            case "suffix" -> {
                if (args.length < 3) { sendSubUsage(player, "suffix <set|remove> [value]"); return true; }
                String action = args[2].toLowerCase();
                if (action.equals("remove")) {
                    player.getInventory().setItemInMainHand(ItemEditUtil.removeSuffix(held));
                    player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Suffix removed.</gradient>"));
                } else if (action.equals("set") || action.equals("edit")) {
                    if (args.length < 4) { sendSubUsage(player, "suffix set <value>"); return true; }
                    String value = joinArgs(args, 3);
                    player.getInventory().setItemInMainHand(ItemEditUtil.setSuffix(held, value));
                    player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Suffix set to: </gradient>" + value));
                } else {
                    sendSubUsage(player, "suffix <set|remove> [value]");
                }
            }
            case "rename" -> {
                if (args.length < 3) { sendSubUsage(player, "rename <set|remove> [name]"); return true; }
                String action = args[2].toLowerCase();
                if (action.equals("remove")) {
                    player.getInventory().setItemInMainHand(ItemEditUtil.setName(held, null));
                    player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Name reset to default.</gradient>"));
                } else if (action.equals("set") || action.equals("edit")) {
                    if (args.length < 4) { sendSubUsage(player, "rename set <name>"); return true; }
                    String name = joinArgs(args, 3);
                    player.getInventory().setItemInMainHand(ItemEditUtil.setName(held, name));
                    player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Name set to: </gradient>" + name));
                } else {
                    sendSubUsage(player, "rename <set|remove> [name]");
                }
            }
            case "lore" -> {
                if (args.length < 3) { sendSubUsage(player, "lore <add|edit|remove|clear> [line] [text]"); return true; }
                String action = args[2].toLowerCase();
                switch (action) {
                    case "add" -> {
                        if (args.length < 4) { sendSubUsage(player, "lore add <text>"); return true; }
                        String line = joinArgs(args, 3);
                        player.getInventory().setItemInMainHand(ItemEditUtil.addLore(held, line));
                        player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Lore line added.</gradient>"));
                    }
                    case "edit" -> {
                        if (args.length < 5) { sendSubUsage(player, "lore edit <line#> <text>"); return true; }
                        int idx = parseLineNumber(args[3], player); if (idx < 0) return true;
                        String text = joinArgs(args, 4);
                        ItemStack updated = ItemEditUtil.editLore(held, idx, text);
                        player.getInventory().setItemInMainHand(updated);
                        player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Lore line " + (idx + 1) + " updated.</gradient>"));
                    }
                    case "remove" -> {
                        if (args.length < 4) { sendSubUsage(player, "lore remove <line#>"); return true; }
                        int idx = parseLineNumber(args[3], player); if (idx < 0) return true;
                        player.getInventory().setItemInMainHand(ItemEditUtil.removeLore(held, idx));
                        player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Lore line " + (idx + 1) + " removed.</gradient>"));
                    }
                    case "clear" -> {
                        player.getInventory().setItemInMainHand(ItemEditUtil.clearLore(held));
                        player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ All lore cleared.</gradient>"));
                    }
                    default -> sendSubUsage(player, "lore <add|edit|remove|clear> [line] [text]");
                }
            }
            case "enchant" -> {
                if (!isAdmin) { player.sendMessage(MM.deserialize("<red>✘ You need staff permissions for enchantments.</red>")); return true; }
                if (args.length < 3) { sendSubUsage(player, "enchant <add|remove|edit> <enchantment> [level]"); return true; }
                String action = args[2].toLowerCase();
                if (args.length < 4) { sendSubUsage(player, "enchant " + action + " <enchantment> [level]"); return true; }
                Enchantment ench = ItemEditUtil.resolveEnchantment(args[3]);
                if (ench == null) {
                    player.sendMessage(MM.deserialize("<red>✘ Unknown enchantment: <white>" + args[3] + "</white></red>"));
                    return true;
                }
                if (action.equals("remove")) {
                    player.getInventory().setItemInMainHand(ItemEditUtil.removeEnchant(held, ench));
                    player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Enchantment " + args[3] + " removed.</gradient>"));
                } else if (action.equals("add") || action.equals("edit")) {
                    int level = 1;
                    if (args.length >= 5) {
                        try { level = Integer.parseInt(args[4]); } catch (NumberFormatException e) {
                            player.sendMessage(MM.deserialize("<red>✘ Invalid level: <white>" + args[4] + "</white></red>")); return true;
                        }
                    }
                    player.getInventory().setItemInMainHand(ItemEditUtil.enchant(held, ench, level));
                    player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Enchantment " + args[3] + " level " + level + " applied.</gradient>"));
                } else {
                    sendSubUsage(player, "enchant <add|remove|edit> <enchantment> [level]");
                }
            }
            case "unbreakable" -> {
                if (!isAdmin) { player.sendMessage(MM.deserialize("<red>✘ You need staff permissions for unbreakable.</red>")); return true; }
                if (args.length < 3) { sendSubUsage(player, "unbreakable <true|false>"); return true; }
                boolean val = Boolean.parseBoolean(args[2]);
                player.getInventory().setItemInMainHand(ItemEditUtil.setUnbreakable(held, val));
                player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Unbreakable set to <white>" + val + "</white>.</gradient>"));
            }
            case "gloweffect" -> {
                if (!isAdmin) { player.sendMessage(MM.deserialize("<red>✘ You need staff permissions for glow effect.</red>")); return true; }
                if (args.length < 3) { sendSubUsage(player, "gloweffect <true|false>"); return true; }
                boolean val = Boolean.parseBoolean(args[2]);
                player.getInventory().setItemInMainHand(ItemEditUtil.setGlow(held, val));
                player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Glow effect set to <white>" + val + "</white>.</gradient>"));
            }
            case "repair" -> {
                if (!ItemEditUtil.isDamageable(held)) {
                    player.sendMessage(MM.deserialize("<red>✘ This item cannot be repaired.</red>"));
                    return true;
                }
                long cooldown = plugin.getRankManager().getRepairCooldown(player.getUniqueId());
                if (cooldown > 0) {
                    player.sendMessage(MM.deserialize("<red>✘ You must wait " + cooldown + "s before repairing again.</red>"));
                    return true;
                }
                player.getInventory().setItemInMainHand(ItemEditUtil.repair(held));
                plugin.getRankManager().setRepairLastUsed(player.getUniqueId());
                player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Item fully repaired.</gradient>"));
            }
            default -> sendUsage(player);
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(MM.deserialize("""
            <newline>\
            <gradient:#54DAF4:#545EB6><bold>  ━━  AnimaKits Item Editor  ━━</bold></gradient>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit prefix <set|remove> [value]</yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit suffix <set|remove> [value]</yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit rename <set|remove> [name]</yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit lore <add|edit|remove|clear> [line] [text]</yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit enchant <add|remove|edit> <enchant> [level]</yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit unbreakable <true|false></yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit gloweffect <true|false></yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit repair</yellow>
            <newline>"""));
    }

    private void sendSubUsage(Player player, String usage) {
        player.sendMessage(MM.deserialize(
            "<newline><gradient:#FF4B4B:#FF8585><bold>  ✕  Wrong Usage</bold></gradient>" +
            "<newline><dark_gray>  ┃  </dark_gray><gradient:#54DAF4:#545EB6>/anima itemedit " + usage + "</gradient><newline>"));
    }

    /** Parses a 1-based line number from user input and converts to 0-based index. */
    private int parseLineNumber(String input, Player player) {
        try {
            int line = Integer.parseInt(input);
            if (line < 1) {
                player.sendMessage(MM.deserialize("<red>✘ Line number must be 1 or greater.</red>"));
                return -1;
            }
            return line - 1; // convert to 0-based
        } catch (NumberFormatException e) {
            player.sendMessage(MM.deserialize("<red>✘ Invalid line number: <white>" + input + "</white></red>"));
            return -1;
        }
    }

    private String joinArgs(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (i > from) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
