package com.worldofnormies.animakits.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ColorUtil – Handles all text colorisation for AnimaKits.
 *
 * Supports:
 *  1. Legacy Bukkit codes          &a, &l, &c …
 *  2. Per-character hex codes      &#RRGGBB  (Birdflop-style)
 *  3. MiniMessage tags             <red>, <gradient:#54DAF4:#545EB6> …
 */
public final class ColorUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder()
                    .character('§')
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    private static final Pattern HEX_PATTERN =
            Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorUtil() {}

    // ── Main public API ────────────────────────────────────────────

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) return Component.empty();
        String converted = hexToMiniMessage(input);
        converted = convertAmpersand(converted);
        try {
            return MM.deserialize(converted);
        } catch (Exception e) {
            return LEGACY.deserialize(converted);
        }
    }

    public static String toLegacy(Component component) {
        return LEGACY.serialize(component);
    }

    public static String colorize(String input) {
        return toLegacy(parse(input));
    }

    public static String strip(String input) {
        if (input == null) return "";
        String s = input;
        // Strip legacy & and § codes (more aggressive to catch things like &u if they are intended as codes)
        s = s.replaceAll("(?i)[&§][a-z0-9]", "");
        // Strip hex &#RRGGBB
        s = s.replaceAll("(?i)&#[A-F0-9]{6}", "");
        // Strip hex §x§r§r§g§g§b§b
        s = s.replaceAll("(?i)§x(§[A-F0-9]){6}", "");
        // Strip MiniMessage
        s = s.replaceAll("(?i)<[^>]*>", "");
        // Also remove brackets if they are being used for naming artifacts
        s = s.replace("[", "").replace("]", "");
        return s.trim();
    }

    // ── Internal helpers ──────────────────────────────────────────

    public static String hexToMiniMessage(String input) {
        if (input == null) return "";
        Matcher m = HEX_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "<#" + m.group(1) + ">");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String convertAmpersand(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static String gradient(String text, String startHex, String endHex) {
        return "<gradient:#" + startHex + ":#" + endHex + ">" + text + "</gradient>";
    }
}
