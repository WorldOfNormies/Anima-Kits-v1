package com.worldofnormies.animakits.gui;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.kit.Kit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * ChatInputSession – waits for a single chat message from a player,
 * then calls the callback.  "cancel" aborts.
 */
public class ChatInputSession implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimaKitsPlugin plugin;
    private final Player          player;
    private final Consumer<String> onInput;
    private final Runnable         onCancel;
    private final UUID             sessionId;

    public ChatInputSession(AnimaKitsPlugin plugin, Player player,
                            Consumer<String> onInput, Runnable onCancel) {
        this.plugin    = plugin;
        this.player    = player;
        this.onInput   = onInput;
        this.onCancel  = onCancel;
        this.sessionId = UUID.randomUUID();
    }

    /** Register and await input. */
    public void await() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player)) return;
        event.setCancelled(true);
        String msg = event.getMessage().trim();
        HandlerList.unregisterAll(this);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (msg.equalsIgnoreCase("cancel")) {
                onCancel.run();
            } else {
                onInput.accept(msg);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // Static helpers to send the instruction messages
    // ─────────────────────────────────────────────────────────────

    /** Chat prompt shown when renaming a kit. */
    public static void sendRenamePrompt(AnimaKitsPlugin plugin, Player player) {
        MiniMessage mm = MiniMessage.miniMessage();

        player.sendMessage(mm.deserialize(
                "<gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits – Rename Kit ━━━━</bold></gradient>"));
        player.sendMessage(mm.deserialize(
                "<gray>How to type a <white>gradient</white> name:</gray>"));
        player.sendMessage(mm.deserialize(
                "<gray>  Legacy:     <white>&6&lGolden Kit</white>  → <gold><bold>Golden Kit</bold></gold></gray>"));
        player.sendMessage(mm.deserialize(
                "<gray>  Hex:        <white>&#FF5500Lava Kit</white>  → <color:#FF5500>Lava Kit</color></gray>"));
        player.sendMessage(mm.deserialize(
                "<gray>  Gradient:   <white><gradient:#54DAF4:#545EB6>My Kit Name</gradient></white></gray>"));
        player.sendMessage(mm.deserialize(
                "<gray>  Rainbow:    <white><rainbow>Rainbow Kit</rainbow></white></gray>"));
        player.sendMessage(Component.empty());
        player.sendMessage(mm.deserialize(
                "<yellow>Enter new kit name in chat, then press <white>Enter</white>:</yellow>"));
        player.sendMessage(Component.empty());

        // Clickable cancel link
        Component cancelLink = mm.deserialize("<red><bold>[Click here to cancel]</bold></red>")
                .clickEvent(ClickEvent.runCommand("/animakits-cancel-input"));
        player.sendMessage(cancelLink);
    }

    /** Chat prompt shown when adding a lore line. */
    public static void sendLorePrompt(AnimaKitsPlugin plugin, Player player) {
        MiniMessage mm = MiniMessage.miniMessage();

        player.sendMessage(mm.deserialize(
                "<gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits – Add Lore Line ━━━━</bold></gradient>"));
        player.sendMessage(mm.deserialize(
                "<gray>How to type a <white>gradient</white> lore line:</gray>"));
        player.sendMessage(mm.deserialize(
                "<gray>  Example:  <white><italic><gray>A blazing hot kit!</gray></italic></white></gray>"));
        player.sendMessage(mm.deserialize(
                "<gray>  Gradient: <white><gradient:#FF6B6B:#FFE66D>Legendary Loot Inside!</gradient></white></gray>"));
        player.sendMessage(mm.deserialize(
                "<gray>  Hex:      <white>&#AA00FFMystic power awaits</white>  → <color:#AA00FF>Mystic power awaits</color></gray>"));
        player.sendMessage(Component.empty());
        player.sendMessage(mm.deserialize(
                "<yellow>Type the lore line in chat, then press <white>Enter</white>:</yellow>"));
        player.sendMessage(Component.empty());

        Component cancelLink = mm.deserialize("<red><bold>[Click here to cancel]</bold></red>")
                .clickEvent(ClickEvent.runCommand("/animakits-cancel-input"));
        player.sendMessage(cancelLink);
    }

    public static void sendClonePrompt(AnimaKitsPlugin plugin, Player player, Kit kit) {
        MiniMessage mm = MiniMessage.miniMessage();

        player.sendMessage(mm.deserialize(
                "<gradient:#54DAF4:#545EB6><bold>━━━━ AnimaKits – Clone Kit ━━━━</bold></gradient>"));
        player.sendMessage(mm.deserialize("<gray>Cloning kit: <white>" + kit.getPlainName() + "</white></gray>"));
        player.sendMessage(mm.deserialize("<yellow>Enter new name for the clone in chat:</yellow>"));
        player.sendMessage(Component.empty());

        Component cancelLink = mm.deserialize("<red><bold>[Click here to cancel]</bold></red>")
                .clickEvent(ClickEvent.runCommand("/animakits-cancel-input"));
        player.sendMessage(cancelLink);
    }
}
