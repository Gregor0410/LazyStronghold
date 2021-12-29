package gregor0410.lazystronghold.mixin;

import gregor0410.lazystronghold.ChunkGeneratorInterface;
import gregor0410.lazystronghold.StrongholdGen;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin implements ChunkGeneratorInterface {
    @Shadow @Final private StructuresConfig config;
    private StrongholdGen strongholdGen = null;
    private final List<ChunkPos> strongholds = new CopyOnWriteArrayList<>();


    @Inject(method="method_28509",at=@At("HEAD"),cancellable = true)
    private void genStrongholds(CallbackInfo ci){
        if(this.config.getStronghold()!=null) {
            if (this.config.getStronghold().getCount()>0 && this.strongholdGen == null) {
                this.strongholdGen = new StrongholdGen((ChunkGenerator) (Object) this);
                this.strongholdGen.start();
            }
        }
        ci.cancel();
    }
    @ModifyVariable(at=@At("STORE"),ordinal = 0,method="locateStructure(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/gen/feature/StructureFeature;Lnet/minecraft/util/math/BlockPos;IZ)Lnet/minecraft/util/math/BlockPos;")
    private Iterator<ChunkPos> getStrongholdIterator(Iterator<ChunkPos> i){
        return this.strongholds.iterator();
    }
    @Redirect(method="method_28507(Lnet/minecraft/util/math/ChunkPos;)Z",at=@At(value="FIELD",target="Lnet/minecraft/world/gen/chunk/ChunkGenerator;field_24749:Ljava/util/List;",opcode = Opcodes.GETFIELD))
    private List<ChunkPos> method_28507_redirect(ChunkGenerator generator){
        return this.strongholds;
    }
    @Override
    public StrongholdGen getStrongholdGen() {
        return this.strongholdGen;
    }

    @Override
    public List<ChunkPos> getStrongholds() {
        return this.strongholds;
    }
}
