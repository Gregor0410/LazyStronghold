package com.gregor0410.lazystronghold.mixin;

import com.gregor0410.lazystronghold.ChunkGeneratorInterface;
import com.gregor0410.lazystronghold.StrongholdGen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow @Final private Map<RegistryKey<World>, ServerWorld> worlds;

    @Shadow private int ticks;

    @Shadow @Final protected SaveProperties saveProperties;
    private boolean isNewWorld;

    @Inject(method ="shutdown",at=@At("HEAD"))
    private void stopStrongholdThreads(CallbackInfo ci){
        this.worlds.values().forEach(world->{
            ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
            StrongholdGen strongholdGen = ((ChunkGeneratorInterface)chunkGenerator).getStrongholdGen();
            if(strongholdGen!=null){
                strongholdGen.stop();
            }
        });
    }
    @Inject(method="prepareStartRegion",at=@At("HEAD"))
    private void startStrongholdThreadIfNotNewWorld(CallbackInfo ci){
        if(!this.isNewWorld){
            this.worlds.values().forEach(world->{
                StrongholdGen strongholdGen = ((ChunkGeneratorInterface) world.getChunkManager().getChunkGenerator()).getStrongholdGen();
                if (strongholdGen != null) {
                    if (!strongholdGen.started) {
                        strongholdGen.start();
                    }
                }
            });
        }
    }

    @Inject(method="prepareStartRegion",at=@At("TAIL"))
    private void waitForStrongholdThreadIfNotNewWorld(CallbackInfo ci){
        if(!this.isNewWorld){
            this.worlds.values().forEach(world->{
                StrongholdGen strongholdGen = ((ChunkGeneratorInterface) world.getChunkManager().getChunkGenerator()).getStrongholdGen();
                if (strongholdGen != null) {
                    if (!strongholdGen.started) {
                        strongholdGen.start();
                    }
                    synchronized (strongholdGen.completedSignal) {
                        while (!strongholdGen.completedSignal.get()) {
                            try {
                                strongholdGen.completedSignal.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

    @Inject(method="tick",at=@At("HEAD"))
    private void startStrongholdThread(CallbackInfo ci){
        //start the thread after 20 ticks so instances paused on world load by reset macros don't have thread started until the player unpauses
        if(this.ticks==20){
            this.worlds.values().forEach(world->{
                StrongholdGen strongholdGen = ((ChunkGeneratorInterface) world.getChunkManager().getChunkGenerator()).getStrongholdGen();
                if (strongholdGen != null) {
                    if (!strongholdGen.started) {
                        strongholdGen.start();
                    }
                }
            });
        }
    }
    @Inject(method="createWorlds",at=@At("HEAD"))
    private void checkIfNewWorld(CallbackInfo ci){
        this.isNewWorld = !this.saveProperties.getMainWorldProperties().isInitialized();
    }
}
