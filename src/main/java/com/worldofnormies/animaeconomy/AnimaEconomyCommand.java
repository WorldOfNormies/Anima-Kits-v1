package com.worldofnormies.animaeconomy;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.util.MessageUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class AnimaEconomyCommand implements CommandExecutor {

    private final AnimaKitsPlugin plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public AnimaEconomyCommand(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            showUsage(sender);
            return true;
        }

        String sub = args[1].toLowerCase();
        return switch (sub) {
            case "pay" -> handlePay(sender, args);
            case "withdraw" -> handleWithdraw(sender, args);
            case "balance", "bal" -> handleBalance(sender, args);
            case "setbalance", "setbal" -> handleSetBalance(sender, args);
            case "addbalance", "addbal" -> handleAddBalance(sender, args);
            default -> {
                showUsage(sender);
                yield true;
            }
        };
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.err(sender, "Only players can use this command.");
            return true;
        }
        if (args.length < 5) {
            MessageUtil.err(sender, "Usage: /anima eco pay <player> <type> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            MessageUtil.err(sender, "Player not found.");
            return true;
        }

        String type = args[3].toLowerCase();
        if (!isValidType(type)) {
            MessageUtil.err(sender, "Invalid type. Use: money/ani, xp, or coins/animaz");
            return true;
        }

        long amount;
        try {
            amount = Long.parseLong(args[4]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            MessageUtil.err(sender, "Amount must be a positive whole number.");
            return true;
        }

        if (plugin.getEconomyManager().withdraw(player.getUniqueId(), type, amount)) {
            plugin.getEconomyManager().addBalance(target.getUniqueId(), type, amount);
            MessageUtil.sendMsg(player, "eco-paid", Map.of("amount", String.valueOf(amount), "type", type, "target", target.getName()));
            MessageUtil.sendMsg(target, "eco-received", Map.of("amount", String.valueOf(amount), "type", type, "sender", player.getName()));
        } else {
            MessageUtil.err(player, "You don't have enough balance.");
        }
        return true;
    }

    private boolean handleWithdraw(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.err(sender, "Only players can use this command.");
            return true;
        }
        if (args.length < 4) {
            MessageUtil.err(sender, "Usage: /anima eco withdraw <type> <amount>");
            return true;
        }

        String type = args[2].toLowerCase();
        if (!isValidType(type)) {
            MessageUtil.err(sender, "Invalid type. Use: money/ani, xp, or coins/animaz");
            return true;
        }

        long amount;
        try {
            amount = Long.parseLong(args[3]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            MessageUtil.err(sender, "Amount must be a positive whole number.");
            return true;
        }

        if (plugin.getEconomyManager().withdraw(player.getUniqueId(), type, amount)) {
            ItemStack item = EconomyUtil.createWithdrawItem(type, amount);
            Map<Integer, ItemStack> remaining = player.getInventory().addItem(item);
            if (!remaining.isEmpty()) {
                for (ItemStack left : remaining.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), left);
                }
            }
            MessageUtil.sendMsg(player, "eco-withdrawn", Map.of("amount", String.valueOf(amount), "type", type));
        } else {
            MessageUtil.err(player, "You don't have enough balance.");
        }
        return true;
    }

    private boolean handleBalance(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                MessageUtil.err(sender, "Player not found.");
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            MessageUtil.err(sender, "Usage: /anima eco balance <player>");
            return true;
        }

        long money = plugin.getEconomyManager().getBalance(target.getUniqueId(), "money");
        long coins = plugin.getEconomyManager().getBalance(target.getUniqueId(), "coins");
        long xp = plugin.getEconomyManager().getBalance(target.getUniqueId(), "xp");

        MessageUtil.send(sender, MM.deserialize("<gradient:#FFCC00:#FFFF00><bold>━━━ " + target.getName() + "'s Balance ━━━</bold></gradient>"));
        MessageUtil.send(sender, MM.deserialize("<gray>Ani (λ): <yellow>" + money + "</yellow></gray>"));
        MessageUtil.send(sender, MM.deserialize("<gray>Anima Coins (Â): <gold>" + coins + "</gold></gray>"));
        MessageUtil.send(sender, MM.deserialize("<gray>Experience: <green>" + xp + "</green></gray>"));
        return true;
    }

    private boolean handleSetBalance(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.eco.admin")) {
            MessageUtil.sendMsg(sender, "no-permission");
            return true;
        }
        if (args.length < 5) {
            MessageUtil.err(sender, "Usage: /anima eco setbalance <player> <type> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            MessageUtil.err(sender, "Player not found.");
            return true;
        }

        String type = args[3].toLowerCase();
        if (!isValidType(type)) {
            MessageUtil.err(sender, "Invalid type.");
            return true;
        }

        long amount;
        try {
            amount = Long.parseLong(args[4]);
        } catch (NumberFormatException e) {
            MessageUtil.err(sender, "Invalid amount.");
            return true;
        }

        plugin.getEconomyManager().setBalance(target.getUniqueId(), type, amount);
        MessageUtil.send(sender, MM.deserialize("<green>Set " + target.getName() + "'s " + type + " to " + amount + ".</green>"));
        return true;
    }

    private boolean handleAddBalance(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.eco.admin")) {
            MessageUtil.sendMsg(sender, "no-permission");
            return true;
        }
        if (args.length < 5) {
            MessageUtil.err(sender, "Usage: /anima eco addbalance <player> <type> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            MessageUtil.err(sender, "Player not found.");
            return true;
        }

        String type = args[3].toLowerCase();
        if (!isValidType(type)) {
            MessageUtil.err(sender, "Invalid type.");
            return true;
        }

        long amount;
        try {
            amount = Long.parseLong(args[4]);
        } catch (NumberFormatException e) {
            MessageUtil.err(sender, "Invalid amount.");
            return true;
        }

        plugin.getEconomyManager().addBalance(target.getUniqueId(), type, amount);
        MessageUtil.send(sender, MM.deserialize("<green>Added " + amount + " " + type + " to " + target.getName() + ".</green>"));
        return true;
    }

    private boolean isValidType(String type) {
        return List.of("money", "ani", "xp", "experience", "coins", "animaz").contains(type.toLowerCase());
    }

    private void showUsage(CommandSender sender) {
        MessageUtil.send(sender, MM.deserialize("<gradient:#FFCC00:#FFFF00><bold>━━━ Anima Economy ━━━</bold></gradient>"));
        MessageUtil.send(sender, MM.deserialize("<yellow>/anima eco balance [player]</yellow>"));
        MessageUtil.send(sender, MM.deserialize("<yellow>/anima eco pay <player> <type> <amount></yellow>"));
        MessageUtil.send(sender, MM.deserialize("<yellow>/anima eco withdraw <type> <amount></yellow>"));
        if (sender.hasPermission("anima.eco.admin")) {
            MessageUtil.send(sender, MM.deserialize("<yellow>/anima eco setbalance <player> <type> <amount></yellow>"));
            MessageUtil.send(sender, MM.deserialize("<yellow>/anima eco addbalance <player> <type> <amount></yellow>"));
        }
    }
}
