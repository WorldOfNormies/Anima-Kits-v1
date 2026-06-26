package com.worldofnormies.animaranks;

import java.util.ArrayList;
import java.util.List;

public class Rank {
    private final String name;
    private int priority;
    private String prefix = "";
    private String suffix = "";
    private String nameColor = "";
    private String chatColor = "";
    private List<String> permissions = new ArrayList<>();
    private List<String> kits = new ArrayList<>();
    private boolean isRankable = false;
    private boolean isBuyable = false;
    private double price = 0;
    private long requiredPlaytime = 0; // in seconds

    public Rank(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public String getName() { return name; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public String getNameColor() { return nameColor; }
    public void setNameColor(String nameColor) { this.nameColor = nameColor; }
    public String getChatColor() { return chatColor; }
    public void setChatColor(String chatColor) { this.chatColor = chatColor; }
    public List<String> getPermissions() { return permissions; }
    public List<String> getKits() { return kits; }
    public boolean isRankable() { return isRankable; }
    public void setRankable(boolean rankable) { isRankable = rankable; }
    public boolean isBuyable() { return isBuyable; }
    public void setBuyable(boolean buyable) { isBuyable = buyable; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public long getRequiredPlaytime() { return requiredPlaytime; }
    public void setRequiredPlaytime(long requiredPlaytime) { this.requiredPlaytime = requiredPlaytime; }
}
