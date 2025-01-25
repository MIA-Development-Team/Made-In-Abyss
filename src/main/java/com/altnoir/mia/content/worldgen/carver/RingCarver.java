package com.altnoir.mia.content.worldgen.carver;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

//“真正的”洞壁，和最外层基岩的雕刻器
public class RingCarver extends WorldCarver<RingCarverConfiguration> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public RingCarver(Codec<RingCarverConfiguration> pCodec) {
        super(pCodec);
    }

    @Nullable
    private SimplexNoise noise = null;

    @Override
    public boolean carve(CarvingContext pContext, RingCarverConfiguration pConfig, ChunkAccess pChunk, Function<BlockPos, Holder<Biome>> pBiomeAccessor, RandomSource pRandom, Aquifer pAquifer, ChunkPos pChunkPos, CarvingMask pCarvingMask) {
        //LOGGER.debug("进入carve");
        if(!pChunk.getPos().equals(pChunkPos)) return false;
        boolean carveFlag = false;
        if(this.noise == null) { this.noise = new SimplexNoise(pRandom); }

        int minY = pChunk.getMinBuildHeight(), maxY = pChunk.getMaxBuildHeight() - 1; //真坑啊这里
        int minX = pChunk.getPos().getMinBlockX(), maxX = minX + 15;
        int minZ = pChunk.getPos().getMinBlockZ(), maxZ = minZ + 15;
        int listOffset = -minY;
        List<Integer> innerRads = fromCriticalToAll(pConfig.innerCriticalRadius, minY, maxY), outerRads = fromCriticalToAll(pConfig.outerCriticalRadius, minY, maxY);
        int oX = 0, oY = 0;

        for(int h = minY; h <= maxY; ++h) {
            int iR = innerRads.get(h + listOffset), oR = outerRads.get(h + listOffset);
            if(iR > oR || oR <= 0) continue;
            for(int x = minX; x <= maxX; ++x) {
                for(int z = minZ; z <= maxZ; ++z) {
                    int rIR = (int) (iR - sampleFromNoise(x, h, z, pConfig.iNHM, pConfig.iNVM) * pConfig.innerMixWidth), rOR = (int) (oR + sampleFromNoise(x, h, z, pConfig.oNHM, pConfig.oNVM) * pConfig.outerMixWidth);
                    int d = (int) Math.sqrt((double)x * x + (double)z * z);
                    if(d >= rIR && d <= rOR) {
                        carveFlag = tryPlaceBlock(pConfig, pChunk, new BlockPos(x, h, z), pRandom.fork()) || carveFlag;
                    }
                }
            }
        }

        return carveFlag;
    }

    private List<Integer> fromCriticalToAll(Map<String, Integer> criticalRadius, int minY, int maxY) {
        List<Pair<Integer, Integer>> vitalRads = new ArrayList<>(List.of());
        for(int y = minY; y <= maxY; ++y) {
            if(criticalRadius.containsKey(y + ""))
                vitalRads.add(new Pair<>(y, criticalRadius.get(y + "")));
        }
        List<Integer> rads = new ArrayList<>(List.of());
        int leftC = 0, rightC = 1;
        if(vitalRads.size() < 1) {
            return List.of();
        }
        for (int h = minY; h <= maxY; ++h) {
            if (h < vitalRads.get(0).getFirst() || h > vitalRads.get(vitalRads.size() - 1).getFirst()) {
                rads.add(0);
            } else if (h == vitalRads.get(leftC).getFirst()) {
                rads.add(vitalRads.get(leftC).getSecond());
            } else if (h > vitalRads.get(leftC).getFirst() && h < vitalRads.get(rightC).getFirst()){
                int r = vitalRads.get(leftC).getSecond() +
                        (vitalRads.get(rightC).getSecond() - vitalRads.get(leftC).getSecond()) *
                                (h - vitalRads.get(leftC).getFirst()) / (vitalRads.get(rightC).getFirst() - vitalRads.get(leftC).getFirst());
                rads.add(r);
            } else if(h == vitalRads.get(rightC).getFirst()) {
                rads.add(vitalRads.get(rightC).getSecond());
                leftC = rightC; rightC++;
            }
        }
        return rads;
    }

    private boolean tryPlaceBlock(RingCarverConfiguration pConfig, ChunkAccess pChunk, BlockPos pPos, RandomSource pRandom) {
        //LOGGER.debug("tryPlaceBlock");
        if(!this.canReplaceBlock(pConfig, pChunk.getBlockState(pPos))) return false;

        if(Math.abs(pRandom.nextDouble()) <= pConfig.continueProbability) {
            List<BlockState> near = new ArrayList<>(List.of());
            for(int i = -1; i <= 1; ++i) {
                for(int j = -1; j <= 1; ++j) {
                    for(int k = -1; k <= 1; ++k) {
                        if(this.canPlaceBlock(pConfig, pChunk.getBlockState(pPos.offset(i, j, k)))) {
                            near.add(pChunk.getBlockState(pPos.offset(i, j, k)));
                        }
                    }
                }
            }
            if(near.size() > 0) {
                int targetIndex = pRandom.nextIntBetweenInclusive(0, near.size() - 1);
                if(true) {
                    //LOGGER.debug("continue");
                    pChunk.setBlockState(pPos, near.get(targetIndex), false);
                    return true;
                } else return false;
            }
        }

        int targetIndex = pRandom.nextIntBetweenInclusive(0, pConfig.placeable.size() - 1);
        BlockState targetState = pConfig.placeable.get(targetIndex).get().defaultBlockState();
        if(true) {
            //LOGGER.debug("again");
            pChunk.setBlockState(pPos, targetState, false);
            return true;
        }
        return false;
    }

    protected boolean canPlaceBlock(RingCarverConfiguration pConfig, BlockState pState) {
        return pState.is(pConfig.placeable);
    }

    private static double PI = 3.1416;
    private double sampleFromNoise(int x, int y, int z, double mH, double mV) {
        double pY = (double)y / 64.0 * mV;
        double pX = z == 0? ((x > 0)? PI : -PI) : 2.0 * (Math.atan((double)x / (double)z) + ((z < 0)? PI : 0.0));
        pX = pX * mH / 128.0 * Math.sqrt(x * x + z * z);
        return Math.abs(noise.getValue(pX, pY));
    }

    @Override
    public boolean isStartChunk(RingCarverConfiguration pConfig, RandomSource pRandom) {
        return pRandom.nextFloat() <= pConfig.probability;
    }
}
