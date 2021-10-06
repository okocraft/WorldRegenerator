package net.okocraft.worldregenerator.config;

import net.md_5.bungee.api.ChatColor;
import net.okocraft.worldregenerator.WorldRegeneratorPlugin;

public class Messages extends CustomConfig {

    public Messages(WorldRegeneratorPlugin plugin) {
        super(plugin, "messages.yml");
    }

    public String startingRegen(String worldName) {
        return colorize(get().getString("starting-regen").replaceAll("%world%", worldName));
    }

    private static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
}
