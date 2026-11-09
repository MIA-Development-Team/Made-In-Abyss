package com.altnoir.mementoinabyss.infrastructure.worldgen.tree;

import com.altnoir.mementoinabyss.init.MiaWorldgenFeatures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.DarkOakFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public final class DuskveilFoliagePlacer extends DarkOakFoliagePlacer {
    public static final MapCodec<DuskveilFoliagePlacer> CODEC =
            MapCodec.unit(DuskveilFoliagePlacer::new);

    public DuskveilFoliagePlacer() {
        super(ConstantInt.of(0), ConstantInt.of(0));
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return MiaWorldgenFeatures.DUSKVEIL_FOLIAGE.get();
    }

    @Override
    protected void createFoliage(
            WorldGenLevel level,
            FoliageSetter foliageSetter,
            RandomSource random,
            TreeConfiguration config,
            int treeHeight,
            FoliageAttachment attachment,
            int foliageHeight,
            int leafRadius,
            int offset) {
        super.createFoliage(
                level,
                foliageSetter,
                random,
                config,
                treeHeight,
                attachment,
                foliageHeight,
                leafRadius,
                offset);
        if (attachment.doubleTrunk()) {
            BlockPos crown = attachment.pos().above(offset);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = -4; x <= 5; x++) {
                int dx = Math.min(Math.abs(x), Math.abs(x - 1));
                for (int z = -4; z <= 5; z++) {
                    int dz = Math.min(Math.abs(z), Math.abs(z - 1));
                    if (Math.max(dx, dz) == 4 && dx + dz <= 6) {
                        pos.setWithOffset(crown, x, 0, z);
                        tryPlaceLeaf(level, foliageSetter, random, config, pos);
                    }
                }
            }
            placeLeavesRow(level, foliageSetter, random, config, crown, 2, -2, true);
        }
    }
}
