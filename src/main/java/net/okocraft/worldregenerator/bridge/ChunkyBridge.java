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

import static org.popcraft.chunky.util.Translator.translate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

                for (World world : generationTasks.keySet()) {
                    if (plugin.getConfigManager().getMainConfig().shouldFullRenderOnComplete(plugin.getServer().getWorld(world.getUUID()))) {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "dynmap fullrender " + world.getName());
                    }
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
