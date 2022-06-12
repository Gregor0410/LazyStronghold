package com.gregor0410.lazystronghold;

import com.gregor0410.lazystronghold.mixin.ChunkGeneratorAccess;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StrongholdConfig;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class StrongholdGen implements Runnable {
    public final StrongholdConfig config;
    private final Thread thread;
    private final ChunkGenerator generator;
    public final BiomeSource biomeSource;
    private final long seed;
    private ArrayList<Biome> list;
    public List<ChunkPos> strongholds;
    public boolean started;
    public final AtomicBoolean completedSignal;
    public boolean shouldStop;


    public StrongholdGen(ChunkGenerator generator, long seed,List<ChunkPos> strongholds){
        this.started=false;
        this.shouldStop = false;
        this.completedSignal = new AtomicBoolean(false);
        this.seed = seed;
        this.biomeSource = ((ChunkGeneratorAccess)generator).getBiomeSource1().withSeed(seed); //create new biome source instance for thread safety
        this.thread = new Thread(this,"Stronghold thread");
        this.config = generator.getConfig().getStronghold();
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
        ((ChunkGeneratorAccess)this.generator).invokeMethod_28509();
        if(this.shouldStop){
            Lazystronghold.log(Level.INFO,"Stronghold thread stopped early");
        }else {
            if (this.strongholds.size() != this.config.getCount()) {
                Lazystronghold.log(Level.ERROR, "Only " + this.strongholds.size() + " strongholds generated!");
            } else {
                Lazystronghold.log(Level.INFO, "Generated " + this.config.getCount() + " strongholds.");
            }
        }
        synchronized (completedSignal){
            completedSignal.set(true);
            completedSignal.notify();
        }
    }
}
