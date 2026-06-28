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

        // If it looks like MiniMessage but doesn't have legacy codes, try MM first
        if (input.contains("<") && input.contains(">") && !input.contains("&")) {
            try {
                return MM.deserialize(input);
            } catch (Exception ignored) {}
        }

        String converted = hexToMiniMessage(input);
        converted = convertAmpersand(converted);

        // After converting hex and ampersands, if it has tags, it might be mixed
        if (converted.contains("<") && converted.contains(">")) {
            try {
                return MM.deserialize(converted);
            } catch (Exception e) {
                return LEGACY.deserialize(converted);
            }
        }

        return LEGACY.deserialize(converted);
    }

    public static String toLegacy(Component component) {
        return LEGACY.serialize(component);
    }

    public static String colorize(String input) {
        return toLegacy(parse(input));
    }

    public static String strip(String input) {
        if (input == null) return "";
        // Strip § color codes (Bukkit legacy)
        String s = ChatColor.stripColor(input);
        // Strip & legacy codes (&a, &l, &1, etc.) that haven't been translated yet
        s = s.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
        // Strip &#RRGGBB hex codes
        s = HEX_PATTERN.matcher(s).replaceAll("");
        // Strip MiniMessage tags like <red>, <gradient:#aaa:#bbb>, </red>, etc.
        s = s.replaceAll("<[^>]*>", "");
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

    public static String parseToMiniMessage(String input) {
        // This is a rough approximation, MiniMessage doesn't have a direct "serialize to MM"
        // for legacy strings without complex logic. We'll just return the input
        // and assume it might already be MM-compatible or legacy.
        return input;
    }
}
