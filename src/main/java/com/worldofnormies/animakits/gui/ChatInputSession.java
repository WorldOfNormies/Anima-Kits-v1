package com.worldofnormies.animakits.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.kit.Kit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;
import java.util.function.Consumer;

/** 
 * ChatInputSession – waits for a single chat message from a player.
 * Cancel by typing //cancel OR by pressing Escape (which fires
 * PlayerCommandPreprocessEvent with "/") in some clients, or by just
 * pressing Enter with no input – we treat blank + escape as cancel.
 *
 * No clickable "cancel" buttons are shown; the prompt just tells the
 * player to type //cancel to abort.
 */
public class ChatInputSession implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin plugin;
    private final Player           player;
    private final Consumer<String> onInput;
    private final Runnable         onCancel;
    private final String           cancelMessage; // shown when cancelled
    private volatile boolean       handled = false;

    public ChatInputSession(AnimaKitsPlugin plugin, Player player,
                            Consumer<String> onInput, Runnable onCancel) {
        this(plugin, player, onInput, onCancel,
             "<gray>No changes were made.</gray>");
    }

    public ChatInputSession(AnimaKitsPlugin plugin, Player player,
                            Consumer<String> onInput, Runnable onCancel,
                            String cancelMessage) {
        this.plugin        = plugin;
        this.player        = player;
        this.onInput       = onInput;
        this.onCancel      = onCancel;
        this.cancelMessage = cancelMessage;
    }

    /** Register listener and start awaiting input. */
    public void await() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // ── Chat listener (primary) ────────────────────────────────────

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player)) return;
        event.setCancelled(true);
        if (handled) return;
        handled = true;
        HandlerList.unregisterAll(this);

        String msg = event.getMessage().trim();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (msg.equalsIgnoreCase("//cancel") || msg.isEmpty()) {
                sendCancelNotice();
                onCancel.run();
            } else {
                onInput.accept(msg);
            }
        });
    }

    // ── Command pre-process: catches /  (Escape in some clients) ───

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().equals(player)) return;
        String msg = event.getMessage().trim();
        // Treat a bare "/" or "//cancel" typed as command as cancel
        if (!msg.equals("/") && !msg.equalsIgnoreCase("//cancel")) return;
        event.setCancelled(true);
        if (handled) return;
        handled = true;
        HandlerList.unregisterAll(this);

        Bukkit.getScheduler().runTask(plugin, () -> {
            sendCancelNotice();
            onCancel.run();
        });
    }

    // ─────────────────────────────────────────────────────────────
    // Internal
    // ─────────────────────────────────────────────────────────────

    private void sendCancelNotice() {
        player.sendMessage(MM.deserialize(
            "<gradient:#FF4B4B:#FF8585><bold>✕ Cancelled.</bold></gradient> " + cancelMessage));
    }

    // ─────────────────────────────────────────────────────────────
    // Static prompt helpers
    // ─────────────────────────────────────────────────────────────

    public static void sendRenamePrompt(AnimaKitsPlugin plugin, Player player) {
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits · Rename Kit ━━━━</bold></gradient>"));
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>⚠ ATTENTION:</bold></gradient> <gray>To apply formatting, you must include the backslashes (\\) exactly as shown:</gray>"));
        player.sendMessage(Component.empty());

        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<gradient:#hex1:#hex2\\></yellow> <gray>➔ </gray><gradient:#FF5500:#FFCC00>Vip Kit</gradient> <dark_gray>(Type: \\<gradient:#FF5500:#FFCC00\\>Vip Kit)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<rainbow\\></yellow> <gray>➔ </gray><rainbow>Arcade Kit</rainbow> <dark_gray>(Type: \\<rainbow\\>Arcade Kit)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<bold\\></yellow> <gray>➔ </gray><bold>Heavy Kit</bold> <dark_gray>(Type: \\<bold\\>Heavy Kit)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<italic\\></yellow> <gray>➔ </gray><italic>Swift Kit</italic> <dark_gray>(Type: \\<italic\\>Swift Kit)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<underline\\></yellow> <gray>➔ </gray><underline>God Kit</underline> <dark_gray>(Type: \\<underline\\>God Kit)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<colorname\\></yellow> <gray>➔ </gray><red>Ruby Kit</red> <dark_gray>(Type: \\<red\\>Ruby Kit)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<#hexcolorcode\\></yellow> <gray>➔ </gray><#A8FF33>Lime Kit</#A8FF33> <dark_gray>(Type: \\<#A8FF33\\>Lime Kit)</dark_gray>"));
        
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize(
            "<yellow>✎ Type the new kit name in chat and press <white>Enter</white>.</yellow>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>Type <white>//cancel</white> or press <white>Escape</white> to abort.</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
    }

    public static void sendLorePrompt(AnimaKitsPlugin plugin, Player player) {
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits · Add Lore Line ━━━━</bold></gradient>"));
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>⚠ ATTENTION:</bold></gradient> <gray>To apply formatting, you must include the backslashes (\\) exactly as shown:</gray>"));
        player.sendMessage(Component.empty());
        
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<gradient:#hex1:#hex2\\></yellow> <gray>➔ </gray><gradient:#00F2FE:#4FACFE>Contains rare weapons!</gradient> <dark_gray>(Type: \\<gradient:#00F2FE:#4FACFE\\>Contains rare weapons!)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<rainbow\\></yellow> <gray>➔ </gray><rainbow>Unlocks a random permanent cosmetic</rainbow> <dark_gray>(Type: \\<rainbow\\>Unlocks a random permanent cosmetic)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<bold\\></yellow> <gray>➔ </gray><bold>WARNING: High tier loot</bold> <dark_gray>(Type: \\<bold\\>WARNING: High tier loot)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<italic\\></yellow> <gray>➔ </gray><italic>Forged in the deep nether...</italic> <dark_gray>(Type: \\<italic\\>Forged in the deep nether...)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<underline\\></yellow> <gray>➔ </gray><underline>Click to claim your daily rewards</underline> <dark_gray>(Type: \\<underline\\>Click to claim your daily rewards)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<colorname\\></yellow> <gray>➔ </gray><gold>Cooldown: 24 Hours</gold> <dark_gray>(Type: \\<gold\\>Cooldown: 24 Hours)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<#hexcolorcode\\></yellow> <gray>➔ </gray><#FF5E7E>Exclusive seasonal event prize</#FF5E7E> <dark_gray>(Type: \\<#FF5E7E\\>Exclusive seasonal event prize)</dark_gray>"));
        
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize(
            "<yellow>✎ Type the lore line in chat and press <white>Enter</white>.</yellow>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>Type <white>//cancel</white> or press <white>Escape</white> to abort.</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
    }

    public static void sendClonePrompt(AnimaKitsPlugin plugin, Player player, Kit kit) {
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits · Clone Kit ━━━━</bold></gradient>"));
        player.sendMessage(MM.deserialize(
            "<gray>Cloning: <bold><white>" + kit.getPlainName() + "</white></bold></gray>"));
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>⚠ ATTENTION:</bold></gradient> <gray>To apply formatting, you must include the backslashes (\\) exactly as shown:</gray>"));
        player.sendMessage(Component.empty());
        
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<gradient:#hex1:#hex2\\></yellow> <gray>➔ </gray><gradient:#F35588:#05DFD7>Clone of " + kit.getPlainName() + "</gradient> <dark_gray>(Type: \\<gradient:#F35588:#05DFD7\\>Clone of " + kit.getPlainName() + ")</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<rainbow\\></yellow> <gray>➔ </gray><rainbow>Copy of " + kit.getPlainName() + "</rainbow> <dark_gray>(Type: \\<rainbow\\>Copy of " + kit.getPlainName() + ")</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<bold\\></yellow> <gray>➔ </gray><bold>Custom " + kit.getPlainName() + "</bold> <dark_gray>(Type: \\<bold\\>Custom " + kit.getPlainName() + ")</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<italic\\></yellow> <gray>➔ </gray><italic>Duplicate Kit</italic> <dark_gray>(Type: \\<italic\\>Duplicate Kit)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<underline\\></yellow> <gray>➔ </gray><underline>New Edition</underline> <dark_gray>(Type: \\<underline\\>New Edition)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<colorname\\></yellow> <gray>➔ </gray><aqua>Secondary Kit</aqua> <dark_gray>(Type: \\<aqua\\>Secondary Kit)</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>▪ </dark_gray><yellow>\\<#hexcolorcode\\></yellow> <gray>➔ </gray><#E0C3FC>Backup Kit</#E0C3FC> <dark_gray>(Type: \\<#E0C3FC\\>Backup Kit)</dark_gray>"));
        
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize(
            "<yellow>✎ Type the name for the clone and press <white>Enter</white>.</yellow>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>Type <white>//cancel</white> or press <white>Escape</white> to abort.</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
    }

    public static void sendCooldownPrompt(Player player) {
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits · Set Cooldown ━━━━</bold></gradient>"));
        player.sendMessage(MM.deserialize(
            "<gray>Enter cooldown restrictions in <white>seconds</white>.</gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>  Examples: <white>300</white> = 5 min · <white>3600</white> = 1 hr · <white>0</white> = none</dark_gray>"));
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize(
            "<yellow>✎ Type the numeric value and press <white>Enter</white>.</yellow>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>Type <white>//cancel</white> or press <white>Escape</white> to abort.</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<gradient:#54DAF4:#545EB6><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
    }
}
