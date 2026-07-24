package com.altnoir.mementoinabyss.worldgen.feature;

import com.altnoir.mementoinabyss.data.MiaArchaeologyLoot;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaTags;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.function.Predicate;

public final class SlabRuinsFeature extends Feature<SlabRuinsConfiguration> {
    public SlabRuinsFeature(Codec<SlabRuinsConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SlabRuinsConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = findFloor(level, context.origin());
        if (origin == null) return false;

        Predicate<BlockState> canReplace = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        SlabRuinsConfiguration config = context.config();
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                cursor.set(origin.getX() + x, origin.getY(), origin.getZ() + z);
                if (level.isEmptyBlock(cursor)) {
                    safeSetBlock(level, cursor, config.slabStateProvider().getState(level, random, cursor), canReplace);
                }
                cursor.move(0, -1, 0);
                safeSetBlock(level, cursor, config.blockStateProvider().getState(level, random, cursor), canReplace);
            }
        }

        for (int attempt = 0; attempt < 3; attempt++) {
            if (random.nextFloat() >= 0.5F) continue;
            int offsetX = random.nextInt(4) - 1;
            int offsetZ = random.nextInt(4) - 1;
            if (offsetX >= 0 && offsetX <= 1 && offsetZ >= 0 && offsetZ <= 1) continue;
            cursor.set(origin.getX() + offsetX, origin.getY(), origin.getZ() + offsetZ);
            if (level.isEmptyBlock(cursor) && !level.isEmptyBlock(cursor.below())) {
                safeSetBlock(level, cursor, config.slabStateProvider().getState(level, random, cursor), canReplace);
            }
        }

        BlockPos suspiciousPos = origin.offset(random.nextInt(2), -1, random.nextInt(2));
        safeSetBlock(level, suspiciousPos,
                MiaBlocks.SUSPICIOUS_ABYSS_ANDESITE.get().defaultBlockState(), canReplace);
        var brushable = BlockEntityType.BRUSHABLE_BLOCK.getBlockEntity(level, suspiciousPos);
        if (brushable == null) return false;
        brushable.setLootTable(MiaArchaeologyLoot.ABYSS_RUINS, suspiciousPos.asLong());
        return true;
    }

    private static BlockPos findFloor(WorldGenLevel level, BlockPos start) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        int minimumY = level.getMinY() + 5;
        while (cursor.getY() > minimumY) {
            if (level.isEmptyBlock(cursor)) {
                BlockState floor = level.getBlockState(cursor.below());
                if (floor.is(BlockTags.DIRT) || floor.is(MiaTags.BlockTags.BASE_STONE_ABYSS.tag)) {
                    return cursor.immutable();
                }
            }
            cursor.move(0, -1, 0);
        }
        return null;
    }
}
