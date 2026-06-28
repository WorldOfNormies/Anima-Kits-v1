package com.worldofnormies.animaitemedit;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.manager.PlayerManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.UUID;

/**
 * ItemEditCommand – handles all /anima itemedit [...] subcommands.
 * Delegates item manipulation to {@link ItemEditUtil}.
 *
 * Permission structure:
 *   anima.itemedit.*          – all itemedit commands (admin wildcard)
 *   anima.itemedit.admin      – admin alias for all
 *   anima.itemedit.enchant    – add/edit/remove enchantments
 *   anima.itemedit.glow       – set glow effect
 *   anima.itemedit.unbreakable – set unbreakable
 *   anima.itemedit.lore       – edit lore (add/edit/remove/clear)
 *   anima.itemedit.rename     – full rename (free-form MiniMessage)
 *   anima.itemedit.prefix     – set/remove prefix on held item
 *   anima.itemedit.suffix     – set/remove suffix on held item
 *   anima.itemedit.repair     – repair held item
 *   anima.itemedit.repair.cooldown – subject to repair cooldown (set in config)
 *
 * Players with anima.itemedit.prefix / suffix / rename / repair can self-edit
 * their held item. Admin nodes control the more powerful operations.
 *
 * args[0] = "itemedit"
 * args[1] = sub
 * args[2+] = action / value
 */
public class ItemEditCommand {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // Config key for repair cooldown (seconds). 0 = no cooldown.
    private static final String REPAIR_CD_KEY = "itemedit.repair-cooldown";

    private final AnimaKitsPlugin plugin;

    public ItemEditCommand(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MM.deserialize("<red>Only players can use /anima itemedit.</red>"));
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

        switch (sub) {

            // ── PREFIX ─────────────────────────────────────────────────
            case "prefix" -> {
                if (!hasPerm(player, "anima.itemedit.prefix")) return noPerm(player);
                if (args.length < 3) return subUsage(player, "prefix <set|remove> [value]");
                String action = args[2].toLowerCase();
                if (action.equals("remove")) {
                    player.getInventory().setItemInMainHand(ItemEditUtil.removePrefix(held));
                    ok(player, "Prefix removed.");
                } else if (action.equals("set") || action.equals("edit")) {
                    if (args.length < 4) return subUsage(player, "prefix set <value>");
                    String value = joinArgs(args, 3);
                    player.getInventory().setItemInMainHand(ItemEditUtil.setPrefix(held, value));
                    player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Prefix set: </gradient>" + value));
                } else {
                    return subUsage(player, "prefix <set|remove> [value]");
                }
            }

            // ── SUFFIX ─────────────────────────────────────────────────
            case "suffix" -> {
                if (!hasPerm(player, "anima.itemedit.suffix")) return noPerm(player);
                if (args.length < 3) return subUsage(player, "suffix <set|remove> [value]");
                String action = args[2].toLowerCase();
                if (action.equals("remove")) {
                    player.getInventory().setItemInMainHand(ItemEditUtil.removeSuffix(held));
                    ok(player, "Suffix removed.");
                } else if (action.equals("set") || action.equals("edit")) {
                    if (args.length < 4) return subUsage(player, "suffix set <value>");
                    String value = joinArgs(args, 3);
                    player.getInventory().setItemInMainHand(ItemEditUtil.setSuffix(held, value));
                    player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Suffix set: </gradient>" + value));
                } else {
                    return subUsage(player, "suffix <set|remove> [value]");
                }
            }

            // ── RENAME ─────────────────────────────────────────────────
            case "rename" -> {
                if (!hasPerm(player, "anima.itemedit.rename")) return noPerm(player);
                if (args.length < 3) return subUsage(player, "rename <set|remove> [name]");
                String action = args[2].toLowerCase();
                if (action.equals("remove")) {
                    player.getInventory().setItemInMainHand(ItemEditUtil.setName(held, null));
                    ok(player, "Name reset to default.");
                } else if (action.equals("set") || action.equals("edit")) {
                    if (args.length < 4) return subUsage(player, "rename set <name>");
                    String name = joinArgs(args, 3);
                    player.getInventory().setItemInMainHand(ItemEditUtil.setName(held, name));
                    player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6>✓ Name set: </gradient>" + name));
                } else {
                    return subUsage(player, "rename <set|remove> [name]");
                }
            }

            // ── LORE ───────────────────────────────────────────────────
            case "lore" -> {
                if (!hasPerm(player, "anima.itemedit.lore")) return noPerm(player);
                if (args.length < 3) return subUsage(player, "lore <add|edit|remove|clear> ...");
                String action = args[2].toLowerCase();
                switch (action) {
                    case "add" -> {
                        if (args.length < 4) return subUsage(player, "lore add <text>");
                        player.getInventory().setItemInMainHand(ItemEditUtil.addLore(held, joinArgs(args, 3)));
                        ok(player, "Lore line added.");
                    }
                    case "edit" -> {
                        if (args.length < 5) return subUsage(player, "lore edit <line#> <text>");
                        int idx = parseLine(args[3], player); if (idx < 0) return true;
                        player.getInventory().setItemInMainHand(ItemEditUtil.editLore(held, idx, joinArgs(args, 4)));
                        ok(player, "Lore line " + (idx + 1) + " updated.");
                    }
                    case "remove" -> {
                        if (args.length < 4) return subUsage(player, "lore remove <line#>");
                        int idx = parseLine(args[3], player); if (idx < 0) return true;
                        player.getInventory().setItemInMainHand(ItemEditUtil.removeLore(held, idx));
                        ok(player, "Lore line " + (idx + 1) + " removed.");
                    }
                    case "clear" -> {
                        player.getInventory().setItemInMainHand(ItemEditUtil.clearLore(held));
                        ok(player, "All lore cleared.");
                    }
                    default -> subUsage(player, "lore <add|edit|remove|clear> ...");
                }
            }

            // ── ENCHANT ────────────────────────────────────────────────
            case "enchant" -> {
                if (!hasPerm(player, "anima.itemedit.enchant")) return noPerm(player);
                if (args.length < 3) return subUsage(player, "enchant <add|remove|edit> <enchantment> [level]");
                String action = args[2].toLowerCase();
                if (args.length < 4) return subUsage(player, "enchant " + action + " <enchantment> [level]");
                Enchantment ench = ItemEditUtil.resolveEnchantment(args[3]);
                if (ench == null) {
                    player.sendMessage(MM.deserialize("<red>✘ Unknown enchantment: <white>" + args[3] + "</white></red>"));
                    return true;
                }
                if (action.equals("remove")) {
                    player.getInventory().setItemInMainHand(ItemEditUtil.removeEnchant(held, ench));
                    ok(player, "Enchantment " + args[3] + " removed.");
                } else if (action.equals("add") || action.equals("edit")) {
                    int level = 1;
                    if (args.length >= 5) {
                        try { level = Integer.parseInt(args[4]); }
                        catch (NumberFormatException e) {
                            player.sendMessage(MM.deserialize("<red>✘ Invalid level: <white>" + args[4] + "</white></red>"));
                            return true;
                        }
                    }
                    player.getInventory().setItemInMainHand(ItemEditUtil.enchant(held, ench, level));
                    ok(player, "Enchantment " + args[3] + " level " + level + " applied.");
                } else {
                    return subUsage(player, "enchant <add|remove|edit> <enchantment> [level]");
                }
            }

            // ── UNBREAKABLE ────────────────────────────────────────────
            case "unbreakable" -> {
                if (!hasPerm(player, "anima.itemedit.unbreakable")) return noPerm(player);
                if (args.length < 3) return subUsage(player, "unbreakable <true|false>");
                boolean val = Boolean.parseBoolean(args[2]);
                player.getInventory().setItemInMainHand(ItemEditUtil.setUnbreakable(held, val));
                ok(player, "Unbreakable set to <white>" + val + "</white>.");
            }

            // ── GLOW EFFECT ────────────────────────────────────────────
            case "gloweffect" -> {
                if (!hasPerm(player, "anima.itemedit.glow")) return noPerm(player);
                if (args.length < 3) return subUsage(player, "gloweffect <true|false>");
                boolean val = Boolean.parseBoolean(args[2]);
                player.getInventory().setItemInMainHand(ItemEditUtil.setGlow(held, val));
                ok(player, "Glow effect set to <white>" + val + "</white>.");
            }

            // ── REPAIR ─────────────────────────────────────────────────
            case "repair" -> {
                if (!hasPerm(player, "anima.itemedit.repair")) return noPerm(player);
                if (!ItemEditUtil.isDamageable(held)) {
                    player.sendMessage(MM.deserialize("<red>✘ This item cannot be repaired.</red>"));
                    return true;
                }
                // Cooldown check (skip if player has bypass or no cooldown configured)
                if (!player.hasPermission("anima.itemedit.*") && !player.hasPermission("anima.itemedit.admin")) {
                    long cdSeconds = plugin.getConfig().getLong(REPAIR_CD_KEY, 0);
                    if (cdSeconds > 0 && player.hasPermission("anima.itemedit.repair.cooldown")) {
                        long remaining = getRepairCooldown(player.getUniqueId());
                        if (remaining > 0) {
                            player.sendMessage(MM.deserialize(
                                "<newline><gradient:#FF4B4B:#FF8585><bold>  ✕  Repair Cooldown</bold></gradient>" +
                                "<newline><dark_gray>  ┃</dark_gray> <gold>⏱ Wait <white><bold>" + formatTime(remaining) + "</bold></white> before repairing again.</gold><newline>"));
                            return true;
                        }
                        setRepairCooldown(player.getUniqueId(), cdSeconds);
                    }
                }
                player.getInventory().setItemInMainHand(ItemEditUtil.repair(held));
                ok(player, "Item fully repaired.");
            }

            default -> sendUsage(player);
        }

        return true;
    }

    // ── Cooldown helpers ───────────────────────────────────────────

    // We store repair cooldowns in the plugin's PlayerManager using a virtual
    // kitId UUID derived from a fixed seed so we don't need a separate store.
    private static final java.util.UUID REPAIR_CD_UUID =
            java.util.UUID.nameUUIDFromBytes("itemedit.repair".getBytes());

    private long getRepairCooldown(UUID playerUuid) {
        return plugin.getPlayerManager().getRemainingCooldown(playerUuid, REPAIR_CD_UUID);
    }

    private void setRepairCooldown(UUID playerUuid, long seconds) {
        plugin.getPlayerManager().setCooldown(playerUuid, REPAIR_CD_UUID, seconds);
    }

    // ── Permission check ──────────────────────────────────────────

    private boolean hasPerm(Player player, String node) {
        return player.hasPermission("anima.itemedit.*")
            || player.hasPermission("anima.itemedit.admin")
            || player.hasPermission(node);
    }

    // ── Messaging ─────────────────────────────────────────────────

    private void sendUsage(Player player) {
        player.sendMessage(MM.deserialize(
            "<newline><gradient:#54DAF4:#545EB6><bold>  ━━  AnimaKits Item Editor  ━━</bold></gradient>" +
            "<newline><dark_gray>  ┃</dark_gray> <yellow>/anima itemedit prefix <set|remove> [value]</yellow>"    +
            "<newline><dark_gray>  ┃</dark_gray> <yellow>/anima itemedit suffix <set|remove> [value]</yellow>"    +
            "<newline><dark_gray>  ┃</dark_gray> <yellow>/anima itemedit rename <set|remove> [name]</yellow>"     +
            "<newline><dark_gray>  ┃</dark_gray> <yellow>/anima itemedit lore <add|edit|remove|clear> ...</yellow>" +
            "<newline><dark_gray>  ┃</dark_gray> <yellow>/anima itemedit enchant <add|remove|edit> <enchant> [level]</yellow>" +
            "<newline><dark_gray>  ┃</dark_gray> <yellow>/anima itemedit unbreakable <true|false></yellow>"       +
            "<newline><dark_gray>  ┃</dark_gray> <yellow>/anima itemedit gloweffect <true|false></yellow>"        +
            "<newline><dark_gray>  ┃</dark_gray> <yellow>/anima itemedit repair</yellow><newline>"));
    }

    private boolean subUsage(Player player, String usage) {
        player.sendMessage(MM.deserialize(
            "<newline><gradient:#FF4B4B:#FF8585><bold>  ✕  Wrong Usage</bold></gradient>" +
            "<newline><dark_gray>  ┃  </dark_gray><gradient:#54DAF4:#545EB6>/anima itemedit " + usage + "</gradient><newline>"));
        return true;
    }

    private void ok(Player player, String msg) {
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6>✓ </gradient><gray>" + msg + "</gray>"));
    }

    private boolean noPerm(Player player) {
        player.sendMessage(MM.deserialize(
            "<newline><gradient:#FF4B4B:#FF8585><bold>  ✕  Access Denied</bold></gradient>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>You do not have permission for this item edit action.</gray><newline>"));
        return true;
    }

    // ── Misc helpers ──────────────────────────────────────────────

    private int parseLine(String input, Player player) {
        try {
            int line = Integer.parseInt(input);
            if (line < 1) { player.sendMessage(MM.deserialize("<red>✘ Line number must be 1 or greater.</red>")); return -1; }
            return line - 1;
        } catch (NumberFormatException e) {
            player.sendMessage(MM.deserialize("<red>✘ Invalid line number: <white>" + input + "</white></red>"));
            return -1;
        }
    }

    private String joinArgs(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) { if (i > from) sb.append(' '); sb.append(args[i]); }
        return sb.toString();
    }

    private String formatTime(long seconds) {
        if (seconds <= 0) return "0s";
        long h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }
}
