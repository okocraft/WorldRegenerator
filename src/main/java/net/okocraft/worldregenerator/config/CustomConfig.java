package net.okocraft.worldregenerator.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class CustomConfig {

    @NotNull
    protected final Plugin plugin;
    @NotNull
    private final String fileName;
    @NotNull
    private final File file;
    private FileConfiguration config;

    public CustomConfig(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.file = new File(plugin.getDataFolder(), fileName);
    }

    public void reload() {
        saveDefault();
        config = YamlConfiguration.loadConfiguration(file);
        InputStream inputStream = plugin.getResource(fileName);
        if (inputStream != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
        }
    }

    public FileConfiguration get() {
        if (config == null) {
            reload();
        }

        return config;
    }

    public void save() {
        if (config != null && file != null) {
            try {
                get().save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + file, e);
            }

        }
    }

    public void saveDefault() {
        if (!file.exists() || file.isDirectory()) {
            plugin.saveResource(fileName, false);
        }
    }

    public void copyDefaults(boolean copyDefaults) {
        get().options().copyDefaults(copyDefaults);
    }
}
