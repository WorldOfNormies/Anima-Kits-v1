package com.worldofnormies.animaitemedit;

import com.worldofnormies.animakits.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * ItemEditUtil – centralised helpers for all /anima itemedit operations.
 * Operates on ItemStack directly; returns the mutated clone.
 */
public final class ItemEditUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private ItemEditUtil() {}

    // ── Name / Prefix / Suffix ────────────────────────────────────

    /**
     * Sets the item display name to an arbitrary MiniMessage string.
     * Pass {@code null} or blank to reset to default (vanilla name).
     */
    public static ItemStack setName(ItemStack item, String miniMessageName) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;
        if (miniMessageName == null || miniMessageName.isBlank()) {
            meta.displayName(null);
        } else {
            meta.displayName(ColorUtil.parse(miniMessageName)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        }
        copy.setItemMeta(meta);
        return copy;
    }

    /**
     * Sets the display name to {@code prefix + " " + currentVanillaName}.
     * If the item already has a custom name, the prefix replaces any existing prefix
     * (defined as everything before the first space or dash separator).
     */
    public static ItemStack setPrefix(ItemStack item, String miniMessagePrefix) {
        String currentName = getPlainDisplayName(item);
        return setName(item, miniMessagePrefix + " " + currentName);
    }

    /** Removes the prefix (everything before and including the first " " space in the display name). */
    public static ItemStack removePrefix(ItemStack item) {
        String plain = getPlainDisplayName(item);
        int idx = plain.indexOf(' ');
        if (idx < 0) return setName(item, null); // nothing to remove
        String rest = plain.substring(idx + 1).trim();
        return setName(item, rest.isEmpty() ? null : rest);
    }

    /** Sets the display name to {@code currentVanillaName + " " + suffix}. */
    public static ItemStack setSuffix(ItemStack item, String miniMessageSuffix) {
        String currentName = getPlainDisplayName(item);
        return setName(item, currentName + " " + miniMessageSuffix);
    }

    /** Removes the suffix (the last word in the display name). */
    public static ItemStack removeSuffix(ItemStack item) {
        String plain = getPlainDisplayName(item);
        int idx = plain.lastIndexOf(' ');
        if (idx < 0) return setName(item, null);
        String left = plain.substring(0, idx).trim();
        return setName(item, left.isEmpty() ? null : left);
    }

    // ── Lore ─────────────────────────────────────────────────────

    public static ItemStack addLore(ItemStack item, String miniMessageLine) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;
        List<Component> lore = new ArrayList<>(meta.lore() != null ? meta.lore() : List.of());
        lore.add(ColorUtil.parse(miniMessageLine)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        meta.lore(lore);
        copy.setItemMeta(meta);
        return copy;
    }

    public static ItemStack editLore(ItemStack item, int lineIndex, String miniMessageLine) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;
        List<Component> lore = new ArrayList<>(meta.lore() != null ? meta.lore() : List.of());
        if (lineIndex < 0 || lineIndex >= lore.size()) return copy;
        lore.set(lineIndex, ColorUtil.parse(miniMessageLine)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        meta.lore(lore);
        copy.setItemMeta(meta);
        return copy;
    }

    public static ItemStack removeLore(ItemStack item, int lineIndex) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;
        List<Component> lore = new ArrayList<>(meta.lore() != null ? meta.lore() : List.of());
        if (lineIndex < 0 || lineIndex >= lore.size()) return copy;
        lore.remove(lineIndex);
        meta.lore(lore);
        copy.setItemMeta(meta);
        return copy;
    }

    public static ItemStack clearLore(ItemStack item) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;
        meta.lore(List.of());
        copy.setItemMeta(meta);
        return copy;
    }

    public static int getLoreSize(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.lore() == null) return 0;
        return meta.lore().size();
    }

    // ── Enchantments ─────────────────────────────────────────────

    /**
     * Adds or updates an enchantment. Level 0 removes it.
     * Allows unsafe levels and cross-type enchantments via {@code ignoreLevelRestriction}.
     */
    public static ItemStack enchant(ItemStack item, Enchantment enchant, int level) {
        ItemStack copy = item.clone();
        if (level <= 0) {
            copy.removeEnchantment(enchant);
            return copy;
        }
        copy.addUnsafeEnchantment(enchant, level);
        return copy;
    }

    public static ItemStack removeEnchant(ItemStack item, Enchantment enchant) {
        ItemStack copy = item.clone();
        copy.removeEnchantment(enchant);
        return copy;
    }

    // ── Glow Effect ──────────────────────────────────────────────

    /**
     * Adds a "glow" effect by adding a hidden enchantment + HIDE_ENCHANTS flag,
     * without adding any actual power. Removing glow clears only the flag-hidden sentinel.
     */
    public static ItemStack setGlow(ItemStack item, boolean glow) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;
        if (glow) {
            // Add a harmless enchant (durability on non-weapons, or luck on anything)
            copy.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
            meta = copy.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            // Remove the sentinel glow enchant if no others are present
            if (copy.getEnchantments().size() == 1
                    && copy.getEnchantments().containsKey(Enchantment.UNBREAKING)
                    && copy.getEnchantmentLevel(Enchantment.UNBREAKING) == 1) {
                copy.removeEnchantment(Enchantment.UNBREAKING);
                meta = copy.getItemMeta();
            }
        }
        copy.setItemMeta(meta);
        return copy;
    }

    public static boolean hasGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
    }

    // ── Unbreakable ──────────────────────────────────────────────

    public static ItemStack setUnbreakable(ItemStack item, boolean unbreakable) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;
        meta.setUnbreakable(unbreakable);
        copy.setItemMeta(meta);
        return copy;
    }

    public static boolean isUnbreakable(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.isUnbreakable();
    }

    // ── Repair ───────────────────────────────────────────────────

    public static ItemStack repair(ItemStack item) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta instanceof Damageable d) {
            d.setDamage(0);
            copy.setItemMeta(meta);
        }
        return copy;
    }

    public static boolean isDamageable(ItemStack item) {
        return item.getItemMeta() instanceof Damageable;
    }

    // ── Enchantment lookup ───────────────────────────────────────

    /**
     * Resolves an enchantment by name. Accepts Minecraft IDs (e.g. "sharpness"),
     * Bukkit names (e.g. "DAMAGE_ALL"), and short aliases.
     */
    public static Enchantment resolveEnchantment(String name) {
        if (name == null || name.isBlank()) return null;
        // Try namespaced key first
        NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase().replace(' ', '_'));
        Enchantment found = Enchantment.getByKey(key);
        if (found != null) return found;
        // Try Bukkit name
        found = Enchantment.getByName(name.toUpperCase());
        return found;
    }

    // ── Internal helpers ─────────────────────────────────────────

    /** Returns the item's current display name as plain text, or vanilla name if none set. */
    public static String getPlainDisplayName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.displayName() != null) {
            Component name = meta.displayName();
            return PlainTextComponentSerializer.plainText().serialize(name);
        }
        return formatVanillaName(item.getType().name());
    }

    /** The "base" name is the vanilla item name, always without any existing prefix/suffix. */
    private static String getBaseName(ItemStack item) {
        return formatVanillaName(item.getType().name());
    }

    private static String formatVanillaName(String materialName) {
        String[] words = materialName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }
}
