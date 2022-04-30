package com.gregor0410.lazystronghold.mixin;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StrongholdFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StrongholdFeature.class)
public interface StrongholdFeatureAccess {
    @Invoker
    void invokeInitialize(ChunkGenerator<?> chunkGenerator);
    @Invoker
    void invokeInvalidateState();
    @Accessor("startPositions")
    void setStartPositions(ChunkPos[] startPositions);
}
