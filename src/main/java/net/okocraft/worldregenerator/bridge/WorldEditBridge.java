package net.okocraft.worldregenerator.bridge;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import net.okocraft.worldregenerator.WorldRegeneratorPlugin;
import net.okocraft.worldregenerator.config.Config;

public class WorldEditBridge {
    
    private final WorldRegeneratorPlugin plugin;
    private final Config mainConfig;
    private final Logger log;
    
    WorldEditBridge(WorldRegeneratorPlugin plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getConfigManager().getMainConfig();
        this.log = plugin.getLogger();
    }

    public boolean pasteBase(@NotNull World world) {
        String baseSchemName = mainConfig.getBaseBuildingSchem(world);
        if (baseSchemName == null) {
            log.warning("Schematic for base building of " + world.getName() + " is not defined in config.");
            return false;
        }

        Location location = mainConfig.getBaseBuildingLocation(world);
        if (location == null) {
            log.warning("Location for base building of " + world.getName() + " is not defined correctly.");
            return false;
        }

        NamespacedKey basePastedKey = NamespacedKey.fromString("basepasted", plugin);
        PersistentDataContainer dataContainer = location.getChunk().getPersistentDataContainer();
        if (dataContainer.getOrDefault(basePastedKey, PersistentDataType.BYTE, (byte) 0) == (byte) 1) {
            return false;
        }

        if (!pasteSchem(location, baseSchemName, plugin.getConfigManager().getMainConfig().shouldIgnoreAir(world))) {
            return false;
        }

        dataContainer.set(basePastedKey, PersistentDataType.BYTE, (byte) 1);
        return true;
    }

    public boolean pasteSchem(Location center, String schemName, boolean ignoreAir) {
        Path pluginsFolder = plugin.getDataFolder().toPath().getParent();
        Path schemFile;
        Optional<Path> optionalSchemFile = getSchemFile(pluginsFolder.resolve("FastAsyncWorldEdit").resolve("schematics"), schemName);
        if (optionalSchemFile.isPresent()) {
            schemFile = optionalSchemFile.get();
        } else {
            optionalSchemFile = getSchemFile(pluginsFolder.resolve("WorldEdit").resolve("schematics"), schemName);
            if (optionalSchemFile.isPresent()) {
                schemFile = optionalSchemFile.get();
            } else {
                return false;
            }
        }
        
        Clipboard clipboard;
        try {
            clipboard = ClipboardFormats.findByFile(schemFile.toFile()).getReader(new FileInputStream(schemFile.toFile())).read();
        } catch (IOException e) {
            log.log(Level.WARNING, "Cannot load " + schemName, e);
            return false;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BukkitAdapter.adapt(center).toVector().toBlockPoint())
                    .copyBiomes(false)
                    .copyEntities(true)
                    .ignoreAirBlocks(ignoreAir)
                    .build();
            Operations.complete(operation);
            return true;
        } catch (WorldEditException e) {
            log.log(Level.WARNING, schemName + " found but cannot be pasted due to exception.", e);
            return false;
        }
    }

    public Optional<Path> getSchemFile(Path root, String schemFileName) {
        String schemName;
        if (schemFileName.endsWith(".schem") || schemFileName.endsWith(".schematic")) {
            schemName = schemFileName.substring(0, schemFileName.lastIndexOf("."));
        } else {
            schemName = schemFileName;
        }

        try {
            return Files.find(root, 5, (path, attribute) -> path.toFile().getName().startsWith(schemName), FileVisitOption.FOLLOW_LINKS).findAny();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
