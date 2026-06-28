package com.worldofnormies.animakits.home;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class Home {
    private final String name;
    private final UUID worldUuid;
    private final double x, y, z;
    private final float yaw, pitch;

    public Home(String name, Location loc) {
        this.name = name;
        this.worldUuid = loc.getWorld().getUID();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    public Home(String name, UUID worldUuid, double x, double y, double z, float yaw, float pitch) {
        this.name = name;
        this.worldUuid = worldUuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public String getName() { return name; }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldUuid);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public UUID getWorldUuid() { return worldUuid; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
}
