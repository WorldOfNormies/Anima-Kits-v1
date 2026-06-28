package com.worldofnormies.animakits.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AnimaInventoryHolder implements InventoryHolder {
    private final String type;
    private final Object data;

    public AnimaInventoryHolder(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() { return type; }
    public Object getData() { return data; }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
