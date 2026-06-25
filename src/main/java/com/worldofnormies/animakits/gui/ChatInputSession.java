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
 * Cancel by typing  //cancel  OR by pressing Escape (which fires
 * PlayerCommandPreprocessEvent with "/") in some clients, or by just
 * pressing Enter with no input – we treat blank + escape as cancel.
 *
 * No clickable "cancel" buttons are shown; the prompt just tells the
 * player to type //cancel to abort.
 */
public class ChatInputSession implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin  plugin;
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
            "<gradient:#FF6060:#CC0000>✘ Cancelled. </gradient>" + cancelMessage));
    }

    // ─────────────────────────────────────────────────────────────
    // Static prompt helpers
    // ─────────────────────────────────────────────────────────────

    public static void sendRenamePrompt(AnimaKitsPlugin plugin, Player player) {
        player.sendMessage(MM.deserialize(
            "<gradient:#FFD700:#FF8C00><bold>━━━━ AnimaKits · Rename Kit ━━━━</bold></gradient>"));
        player.sendMessage(MM.deserialize(
            "<gray>Supports <white>legacy &codes</white>, <white>&#HEX</white>, and <white>MiniMessage</white>:</gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>  &6&lGolden Kit  · &#FF5500Lava Kit  · <gradient:#54DAF4:#545EB6>Gradient Kit</gradient>  · <rainbow>Rainbow Kit</rainbow></dark_gray>"));
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize(
            "<yellow>✎ Type the new kit name in chat and press <white>Enter</white>.</yellow>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>Type <white>//cancel</white> or press <white>Escape</white> to abort.</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<gradient:#FFD700:#FF8C00><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
    }

    public static void sendLorePrompt(AnimaKitsPlugin plugin, Player player) {
        player.sendMessage(MM.deserialize(
            "<gradient:#A5D6A7:#2E7D32><bold>━━━━ AnimaKits · Add Lore Line ━━━━</bold></gradient>"));
        player.sendMessage(MM.deserialize(
            "<gray>Supports MiniMessage, hex and legacy codes:</gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>  <italic><gray>A blazing hot kit!</gray></italic>  ·  " +
            "<gradient:#FF6B6B:#FFE66D>Legendary Loot Inside!</gradient>  ·  <color:#AA00FF>Mystic power</color></dark_gray>"));
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize(
            "<yellow>✎ Type the lore line in chat and press <white>Enter</white>.</yellow>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>Type <white>//cancel</white> or press <white>Escape</white> to abort.</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<gradient:#A5D6A7:#2E7D32><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
    }

    public static void sendClonePrompt(AnimaKitsPlugin plugin, Player player, Kit kit) {
        player.sendMessage(MM.deserialize(
            "<gradient:#CE93D8:#6A1B9A><bold>━━━━ AnimaKits · Clone Kit ━━━━</bold></gradient>"));
        player.sendMessage(MM.deserialize(
            "<gray>Cloning: <white>" + kit.getPlainName() + "</white></gray>"));
        player.sendMessage(MM.deserialize(
            "<yellow>✎ Type the name for the clone and press <white>Enter</white>.</yellow>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>Type <white>//cancel</white> or press <white>Escape</white> to abort.</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<gradient:#CE93D8:#6A1B9A><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
    }

    public static void sendCooldownPrompt(Player player) {
        player.sendMessage(MM.deserialize(
            "<gradient:#CE93D8:#6A1B9A><bold>━━━━ AnimaKits · Set Cooldown ━━━━</bold></gradient>"));
        player.sendMessage(MM.deserialize(
            "<gray>Enter cooldown in <white>seconds</white>.</gray>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>  Examples: <white>300</white> = 5 min · <white>3600</white> = 1 hr · <white>0</white> = none</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<yellow>✎ Type the value and press <white>Enter</white>.</yellow>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>Type <white>//cancel</white> or press <white>Escape</white> to abort.</dark_gray>"));
        player.sendMessage(MM.deserialize(
            "<gradient:#CE93D8:#6A1B9A><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
    }
}