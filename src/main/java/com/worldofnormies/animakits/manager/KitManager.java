package com.worldofnormies.animakits.manager;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import com.worldofnormies.animakits.gui.AnimaKitsMainGUI;
import com.worldofnormies.animakits.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KitManager – persists kits to kits.yml and refreshes open GUIs on change.
 */
public class KitManager {

    private final AnimaKitsPlugin plugin;
    private final File kitsFile;
    private FileConfiguration kitsConfig;

    /** In-memory kit map: UUID → Kit */
    private final Map<UUID, Kit> kits = new LinkedHashMap<>();

    /** Track which players have an AnimaKitsMainGUI open so we can refresh them. */
    private final Map<UUID, AnimaKitsMainGUI> openBrowsers = new ConcurrentHashMap<>();

    public KitManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");
    }

    // ── Persistence ────────────────────────────────────────────────

    public void loadKits() {
        kits.clear();
        if (!kitsFile.exists()) {
            try { kitsFile.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Could not create kits.yml: " + e.getMessage());
                return;
            }
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        ConfigurationSection section = kitsConfig.getConfigurationSection("kits");
        if (section == null) return;

        for (String uuidStr : section.getKeys(false)) {
            UUID id;
            try { id = UUID.fromString(uuidStr); } catch (IllegalArgumentException e) { continue; }

            ConfigurationSection ks = section.getConfigurationSection(uuidStr);
            if (ks == null) continue;

            String rawName = ks.getString("name", "Unnamed Kit");
            Kit kit = new Kit(id, rawName);

            kit.setIconMaterial(Material.getMaterial(ks.getString("icon", "CHEST")));
            kit.setCooldown(ks.getLong("cooldown", 0));
            kit.setSingleClaim(ks.getBoolean("single-claim", false));

            List<String> lore = ks.getStringList("lore");
            kit.setLore(lore);

            List<?> rawItems = ks.getList("items", new ArrayList<>());
            List<ItemStack> items = new ArrayList<>();
            for (Object obj : rawItems) {
                if (obj instanceof ItemStack is) items.add(is);
            }
            kit.setItems(items);

            kits.put(id, kit);
        }
    }

    public void saveKits() {
        if (kitsConfig == null) kitsConfig = new YamlConfiguration();
        kitsConfig.set("kits", null); // wipe and rewrite

        for (Kit kit : kits.values()) {
            String path = "kits." + kit.getId().toString();
            kitsConfig.set(path + ".name", kit.getRawName());
            kitsConfig.set(path + ".icon", kit.getIconMaterial().name());
            kitsConfig.set(path + ".cooldown", kit.getCooldown());
            kitsConfig.set(path + ".single-claim", kit.isSingleClaim());
            kitsConfig.set(path + ".lore", kit.getLore());
            kitsConfig.set(path + ".items", kit.getItems());
        }

        try {
            kitsConfig.save(kitsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save kits.yml: " + e.getMessage());
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────

    public Kit createKit(String rawName) {
        Kit kit = new Kit(UUID.randomUUID(), rawName);
        kits.put(kit.getId(), kit);
        saveKits();
        refreshAllBrowsers();
        return kit;
    }

    public boolean deleteKit(String plainName) {
        Kit kit = getKitByPlainName(plainName);
        if (kit == null) return false;
        kits.remove(kit.getId());
        saveKits();
        refreshAllBrowsers();
        return true;
    }

    public boolean renameKit(String plainName, String newRawName) {
        Kit kit = getKitByPlainName(plainName);
        if (kit == null) return false;
        kit.setRawName(newRawName);
        saveKits();
        refreshAllBrowsers();
        return true;
    }

    public Kit cloneKit(String plainName, String newRawName) {
        Kit src = getKitByPlainName(plainName);
        if (src == null) return null;
        Kit clone = new Kit(UUID.randomUUID(), newRawName);
        clone.setLore(new ArrayList<>(src.getLore()));
        clone.setItems(new ArrayList<>(src.getItems()));
        kits.put(clone.getId(), clone);
        saveKits();
        refreshAllBrowsers();
        return clone;
    }

    // ── Lore ──────────────────────────────────────────────────────

    public boolean addLore(String plainName, String line) {
        Kit kit = getKitByPlainName(plainName);
        if (kit == null) return false;
        kit.addLoreLine(line);
        saveKits();
        return true;
    }

    public boolean editLore(String plainName, int lineIndex, String line) {
        Kit kit = getKitByPlainName(plainName);
        if (kit == null || lineIndex < 0 || lineIndex >= kit.getLore().size()) return false;
        kit.setLoreLine(lineIndex, line);
        saveKits();
        return true;
    }

    public boolean removeLore(String plainName, int lineIndex) {
        Kit kit = getKitByPlainName(plainName);
        if (kit == null || lineIndex < 0 || lineIndex >= kit.getLore().size()) return false;
        kit.removeLoreLine(lineIndex);
        saveKits();
        return true;
    }

    // ── Lookups ────────────────────────────────────────────────────

    public Kit getKitByPlainName(String plainName) {
        for (Kit k : kits.values()) {
            if (k.getPlainName().equalsIgnoreCase(plainName)) return k;
        }
        return null;
    }

    public boolean kitExists(String plainName) {
        return getKitByPlainName(plainName) != null;
    }

    public Collection<Kit> getAllKits() {
        return Collections.unmodifiableCollection(kits.values());
    }

    public List<String> getKitIds() {
        List<String> ids = new ArrayList<>();
        for (Kit k : kits.values()) {
            ids.add(k.getPlainName());
        }
        return ids;
    }

    public Kit getKitByName(String name) {
        return getKitByPlainName(name);
    }

    // ── GUI Refresh ────────────────────────────────────────────────

    /** Register an open browser GUI for a player so it gets refreshed on kit changes. */
    public void registerBrowser(Player player, AnimaKitsMainGUI gui) {
        openBrowsers.put(player.getUniqueId(), gui);
    }

    /** Remove a player's registered browser (called when they close it). */
    public void unregisterBrowser(Player player) {
        openBrowsers.remove(player.getUniqueId());
    }

    /** Public alias for use by editor GUIs that save externally. */
    public void refreshAllBrowsersSafe() { refreshAllBrowsers(); }

    /** Refresh every currently-open kit browser. Called after any kit mutation. */
    private void refreshAllBrowsers() {
        // Must run on the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Map.Entry<UUID, AnimaKitsMainGUI> entry : openBrowsers.entrySet()) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null && p.isOnline()) {
                    entry.getValue().refresh();
                }
            }
        });
    }
}
