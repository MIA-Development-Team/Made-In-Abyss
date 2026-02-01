package com.altnoir.mia.worldgen.feature.foliage;

import com.altnoir.mia.init.worldgen.MiaFoliagePlacerTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class InvertedFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<InvertedFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> foliagePlacerParts(instance).apply(instance, InvertedFoliagePlacer::new)
    );

    public InvertedFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return MiaFoliagePlacerTypes.INVERTED_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(
            LevelSimulatedReader level,
            FoliageSetter blockSetter,
            RandomSource random,
            TreeConfiguration config,
            int maxFreeTreeHeight,
            FoliageAttachment attachment,
            int foliageHeight,
            int foliageRadius,
            int offset
    ) {
        boolean flag = attachment.doubleTrunk();
        BlockPos blockpos = attachment.pos().below(offset);
        this.placeLeavesRow(level, blockSetter, random, config, blockpos, foliageRadius + attachment.radiusOffset() - 1, 0, flag);
        this.placeLeavesRow(level, blockSetter, random, config, blockpos, foliageRadius + attachment.radiusOffset() - 1, foliageHeight, flag);
        this.placeLeavesRow(level, blockSetter, random, config, blockpos, foliageRadius + attachment.radiusOffset(), 1 + foliageHeight, flag);
    }

    @Override
    public int foliageHeight(RandomSource random, int height, TreeConfiguration config) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        return localY == 0
                ? (localX > 1 || localZ > 1) && localX != 0 && localZ != 0
                : localX == range && localZ == range && range > 0;
    }
}
