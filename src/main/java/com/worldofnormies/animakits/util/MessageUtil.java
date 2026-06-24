package com.worldofnormies.animakits.util;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * MessageUtil – Centralised message sending for AnimaKits.
 */
public final class MessageUtil {

    private static AnimaKitsPlugin plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private MessageUtil() {}

    public static void reload(AnimaKitsPlugin pl) { plugin = pl; }
    public static void init(AnimaKitsPlugin pl)   { plugin = pl; }

    // ── Core send ──────────────────────────────────────────────────

    public static void send(CommandSender sender, Component component) {
        if (sender instanceof Player player) {
            plugin.adventure().player(player).sendMessage(component);
        } else {
            plugin.adventure().console().sendMessage(component);
        }
    }

    public static void send(CommandSender sender, String miniMessage) {
        send(sender, MM.deserialize(miniMessage));
    }

    public static void sendMsg(CommandSender sender, String key,
                                Map<String, String> placeholders) {
        String raw = plugin.getConfig().getString("messages." + key,
                "<red>Missing message key: " + key + "</red>");
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                raw = raw.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        raw = ColorUtil.hexToMiniMessage(raw);
        send(sender, raw);
    }

    public static void sendMsg(CommandSender sender, String key) {
        sendMsg(sender, key, null);
    }

    public static void sendPrefixed(CommandSender sender, String miniMessage) {
        String prefix = plugin.getConfig().getString("general.prefix",
                "<gradient:#54DAF4:#545EB6>AnimaKits</gradient> <dark_gray>»</dark_gray>");
        send(sender, prefix + " " + miniMessage);
    }

    // ── Shorthands ─────────────────────────────────────────────────

    public static void ok(CommandSender s, String msg)   { sendPrefixed(s, "<green>" + msg + "</green>"); }
    public static void err(CommandSender s, String msg)  { sendPrefixed(s, "<red>" + msg + "</red>"); }
    public static void info(CommandSender s, String msg) { sendPrefixed(s, "<gray>" + msg + "</gray>"); }
}
