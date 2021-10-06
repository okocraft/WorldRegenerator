package net.okocraft.worldregenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.quartz.SchedulerException;

import net.okocraft.worldregenerator.bridge.BridgeManager;
import net.okocraft.worldregenerator.config.ConfigManager;
import net.okocraft.worldregenerator.scheduler.AutoRegenerationScheduler;

public class WorldRegeneratorPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private BridgeManager bridgeManager;

    private Map<String, AutoRegenerationScheduler> autoRegenSchedulers = new HashMap<>();
    
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

        for (World world : getServer().getWorlds()) {
            try {
                AutoRegenerationScheduler autoRegenScheduler = new AutoRegenerationScheduler(this, world.getName());
                autoRegenSchedulers.put(world.getName(), autoRegenScheduler);
            } catch (SchedulerException e) {
                getLogger().log(Level.WARNING, "Cannot initialize auto-regeneration scheduler for world " + world.getName(), e);
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
