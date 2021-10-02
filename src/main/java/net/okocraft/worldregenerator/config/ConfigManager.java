package net.okocraft.worldregenerator.config;

import net.okocraft.worldregenerator.WorldRegeneratorPlugin;

public class ConfigManager {
    
    private final Config mainConfig;

    public ConfigManager(WorldRegeneratorPlugin plugin) {
        this.mainConfig = new Config(plugin);
    }

    public Config getMainConfig() {
        return mainConfig;
    }

    public void reloadAll() {
        mainConfig.reload();
    }

    public void saveDefaultAll() {
        mainConfig.saveDefault();
    }
    

}
