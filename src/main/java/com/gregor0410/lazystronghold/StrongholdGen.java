package com.gregor0410.lazystronghold;

import com.gregor0410.lazystronghold.mixin.StrongholdFeatureAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.logging.log4j.Level;

import java.util.concurrent.atomic.AtomicBoolean;

public class StrongholdGen implements Runnable {
    private final Thread thread;
    private final ChunkGenerator<?> generator;
    public BiomeSource biomeSource;
    public boolean started;
    public final AtomicBoolean completedSignal;
    public boolean shouldStop;

    public StrongholdGen(ChunkGenerator<?> generator) {
        this.started = false;
        this.shouldStop = false;
        this.completedSignal = new AtomicBoolean(false);
        this.thread = new Thread(this, "Stronghold thread");
        this.generator = generator;
    }

    public void start() {
        this.started = true;
        this.biomeSource = ((IBiomeSource) this.generator.getBiomeSource()).lazyStronghold$copy(); //create new biome source instance for thread safety
        this.thread.start();
    }

    public void stop() {
        this.shouldStop = true;
    }

    @Override
    public void run() {
        Lazystronghold.log(Level.INFO, "Started stronghold gen thread");
        ((StrongholdFeatureAccess) StructureFeature.STRONGHOLD).invokeInvalidateState();
        ((StrongholdFeatureAccess) StructureFeature.STRONGHOLD).invokeInitialize(this.generator);
        if (this.shouldStop) {
            Lazystronghold.log(Level.INFO, "Stronghold thread stopped early");
        } else {
            Lazystronghold.log(Level.INFO, "Generated strongholds.");
        }
        synchronized (completedSignal) {
            completedSignal.set(true);
            completedSignal.notifyAll();
        }
        this.biomeSource = null;
    }
}
