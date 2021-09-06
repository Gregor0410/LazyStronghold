package gregor0410.lazystronghold.mixin;

import gregor0410.lazystronghold.ChunkGeneratorInterface;
import gregor0410.lazystronghold.StrongholdGen;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin implements ChunkGeneratorInterface {
    private StrongholdGen strongholdGen = null;

    @Inject(method="method_28509",at=@At("HEAD"),cancellable = true)
    private void genStrongholds(CallbackInfo ci){
        if(this.strongholdGen==null){
            this.strongholdGen = new StrongholdGen((ChunkGenerator)(Object) this);
        }
        ci.cancel();
    }

    @Override
    public StrongholdGen getStrongholdGen() {
        return this.strongholdGen;
    }
}
