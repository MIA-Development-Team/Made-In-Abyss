package com.altnoir.mia.content.worldgen.carver;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/*
这是用于生成深渊中央大洞的雕刻器，也就是“中央空洞雕刻器”，一个在世界0,0开洞的东西，以下阐释其如何使用 (by yggdyy_)

1. 注意，一般来说一个深渊维度一个中央空洞雕刻器
2. 首先，你需要在data/mia/worldgen/configured_carver/ 下面创建一个.json文件，表示一个配置过了的中央空洞雕刻器
3. 参考 https://zh.minecraft.wiki/w/%E9%9B%95%E5%88%BB%E5%99%A8%E6%95%B0%E6%8D%AE%E6%A0%BC%E5%BC%8F
   以及上述文件夹下我创建的 abyss_central_hole_overworld.json, 完成这个文件，以下解释部分参数:
   (1) critical_radius: 关键半径表，用于描绘中央空洞的大小，其中键为该关键半径作用的高度，值为该高度半径，相邻高差
       最近的关键半径之间空洞半径线性变化。高于最高定义高度和低于最低定义高度的半径为0. 我讲不明白，给个栗子吧：
       如果有以下关键半径表:
        "critical_radius": {
            "0": 10,
            "5": 10,
            "10": 15
        }
        假设其作用在主世界，那么主世界的y0到y5的0,0处都会有半径10格的圆柱形空洞，而y6到y10则是下底半径11，上底半径15，
        高5，的圆台型空洞。而其他高度都不会存在这个雕刻器造成的空洞
   (2) mix_width: 混合宽度(正整数，单位格)，空洞壁的起伏使用噪声生成，这个值决定了振幅。比方说，当这个参数是8时，某高
       度洞壁凸起的方块到直线x = z = 0最多比该高度半径 [由(1)说的定义] 小8
   (3) noise_horizontal_multiplier和noise_vertical_multiplier，水平和竖直噪声参数，两个double，绝对值越小对
       应的方向上起伏越缓
   (4) probability，出现概率，一个float，你应当将其设置为1，你也不想你的空洞不空了吧qwq
   (5) 其他的一些原版雕刻器用到的参数，比方说y和yScale等，虽然对本雕刻器应当无影响，但仍要写上
4. 最后，记得给对应的群系加上配置后的雕刻器。如果要给原版群系加，你可以考虑使用forge的BiomeModifier，参考:
   https://forge.gemwire.uk/wiki/Biome_Modifiers#Builtin_Biome_Modifier_Types,
   不过你要用的多半是我自己写的这个，参考 data/mia/forge/biome_modifier/abyss_central_hole_overworld.json
 */
public class CentralHoleCarver extends WorldCarver<CentralHoleCarverConfiguration> {
    @Nullable
    private SimplexNoise noise = null;

    public CentralHoleCarver(Codec<CentralHoleCarverConfiguration> pCodec) {
        super(pCodec);
    }

    private static final double PI = Math.PI;
    @Override
    public boolean carve(CarvingContext pContext, CentralHoleCarverConfiguration pConfig, ChunkAccess pChunk, Function<BlockPos, Holder<Biome>> pBiomeAccessor, RandomSource pRandom, Aquifer pAquifer, ChunkPos pChunkPos, CarvingMask pCarvingMask) {
        if(!pChunk.getPos().equals(pChunkPos)) return false;

        //标记有没有成功放置空气(雕刻)
        boolean carveFlag = false;

        int minY = pChunk.getMinBuildHeight(), maxY = pChunk.getMaxBuildHeight() - 1; //真坑啊这里
        int minX = pChunk.getPos().getMinBlockX(), maxX = minX + 15;
        int minZ = pChunk.getPos().getMinBlockZ(), maxZ = minZ + 15;
        if(this.noise == null) this.noise = new SimplexNoise(pRandom);

        //关键半径
        List<Pair<Integer, Integer>> vitalRads = new ArrayList<>(List.of());
        for(int y = minY; y <= maxY; ++y) {
            if(pConfig.criticalRadius.containsKey(y + ""))
                vitalRads.add(new Pair<>(y, pConfig.criticalRadius.get(y + "")));
        }

        //全部半径
        List<Integer> rads = new ArrayList<>(List.of());
        int leftC = 0, rightC = 1;
        if(vitalRads.size() < 1) {
            return false;
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

        //完成雕刻
        int listOffset = -minY;
        for (int h = maxY; h >= minY; --h) {
            long r1 = rads.get(h + listOffset);
            long r2 = (r1 - pConfig.mixWidth) * (r1 - pConfig.mixWidth);
            long R2 = r1 * r1;
            r2 = Math.min(R2, r2);
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    var targetPos = new BlockPos(x, h, z);
                    long nowR2 = (long) x * x + (long) z * z;
                    if (nowR2 < r2) {
                        if(this.canReplaceBlock(pConfig, pChunk.getBlockState(targetPos))) {
                            pChunk.setBlockState(targetPos, Blocks.AIR.defaultBlockState(), false);
                            carveFlag = true;
                        }
                    } else if(nowR2 < R2) {
                        if(this.canReplaceBlock(pConfig, pChunk.getBlockState(targetPos))) {
                            double nowR = Math.sqrt(nowR2);
                            double px = z == 0? ((x > 0)? PI : -PI) : 2.0 * (Math.atan((double)x / (double)z) + ((z < 0)? PI : 0.0));
                            px = px * (double)r1 / 128.0 * pConfig.nHM;
                            double nowMixWidth = Math.abs(noise.getValue(px, (double)h / 64.0 * pConfig.nVM)) * pConfig.mixWidth;
                            if(r1 - nowR > nowMixWidth) {
                                pChunk.setBlockState(targetPos, Blocks.AIR.defaultBlockState(), false);
                                carveFlag = true;
                            }
                        }
                    }
                }
            }
        }

        return carveFlag;
    }

    @Override
    public boolean isStartChunk(CentralHoleCarverConfiguration pConfig, RandomSource pRandom) {
        return pRandom.nextFloat() <= pConfig.probability;
    }
}
