package com.worldofnormies.animakits.suffix;

import com.worldofnormies.animakits.AnimaKitsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SuffixManager {
    private final AnimaKitsPlugin plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<String, String> templates = new LinkedHashMap<>();

    public SuffixManager(AnimaKitsPlugin plugin) {
        this.plugin = plugin;
        File folder = new File(plugin.getDataFolder(), "suffixes");
        if (!folder.exists()) folder.mkdirs();
        this.file = new File(folder, "suffixes.yml");
    }

    public void load() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        config = YamlConfiguration.loadConfiguration(file);
        templates.clear();
        ConfigurationSection sec = config.getConfigurationSection("templates");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                templates.put(key, sec.getString(key));
            }
        }
    }

    public void addTemplate(String id, String suffix) {
        templates.put(id, suffix);
        config.set("templates." + id, suffix);
        try { config.save(file); } catch (IOException ignored) {}
    }

    public Map<String, String> getTemplates() {
        return Collections.unmodifiableMap(templates);
    }
}
