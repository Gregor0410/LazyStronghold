package com.gregor0410.lazystronghold.mixin;

import com.gregor0410.lazystronghold.ChunkGeneratorInterface;
import com.gregor0410.lazystronghold.StrongholdGen;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin implements ChunkGeneratorInterface {
    @Shadow @Final private StructuresConfig structuresConfig;
    @Shadow @Final private long seed;
    @Mutable
    @Shadow @Final private List<ChunkPos> strongholdPositions;
    private StrongholdGen strongholdGen = null;
    private final List<ChunkPos> strongholds = new CopyOnWriteArrayList<>();
    private static final double ROOT_2 = Math.sqrt(2);
    private static final int PADDING = 10;


    @Inject(method="<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/gen/chunk/StructuresConfig;J)V",at=@At("TAIL"))
    private void init(CallbackInfo ci){
        this.strongholdPositions=this.strongholds;
        if(this.structuresConfig.getStronghold()!=null){
            if(this.structuresConfig.getStronghold().getCount()>0){
                this.strongholdGen = new StrongholdGen((ChunkGenerator) (Object) this,this.seed,this.strongholds);
            }
        }
    }


    private int minSquaredDistance(){
        double d = 2.75 * this.structuresConfig.getStronghold().getDistance() * 16 - 128 * ROOT_2;
        if(d <0) return 0;
        return (int) Math.pow((int) d >>4,2);
    }
    private int minSquaredDistanceWithPadding(){
        double d = 2.75 * this.structuresConfig.getStronghold().getDistance() * 16 - 128 * ROOT_2;
        if(d - PADDING * 16 <0) return 0;
        return (int) Math.pow(((int) d >>4)-PADDING,2);
    }

    @Inject(method="isStrongholdAt",at=@At("HEAD"))
    private void waitForStrongholds(ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir) throws InterruptedException {
        if(this.strongholdGen!=null){
            int squaredDistance = (chunkPos.x * chunkPos.x) + (chunkPos.z * chunkPos.z);
            if(squaredDistance >=minSquaredDistanceWithPadding()) {
                if (!strongholdGen.started) strongholdGen.start();
                if(squaredDistance>=minSquaredDistance()){
                    synchronized (strongholdGen.completedSignal){
                        while(!strongholdGen.completedSignal.get()){
                            strongholdGen.completedSignal.wait();
                        }
                    }
                }
            }
        }
    }

    @Redirect(method="locateStructure",at=@At(value="INVOKE",target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;generateStrongholdPositions()V"))
    private void waitForStrongholds2(ChunkGenerator instance) throws InterruptedException {
        if(this.strongholdGen!=null){
            if(!strongholdGen.started)strongholdGen.start();
            synchronized (strongholdGen.completedSignal){
                while(!strongholdGen.completedSignal.get()){
                    strongholdGen.completedSignal.wait();
                }
            }
        }
    }


    @Redirect(method="generateStrongholdPositions",at=@At(value="FIELD",target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;biomeSource:Lnet/minecraft/world/biome/source/BiomeSource;",opcode = Opcodes.GETFIELD))
    private BiomeSource getBiomeSource(ChunkGenerator instance){
        return this.strongholdGen.biomeSource;
    }
    @Inject(method = "generateStrongholdPositions",at=@At(value="JUMP",ordinal = 6), cancellable = true)
    private void stopGenOnLeave(CallbackInfo ci){
        if(this.strongholdGen!=null){
            if(this.strongholdGen.shouldStop){
                ci.cancel();
            }
        }
    }
    @Redirect(method = "isStrongholdAt",at=@At(value = "INVOKE",target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;generateStrongholdPositions()V"))
    private void cancelStrongholdGen(ChunkGenerator instance){
    }

    @Override
    public StrongholdGen getStrongholdGen() {
        return this.strongholdGen;
    }

}
