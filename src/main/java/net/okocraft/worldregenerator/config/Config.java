package net.okocraft.worldregenerator.config;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.okocraft.worldregenerator.bridge.ChunkyBridge.Shape;

public class Config extends CustomConfig {

    public Config(Plugin plugin) {
        super(plugin, "config.yml");
    }

    public String getAutoRegenerationSchedule(World world) {
        return get().getString("world-settings." + world.getName() + ".auto-regeneration.schedule", "0 0 20 1 * ?");
    }

    public boolean isAutoRegenerationNewUid(World world) {
        return get().getBoolean("world-settings." + world.getName() + ".auto-regeneration.new-uid", true);
    }

    public boolean isAutoRegenerationNewSeed(World world) {
        return get().getBoolean("world-settings." + world.getName() + ".auto-regeneration.new-seed", true);
    }

    public boolean shouldAutoRegenerationArchive(World world) {
        return get().getBoolean("world-settings." + world.getName() + ".auto-regeneration.archive-old-world", true);
    }

    @Nullable
    public Location getNewSpawnPoint(World world) {
        String value = get().getString("world-settings." + world.getName() + ".auto-regeneration.new-spawn-point");
        if (value == null) {
            return null;
        }
        if (value.equalsIgnoreCase("KEEP")) {
            Location spawn = world.getSpawnLocation();
            spawn.setWorld(null);
            return spawn;
        }

        try {
            String[] valueSplit = value.split(",", -1);
            return new Location(
                    null,
                    Double.parseDouble(valueSplit[0]),
                    Double.parseDouble(valueSplit[1]),
                    Double.parseDouble(valueSplit[2])
            );
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private Location getLocation(String path, World world) {
        String locationStr = get().getString(path, null);
        if (locationStr == null) {
            return null;
        }
    
        if (locationStr.equalsIgnoreCase("SPAWN_POINT")) {
            return world.getSpawnLocation();
        }
        try {
            String[] locationStrSplit = locationStr.split(",", -1);
            return new Location(
                    world,
                    Double.parseDouble(locationStrSplit[0]),
                    Double.parseDouble(locationStrSplit[1]),
                    Double.parseDouble(locationStrSplit[2])
            );
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
    
    @Nullable
    public String getBaseBuildingSchem(@NotNull World world) {
        return get().getString("world-settings." + world.getName() + ".base-building.schem", null);
    }
    
    public boolean shouldIgnoreAir(@NotNull World world) {
        return get().getBoolean("world-settings." + world.getName() + ".base-building.ignore-air", false);
    }

    @Nullable
    public Location getBaseBuildingLocation(@NotNull World world) {
        return getLocation("world-settings." + world.getName() + ".base-building.location", world);
    }

    public List<String> getFillBeforeCommands(World world) {
        return get().getStringList("world-settings." + world.getName() + ".fill-on-world-creation.before-commands");
    }

    public List<String> getFillAfterCommands(World world) {
        return get().getStringList("world-settings." + world.getName() + ".fill-on-world-creation.after-commands");
    }

    @Nullable
    public Shape getFillShape(World world) {
        try {
            return Shape.valueOf(get().getString("world-settings." + world.getName() + ".fill-on-world-creation.shape", ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Location getFillCenter(World world) {
        Location loc = getLocation("world-settings." + world.getName() + ".fill-on-world-creation.center", world);
        if (loc == null) {
            return world.getSpawnLocation();
        } else {
            return loc;
        }
    }

    public int getFillRadiusX(World world) {
        return get().getInt("world-settings." + world.getName() + ".fill-on-world-creation.radius-x", 3000);
    }

    public int getFillRadiusZ(World world) {
        return get().getInt("world-settings." + world.getName() + ".fill-on-world-creation.radius-z", 3000);
    }

    public Entry<Integer, Integer> getWorldBoaderCenter(World world) {
        String[] centerStrSplit = get().getString("world-settings." + world.getName() + ".set-world-boarder-after-fill.center", "SPAWN_POINT").split(",", -1);
        if (centerStrSplit.length >= 2) {
            try {
                return Map.entry(
                        Double.valueOf(centerStrSplit[0]).intValue(),
                        Double.valueOf(centerStrSplit[1]).intValue()
                );
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            }
        }
        return Map.entry(
                world.getSpawnLocation().getBlockX(),
                world.getSpawnLocation().getBlockX()
        );
    }

    public int getWorldBoaderRadius(World world) {
        return get().getInt("world-settings." + world.getName() + ".set-world-boarder-after-fill.radius", 6000);
    }

    public boolean shouldFullRenderOnComplete(World world) {
        return get().getBoolean("world-settings." + world.getName() + ".fullrender-after-fill");
    }
}
