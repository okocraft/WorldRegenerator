package net.okocraft.worldregenerator.config;

import net.okocraft.worldregenerator.WorldRegeneratorPlugin;

public class ConfigManager {
    
    private final Config mainConfig;
    private final Messages messages;

    public ConfigManager(WorldRegeneratorPlugin plugin) {
        this.mainConfig = new Config(plugin);
        this.messages = new Messages(plugin);
    }

    public Config getMainConfig() {
        return mainConfig;
    }

    public Messages getMessages() {
        return messages;
    }

    public void reloadAll() {
        mainConfig.reload();
    }

    public void saveDefaultAll() {
        mainConfig.saveDefault();
    }
    

}
