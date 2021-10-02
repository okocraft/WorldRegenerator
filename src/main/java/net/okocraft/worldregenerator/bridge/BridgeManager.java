package net.okocraft.worldregenerator.bridge;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.okocraft.worldregenerator.WorldRegeneratorPlugin;

public class BridgeManager {
    
    private final MultiverseCoreBridge multiverseCoreBridge;
    private final ChunkyBridge chunkyBridge;
    private final WorldEditBridge worldeditBridge;

    public BridgeManager(WorldRegeneratorPlugin plugin) {
        try {
            this.multiverseCoreBridge = new MultiverseCoreBridge(plugin);
        } catch (NoClassDefFoundError e) {
            throw new IllegalStateException("MultiverseCore is not loaded.", e);
        }

        WorldEditBridge tryWorldeditBridge = null;
        try {
            tryWorldeditBridge = new WorldEditBridge(plugin);
        } catch (NoClassDefFoundError ignored) {
        }
        this.worldeditBridge = tryWorldeditBridge;

        ChunkyBridge tryChunkyBridge = null;
        try {
            tryChunkyBridge = new ChunkyBridge(plugin);
        } catch (NoClassDefFoundError ignored) {
        }
        this.chunkyBridge = tryChunkyBridge;
    }

    @NotNull
    public MultiverseCoreBridge getMultiverseCoreBridge() {
        return multiverseCoreBridge;
    }
    
    @Nullable
    public ChunkyBridge getChunkyBridge() {
        return chunkyBridge;
    }

    @Nullable
    public WorldEditBridge getWorldeditBridge() {
        return worldeditBridge;
    }
}
