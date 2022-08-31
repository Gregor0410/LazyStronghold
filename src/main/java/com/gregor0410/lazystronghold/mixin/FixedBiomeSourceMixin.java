package com.gregor0410.lazystronghold.mixin;

import com.gregor0410.lazystronghold.IBiomeSource;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSourceConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FixedBiomeSource.class)
public class FixedBiomeSourceMixin implements IBiomeSource {

    private FixedBiomeSourceConfig config;

    @Inject(method="<init>",at=@At("TAIL"))
    private void init(FixedBiomeSourceConfig config, CallbackInfo ci){
        this.config = config;
    }
    @Override
    public BiomeSource copy() {
        return new FixedBiomeSource(this.config) ;
    }
}
