package com.altnoir.mementoinabyss.infrastructure.worldgen.tree;

import com.altnoir.mementoinabyss.init.MiaWorldgenFeatures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public final class DuskveilFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<DuskveilFoliagePlacer> CODEC =
            MapCodec.unit(DuskveilFoliagePlacer::new);
    private static final int[] MAIN_RADII = {3, 4, 5, 6, 7, 6, 4, 2};
    private static final int[] BRANCH_RADII = {2, 3, 4, 3, 2};

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
        boolean mainCrown = attachment.doubleTrunk();
        int[] radii = mainCrown ? MAIN_RADII : BRANCH_RADII;
        int bottom = mainCrown ? -3 : -2;
        int driftX = random.nextInt(3) - 1;
        int driftZ = random.nextInt(3) - 1;
        BlockPos center = attachment.pos().above(offset);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int layer = 0; layer < radii.length; layer++) {
            int y = bottom + layer;
            int radius = radii[layer];
            if (!mainCrown && y >= -1 && y <= 1) radius += attachment.radiusOffset();
            BlockPos rowCenter = y >= 2 ? center.offset(driftX, 0, driftZ) : center;
            int end = radius + (mainCrown ? 1 : 0);

            for (int x = -radius; x <= end; x++) {
                for (int z = -radius; z <= end; z++) {
                    if (shouldSkipLocationSigned(random, x, y, z, radius, mainCrown)) continue;
                    pos.setWithOffset(rowCenter, x, y, z);
                    if (level.isInsideBuildHeight(pos) && level.isEmptyBlock(pos)) {
                        // Only use air so pruning a disconnected lobe never removes existing
                        // terrain.
                        tryPlaceLeaf(level, foliageSetter, random, config, pos);
                    }
                }
            }
        }
    }

    @Override
    protected boolean shouldSkipLocation(
            RandomSource random, int dx, int y, int dz, int radius, boolean doubleTrunk) {
        int squared = dx * dx + dz * dz;
        return squared > radius * radius + 1
                || squared > radius * (radius - 1) && random.nextInt(4) == 0;
    }

    @Override
    public int foliageHeight(RandomSource random, int treeHeight, TreeConfiguration config) {
        return MAIN_RADII.length;
    }
}
