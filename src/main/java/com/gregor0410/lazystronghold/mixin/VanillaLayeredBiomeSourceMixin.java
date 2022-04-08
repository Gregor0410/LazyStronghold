package com.gregor0410.lazystronghold.mixin;

import com.gregor0410.lazystronghold.IVanillaLayeredBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSourceConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VanillaLayeredBiomeSource.class)
public class VanillaLayeredBiomeSourceMixin implements IVanillaLayeredBiomeSource {

    private VanillaLayeredBiomeSourceConfig config;

    @Inject(method="<init>",at=@At("TAIL"))
    private void init(VanillaLayeredBiomeSourceConfig config, CallbackInfo ci){
        this.config = config;
    }

    @Override
    public VanillaLayeredBiomeSource copy() {
        return new VanillaLayeredBiomeSource(this.config);
    }
}
