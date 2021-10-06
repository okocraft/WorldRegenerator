package net.okocraft.worldregenerator.bridge;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.BukkitSender;
import org.popcraft.chunky.platform.BukkitWorld;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.Limit;

import net.okocraft.worldregenerator.WorldRegeneratorPlugin;
import net.okocraft.worldregenerator.config.Config;

import static org.popcraft.chunky.util.Translator.translate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;

public class ChunkyBridge {

    private final WorldRegeneratorPlugin plugin;
    private final Chunky chunky;

    private final Map<World, GenerationTask> generationTasks = new HashMap<>();

    ChunkyBridge(WorldRegeneratorPlugin plugin) {
        this.plugin = plugin;
        this.chunky = ChunkyBukkit.getPlugin(ChunkyBukkit.class).getChunky();
        generationTasks.putAll(chunky.getGenerationTasks());
        
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (generationTasks.size() != chunky.getGenerationTasks().size()) {
                generationTasks.keySet().removeAll(chunky.getGenerationTasks().keySet());

                for (World tempWorld : generationTasks.keySet()) {
                    afterFill(plugin.getServer().getWorld(tempWorld.getName().replaceAll("__(\\w+)_temp__", "$1")), tempWorld.getName());
                }

                generationTasks.clear();
                generationTasks.putAll(chunky.getGenerationTasks());
            }
        }, 1L, 60L);
    }

    public void start(Shape shape, Location center, int radiusX, int radiusZ) {
        final Sender sender = new BukkitSender(plugin.getServer().getConsoleSender());
        final Selection current = chunky.getSelection()
                .world(new BukkitWorld(center.getWorld()))
                .shape(shape.name().toLowerCase(Locale.ROOT))
                .center(center.getX(), center.getZ())
                .radiusX(radiusX)
                .radiusZ(radiusZ)
                .build();
        if (chunky.getGenerationTasks().containsKey(current.world())) {
            sender.sendMessagePrefixed("format_started_already", current.world().getName());
            return;
        }
        if (current.radiusX() > Limit.get()) {
            sender.sendMessagePrefixed("format_start_limit", Formatting.number(Limit.get()));
            return;
        }
        final Runnable startAction = () -> {
            GenerationTask generationTask = new GenerationTask(chunky, current);
            chunky.getGenerationTasks().put(current.world(), generationTask);
            chunky.getPlatform().getServer().getScheduler().runTaskAsync(generationTask);
            sender.sendMessagePrefixed("format_start", current.world().getName(), translate("shape_" + current.shape()), Formatting.number(current.centerX()), Formatting.number(current.centerZ()), Formatting.radius(current));
        };
        if (chunky.getConfig().loadTask(current.world()).isPresent()) {
            chunky.setPendingAction(sender, startAction);
            sender.sendMessagePrefixed("format_start_confirm", "/chunky continue", "/chunky confirm");
        } else {
            startAction.run();
        }
    }

    public void afterFill(org.bukkit.World world, String tempName) {
        Config config = plugin.getConfigManager().getMainConfig();
        MultiverseCoreBridge mv = plugin.getBridgeManager().getMultiverseCoreBridge();
        String worldName = world.getName();
        
        // ベース建築のスケマティクを設置
        plugin.getBridgeManager().getWorldeditBridge().pasteBase(world);

        // 指定した生成後コマンドを実行
        for (String command : config.getFillAfterCommands(world)) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        }

        // 元のワールドのzip化
        if (config.shouldAutoRegenerationArchive(world)) {
            mv.zipWorld(worldName);
        }

        // 元のワールドの削除、新しいワールドへの置き換え
        if (config.isAutoRegenerationNewUid(world)) {
            mv.renameWorld(tempName, worldName);
        } else {
            try {
                Path uidFile = plugin.getServer().getWorld(worldName).getWorldFolder().toPath().resolve("uid.dat");
                Path copyUidFile = plugin.getServer().getWorld(tempName).getWorldFolder().toPath().resolve("uid.dat_copy");
                Files.copy(uidFile, copyUidFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.COPY_ATTRIBUTES,
                        StandardCopyOption.REPLACE_EXISTING);
                mv.renameWorld(tempName, worldName);
                mv.unloadWorld(worldName);
                Files.copy(copyUidFile, uidFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.COPY_ATTRIBUTES,
                        StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(copyUidFile);
                mv.loadWorld(tempName);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        // 新しいワールドにワールドボーダーを設定
        world = plugin.getServer().getWorld(worldName);
        Entry<Integer, Integer> center = config.getWorldBoaderCenter(world);
        world.getWorldBorder().setCenter(center.getKey(), center.getValue());
        world.getWorldBorder().setSize(config.getWorldBoaderRadius(world));
    }

    public enum Shape {
        CIRCLE,
        DIAMOND,
        OVAL,
        ELLIPSE,
        PENTAGON,
        RECTANGLE,
        STAR,
        TRIANGLE,
        SQUARE;
    }
}
