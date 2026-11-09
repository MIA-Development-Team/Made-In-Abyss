package com.altnoir.mementoinabyss.infrastructure.worldgen.tree;

import com.altnoir.mementoinabyss.init.MiaTreeDecorators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class GroundPatchTreeDecorator extends TreeDecorator {
    public static final MapCodec<GroundPatchTreeDecorator> CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(
                                            Codec.intRange(0, 4096)
                                                    .fieldOf("tries")
                                                    .forGetter(d -> d.tries),
                                            Codec.intRange(0, 16)
                                                    .fieldOf("radius")
                                                    .forGetter(d -> d.radius),
                                            Codec.intRange(0, 8)
                                                    .fieldOf("height")
                                                    .forGetter(d -> d.height),
                                            Codec.intRange(1, 4)
                                                    .fieldOf("max_segments")
                                                    .forGetter(d -> d.maxSegments),
                                            BlockStateProvider.CODEC
                                                    .fieldOf("block_state_provider")
                                                    .forGetter(d -> d.provider),
                                            BlockPredicate.CODEC
                                                    .fieldOf("ground_predicate")
                                                    .forGetter(d -> d.ground))
                                    .apply(instance, GroundPatchTreeDecorator::new));

    private final int tries;
    private final int radius;
    private final int height;
    private final int maxSegments;
    private final BlockStateProvider provider;
    private final BlockPredicate ground;

    public GroundPatchTreeDecorator(
            int tries,
            int radius,
            int height,
            int maxSegments,
            BlockStateProvider provider,
            BlockPredicate ground) {
        this.tries = tries;
        this.radius = radius;
        this.height = height;
        this.maxSegments = maxSegments;
        this.provider = provider;
        this.ground = ground;
    }

    public static GroundPatchTreeDecorator of(
            int tries, int radius, BlockStateProvider provider, BlockPredicate ground) {
        return new GroundPatchTreeDecorator(tries, radius, 2, 4, provider, ground);
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return MiaTreeDecorators.GROUND_PATCH.get();
    }

    @Override
    public void place(Context context) {
        List<BlockPos> roots = TreeFeature.getLowestTrunkOrRootOfTree(context);
        if (roots.isEmpty()) return;
        BlockPos origin = roots.getFirst();
        int minX = origin.getX();
        int maxX = minX;
        int minZ = origin.getZ();
        int maxZ = minZ;
        for (BlockPos root : roots) {
            if (root.getY() != origin.getY()) continue;
            minX = Math.min(minX, root.getX());
            maxX = Math.max(maxX, root.getX());
            minZ = Math.min(minZ, root.getZ());
            maxZ = Math.max(maxZ, root.getZ());
        }
        RandomSource random = context.random();
        for (int i = 0; i < this.tries; i++) {
            BlockPos ground =
                    new BlockPos(
                            random.nextIntBetweenInclusive(minX - this.radius, maxX + this.radius),
                            random.nextIntBetweenInclusive(
                                    origin.getY() - this.height, origin.getY() + this.height),
                            random.nextIntBetweenInclusive(minZ - this.radius, maxZ + this.radius));
            BlockPos above = ground.above();
            if (!context.level().isInsideBuildHeight(ground)
                    || !context.level().isInsideBuildHeight(above)
                    || !context.isAir(above)
                    || !this.ground.test(context.level(), ground)) continue;
            var state = this.provider.getState(context.level(), random, above);
            if (state.getBlock() instanceof SegmentableBlock segmented) {
                var amount = segmented.getSegmentAmountProperty();
                if (state.hasProperty(amount)) {
                    var values = amount.getPossibleValues();
                    int min = values.getFirst();
                    int max = Math.min(this.maxSegments, values.getLast());
                    if (min > max) continue;
                    state = state.setValue(amount, random.nextIntBetweenInclusive(min, max));
                }
                if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    state =
                            state.setValue(
                                    BlockStateProperties.HORIZONTAL_FACING,
                                    Direction.from2DDataValue(random.nextInt(4)));
                }
            }
            if (!state.canSurvive(context.level(), above)) continue;
            context.setBlock(above, state);
        }
    }
}
