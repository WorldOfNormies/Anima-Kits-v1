package com.worldofnormies.animaranks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.util.*;

public class Rank {
    private String id;
    private int hierarchy;
    private String prefix = "";
    private String suffix = "";
    private String nameColor = "";
    private String chatColor = "";
    private boolean rankable = false;
    private long requiredPlaytime = 0; // seconds
    private double rankupPrice = 0;
    private boolean buyable = false;
    private double buyPrice = 0;
    private long buyDuration = -1; // seconds, -1 = permanent
    private List<String> permissions = new ArrayList<>();

    public Rank(String id, int hierarchy) {
        this.id = id;
        this.hierarchy = hierarchy;
    }

    public String getId() { return id; }
    public int getHierarchy() { return hierarchy; }
    public void setHierarchy(int hierarchy) { this.hierarchy = hierarchy; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }

    public String getNameColor() { return nameColor; }
    public void setNameColor(String nameColor) { this.nameColor = nameColor; }

    public String getChatColor() { return chatColor; }
    public void setChatColor(String chatColor) { this.chatColor = chatColor; }

    public boolean isRankable() { return rankable; }
    public void setRankable(boolean rankable) { this.rankable = rankable; }

    public long getRequiredPlaytime() { return requiredPlaytime; }
    public void setRequiredPlaytime(long requiredPlaytime) { this.requiredPlaytime = requiredPlaytime; }

    public double getRankupPrice() { return rankupPrice; }
    public void setRankupPrice(double rankupPrice) { this.rankupPrice = rankupPrice; }

    public boolean isBuyable() { return buyable; }
    public void setBuyable(boolean buyable) { this.buyable = buyable; }

    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }

    public long getBuyDuration() { return buyDuration; }
    public void setBuyDuration(long buyDuration) { this.buyDuration = buyDuration; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
