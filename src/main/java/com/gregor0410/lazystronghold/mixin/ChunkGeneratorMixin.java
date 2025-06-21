package com.gregor0410.lazystronghold.mixin;

import com.gregor0410.lazystronghold.ChunkGeneratorInterface;
import com.gregor0410.lazystronghold.StrongholdGen;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.OverworldChunkGenerator;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin implements ChunkGeneratorInterface {
    @Unique
    private static final double ROOT_2 = Math.sqrt(2);
    @Unique
    private static final int PADDING = 10;

    @Unique
    private StrongholdGen strongholdGen = null;
    @Unique
    private final CopyOnWriteArrayList<ChunkPos> strongholds = new CopyOnWriteArrayList<>();

    @Mutable
    @Shadow
    @Final
    protected ChunkGeneratorConfig config;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (((Object) this instanceof OverworldChunkGenerator || (Object) this instanceof FlatChunkGenerator) && this.config.getStrongholdCount() > 0) {
            this.strongholdGen = new StrongholdGen((ChunkGenerator<?>) (Object) this, this.strongholds);
        }
    }

    @Inject(method = "setStructureStarts", at = @At("HEAD"))
    private void waitForStrongholds(BiomeAccess biomeAccess, Chunk chunk, ChunkGenerator<?> chunkGenerator, StructureManager structureManager, CallbackInfo ci) throws InterruptedException {
        if (this.strongholdGen != null) {
            ChunkPos chunkPos = chunk.getPos();
            int squaredDistance = (chunkPos.x * chunkPos.x) + (chunkPos.z * chunkPos.z);
            if (squaredDistance >= minSquaredDistanceWithPadding()) {
                if (!strongholdGen.started) strongholdGen.start();
                if (squaredDistance >= minSquaredDistance()) {
                    synchronized (strongholdGen.completedSignal) {
                        while (!strongholdGen.completedSignal.get()) {
                            strongholdGen.completedSignal.wait();
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "locateStructure", at = @At("HEAD"))
    private void waitForStrongholds2(World world, String id, BlockPos center, int radius, boolean skipExistingChunks, CallbackInfoReturnable<BlockPos> cir) throws InterruptedException {
        if (Objects.equals(id, "Stronghold") && this.strongholdGen != null) {
            if (!strongholdGen.started) strongholdGen.start();
            synchronized (strongholdGen.completedSignal) {
                while (!strongholdGen.completedSignal.get()) {
                    strongholdGen.completedSignal.wait();
                }
            }
        }
    }

    @Unique
    private int minSquaredDistance() {
        double d = 2.75 * this.config.getStrongholdDistance() * 16 - 128 * ROOT_2;
        if (d < 0) return 0;
        return (int) Math.pow((int) d >> 4, 2);
    }

    @Unique
    private int minSquaredDistanceWithPadding() {
        double d = 2.75 * this.config.getStrongholdDistance() * 16 - 128 * ROOT_2;
        if (d - PADDING * 16 < 0) return 0;
        return (int) Math.pow(((int) d >> 4) - PADDING, 2);
    }

    @Override
    public StrongholdGen lazyStronghold$getStrongholdGen() {
        return this.strongholdGen;
    }
}
