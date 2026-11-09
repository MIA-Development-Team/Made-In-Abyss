package com.altnoir.mementoinabyss.infrastructure.worldgen.tree;

import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaTreeDecorators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class DuskveilIchorDecorator extends TreeDecorator {
    public static final MapCodec<DuskveilIchorDecorator> CODEC =
            Codec.floatRange(0.0F, 1.0F)
                    .optionalFieldOf("probability", 1.0F)
                    .xmap(DuskveilIchorDecorator::new, decorator -> decorator.probability);

    private final float probability;

    public DuskveilIchorDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return MiaTreeDecorators.DUSKVEIL_ICHOR.get();
    }

    @Override
    public void place(Context context) {
        if (context.random().nextFloat() >= probability) return;

        Set<BlockPos> logs = new HashSet<>(context.logs());
        int baseY =
                context.logs().stream().mapToInt(BlockPos::getY).min().orElse(Integer.MAX_VALUE);
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : context.logs()) {
            if (pos.getY() > baseY
                    && pos.getY() <= baseY + 4
                    && logs.contains(pos.below())
                    && logs.contains(pos.above())) {
                candidates.add(pos);
            }
        }
        if (!candidates.isEmpty()) {
            var pos = candidates.get(context.random().nextInt(candidates.size()));
            context.setBlock(pos, MiaBlocks.DUSKVEIL_ICHOR.get().defaultBlockState());
        }
    }
}
