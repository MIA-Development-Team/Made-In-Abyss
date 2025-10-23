package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.datagen.loottable.MiaArchaeologyLoot;
import com.altnoir.mia.datagen.loottable.MiaCheatLootTable;
import com.altnoir.mia.init.worldgen.MiaFeature;
import com.altnoir.mia.worldgen.feature.configurations.SlabRuinsConfiguration;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.function.Predicate;

public class SlabRuinsFeature extends Feature<SlabRuinsConfiguration> {
    public SlabRuinsFeature(Codec<SlabRuinsConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SlabRuinsConfiguration> context) {
        Predicate<BlockState> predicate = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos blockpos = context.origin();
        RandomSource randomsource = context.random();
        WorldGenLevel worldgenlevel = context.level();

        SlabRuinsConfiguration src;
        for (src = context.config(); blockpos.getY() > worldgenlevel.getMinBuildHeight() + 3; blockpos = blockpos.below()) {
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
            BlockPos.MutableBlockPos muPos = new BlockPos.MutableBlockPos();
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    muPos.set(blockpos.getX() + x, blockpos.getY(), blockpos.getZ() + z);
                    BlockState slabState = src.slabStateProvider.getState(randomsource, muPos);
                    if (worldgenlevel.getBlockState(muPos).isAir()) {
                        this.safeSetBlock(worldgenlevel, muPos, slabState, predicate);
                    }

                    muPos.move(0, -1, 0);
                    BlockState blockState = src.blockStateProvider.getState(randomsource, muPos);
                    this.safeSetBlock(worldgenlevel, muPos, blockState, predicate);

                    muPos.move(0, 1, 0);
                }
            }
            for (int i = 0; i < 3; i++) {
                if (randomsource.nextFloat() < 0.5F) {
                    int offsetX = randomsource.nextInt(4) - 1;
                    int offsetZ = randomsource.nextInt(4) - 1;

                    if ((offsetX < 0 || offsetX > 1) || (offsetZ < 0 || offsetZ > 1)) {
                        muPos.set(blockpos.getX() + offsetX, blockpos.getY(), blockpos.getZ() + offsetZ);
                        BlockState extraSlabState = src.slabStateProvider.getState(randomsource, muPos);
                        if (worldgenlevel.getBlockState(muPos).isAir() && !worldgenlevel.getBlockState(muPos.below()).isAir()) {
                            this.safeSetBlock(worldgenlevel, muPos, extraSlabState, predicate);
                        }
                    }
                }
            }
            int chestX = randomsource.nextInt(2);
            int chestZ = randomsource.nextInt(2);

            BlockPos chestPos = new BlockPos(blockpos.getX() + chestX, blockpos.getY() - 1, blockpos.getZ() + chestZ);
            this.safeSetBlock(worldgenlevel, chestPos, Blocks.SUSPICIOUS_GRAVEL.defaultBlockState(), predicate);
            worldgenlevel.getBlockEntity(chestPos, BlockEntityType.BRUSHABLE_BLOCK)
                    .ifPresent(blockEntity -> blockEntity.setLootTable(MiaArchaeologyLoot.ABYSS_RUINS, chestPos.asLong()));
        }
        return true;
    }
}
