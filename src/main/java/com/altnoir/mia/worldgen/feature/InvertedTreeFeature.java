package com.altnoir.mia.worldgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;

public class InvertedTreeFeature extends Feature<TreeConfiguration> {
    public InvertedTreeFeature(Codec<TreeConfiguration> codec) {
        super(codec);
    }

    private static boolean isVine(LevelSimulatedReader level, BlockPos pos) {
        return level.isStateAtPosition(pos, blockState -> blockState.is(Blocks.VINE));
    }

    public static boolean isAirOrLeaves(LevelSimulatedReader level, BlockPos pos) {
        return level.isStateAtPosition(pos, blockState -> blockState.isAir() || blockState.is(BlockTags.LEAVES));
    }

    private static void setBlockKnownShape(LevelWriter level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 19);
    }

    public static boolean validTreePos(LevelSimulatedReader level, BlockPos pos) {
        return level.isStateAtPosition(pos, blockState -> blockState.isAir() || blockState.is(BlockTags.REPLACEABLE_BY_TREES));
    }

    private boolean doPlace(
            WorldGenLevel level,
            RandomSource random,
            BlockPos pos,
            BiConsumer<BlockPos, BlockState> rootBlockSetter,
            BiConsumer<BlockPos, BlockState> trunkBlockSetter,
            FoliagePlacer.FoliageSetter foliageBlockSetter,
            TreeConfiguration config
    ) {
        int i = config.trunkPlacer.getTreeHeight(random);
        int j = config.foliagePlacer.foliageHeight(random, i, config);
        int k = i - j;
        int l = config.foliagePlacer.foliageRadius(random, k);
        BlockPos blockpos = config.rootPlacer.<BlockPos>map(rootPlacer -> rootPlacer.getTrunkOrigin(pos, random)).orElse(pos);
        int i1 = Math.min(pos.getY(), blockpos.getY());
        int j1 = Math.max(pos.getY(), blockpos.getY()) + i + 1;
        if (i1 >= level.getMinBuildHeight() + 1 && j1 <= level.getMaxBuildHeight()) {
            OptionalInt optionalint = config.minimumSize.minClippedHeight();
            int k1 = this.getMaxFreeTreeHeight(level, i, blockpos, config);
            if (k1 >= i || !optionalint.isEmpty() && k1 >= optionalint.getAsInt()) {
                if (config.rootPlacer.isPresent() && !config.rootPlacer.get().placeRoots(level, rootBlockSetter, random, pos, blockpos, config)
                ) {
                    return false;
                } else {
                    List<FoliagePlacer.FoliageAttachment> list = config.trunkPlacer.placeTrunk(level, trunkBlockSetter, random, k1, blockpos, config);
                    list.forEach(foliageAttachment -> config.foliagePlacer.createFoliage(level, foliageBlockSetter, random, config, k1, foliageAttachment, j, l));
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private int getMaxFreeTreeHeight(LevelSimulatedReader level, int trunkHeight, BlockPos topPosition, TreeConfiguration config) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int i = 0; i <= trunkHeight + 1; i++) {
            int j = config.minimumSize.getSizeAtHeight(trunkHeight, i);

            for (int k = -j; k <= j; k++) {
                for (int l = -j; l <= j; l++) {
                    blockpos$mutableblockpos.setWithOffset(topPosition, k, -i, l);
                    if (i == 0) {
                        continue;
                    }

                    if (!config.trunkPlacer.isFree(level, blockpos$mutableblockpos) || !config.ignoreVines && isVine(level, blockpos$mutableblockpos)
                    ) {
                        return i - 2;
                    }
                }
            }
        }

        return trunkHeight;
    }

    @Override
    protected void setBlock(LevelWriter level, BlockPos pos, BlockState state) {
        setBlockKnownShape(level, pos, state);
    }

    @Override
    public boolean place(FeaturePlaceContext<TreeConfiguration> context) {
        final WorldGenLevel worldgenlevel = context.level();
        RandomSource randomsource = context.random();
        BlockPos blockpos = context.origin();
        TreeConfiguration treeconfiguration = context.config();
        Set<BlockPos> set = Sets.newHashSet();
        Set<BlockPos> set1 = Sets.newHashSet();
        final Set<BlockPos> set2 = Sets.newHashSet();
        Set<BlockPos> set3 = Sets.newHashSet();
        BiConsumer<BlockPos, BlockState> biconsumer = (pos, state) -> {
            set.add(pos.immutable());
            worldgenlevel.setBlock(pos, state, 19);
        };
        BiConsumer<BlockPos, BlockState> biconsumer1 = (pos, state) -> {
            set1.add(pos.immutable());
            worldgenlevel.setBlock(pos, state, 19);
        };
        FoliagePlacer.FoliageSetter foliageplacer$foliagesetter = new FoliagePlacer.FoliageSetter() {
            @Override
            public void set(BlockPos pos, BlockState state) {
                set2.add(pos.immutable());
                worldgenlevel.setBlock(pos, state, 19);
            }

            @Override
            public boolean isSet(BlockPos pos) {
                return set2.contains(pos);
            }
        };
        BiConsumer<BlockPos, BlockState> biconsumer2 = (pos, state) -> {
            set3.add(pos.immutable());
            worldgenlevel.setBlock(pos, state, 19);
        };
        boolean flag = this.doPlace(worldgenlevel, randomsource, blockpos, biconsumer, biconsumer1, foliageplacer$foliagesetter, treeconfiguration);
        if (flag && (!set1.isEmpty() || !set2.isEmpty())) {
            if (!treeconfiguration.decorators.isEmpty()) {
                TreeDecorator.Context treedecorator$context = new TreeDecorator.Context(worldgenlevel, biconsumer2, randomsource, set1, set2, set);
                treeconfiguration.decorators.forEach(p_225282_ -> p_225282_.place(treedecorator$context));
            }

            return BoundingBox.encapsulatingPositions(Iterables.concat(set, set1, set2, set3)).map(p_225270_ -> {
                DiscreteVoxelShape discretevoxelshape = updateLeaves(worldgenlevel, p_225270_, set1, set3, set);
                StructureTemplate.updateShapeAtEdge(worldgenlevel, 3, discretevoxelshape, p_225270_.minX(), p_225270_.minY(), p_225270_.minZ());
                return true;
            }).orElse(false);
        } else {
            return false;
        }
    }

    private static DiscreteVoxelShape updateLeaves(
            LevelAccessor level, BoundingBox box, Set<BlockPos> rootPositions, Set<BlockPos> trunkPositions, Set<BlockPos> foliagePositions
    ) {
        DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(box.getXSpan(), box.getYSpan(), box.getZSpan());
        int i = 7;
        List<Set<BlockPos>> list = Lists.newArrayList();

        for (int j = 0; j < 7; j++) {
            list.add(Sets.newHashSet());
        }

        for (BlockPos blockpos : Lists.newArrayList(Sets.union(trunkPositions, foliagePositions))) {
            if (box.isInside(blockpos)) {
                discretevoxelshape.fill(blockpos.getX() - box.minX(), blockpos.getY() - box.minY(), blockpos.getZ() - box.minZ());
            }
        }

        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int k1 = 0;
        list.get(0).addAll(rootPositions);

        while (true) {
            while (k1 >= 7 || !list.get(k1).isEmpty()) {
                if (k1 >= 7) {
                    return discretevoxelshape;
                }

                Iterator<BlockPos> iterator = list.get(k1).iterator();
                BlockPos blockpos1 = iterator.next();
                iterator.remove();
                if (box.isInside(blockpos1)) {
                    if (k1 != 0) {
                        BlockState blockstate = level.getBlockState(blockpos1);
                        setBlockKnownShape(level, blockpos1, blockstate.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(k1)));
                    }

                    discretevoxelshape.fill(blockpos1.getX() - box.minX(), blockpos1.getY() - box.minY(), blockpos1.getZ() - box.minZ());

                    for (Direction direction : Direction.values()) {
                        blockpos$mutableblockpos.setWithOffset(blockpos1, direction);
                        if (box.isInside(blockpos$mutableblockpos)) {
                            int k = blockpos$mutableblockpos.getX() - box.minX();
                            int l = blockpos$mutableblockpos.getY() - box.minY();
                            int i1 = blockpos$mutableblockpos.getZ() - box.minZ();
                            if (!discretevoxelshape.isFull(k, l, i1)) {
                                BlockState blockstate1 = level.getBlockState(blockpos$mutableblockpos);
                                OptionalInt optionalint = LeavesBlock.getOptionalDistanceAt(blockstate1);
                                if (!optionalint.isEmpty()) {
                                    int j1 = Math.min(optionalint.getAsInt(), k1 + 1);
                                    if (j1 < 7) {
                                        list.get(j1).add(blockpos$mutableblockpos.immutable());
                                        k1 = Math.min(k1, j1);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            k1++;
        }
    }
}