package gregor0410.lazystronghold.mixin;

import gregor0410.lazystronghold.ChunkGeneratorInterface;
import gregor0410.lazystronghold.Lazystronghold;
import gregor0410.lazystronghold.StrongholdGen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
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
    private StrongholdGen strongholdGen=null;

    @Inject(method="Lnet/minecraft/server/MinecraftServer;prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V",at=@At("HEAD"))
    private void prepareStartRegion(CallbackInfo ci){
        this.worlds.get(World.OVERWORLD).getChunkManager().getChunkGenerator().method_28507(new ChunkPos(0,0));
        strongholdGen = ((ChunkGeneratorInterface)this.worlds.get(World.OVERWORLD).getChunkManager().getChunkGenerator()).getStrongholdGen();
        if(strongholdGen!=null) {
            strongholdGen.start();
        }
    }
    @Inject(method="Lnet/minecraft/server/MinecraftServer;prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V",at=@At("TAIL"))
    private void waitForStrongholdGen(CallbackInfo ci){
        if(strongholdGen!=null) {
            while (!strongholdGen.isComplete()) ;
            Lazystronghold.log(Level.INFO, "Entered world with " + strongholdGen.strongholds.size() + " strongholds generated");
        }
    }
}
