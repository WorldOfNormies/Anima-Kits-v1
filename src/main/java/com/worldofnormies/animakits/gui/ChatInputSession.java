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
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * ChatInputSession – waits for a single chat message from a player.
 * Cancel by typing /cancel, or by walking/moving away (interpreted as closing chatbox/Esc).
 */
public class ChatInputSession implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin plugin;
    private final Player player;
    private final Consumer<String> onInput;
    private final Runnable onCancel;
    private final String cancelMessage; 
    private volatile boolean handled = false;

    public ChatInputSession(AnimaKitsPlugin plugin, Player player,
                              Consumer<String> onInput, Runnable onCancel) {
        this(plugin, player, onInput, onCancel,
             "<gray>No adjustments were saved.</gray>");
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

    // ── Chat listener ──────────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player)) return;
        event.setCancelled(true);
        if (handled) return;
        handled = true;
        HandlerList.unregisterAll(this);

        String msg = event.getMessage().trim();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (msg.equalsIgnoreCase("/cancel") || msg.equalsIgnoreCase("//cancel") || msg.isEmpty()) {
                sendCancelNotice();
                onCancel.run();
            } else {
                onInput.accept(msg);
            }
        });
    }

    // ── Command Preprocess listener (Catches /cancel) ──────────────

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().equals(player)) return;
        String msg = event.getMessage().trim();
        
        if (!msg.equalsIgnoreCase("/cancel") && !msg.equalsIgnoreCase("//cancel") && !msg.equals("/")) return;
        
        event.setCancelled(true);
        if (handled) return;
        handled = true;
        HandlerList.unregisterAll(this);

        Bukkit.getScheduler().runTask(plugin, () -> {
            sendCancelNotice();
            onCancel.run();
        });
    }

    // ── Physical Movement fallback (Catches closing chatbox / ESC) ──

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.getPlayer().equals(player)) return;
        
        // Ensure player actually changed position blocks or jumped, instead of just rotating camera pitch/yaw
        if (event.getFrom().getX() == event.getTo().getX() && 
            event.getFrom().getY() == event.getTo().getY() && 
            event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }

        if (handled) return;
        handled = true;
        HandlerList.unregisterAll(this);

        Bukkit.getScheduler().runTask(plugin, () -> {
            sendCancelNotice();
            onCancel.run();
        });
    }

    // ─────────────────────────────────────────────────────────────
    // Internal Utilities
    // ─────────────────────────────────────────────────────────────

    private void sendCancelNotice() {
        player.sendMessage(MM.deserialize(
            "<gradient:#FF5555:#FF2222><b>✕</b> Input Terminated. </gradient>" + cancelMessage));
    }

    // ─────────────────────────────────────────────────────────────
    // Formatted Chat Container Displays
    // ─────────────────────────────────────────────────────────────

    public static void sendRenamePrompt(AnimaKitsPlugin plugin, Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("<gradient:#FFD700:#FF8C00><b>┌──────────────────────────────────────────────────┐</b></gradient>"));
        player.sendMessage(MM.deserialize("   <gradient:#FFD700:#FF8C00><b>✎ EDIT KIT NAME</b></gradient>"));
        player.sendMessage(MM.deserialize("   <gray>Type your value in chat and press enter.</gray>"));
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("   <dark_gray>▪ </dark_gray><gray>Formatting Options Supported:</gray>"));
        player.sendMessage(MM.deserialize("     <gold>&6Legacy Colors</gold><gray>, </gray><b>&#FF5500HEX Formats</b><gray>, or </gray><gradient:#54DAF4:#545EB6>MiniMessage</gradient>"));
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("   <dark_gray>▪ Type <white><b>/cancel</b></white> or move/walk away to exit prompt safely.</dark_gray>"));
        player.sendMessage(MM.deserialize("<gradient:#FFD700:#FF8C00><b>└──────────────────────────────────────────────────┘</b></gradient>"));
        player.sendMessage(Component.empty());
    }

    public static void sendLorePrompt(AnimaKitsPlugin plugin, Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("<gradient:#A5D6A7:#2E7D32><b>┌──────────────────────────────────────────────────┐</b></gradient>"));
        player.sendMessage(MM.deserialize("   <gradient:#A5D6A7:#2E7D32><b>✎ ADD LORE STRING LINE</b></gradient>"));
        player.sendMessage(MM.deserialize("   <gray>Type your lore line string content in chat.</gray>"));
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("   <dark_gray>▪ </dark_gray><gray>Example Formatting Layout Preview:</gray>"));
        player.sendMessage(MM.deserialize("     <i><gray>\"A mystical sword...\"</gray></i> <dark_gray>·</dark_gray> <gradient:#FF6B6B:#FFE66D><b>Legendary Loot</b></gradient>"));
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("   <dark_gray>▪ Type <white><b>/cancel</b></white> or move/walk away to exit prompt safely.</dark_gray>"));
        player.sendMessage(MM.deserialize("<gradient:#A5D6A7:#2E7D32><b>└──────────────────────────────────────────────────┘</b></gradient>"));
        player.sendMessage(Component.empty());
    }

    public static void sendClonePrompt(AnimaKitsPlugin plugin, Player player, Kit kit) {
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("<gradient:#CE93D8:#6A1B9A><b>┌──────────────────────────────────────────────────┐</b></gradient>"));
        player.sendMessage(MM.deserialize("   <gradient:#CE93D8:#6A1B9A><b>✎ DUPLICATE & CLONE KIT</b></gradient>"));
        player.sendMessage(MM.deserialize("   <gray>Target Object Source Name: <white>" + kit.getPlainName() + "</white></gray>"));
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("   <dark_gray>▪ </dark_gray><yellow>Provide a completely unique identifier string name.</yellow>"));
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("   <dark_gray>▪ Type <white><b>/cancel</b></white> or move/walk away to exit prompt safely.</dark_gray>"));
        player.sendMessage(MM.deserialize("<gradient:#CE93D8:#6A1B9A><b>└──────────────────────────────────────────────────┘</b></gradient>"));
        player.sendMessage(Component.empty());
    }

    public static void sendCooldownPrompt(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6><b>┌──────────────────────────────────────────────────┐</b></gradient>"));
        player.sendMessage(MM.deserialize("   <gradient:#54DAF4:#545EB6><b>✎ ADJUST COOLDOWN TIMER</b></gradient>"));
        player.sendMessage(MM.deserialize("   <gray>Specify duration thresholds using integer seconds.</gray>"));
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("   <dark_gray>▪ </dark_gray><gray>Reference Configurations Quick Sheet:</gray>"));
        player.sendMessage(MM.deserialize("     <aqua>300</aqua> <gray>(5 mins)</gray> <dark_gray>·</dark_gray> <aqua>3600</aqua> <gray>(1 hour)</gray> <dark_gray>·</dark_gray> <aqua>0</aqua> <gray>(Instant Reset)</gray>"));
        player.sendMessage(Component.empty());
        player.sendMessage(MM.deserialize("   <dark_gray>▪ Type <white><b>/cancel</b></white> or move/walk away to exit prompt safely.</dark_gray>"));
        player.sendMessage(MM.deserialize("<gradient:#54DAF4:#545EB6><b>└──────────────────────────────────────────────────┘</b></gradient>"));
        player.sendMessage(Component.empty());
    }
}