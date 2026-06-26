package com.worldofnormies.animaitemedit;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.ChatInputSession;
import com.worldofnormies.animakits.util.ColorUtil;
import com.worldofnormies.animakits.util.MessageUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.Map;

/**
 * ItemEditCommand – handles all /anima itemedit [...] subcommands.
 */
public class ItemEditCommand implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final AnimaKitsPlugin plugin;

    public ItemEditCommand(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            showUsage(sender);
            return true;
        }

        if (!(sender instanceof Player player)) {
            MessageUtil.err(sender, "This command requires an in-game player.");
            return true;
        }

        String sub = args[1].toLowerCase();
        return switch (sub) {
            case "prefix"      -> handlePrefix(player, args);
            case "suffix"      -> handleSuffix(player, args);
            case "rename"      -> handleRename(player, args);
            case "lore"        -> handleLore(player, args);
            case "enchant"     -> handleEnchant(player, args);
            case "unbreakable" -> handleUnbreakable(player, args);
            case "gloweffect"  -> handleGlow(player, args);
            case "repair"      -> handleRepair(player);
            default            -> { showUsage(sender); yield true; }
        };
    }

    private boolean handlePrefix(Player player, String[] args) {
        if (!player.hasPermission("anima.itemedit.prefix") && !player.hasPermission("anima.itemedit.*")) {
            MessageUtil.sendMsg(player, "no-permission"); return true;
        }
        ItemStack item = getHeldItem(player);
        if (item == null) return true;

        if (args.length < 3) {
            usage(player, "/anima itemedit prefix <set|remove|edit> [value]"); return true;
        }
        String action = args[2].toLowerCase();
        switch (action) {
            case "remove" -> {
                applyAndUpdate(player, item, ItemEditUtil.removePrefix(item));
                ok(player, "Prefix removed from <white>" + ItemEditUtil.getPlainDisplayName(item) + "</white>.");
            }
            case "set", "edit" -> {
                if (args.length < 4) {
                    openChatForPrefix(player, item);
                } else {
                    String val = joinArgs(args, 3);
                    applyAndUpdate(player, item, ItemEditUtil.setPrefix(item, val));
                    ok(player, "Prefix set on <white>" + ItemEditUtil.getPlainDisplayName(item) + "</white>.");
                }
            }
            default -> usage(player, "/anima itemedit prefix <set|remove|edit> [value]");
        }
        return true;
    }

    private void openChatForPrefix(Player player, ItemStack item) {
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>✎ Set Item Prefix</bold></gradient>\n" +
            "<gray>Type the prefix to add before the item name. Supports MiniMessage.\n" +
            "Type <white>//cancel</white> to abort.</gray>"));
        new ChatInputSession(plugin, player,
            input -> {
                applyAndUpdate(player, item, ItemEditUtil.setPrefix(item, input));
                ok(player, "Prefix <white>" + input + "</white> applied.");
            },
            () -> {}
        ).await();
    }

    private boolean handleSuffix(Player player, String[] args) {
        if (!player.hasPermission("anima.itemedit.suffix") && !player.hasPermission("anima.itemedit.*")) {
            MessageUtil.sendMsg(player, "no-permission"); return true;
        }
        ItemStack item = getHeldItem(player);
        if (item == null) return true;

        if (args.length < 3) {
            usage(player, "/anima itemedit suffix <set|remove|edit> [value]"); return true;
        }
        String action = args[2].toLowerCase();
        switch (action) {
            case "remove" -> {
                applyAndUpdate(player, item, ItemEditUtil.removeSuffix(item));
                ok(player, "Suffix removed.");
            }
            case "set", "edit" -> {
                if (args.length < 4) {
                    openChatForSuffix(player, item);
                } else {
                    String val = joinArgs(args, 3);
                    applyAndUpdate(player, item, ItemEditUtil.setSuffix(item, val));
                    ok(player, "Suffix set on <white>" + ItemEditUtil.getPlainDisplayName(item) + "</white>.");
                }
            }
            default -> usage(player, "/anima itemedit suffix <set|remove|edit> [value]");
        }
        return true;
    }

    private void openChatForSuffix(Player player, ItemStack item) {
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>✎ Set Item Suffix</bold></gradient>\n" +
            "<gray>Type the suffix to add after the item name. Supports MiniMessage.\n" +
            "Type <white>//cancel</white> to abort.</gray>"));
        new ChatInputSession(plugin, player,
            input -> {
                applyAndUpdate(player, item, ItemEditUtil.setSuffix(item, input));
                ok(player, "Suffix <white>" + input + "</white> applied.");
            },
            () -> {}
        ).await();
    }

    private boolean handleRename(Player player, String[] args) {
        if (!player.hasPermission("anima.itemedit.rename") && !player.hasPermission("anima.itemedit.*")) {
            MessageUtil.sendMsg(player, "no-permission"); return true;
        }
        ItemStack item = getHeldItem(player);
        if (item == null) return true;

        if (args.length < 3) {
            usage(player, "/anima itemedit rename <set|remove|edit> [name]"); return true;
        }
        String action = args[2].toLowerCase();
        switch (action) {
            case "remove" -> {
                applyAndUpdate(player, item, ItemEditUtil.setName(item, null));
                ok(player, "Custom name removed — item shows vanilla name.");
            }
            case "set", "edit" -> {
                if (args.length < 4) {
                    openChatForRename(player, item);
                } else {
                    String val = joinArgs(args, 3);
                    applyAndUpdate(player, item, ItemEditUtil.setName(item, val));
                    ok(player, "Item renamed to <white>" + val + "</white>.");
                }
            }
            default -> usage(player, "/anima itemedit rename <set|remove|edit> [name]");
        }
        return true;
    }

    private void openChatForRename(Player player, ItemStack item) {
        ChatInputSession.sendRenamePrompt(plugin, player);
        new ChatInputSession(plugin, player,
            input -> {
                applyAndUpdate(player, item, ItemEditUtil.setName(item, input));
                ok(player, "Item renamed.");
            },
            () -> {}
        ).await();
    }

    private boolean handleLore(Player player, String[] args) {
        if (!player.hasPermission("anima.itemedit.lore") && !player.hasPermission("anima.itemedit.*")) {
            MessageUtil.sendMsg(player, "no-permission"); return true;
        }
        ItemStack item = getHeldItem(player);
        if (item == null) return true;

        if (args.length < 3) {
            usage(player, "/anima itemedit lore <add|remove|edit|clear> [line#] [text]"); return true;
        }
        String action = args[2].toLowerCase();
        int size = ItemEditUtil.getLoreSize(item);

        switch (action) {
            case "add" -> {
                if (args.length < 4) {
                    openChatForLore(player, item);
                } else {
                    String text = joinArgs(args, 3);
                    applyAndUpdate(player, item, ItemEditUtil.addLore(item, text));
                    ok(player, "Lore line added.");
                }
            }
            case "clear" -> {
                applyAndUpdate(player, item, ItemEditUtil.clearLore(item));
                ok(player, "All lore cleared.");
            }
            case "remove" -> {
                if (args.length < 4) { usage(player, "/anima itemedit lore remove <line#>"); return true; }
                int line = parseLineNum(player, args[3], size);
                if (line < 0) return true;
                applyAndUpdate(player, item, ItemEditUtil.removeLore(item, line));
                ok(player, "Lore line <yellow>#" + (line + 1) + "</yellow> removed.");
            }
            case "edit" -> {
                if (args.length < 4) { usage(player, "/anima itemedit lore edit <line#> [text]"); return true; }
                int line = parseLineNum(player, args[3], size);
                if (line < 0) return true;
                if (args.length < 5) {
                    openChatForLoreEdit(player, item, line);
                } else {
                    String text = joinArgs(args, 4);
                    applyAndUpdate(player, item, ItemEditUtil.editLore(item, line, text));
                    ok(player, "Lore line <yellow>#" + (line + 1) + "</yellow> updated.");
                }
            }
            default -> usage(player, "/anima itemedit lore <add|remove|edit|clear> [line#] [text]");
        }
        return true;
    }

    private void openChatForLore(Player player, ItemStack item) {
        ChatInputSession.sendLorePrompt(plugin, player);
        new ChatInputSession(plugin, player,
            input -> {
                applyAndUpdate(player, item, ItemEditUtil.addLore(item, input));
                ok(player, "Lore line added.");
            },
            () -> {}
        ).await();
    }

    private void openChatForLoreEdit(Player player, ItemStack item, int lineIndex) {
        ChatInputSession.sendLorePrompt(plugin, player);
        new ChatInputSession(plugin, player,
            input -> {
                applyAndUpdate(player, item, ItemEditUtil.editLore(item, lineIndex, input));
                ok(player, "Lore line <yellow>#" + (lineIndex + 1) + "</yellow> updated.");
            },
            () -> {}
        ).await();
    }

    private boolean handleEnchant(Player player, String[] args) {
        if (!player.hasPermission("anima.itemedit.enchant") && !player.hasPermission("anima.itemedit.*")) {
            MessageUtil.sendMsg(player, "no-permission"); return true;
        }
        ItemStack item = getHeldItem(player);
        if (item == null) return true;

        if (args.length < 4) {
            usage(player, "/anima itemedit enchant <add|remove|edit> <enchantment> [level]"); return true;
        }
        String action = args[2].toLowerCase();
        String enchName = args[3];
        Enchantment enchant = ItemEditUtil.resolveEnchantment(enchName);
        if (enchant == null) {
            err(player, "Unknown enchantment: <white>" + enchName + "</white>. Use Minecraft ID e.g. sharpness, unbreaking.");
            return true;
        }

        switch (action) {
            case "remove" -> {
                applyAndUpdate(player, item, ItemEditUtil.removeEnchant(item, enchant));
                ok(player, "Removed enchantment <yellow>" + enchant.getKey().getKey() + "</yellow>.");
            }
            case "add", "edit" -> {
                int level = 1;
                if (args.length >= 5) {
                    try { level = Integer.parseInt(args[4]); }
                    catch (NumberFormatException e) {
                        err(player, "Level must be an integer (1–255).");
                        return true;
                    }
                }
                if (level < 1 || level > 255) {
                    err(player, "Level must be between 1 and 255.");
                    return true;
                }
                applyAndUpdate(player, item, ItemEditUtil.enchant(item, enchant, level));
                ok(player, "Applied <yellow>" + enchant.getKey().getKey() + " " + toRoman(level) + "</yellow>.");
            }
            default -> usage(player, "/anima itemedit enchant <add|remove|edit> <enchantment> [level]");
        }
        return true;
    }

    private boolean handleUnbreakable(Player player, String[] args) {
        if (!player.hasPermission("anima.itemedit.unbreakable") && !player.hasPermission("anima.itemedit.*")) {
            MessageUtil.sendMsg(player, "no-permission"); return true;
        }
        ItemStack item = getHeldItem(player);
        if (item == null) return true;

        if (args.length < 3) {
            usage(player, "/anima itemedit unbreakable <true|false>"); return true;
        }
        boolean val = Boolean.parseBoolean(args[2]);
        applyAndUpdate(player, item, ItemEditUtil.setUnbreakable(item, val));
        ok(player, "Unbreakable set to <white>" + (val ? "<green>ON" : "<red>OFF") + "</white><gray>.</gray>");
        return true;
    }

    private boolean handleGlow(Player player, String[] args) {
        if (!player.hasPermission("anima.itemedit.gloweffect") && !player.hasPermission("anima.itemedit.*")) {
            MessageUtil.sendMsg(player, "no-permission"); return true;
        }
        ItemStack item = getHeldItem(player);
        if (item == null) return true;

        if (args.length < 3) {
            usage(player, "/anima itemedit gloweffect <true|false>"); return true;
        }
        boolean val = Boolean.parseBoolean(args[2]);
        applyAndUpdate(player, item, ItemEditUtil.setGlow(item, val));
        ok(player, "Glow effect set to <white>" + (val ? "<green>ON" : "<red>OFF") + "</white><gray>.</gray>");
        return true;
    }

    private boolean handleRepair(Player player) {
        if (!player.hasPermission("anima.itemedit.repair") && !player.hasPermission("anima.itemedit.*")) {
            MessageUtil.sendMsg(player, "no-permission"); return true;
        }
        ItemStack item = getHeldItem(player);
        if (item == null) return true;

        if (!ItemEditUtil.isDamageable(item)) {
            err(player, "This item cannot be repaired (not damageable).");
            return true;
        }

        // Check repair cooldown
        long now = Instant.now().getEpochSecond();
        long last = plugin.getPlayerManager().getLastRepair(player.getUniqueId());
        long cooldown = plugin.getPlayerManager().getRepairCooldown(player.getUniqueId());

        if (now < last + cooldown) {
            long remaining = (last + cooldown) - now;
            err(player, "You must wait " + formatDuration(remaining) + " before repairing again.");
            return true;
        }

        applyAndUpdate(player, item, ItemEditUtil.repair(item));
        plugin.getPlayerManager().setLastRepair(player.getUniqueId(), now);
        ok(player, "Item fully repaired.");
        return true;
    }

    private ItemStack getHeldItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            err(player, "You must be holding an item in your main hand.");
            return null;
        }
        return item;
    }

    private void applyAndUpdate(Player player, ItemStack original, ItemStack updated) {
        player.getInventory().setItemInMainHand(updated);
    }

    private int parseLineNum(Player player, String raw, int size) {
        try {
            int n = Integer.parseInt(raw);
            if (n < 1 || n > size) {
                err(player, "Line number must be between 1 and " + size + ".");
                return -1;
            }
            return n - 1;
        } catch (NumberFormatException e) {
            err(player, "Line number must be an integer.");
            return -1;
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

    private void ok(Player p, String msg) {
        MessageUtil.send(p, "<gradient:#44FF88:#00CC55><bold>  ✓  </bold></gradient>" + msg);
    }

    private void err(Player p, String msg) {
        MessageUtil.send(p, "<gradient:#FF4B4B:#FF8585><bold>  ✕  </bold></gradient><gray>" + msg + "</gray>");
    }

    private void usage(Player p, String fmt) {
        MessageUtil.send(p,
            "<newline><gradient:#FF4B4B:#FF8585><bold>  ✕  Wrong Usage</bold></gradient>" +
            "<newline><dark_gray>  ┃</dark_gray> <gray>Correct format:</gray>" +
            "<newline><dark_gray>  ┃  </dark_gray><gradient:#54DAF4:#545EB6>" + fmt + "</gradient><newline>");
    }

    private void showUsage(CommandSender s) {
        MessageUtil.send(s, MM.deserialize("""
            <newline><gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits · Item Edit ━━━━</bold></gradient>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit prefix  <set|remove|edit> [value]</yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit suffix  <set|remove|edit> [value]</yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit rename  <set|remove|edit> [name]</yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit lore    <add|remove|edit|clear> [line] [text]</yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit enchant <add|remove|edit> <enchant> [level]</yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit unbreakable <true|false></yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit gloweffect  <true|false></yellow>
            <dark_gray>  ┃</dark_gray> <yellow>/anima itemedit repair</yellow>
            <gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient><newline>
            """));
    }

    private String toRoman(int n) {
        return switch (n) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV";
            case 5 -> "V"; case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII";
            case 9 -> "IX"; case 10 -> "X"; default -> String.valueOf(n);
        };
    }

    private String formatDuration(long seconds) {
        if (seconds <= 0) return "0s";
        long h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (s > 0 || sb.isEmpty()) sb.append(s).append("s");
        return sb.toString().trim();
    }
}
