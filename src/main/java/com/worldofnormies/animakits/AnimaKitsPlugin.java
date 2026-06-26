package com.worldofnormies.animakits;

import com.worldofnormies.animakits.commands.AnimaKitsCommand;
import com.worldofnormies.animakits.commands.AnimaKitsTabCompleter;
import com.worldofnormies.animakits.listener.PlayerJoinListener;
import com.worldofnormies.animaeconomy.AnimaEconomyManager;
import com.worldofnormies.animaranks.RankListener;
import com.worldofnormies.animaranks.RankManager;
import com.worldofnormies.animakits.manager.KitManager;
import com.worldofnormies.animakits.manager.PermissionManager;
import com.worldofnormies.animakits.manager.PlayerManager;
import com.worldofnormies.animakits.util.MessageUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AnimaKitsPlugin – main entry point.
 */
public final class AnimaKitsPlugin extends JavaPlugin {

    private BukkitAudiences adventure;
    private KitManager kitManager;
    private PermissionManager permissionManager;
    private PlayerManager playerManager;
    private AnimaEconomyManager economyManager;
    private RankManager rankManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Adventure audiences (for hex/gradient colour support on Spigot)
        this.adventure = BukkitAudiences.create(this);

        // Managers
        this.kitManager = new KitManager(this);
        this.permissionManager = new PermissionManager(this);
        this.playerManager = new PlayerManager(this);
        this.economyManager = new AnimaEconomyManager(this);
        this.rankManager = new RankManager(this);

        kitManager.loadKits();
        permissionManager.load();
        playerManager.load();
        economyManager.load();
        rankManager.load();

        // Autosave Task
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            economyManager.save();
            rankManager.save();
            playerManager.save();
        }, 6000L, 6000L); // Every 5 minutes

        // Utilities
        MessageUtil.init(this);

        // Commands
        AnimaKitsCommand commandExecutor = new AnimaKitsCommand(this);
        AnimaKitsTabCompleter tabCompleter = new AnimaKitsTabCompleter(this);

        var cmd = getCommand("anima");
        if (cmd != null) {
            cmd.setExecutor(commandExecutor);
            cmd.setTabCompleter(tabCompleter);
        }

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new RankListener(this, rankManager), this);

        getLogger().info("AnimaKits enabled! Created by World Of Normies.");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) economyManager.save();
        if (rankManager != null) rankManager.save();
        if (playerManager != null) playerManager.save();
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        getLogger().info("AnimaKits disabled.");
    }

    /** Reload config + kit data. */
    public void reload() {
        reloadConfig();
        MessageUtil.reload(this);
        kitManager.loadKits();
        permissionManager.load();
        playerManager.load();
    }

    public BukkitAudiences adventure() { return adventure; }
    public KitManager getKitManager() { return kitManager; }
    public PermissionManager getPermissionManager() { return permissionManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public AnimaEconomyManager getEconomyManager() { return economyManager; }
    public RankManager getRankManager() { return rankManager; }
}
