package com.gregor0410.lazystronghold;

import com.gregor0410.lazystronghold.mixin.StrongholdFeatureAccess;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class StrongholdGen implements Runnable {
    private final Thread thread;
    private final ChunkGenerator generator;
    public final BiomeSource biomeSource;
    private final long seed;
    private ArrayList<Biome> list;
    public CopyOnWriteArrayList<ChunkPos> strongholds;
    public boolean started;
    public final AtomicBoolean completedSignal;
    public boolean shouldStop;
    public int count;


    public StrongholdGen(ChunkGenerator<?> generator, long seed, CopyOnWriteArrayList<ChunkPos> strongholds){
        this.started=false;
        this.shouldStop = false;
        this.completedSignal = new AtomicBoolean(false);
        this.seed = seed;
        this.biomeSource = ((IVanillaLayeredBiomeSource)generator.getBiomeSource()).copy(); //create new biome source instance for thread safety
        this.thread = new Thread(this,"Stronghold thread");
        this.count = generator.getConfig().getStrongholdCount();
        this.strongholds = strongholds;
        this.generator = generator;
    }
    public void start(){
        this.started = true;
        this.thread.start();
    }
    public void stop(){
        this.shouldStop=true;
    }
    @Override
    public void run() {
        Lazystronghold.log(Level.INFO,"Started stronghold gen thread");
        ((StrongholdFeatureAccess)StructureFeature.STRONGHOLD).invokeInvalidateState();
        ((StrongholdFeatureAccess)StructureFeature.STRONGHOLD).invokeInitialize(this.generator);
        ((StrongholdFeatureAccess)StructureFeature.STRONGHOLD).setStartPositions(this.strongholds.toArray(new ChunkPos[count]));
        if(this.shouldStop){
            Lazystronghold.log(Level.INFO,"Stronghold thread stopped early");
        }else {
            if (this.strongholds.size() != this.count) {
                Lazystronghold.log(Level.ERROR, "Only " + this.strongholds.size() + " strongholds generated!");
            } else {
                Lazystronghold.log(Level.INFO, "Generated " + this.count + " strongholds.");
            }
        }
        synchronized (completedSignal){
            completedSignal.set(true);
            completedSignal.notifyAll();
        }
    }
}
