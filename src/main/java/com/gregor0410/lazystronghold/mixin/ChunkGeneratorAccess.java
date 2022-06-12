package com.gregor0410.lazystronghold.mixin;

import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorAccess {
    @Invoker
    void invokeMethod_28509();
    @Accessor("biomeSource")
    BiomeSource getBiomeSource1();
}
