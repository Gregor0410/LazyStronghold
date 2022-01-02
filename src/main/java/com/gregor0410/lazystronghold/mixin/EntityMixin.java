package com.gregor0410.lazystronghold.mixin;

import com.gregor0410.lazystronghold.ChunkGeneratorInterface;
import com.gregor0410.lazystronghold.StrongholdGen;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method="setWorld",at=@At("HEAD"))
    private void startStrongholdGen(World world, CallbackInfo ci){
        //start stronghold gen on nether entry to prevent lag when throwing eyes
        if(world.getDimensionRegistryKey()!= DimensionType.OVERWORLD_REGISTRY_KEY&&world instanceof ServerWorld) {
            MinecraftServer server = ((ServerWorldAccess) world).getServer();
            for(ServerWorld serverWorld : server.getWorlds()) {
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
