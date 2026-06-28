package com.worldofnormies.animaranks;

import org.bukkit.Material;

import java.util.*;

/**
 * Rank – represents a single server rank.
 *
 * Hierarchy: lower number = higher rank (0 = top, e.g. Owner).
 * Players are assigned a rank via RankManager.
 */
public class Rank {

    private final String id;          // internal key (plain, lowercase, e.g. "owner")
    private String displayName;       // raw MiniMessage display name
    private int    hierarchy;         // 0 = highest, higher = lower in hierarchy
    private String prefix;            // MiniMessage prefix shown before player name
    private String suffix;            // MiniMessage suffix shown after player name
    private String colorName;         // MiniMessage color applied to player name
    private String chatColor;         // MiniMessage color applied to player chat text
    private Material iconMaterial;    // Material used for GUI icon
    private String kitId;             // Attached kit ID (plain name)

    // Rankup settings
    private boolean rankable;         // can players rank up TO this rank from one rank below?
    private long    rankupPlaytime;   // required playtime in seconds for rankup
    private double  rankupPrice;      // price in economy for rankup

    // Perk settings
    private int     homeLimit;        // max homes for this rank
    private int     repairCooldown;   // seconds, 0 = no cooldown
    private boolean canRepairAll;     // can use /repair all
    private int     rtpCooldown;      // seconds, cooldown for /rtp
    private int     feedCooldown;     // seconds, cooldown for /feed
    private int     echestRows;       // number of echest rows (1-6)

    // Buyable settings
    private boolean buyable;          // can players buy this rank?
    private boolean buyableWithAnimaz; // true = Animaz (Â), false = Ani (λ)
    private double  buyPrice;         // cost to buy
    private long    buyDuration;      // seconds, -1 = permanent

    // Permissions this rank grants
    private final Map<String, Boolean> permissions = new LinkedHashMap<>();

    public Rank(String id, String displayName, int hierarchy) {
        this.id          = id;
        this.displayName = displayName;
        this.hierarchy   = hierarchy;
        this.prefix      = "";
        this.suffix      = "";
        this.colorName   = "";
        this.chatColor   = "";
        this.iconMaterial = Material.STONE;
        this.kitId       = "";
        this.rankable    = false;
        this.rankupPlaytime = 0;
        this.rankupPrice = 0;
        this.homeLimit   = 2;
        this.repairCooldown = 0;
        this.canRepairAll = false;
        this.rtpCooldown = 300;
        this.feedCooldown = 300;
        this.echestRows  = 1;
        this.buyable     = false;
        this.buyableWithAnimaz = false;
        this.buyPrice    = 0;
        this.buyDuration = -1;
    }

    // ── Identity ───────────────────────────────────────────────────

    public String getId()                      { return id; }
    public String getDisplayName()             { return displayName; }
    public void   setDisplayName(String n)     { this.displayName = n; }
    public int    getHierarchy()               { return hierarchy; }
    public void   setHierarchy(int h)          { this.hierarchy = h; }

    // ── Cosmetics ──────────────────────────────────────────────────

    public String getPrefix()                  { return prefix; }
    public void   setPrefix(String p)          { this.prefix = p == null ? "" : p; }
    public String getSuffix()                  { return suffix; }
    public void   setSuffix(String s)          { this.suffix = s == null ? "" : s; }
    public String getColorName()               { return colorName; }
    public void   setColorName(String c)       { this.colorName = c == null ? "" : c; }
    public String getChatColor()               { return chatColor; }
    public void   setChatColor(String c)       { this.chatColor = c == null ? "" : c; }
    public Material getIconMaterial()          { return iconMaterial; }
    public void   setIconMaterial(Material m)  { this.iconMaterial = m == null ? Material.STONE : m; }
    public String getKitId()                   { return kitId; }
    public void   setKitId(String k)           { this.kitId = k == null ? "" : k; }

    // ── Rankup ─────────────────────────────────────────────────────

    public boolean isRankable()                { return rankable; }
    public void    setRankable(boolean r)      { this.rankable = r; }
    public long    getRankupPlaytime()         { return rankupPlaytime; }
    public void    setRankupPlaytime(long t)   { this.rankupPlaytime = t; }
    public double  getRankupPrice()            { return rankupPrice; }
    public void    setRankupPrice(double p)    { this.rankupPrice = p; }

    // ── Perks ──────────────────────────────────────────────────────

    public int     getHomeLimit()              { return homeLimit; }
    public void    setHomeLimit(int l)         { this.homeLimit = l; }
    public int     getRepairCooldown()         { return repairCooldown; }
    public void    setRepairCooldown(int c)    { this.repairCooldown = c; }
    public boolean canRepairAll()              { return canRepairAll; }
    public void    setCanRepairAll(boolean b)  { this.canRepairAll = b; }
    public int     getRtpCooldown()            { return rtpCooldown; }
    public void    setRtpCooldown(int c)       { this.rtpCooldown = c; }
    public int     getFeedCooldown()           { return feedCooldown; }
    public void    setFeedCooldown(int c)      { this.feedCooldown = c; }
    public int     getEchestRows()             { return echestRows; }
    public void    setEchestRows(int r)        { this.echestRows = r; }

    // ── Buyable ────────────────────────────────────────────────────

    public boolean isBuyable()                 { return buyable; }
    public void    setBuyable(boolean b)       { this.buyable = b; }
    public boolean isBuyableWithAnimaz()       { return buyableWithAnimaz; }
    public void    setBuyableWithAnimaz(boolean b) { this.buyableWithAnimaz = b; }
    public double  getBuyPrice()               { return buyPrice; }
    public void    setBuyPrice(double p)       { this.buyPrice = p; }
    public long    getBuyDuration()            { return buyDuration; }
    public void    setBuyDuration(long d)      { this.buyDuration = d; }

    // ── Permissions ────────────────────────────────────────────────

    public Map<String, Boolean> getPermissions()             { return Collections.unmodifiableMap(permissions); }
    public void setPermission(String node, boolean value)    { permissions.put(node, value); }
    public void removePermission(String node)                { permissions.remove(node); }
    public void clearPermissions()                           { permissions.clear(); }

    // ── Helpers ────────────────────────────────────────────────────

    /** True if prefix, suffix, colorName, chatColor are all blank. */
    public boolean hasNoCosmetics() {
        return prefix.isBlank() && suffix.isBlank() && colorName.isBlank() && chatColor.isBlank();
    }
}
