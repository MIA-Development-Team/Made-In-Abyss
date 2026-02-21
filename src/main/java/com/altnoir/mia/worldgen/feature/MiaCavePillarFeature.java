package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.worldgen.feature.configurations.MiaCavePillarConfiguration;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class MiaCavePillarFeature extends Feature<MiaCavePillarConfiguration> {
    public MiaCavePillarFeature(Codec<MiaCavePillarConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MiaCavePillarConfiguration> context) {
        WorldGenLevel worldgenlevel = context.level();
        BlockPos blockpos = context.origin();
        MiaCavePillarConfiguration ldg = context.config();
        RandomSource randomsource = context.random();
        if (!MaCavePillarUtils.isEmptyOrWater(worldgenlevel, blockpos)) {
            return false;
        } else {
            Optional<Column> optional = Column.scan(
                    worldgenlevel,
                    blockpos,
                    ldg.floorToCeilingSearchRange,
                    MaCavePillarUtils::isEmptyOrWater,
                    MaCavePillarUtils::isPillarBaseOrLava
            );
            if (optional.isPresent() && optional.get() instanceof Column.Range column$range) {
                if (column$range.height() < 4) {
                    return false;
                } else {
                    int i = (int) ((float) column$range.height() * ldg.maxColumnRadiusToCaveHeightRatio);
                    int j = Mth.clamp(i, ldg.columnRadius.getMinValue(), ldg.columnRadius.getMaxValue());
                    int k = Mth.randomBetweenInclusive(randomsource, ldg.columnRadius.getMinValue(), j);
                    CavePillar miaCavePillarFeature$largedripstone = makePillar(
                            blockpos.atY(column$range.ceiling() - 1),
                            false,
                            randomsource,
                            k,
                            ldg.stalactiteBluntness,
                            ldg.heightScale
                    );
                    CavePillar miaCavePillarFeature$largedripstone1 = makePillar(
                            blockpos.atY(column$range.floor() + 1),
                            true,
                            randomsource,
                            k,
                            ldg.stalagmiteBluntness,
                            ldg.heightScale
                    );
                    MiaCavePillarFeature.WindOffsetter MiaLargeDripstoneFeature$windoffsetter;
                    if (miaCavePillarFeature$largedripstone.isSuitableForWind(ldg)
                            && miaCavePillarFeature$largedripstone1.isSuitableForWind(ldg)) {
                        MiaLargeDripstoneFeature$windoffsetter = new MiaCavePillarFeature.WindOffsetter(
                                blockpos.getY(), randomsource, ldg.windSpeed
                        );
                    } else {
                        MiaLargeDripstoneFeature$windoffsetter = MiaCavePillarFeature.WindOffsetter.noWind();
                    }

                    boolean flag = miaCavePillarFeature$largedripstone.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(
                            worldgenlevel, MiaLargeDripstoneFeature$windoffsetter
                    );
                    boolean flag1 = miaCavePillarFeature$largedripstone1.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(
                            worldgenlevel, MiaLargeDripstoneFeature$windoffsetter
                    );
                    if (flag) {
                        miaCavePillarFeature$largedripstone.placeBlocks(worldgenlevel, randomsource, MiaLargeDripstoneFeature$windoffsetter, blockpos);
                    }

                    if (flag1) {
                        miaCavePillarFeature$largedripstone1.placeBlocks(worldgenlevel, randomsource, MiaLargeDripstoneFeature$windoffsetter, blockpos);
                    }

                    return true;
                }
            } else {
                return false;
            }
        }
    }

    private static CavePillar makePillar(
            BlockPos root, boolean pointingUp, RandomSource random, int radius, FloatProvider bluntnessBase, FloatProvider scaleBase
    ) {
        return new CavePillar(
                root, pointingUp, radius, (double) bluntnessBase.sample(random), (double) scaleBase.sample(random)
        );
    }

    static final class CavePillar {
        private BlockPos root;
        private final boolean pointingUp;
        private int radius;
        private final double bluntness;
        private final double scale;

        CavePillar(BlockPos root, boolean pointingUp, int radius, double bluntness, double scale) {
            this.root = root;
            this.pointingUp = pointingUp;
            this.radius = radius;
            this.bluntness = bluntness;
            this.scale = scale;
        }

        private int getHeight() {
            return this.getHeightAtRadius(0.0F);
        }

        private int getMinY() {
            return this.pointingUp ? this.root.getY() : this.root.getY() - this.getHeight();
        }

        private int getMaxY() {
            return !this.pointingUp ? this.root.getY() : this.root.getY() + this.getHeight();
        }

        boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel level, MiaCavePillarFeature.WindOffsetter windOffsetter) {
            while (this.radius > 1) {
                BlockPos.MutableBlockPos blockpos$mutableblockpos = this.root.mutable();
                int i = Math.min(10, this.getHeight());

                for (int j = 0; j < i; j++) {
                    if (level.getBlockState(blockpos$mutableblockpos).is(Blocks.LAVA)) {
                        return false;
                    }

                    if (MaCavePillarUtils.isCircleMostlyEmbeddedInStone(level, windOffsetter.offset(blockpos$mutableblockpos), this.radius)) {
                        this.root = blockpos$mutableblockpos;
                        return true;
                    }

                    blockpos$mutableblockpos.move(this.pointingUp ? Direction.DOWN : Direction.UP);
                }

                this.radius /= 2;
            }

            return false;
        }

        private int getHeightAtRadius(float radius) {
            return (int) MaCavePillarUtils.getPillarHeight((double) radius, (double) this.radius, this.scale, this.bluntness);
        }

        void placeBlocks(WorldGenLevel level, RandomSource random, MiaCavePillarFeature.WindOffsetter windOffsetter, BlockPos origin) {
            for (int i = -this.radius; i <= this.radius; i++) {
                for (int j = -this.radius; j <= this.radius; j++) {
                    float f = Mth.sqrt((float) (i * i + j * j));
                    if (!(f > (float) this.radius)) {
                        int k = this.getHeightAtRadius(f);
                        if (k > 0) {
                            if ((double) random.nextFloat() < 0.2) {
                                k = (int) ((float) k * Mth.randomBetween(random, 0.8F, 1.0F));
                            }

                            BlockPos.MutableBlockPos blockpos$mutableblockpos = this.root.offset(i, 0, j).mutable();
                            boolean flag = false;
                            int l = this.pointingUp
                                    ? level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getZ())
                                    : Integer.MAX_VALUE;

                            for (int i1 = 0; i1 < k && blockpos$mutableblockpos.getY() < l; i1++) {
                                BlockPos blockpos = windOffsetter.offset(blockpos$mutableblockpos);

                                if (level.isStateAtPosition(blockpos, MaCavePillarUtils::isPillarBase)) {
                                    flag = true;
                                    Block block = MiaBlocks.FOSSILIZED_LOG.get();
                                    level.setBlock(blockpos, block.defaultBlockState(), 2);
                                } else if (flag && level.getBlockState(blockpos).is(BlockTags.BASE_STONE_OVERWORLD)) {
                                    break;
                                }

                                blockpos$mutableblockpos.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                            }
                        }
                    }
                }
            }
        }

        boolean isSuitableForWind(MiaCavePillarConfiguration config) {
            return this.radius >= config.minRadiusForWind && this.bluntness >= (double) config.minBluntnessForWind;
        }
    }

    static final class WindOffsetter {
        private final int originY;
        @Nullable
        private final Vec3 windSpeed;

        WindOffsetter(int originY, RandomSource random, FloatProvider magnitude) {
            this.originY = originY;
            float f = magnitude.sample(random);
            float f1 = Mth.randomBetween(random, 0.0F, (float) Math.PI);
            this.windSpeed = new Vec3((double) (Mth.cos(f1) * f), 0.0, (double) (Mth.sin(f1) * f));
        }

        private WindOffsetter() {
            this.originY = 0;
            this.windSpeed = null;
        }

        static MiaCavePillarFeature.WindOffsetter noWind() {
            return new MiaCavePillarFeature.WindOffsetter();
        }

        BlockPos offset(BlockPos pos) {
            if (this.windSpeed == null) {
                return pos;
            } else {
                int i = this.originY - pos.getY();
                Vec3 vec3 = this.windSpeed.scale((double) i);
                return pos.offset(Mth.floor(vec3.x), 0, Mth.floor(vec3.z));
            }
        }
    }
}

