package com.gregor0410.lazystronghold.mixin;

import com.gregor0410.lazystronghold.ChunkGeneratorInterface;
import com.gregor0410.lazystronghold.StrongholdGen;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StrongholdFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(StrongholdFeature.class)
public abstract class StrongholdFeatureMixin {

    @Inject(method = "shouldStartAt", at = @At("HEAD"), cancellable = true)
    private void shouldStartAt(BiomeAccess biomeAccess, ChunkGenerator<?> chunkGenerator, Random random, int chunkZ, int i, Biome biome, CallbackInfoReturnable<Boolean> cir) {
        StrongholdGen strongholdGen = ((ChunkGeneratorInterface) chunkGenerator).lazyStronghold$getStrongholdGen();
        if (strongholdGen != null && !strongholdGen.completedSignal.get()) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "shouldStartAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/feature/StrongholdFeature;initialize(Lnet/minecraft/world/gen/chunk/ChunkGenerator;)V"))
    private void cancelStrongholdGen(StrongholdFeature instance, ChunkGenerator<?> chunkGenerator) {
    }

    @Redirect(method = "locateStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/feature/StrongholdFeature;initialize(Lnet/minecraft/world/gen/chunk/ChunkGenerator;)V"))
    private void cancelStrongholdGen2(StrongholdFeature instance, ChunkGenerator<?> chunkGenerator) {
    }

    @Redirect(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;getBiomeSource()Lnet/minecraft/world/biome/source/BiomeSource;"))
    private BiomeSource getBiomeSource(ChunkGenerator<?> generator) {
        StrongholdGen strongholdGen = ((ChunkGeneratorInterface) generator).lazyStronghold$getStrongholdGen();
        if (strongholdGen != null) {
            return strongholdGen.biomeSource;
        }
        return generator.getBiomeSource(); //this should never be executed but i've left it in to prevent crashing
    }

    @Inject(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/source/BiomeSource;locateBiome(IIIILjava/util/List;Ljava/util/Random;)Lnet/minecraft/util/math/BlockPos;"), cancellable = true)
    private void stopStrongholdGen(ChunkGenerator<?> generator, CallbackInfo ci) {
        StrongholdGen strongholdGen = ((ChunkGeneratorInterface) generator).lazyStronghold$getStrongholdGen();
        if (strongholdGen != null && strongholdGen.shouldStop) {
            ci.cancel();
        }
    }
}
