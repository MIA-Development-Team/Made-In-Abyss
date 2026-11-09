package com.altnoir.mementoinabyss.infrastructure.worldgen.tree;

import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaTreeDecorators;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class DuskveilVinesDecorator extends TreeDecorator {
    public static final DuskveilVinesDecorator INSTANCE = new DuskveilVinesDecorator();
    public static final MapCodec<DuskveilVinesDecorator> CODEC = MapCodec.unit(INSTANCE);

    private DuskveilVinesDecorator() {}

    @Override
    protected TreeDecoratorType<?> type() {
        return MiaTreeDecorators.DUSKVEIL_VINES.get();
    }

    @Override
    public void place(Context context) {
        for (BlockPos leaf : context.leaves()) {
            if (context.random().nextFloat() >= 0.15F) continue;
            BlockPos pos = leaf.below();
            if (!context.level().isInsideBuildHeight(pos) || !context.isAir(pos)) continue;
            int length = 1;
            while (length < 8
                    && context.random().nextBoolean()
                    && context.level().isInsideBuildHeight(pos.below(length))
                    && context.isAir(pos.below(length))) {
                length++;
            }
            for (int i = 0; i < length; i++) {
                var block =
                        i == length - 1
                                ? MiaBlocks.DUSKVEIL_VINES.get()
                                : MiaBlocks.DUSKVEIL_VINES_PLANT.get();
                context.setBlock(
                        pos.below(i),
                        block.defaultBlockState()
                                .setValue(CaveVines.BERRIES, context.random().nextFloat() < 0.11F));
            }
        }
    }
}
