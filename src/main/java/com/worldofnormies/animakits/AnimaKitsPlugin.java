package com.worldofnormies.animakits;

import com.worldofnormies.animakits.commands.AnimaKitsCommand;
import com.worldofnormies.animakits.commands.AnimaKitsTabCompleter;
import com.worldofnormies.animakits.listener.PlayerJoinListener;
import com.worldofnormies.animakits.home.HomeManager;
import com.worldofnormies.animakits.rtp.RtpManager;
import com.worldofnormies.animakits.echest.EChestManager;
import com.worldofnormies.animakits.echest.EChestListener;
import com.worldofnormies.animakits.suffix.SuffixManager;
import com.worldofnormies.animakits.manager.KitManager;
import com.worldofnormies.animakits.manager.PermissionManager;
import com.worldofnormies.animakits.manager.PlayerManager;
import com.worldofnormies.animaranks.RankListener;
import com.worldofnormies.animaranks.manager.RankManager;
import com.worldofnormies.animaeconomy.EconomyManager;
import com.worldofnormies.animaeconomy.EconomyListener;
import com.worldofnormies.animakits.scoreboard.ScoreboardManager;
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
    private RankManager rankManager;
    private EconomyManager economyManager;
    private HomeManager homeManager;
    private RtpManager rtpManager;
    private EChestManager eChestManager;
    private SuffixManager suffixManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.adventure = BukkitAudiences.create(this);

        // Managers
        this.kitManager        = new KitManager(this);
        this.permissionManager = new PermissionManager(this);
        this.playerManager     = new PlayerManager(this);
        this.rankManager       = new RankManager(this);
        this.economyManager    = new EconomyManager(this);
        this.homeManager       = new HomeManager(this);
        this.rtpManager        = new RtpManager(this);
        this.eChestManager     = new EChestManager(this);
        this.suffixManager     = new SuffixManager(this);
        this.scoreboardManager = new ScoreboardManager(this);

        kitManager.loadKits();
        suffixManager.load();
        permissionManager.load();
        playerManager.load();
        rankManager.load();
        economyManager.load();
        scoreboardManager.init();

        MessageUtil.init(this);

        // Commands
        AnimaKitsCommand commandExecutor = new AnimaKitsCommand(this);
        AnimaKitsTabCompleter tabCompleter = new AnimaKitsTabCompleter(this);

        var cmd = getCommand("anima");
        if (cmd != null) {
            cmd.setExecutor(commandExecutor);
            cmd.setTabCompleter(tabCompleter);
        }

        String[] shortcuts = {"kits", "itemedit", "ranks", "echo", "feed", "repair", "ender", "home", "astore", "claims", "permissions", "rtp", "scoreboard"};
        for (String s : shortcuts) {
            var sc = getCommand(s);
            if (sc != null) {
                sc.setExecutor(commandExecutor);
                sc.setTabCompleter(tabCompleter);
            }
        }

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new RankListener(this), this);
        getServer().getPluginManager().registerEvents(new EconomyListener(this), this);
        getServer().getPluginManager().registerEvents(new EChestListener(this), this);
        getServer().getPluginManager().registerEvents(new com.worldofnormies.animakits.listener.MenuListener(this), this);

        getLogger().info("AnimaKits enabled! Created by World Of Normies.");
    }

    @Override
    public void onDisable() {
        // Save playtime for all online players before shutdown
        if (rankManager != null) {
            getServer().getOnlinePlayers().forEach(p -> {
                rankManager.onPlayerQuit(p);
                permissionManager.savePlayer(p.getUniqueId());
                homeManager.savePlayer(p.getUniqueId());
                eChestManager.savePlayer(p.getUniqueId());
            });
        }
        if (economyManager != null) {
            economyManager.save();
        }
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        getLogger().info("AnimaKits disabled.");
    }

    public void reload() {
        reloadConfig();
        MessageUtil.reload(this);
        kitManager.loadKits();
        permissionManager.load();
        playerManager.load();
        rankManager.load();
        economyManager.load();
    }

    public BukkitAudiences adventure()              { return adventure; }
    public KitManager getKitManager()               { return kitManager; }
    public PermissionManager getPermissionManager() { return permissionManager; }
    public PlayerManager getPlayerManager()         { return playerManager; }
    public RankManager getRankManager()             { return rankManager; }
    public EconomyManager getEconomyManager()       { return economyManager; }
    public HomeManager getHomeManager()             { return homeManager; }
    public RtpManager getRtpManager()               { return rtpManager; }
    public EChestManager getEChestManager()         { return eChestManager; }
    public SuffixManager getSuffixManager()         { return suffixManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
}
