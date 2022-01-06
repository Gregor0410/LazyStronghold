package com.gregor0410.lazystronghold.mixin;

import com.gregor0410.lazystronghold.ChunkGeneratorInterface;
import com.gregor0410.lazystronghold.StrongholdGen;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin implements ChunkGeneratorInterface {
    @Shadow @Final private StructuresConfig config;
    private StrongholdGen strongholdGen = null;
    private final List<ChunkPos> strongholds = new CopyOnWriteArrayList<>();
    private static final double ROOT_2 = Math.sqrt(2);
    private static final int PADDING = 10;
    private final AtomicBoolean strongholdsCompletedSignal = new AtomicBoolean(false);


    @Inject(method="<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/gen/chunk/StructuresConfig;J)V",at=@At("TAIL"))
    private void init(CallbackInfo ci){
        if(this.config.getStronghold()!=null){
            if(this.config.getStronghold().getCount()>0){
                this.strongholdGen = new StrongholdGen((ChunkGenerator) (Object) this,strongholdsCompletedSignal);
            }
        }
    }


    private int minSquaredDistance(){
        if(2.75*this.config.getStronghold().getDistance()*16-128*ROOT_2<0) return 0;
        return (int) Math.pow((int)(2.75*this.config.getStronghold().getDistance()*16-128*ROOT_2)>>4,2);
    }
    private int minSquaredDistanceWithPadding(){
        if(2.75*this.config.getStronghold().getDistance()*16-128*ROOT_2-PADDING*16<0) return 0;
        return (int) Math.pow(((int)(2.75*this.config.getStronghold().getDistance()*16-128*ROOT_2)>>4)-PADDING,2);
    }

    @Inject(method="method_28507",at=@At("HEAD"))
    private void waitForStrongholds(ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir) throws InterruptedException {
        if(this.strongholdGen!=null){
            int squaredDistance = (chunkPos.x * chunkPos.x) + (chunkPos.z * chunkPos.z);
            if(squaredDistance >=minSquaredDistanceWithPadding()) {
                if (!strongholdGen.started) strongholdGen.start();
                if(squaredDistance>=minSquaredDistance()){
                    synchronized (strongholdsCompletedSignal){
                        while(!strongholdsCompletedSignal.get()){
                            strongholdsCompletedSignal.wait();
                        }
                    }
                }
            }
        }
    }

    @Redirect(method="locateStructure",at=@At(value="INVOKE",target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;method_28509()V"))
    private void waitForStrongholds2(ChunkGenerator instance) throws InterruptedException {
        if(this.strongholdGen!=null){
            if(!strongholdGen.started)strongholdGen.start();
            synchronized (strongholdsCompletedSignal){
                while(!strongholdsCompletedSignal.get()){
                    strongholdsCompletedSignal.wait();
                }
            }
        }
    }


    @Inject(method="method_28509",at=@At("HEAD"),cancellable = true)
    private void genStrongholds(CallbackInfo ci){
        ci.cancel();
    }
    @Redirect(method="<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/gen/chunk/StructuresConfig;J)V",at=@At(value="FIELD",target="Lnet/minecraft/world/gen/chunk/ChunkGenerator;field_24749:Ljava/util/List;",opcode = Opcodes.PUTFIELD))
    private void modifyStrongholdList(ChunkGenerator instance, List<ChunkPos> value){
        ((ChunkGeneratorAccess)instance).setField_24749(this.strongholds);
    }

    @Override
    public StrongholdGen getStrongholdGen() {
        return this.strongholdGen;
    }

    @Override
    public List<ChunkPos> getStrongholds() {
        return this.strongholds;
    }
}
