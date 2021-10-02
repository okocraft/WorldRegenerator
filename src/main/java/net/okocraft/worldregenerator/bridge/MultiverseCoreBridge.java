package net.okocraft.worldregenerator.bridge;

import java.util.logging.Logger;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataType;

import net.okocraft.worldregenerator.WorldRegeneratorPlugin;
import net.okocraft.worldregenerator.bridge.ChunkyBridge.Shape;
import net.okocraft.worldregenerator.config.Config;

public class MultiverseCoreBridge {
    
    private final WorldRegeneratorPlugin plugin;
    private final Config mainConfig;
    private final Logger log;

    private final MultiverseCore multiverseCore;
    
    MultiverseCoreBridge(WorldRegeneratorPlugin plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getConfigManager().getMainConfig();
        this.log = plugin.getLogger();

        this.multiverseCore = MultiverseCore.getPlugin(MultiverseCore.class);
    }

    public void regenWorld(World world, long seed) {
        String worldName = world.getName();
        Location newSpawnPoint = mainConfig.getNewSpawnPoint(world);
        boolean isRegenerated = multiverseCore.getMVWorldManager().regenWorld(worldName, true, true, null, true);
        if (!isRegenerated) {
            log.warning("World " + worldName + " cannot be regenerated.");
            return;
        }

        MultiverseWorld mvWorld = multiverseCore.getMVWorldManager().getMVWorld(worldName);
        world = mvWorld.getCBWorld();
        if (newSpawnPoint != null) {
            newSpawnPoint.setWorld(world);
            mvWorld.setSpawnLocation(newSpawnPoint);
        }

        world.getSpawnLocation().getChunk().getPersistentDataContainer()
                .set(NamespacedKey.fromString("initdate", plugin), PersistentDataType.LONG, System.currentTimeMillis());

        WorldEditBridge worldEditBridge = plugin.getBridgeManager().getWorldeditBridge();
        if (worldEditBridge == null) {
            return;
        }

        ChunkyBridge chunkyBridge = plugin.getBridgeManager().getChunkyBridge();
        if (worldEditBridge.pasteBase(world) && chunkyBridge != null) {
            Shape shape = mainConfig.getFillShape(world);
            if (shape == null) {
                return;
            }
            plugin.getBridgeManager().getChunkyBridge().start(
                    shape,
                    mainConfig.getFillCenter(world),
                    mainConfig.getFillRadiusX(world),
                    mainConfig.getFillRadiusZ(world)
            );
        }
    }
}
