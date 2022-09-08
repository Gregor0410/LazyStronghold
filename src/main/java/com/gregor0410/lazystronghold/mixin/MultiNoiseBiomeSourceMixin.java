package com.gregor0410.lazystronghold.mixin;

import com.gregor0410.lazystronghold.IBiomeSource;
import net.minecraft.world.biome.source.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiNoiseBiomeSource.class)
public class MultiNoiseBiomeSourceMixin implements IBiomeSource {

    private MultiNoiseBiomeSourceConfig config;

    @Inject(method="<init>",at=@At("TAIL"))
    private void init(MultiNoiseBiomeSourceConfig config, CallbackInfo ci){
        this.config = config;
    }
    @Override
    public BiomeSource copy() {
        return new MultiNoiseBiomeSource(this.config) ;
    }

}
