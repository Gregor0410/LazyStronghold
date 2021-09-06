package gregor0410.lazystronghold.mixin;

import gregor0410.lazystronghold.ChunkGeneratorInterface;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow @Final private Map<RegistryKey<World>, ServerWorld> worlds;

    @Inject(method="Lnet/minecraft/server/MinecraftServer;prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V",at=@At("TAIL"))
    private void prepareStartRegion(CallbackInfo ci){
        ((ChunkGeneratorInterface)this.worlds.get(World.OVERWORLD).getChunkManager().getChunkGenerator()).getStrongholdGen().start();
    }
}
