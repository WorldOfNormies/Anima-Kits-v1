package com.worldofnormies.animaeconomy;

import com.worldofnormies.animakits.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class EconomyUtil {

    public static final NamespacedKey ECO_TYPE_KEY = new NamespacedKey("anima", "eco_type");
    public static final NamespacedKey ECO_AMOUNT_KEY = new NamespacedKey("anima", "eco_amount");

    public static ItemStack createWithdrawItem(String type, long amount) {
        Material material = switch (type.toLowerCase()) {
            case "money", "ani" -> Material.ORANGE_BUNDLE;
            case "xp", "experience" -> Material.EXPERIENCE_BOTTLE;
            case "coins", "animaz" -> Material.YELLOW_BUNDLE;
            default -> Material.PAPER;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String symbol = switch (type.toLowerCase()) {
            case "money", "ani" -> "λ";
            case "coins", "animaz" -> "Â";
            case "xp", "experience" -> "XP";
            default -> "";
        };

        String name = switch (type.toLowerCase()) {
            case "money", "ani" -> "Ani";
            case "coins", "animaz" -> "Anima Coins";
            case "xp", "experience" -> "Experience";
            default -> "Economy Item";
        };

        meta.displayName(ColorUtil.parse("<gradient:#FFCC00:#FFFF00><bold>" + name + " (" + symbol + ")</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(ColorUtil.parse("<gray>Contains: <yellow>" + amount + " " + symbol + "</yellow></gray>"));
        lore.add(Component.empty());
        lore.add(ColorUtil.parse("<gray>Right-click to claim!</gray>"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(ECO_TYPE_KEY, PersistentDataType.STRING, type.toLowerCase());
        meta.getPersistentDataContainer().set(ECO_AMOUNT_KEY, PersistentDataType.LONG, amount);

        if (material != Material.EXPERIENCE_BOTTLE) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }
}
