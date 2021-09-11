package gregor0410.lazystronghold;

import com.google.common.collect.Lists;
import gregor0410.lazystronghold.mixin.ChunkGeneratorAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StrongholdConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StrongholdGen implements Runnable {
    private final StrongholdConfig config;
    private ChunkGenerator generator;
    private Thread thread;
    private Random random;
    private int i;
    private int j;
    private int k;
    private double d;
    private int l;
    private int m;
    private int n;
    private BiomeSource biomeSource;
    private long seed;
    private ArrayList<Biome> list;

    public StrongholdGen(ChunkGenerator generator){
        this.generator = generator;
        this.seed = ((ChunkGeneratorAccess)generator).getField_24748();
        this.biomeSource = generator.getBiomeSource();
        this.thread = new Thread(this,"Stronghold thread");
        this.config = this.generator.getConfig().getStronghold();
    }
    public void start(){
        this.thread.start();
    }
    @Override
    public void run() {
        Lazystronghold.log(Level.INFO,"Started stronghold gen thread");
        while(!generateStronghold());
        if(((ChunkGeneratorAccess)this.generator).getField_24749().size()!=this.config.getCount()){
            Lazystronghold.log(Level.ERROR,"Only "+((ChunkGeneratorAccess)this.generator).getField_24749().size() +" strongholds generated!");
        }else{
            Lazystronghold.log(Level.INFO,"Generated "+this.config.getCount() +" strongholds.");
        }
    }
    private boolean generateStronghold(){
        if(n==0){
            //initialise algorithm
            this.random = new Random(seed);
            this.i = config.getDistance();
            this.j = config.getCount();
            this.k = config.getSpread();
            this.d = this.random.nextDouble() * Math.PI * 2.0;
            this.l = 0;
            this.m = 0;
            this.list = Lists.<Biome>newArrayList();
            for(Biome biome : this.biomeSource.method_28443()) {
                if (biome.hasStructureFeature(StructureFeature.STRONGHOLD)) {
                    list.add(biome);
                }
            }
        }
        if(n<j){
            double e = (double)(4 * i + i * m * 6) + (random.nextDouble() - 0.5) * (double)i * 2.5;
            int o = (int)Math.round(Math.cos(d) * e);
            int p = (int)Math.round(Math.sin(d) * e);
            BlockPos blockPos = this.biomeSource.locateBiome((o << 4) + 8, 0, (p << 4) + 8, 112, list, random);
            if (blockPos != null) {
                o = blockPos.getX() >> 4;
                p = blockPos.getZ() >> 4;
            }
            ((ChunkGeneratorAccess)this.generator).getField_24749().add(new ChunkPos(o,p));
            d += Math.PI * 2 / (double)k;
            ++l;
            if (l == k) {
                ++m;
                l = 0;
                k += 2 * k / (m + 1);
                k = Math.min(k, j - n);
                d += random.nextDouble() * Math.PI * 2.0;
            }
            n++;
            return false;
        }else{
            return true;
        }
    }
}
