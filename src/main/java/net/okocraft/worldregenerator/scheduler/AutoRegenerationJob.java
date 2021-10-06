package net.okocraft.worldregenerator.scheduler;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.okocraft.worldregenerator.WorldRegeneratorPlugin;
import net.okocraft.worldregenerator.bridge.ChunkyBridge;
import net.okocraft.worldregenerator.bridge.MultiverseCoreBridge;
import net.okocraft.worldregenerator.bridge.ChunkyBridge.Shape;
import net.okocraft.worldregenerator.config.Config;

public class AutoRegenerationJob implements Job {

    private static NamespacedKey previousRegenkey = null;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        if (data == null || !(data.get("owning-plugin") instanceof WorldRegeneratorPlugin plugin)) {
            return;
        }

        if (previousRegenkey == null) {
            AutoRegenerationJob.previousRegenkey = NamespacedKey.fromString("previous_regen", plugin);
        }

        String worldName = context.getJobDetail().getJobDataMap().getString("target-world");
        if (worldName == null) {
            return;
        }
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            return;
        }

        // 前回の再生成日時を確認して、一日以内だったらキャンセルする
        Chunk spawnChunk = world.getSpawnLocation().getChunk();
        long previousRegenTime = spawnChunk.getPersistentDataContainer().getOrDefault(previousRegenkey, PersistentDataType.LONG, 0L);
        if (System.currentTimeMillis() - previousRegenTime < 1000 * 60 * 60 * 24L) {
            plugin.getLogger().warning("The world " + worldName + " tried to regen twice in a day. Cancelled.");
            return;
        }
        
        // ワールドの再生成を行う警告をプレイヤー全員に送信
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(plugin.getConfigManager().getMessages().startingRegen(worldName));
        }

        MultiverseCoreBridge mv = plugin.getBridgeManager().getMultiverseCoreBridge();
        
        // 元のワールドを複製->新しい一時ワールド
        String tempName = "__" + worldName + "_temp__";
        mv.cloneWorld(worldName, tempName);

        // 新しい一時ワールドのシードを変更
        Config config = plugin.getConfigManager().getMainConfig();
        if (config.isAutoRegenerationNewSeed(world)) {
            mv.regenWorld(tempName, new Random().nextLong(), true);
        } else {
            mv.regenWorld(tempName, true);
        }
        
        // 新しい一時ワールドのスポーン地点を変更
        Location spawn = config.getNewSpawnPoint(world);
        if (spawn != null) {
            mv.setSpawn(tempName, spawn);
        }
        
        // 指定した生成前コマンドを実行
        for (String command : config.getFillBeforeCommands(world)) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        }

        // 新しい一時ワールドを全生成
        ChunkyBridge chunkyBridge = plugin.getBridgeManager().getChunkyBridge();
        if (chunkyBridge != null) {
            Shape shape = config.getFillShape(world);
            if (shape == null) {
                return;
            }
            chunkyBridge.start(
                    shape,
                    config.getFillCenter(world),
                    config.getFillRadiusX(world),
                    config.getFillRadiusZ(world)
            );
        }
    }
    
}
