package com.worldofnormies.animakits.kit;

import com.worldofnormies.animakits.util.ColorUtil;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single AnimaKit with a name, lore, and item contents.
 */
public class Kit {

    private final UUID id;
    private String rawName;   // stored exactly as typed (MiniMessage / &codes)
    private List<String> lore;
    private List<ItemStack> items;

    public Kit(UUID id, String rawName) {
        this.id = id;
        this.rawName = rawName;
        this.lore = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    // ── Identity ───────────────────────────────────────────────────

    public UUID getId() { return id; }

    public String getRawName()   { return rawName; }
    public void   setRawName(String n) { this.rawName = n; }

    /** Colour-stripped name for config keys / comparison. */
    public String getPlainName() { return ColorUtil.strip(rawName); }

    // ── Lore ──────────────────────────────────────────────────────

    public List<String> getLore()                        { return Collections.unmodifiableList(lore); }
    public void         setLore(List<String> lore)       { this.lore = new ArrayList<>(lore); }
    public void         addLoreLine(String line)         { lore.add(line); }
    public void         setLoreLine(int idx, String line){ lore.set(idx, line); }
    public void         removeLoreLine(int idx)          { lore.remove(idx); }

    // ── Items ──────────────────────────────────────────────────────

    public List<ItemStack> getItems()               { return Collections.unmodifiableList(items); }
    public void            setItems(List<ItemStack> items) { this.items = new ArrayList<>(items); }
    public void            addItem(ItemStack item)  { items.add(item); }
    public void            clearItems()             { items.clear(); }
}
