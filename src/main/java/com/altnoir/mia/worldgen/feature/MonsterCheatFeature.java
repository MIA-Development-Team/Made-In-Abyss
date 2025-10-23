package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.MIA;
import com.altnoir.mia.datagen.loottable.MiaCheatLootTable;
import com.altnoir.mia.init.worldgen.MiaFeature;
import com.altnoir.mia.worldgen.feature.configurations.MonsterCheatConfiguration;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

import java.util.function.Predicate;

public class MonsterCheatFeature extends Feature<MonsterCheatConfiguration> {
    private static final EntityType<?>[] MOBS = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public MonsterCheatFeature(Codec<MonsterCheatConfiguration> codec) {
        super(codec);
    }

    /**
     * Places the given feature at the given location.
     * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated, that they can safely generate into.
     *
     * @param context A context object with a reference to the level and the position
     *                the feature is being placed at
     */
    @Override
    public boolean place(FeaturePlaceContext<MonsterCheatConfiguration> context) {
        Predicate<BlockState> predicate = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos blockpos = context.origin();
        RandomSource randomsource = context.random();
        WorldGenLevel worldgenlevel = context.level();

        MonsterCheatConfiguration bsc;
        for (bsc = context.config(); blockpos.getY() > worldgenlevel.getMinBuildHeight() + 5; blockpos = blockpos.below()) {
            if (worldgenlevel.isEmptyBlock(blockpos)) {
                BlockState blockstate = worldgenlevel.getBlockState(blockpos.below());
                if (isDirt(blockstate) || MiaFeature.isStone(blockstate)) {
                    break;
                }
            }
        }

        if (blockpos.getY() <= worldgenlevel.getMinBuildHeight() + 5) {
            return false;
        } else {
            BlockPos spawnerPos = blockpos.below();
            this.safeSetBlock(worldgenlevel, spawnerPos, Blocks.SPAWNER.defaultBlockState(), predicate);
            if (worldgenlevel.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawnerblockentity) {
                spawnerblockentity.setEntityId(this.randomEntityId(randomsource), randomsource);
            } else {
                MIA.LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", spawnerPos.getX(), spawnerPos.getY(), spawnerPos.getZ());
            }

            BlockPos chestPos = spawnerPos.below();
            this.safeSetBlock(worldgenlevel, chestPos, StructurePiece.reorient(worldgenlevel, chestPos, Blocks.CHEST.defaultBlockState()), predicate);
            RandomizableContainer.setBlockEntityLootTable(worldgenlevel, randomsource, chestPos, MiaCheatLootTable.SIMPLE_RUINS);

            if (worldgenlevel.isEmptyBlock(blockpos)) {
                this.safeSetBlock(worldgenlevel, blockpos, bsc.coreStateProvider.getState(randomsource, blockpos), predicate);

                for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP}) {
                    BlockPos statePos = blockpos.relative(direction);
                    if (worldgenlevel.getBlockState(statePos).canBeReplaced()) {
                        this.safeSetBlock(worldgenlevel, statePos, bsc.outerStateProvider.getState(randomsource, statePos), predicate);

                        if (direction != Direction.UP) {
                            BlockPos statePos2 = statePos.below();
                            BlockPos statePos3 = statePos2.below();
                            if (worldgenlevel.getBlockState(statePos2).canBeReplaced()) {
                                this.safeSetBlock(worldgenlevel, statePos2, bsc.outerStateProvider.getState(randomsource, statePos2), predicate);
                            }
                            if (worldgenlevel.getBlockState(statePos3).canBeReplaced()) {
                                this.safeSetBlock(worldgenlevel, statePos3, bsc.outerStateProvider.getState(randomsource, statePos3), predicate);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private EntityType<?> randomEntityId(RandomSource random) {
        return MOBS[random.nextInt(MOBS.length)];
    }
}
