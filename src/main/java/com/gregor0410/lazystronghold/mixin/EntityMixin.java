package com.gregor0410.lazystronghold.mixin;

import com.gregor0410.lazystronghold.ChunkGeneratorInterface;
import com.gregor0410.lazystronghold.StrongholdGen;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract @Nullable MinecraftServer getServer();

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void startStrongholdGen(World world, CallbackInfo ci) {
        //start stronghold gen on nether entry to prevent lag when throwing eyes
        if (world.getRegistryKey() != World.OVERWORLD && world instanceof ServerWorld) {
            MinecraftServer server = this.getServer();
            if (server != null) {
                for (ServerWorld serverWorld : server.getWorlds()) {
                    StrongholdGen strongholdGen = ((ChunkGeneratorInterface) serverWorld.getChunkManager().getChunkGenerator()).getStrongholdGen();
                    if (strongholdGen != null) {
                        if (!strongholdGen.started) {
                            strongholdGen.start();
                        }
                    }
                }
            }
        }
    }
}
