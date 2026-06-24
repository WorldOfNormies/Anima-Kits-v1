package com.worldofnormies.animakits;

import com.worldofnormies.animakits.commands.AnimaKitsCommand;
import com.worldofnormies.animakits.commands.AnimaKitsTabCompleter;
import com.worldofnormies.animakits.manager.KitManager;
import com.worldofnormies.animakits.manager.PermissionManager;
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

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Adventure audiences (for hex/gradient colour support on Spigot)
        this.adventure = BukkitAudiences.create(this);

        // Managers
        this.kitManager = new KitManager(this);
        this.permissionManager = new PermissionManager(this);

        kitManager.loadKits();
        permissionManager.load();

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

        getLogger().info("AnimaKits enabled! Created by World Of Normies.");
    }

    @Override
    public void onDisable() {
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
    }

    public BukkitAudiences adventure() { return adventure; }
    public KitManager getKitManager() { return kitManager; }
    public PermissionManager getPermissionManager() { return permissionManager; }
}
