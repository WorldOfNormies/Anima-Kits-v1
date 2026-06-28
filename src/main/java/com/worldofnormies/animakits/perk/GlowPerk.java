package com.worldofnormies.animakits.perk;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.util.ColorUtil;
import org.bukkit.entity.Player;

public class GlowPerk {
    public static void toggleGlow(AnimaKitsPlugin plugin, Player player) {
        if (player.isGlowing()) {
            player.setGlowing(false);
            player.sendMessage(ColorUtil.colorize("&cGlow disabled!"));
        } else {
            player.setGlowing(true);
            player.sendMessage(ColorUtil.colorize("&aGlow enabled!"));
        }
    }
}
