package com.worldofnormies.animaeconomy;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * AnimaEconomyCommand – handles all economy-related subcommands.
 */
public class AnimaEconomyCommand {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final AnimaKitsPlugin plugin;
    private final AnimaEconomyManager economyManager;

    public AnimaEconomyCommand(AnimaKitsPlugin plugin, AnimaEconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) return false;

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "balance" -> handleBalance(sender, args);
            case "pay" -> handlePay(sender, args);
            case "withdraw" -> handleWithdraw(sender, args);
            case "economy" -> handleEconomy(sender, args);
            case "tokens" -> handleTokens(sender, args);
            default -> { return false; }
        }

        return true;
    }

    private void handleBalance(CommandSender sender, String[] args) {
        OfflinePlayer target;
        if (args.length > 1) {
            target = Bukkit.getOfflinePlayer(args[1]);
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(MM.deserialize("<red>Usage: /anima balance <player></red>"));
            return;
        }

        UUID uuid = target.getUniqueId();
        double money = economyManager.getMoney(uuid);
        long tokens = economyManager.getTokens(uuid);
        long xp = economyManager.getExperience(uuid);

        sender.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6><bold>━━ Balance: " + target.getName() + " ━━</bold></gradient>"));
        sender.sendMessage(MM.deserialize("<gray>Money: <green>$" + String.format("%.2f", money) + "</green></gray>"));
        sender.sendMessage(MM.deserialize("<gray>Tokens: <gold>" + tokens + "</gold></gray>"));
        sender.sendMessage(MM.deserialize("<gray>Experience: <aqua>" + xp + "</aqua></gray>"));
    }

    private void handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MM.deserialize("<red>Only players can use /pay.</red>"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(MM.deserialize("<red>Usage: /anima pay <player> <amount></red>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(MM.deserialize("<red>Player not found.</red>"));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(MM.deserialize("<red>Invalid amount.</red>"));
            return;
        }

        if (economyManager.withdrawMoney(player.getUniqueId(), amount)) {
            economyManager.addMoney(target.getUniqueId(), amount);
            player.sendMessage(MM.deserialize("<green>You paid $" + amount + " to " + target.getName() + ".</green>"));
            target.sendMessage(MM.deserialize("<green>You received $" + amount + " from " + player.getName() + ".</green>"));
        } else {
            player.sendMessage(MM.deserialize("<red>Insufficient funds.</red>"));
        }
    }

    private void handleWithdraw(CommandSender sender, String[] args) {
        // Implementation for withdrawing to a physical item if needed, but for now just basic check
        sender.sendMessage(MM.deserialize("<gray>Physical withdrawal not implemented yet.</gray>"));
    }

    private void handleEconomy(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.economy.admin")) {
            sender.sendMessage(MM.deserialize("<red>No permission.</red>"));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(MM.deserialize("<red>Usage: /anima economy <set|add|take> <player> <amount></red>"));
            return;
        }

        String action = args[1].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        double amount;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MM.deserialize("<red>Invalid amount.</red>"));
            return;
        }

        UUID uuid = target.getUniqueId();
        switch (action) {
            case "set" -> economyManager.setMoney(uuid, amount);
            case "add" -> economyManager.addMoney(uuid, amount);
            case "take" -> economyManager.withdrawMoney(uuid, amount);
        }
        sender.sendMessage(MM.deserialize("<green>Updated balance for " + target.getName() + ".</green>"));
    }

    private void handleTokens(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anima.economy.admin")) {
            sender.sendMessage(MM.deserialize("<red>No permission.</red>"));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(MM.deserialize("<red>Usage: /anima tokens <set|add|take> <player> <amount></red>"));
            return;
        }

        String action = args[1].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        long amount;
        try {
            amount = Long.parseLong(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MM.deserialize("<red>Invalid amount.</red>"));
            return;
        }

        UUID uuid = target.getUniqueId();
        switch (action) {
            case "set" -> economyManager.setTokens(uuid, amount);
            case "add" -> economyManager.addTokens(uuid, amount);
            case "take" -> economyManager.withdrawTokens(uuid, amount);
        }
        sender.sendMessage(MM.deserialize("<green>Updated tokens for " + target.getName() + ".</green>"));
    }
}
