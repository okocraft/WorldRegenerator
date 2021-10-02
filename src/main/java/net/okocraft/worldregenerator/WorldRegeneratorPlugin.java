package net.okocraft.worldregenerator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.okocraft.worldregenerator.bridge.BridgeManager;
import net.okocraft.worldregenerator.config.Config;
import net.okocraft.worldregenerator.config.ConfigManager;

public class WorldRegeneratorPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private BridgeManager bridgeManager;
    
    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        configManager.saveDefaultAll();

        try {
            this.bridgeManager = new BridgeManager(this);
        } catch (IllegalStateException e) {
            getLogger().severe("Hard depend plugin is not available. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Config mainConfig = configManager.getMainConfig();
        Random random = new Random();

        // Trigger regen and Print next regen date.
        for (World world : getServer().getWorlds()) {
            if (mainConfig.shouldPrintAutoRegenerationDate(world)) {
                int interval = mainConfig.getAutoRegenerationInterval(world);
                long prevRegen = world.getSpawnLocation().getChunk().getPersistentDataContainer().getOrDefault(NamespacedKey.fromString("initdate", this), PersistentDataType.LONG, -1L);
                long nextRegen = prevRegen + (interval * 24 * 60 * 60 * 1000);
                if (System.currentTimeMillis() > nextRegen) {
                    bridgeManager.getMultiverseCoreBridge().regenWorld(world, random.nextLong());
                } else {
                    getLogger().info("Next regen date for " + world.getName() + " is " + LocalDateTime.ofInstant(Instant.ofEpochMilli(nextRegen), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE));
                }
            }
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BridgeManager getBridgeManager() {
        return bridgeManager;
    }

    /**
     * Equivalent to {@code getConfigManager().getMainConfig().get()}
     */
    @Override
    public FileConfiguration getConfig() {
        return configManager.getMainConfig().get();
    }

    /**
     * Equivalent to {@code getConfigManager().getMainConfig().reload()}
     */
    @Override
    public void reloadConfig() {
        configManager.getMainConfig().reload();
    }
}
