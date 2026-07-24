package com.altnoir.mementoinabyss.content.block;

import com.altnoir.mementoinabyss.init.MiaArtifactItems;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.worldgen.feature.AbyssPortalFeature;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AbyssPortalCoreBlock extends Block {
    public static final MapCodec<AbyssPortalCoreBlock> CODEC = simpleCodec(AbyssPortalCoreBlock::new);
    public static final BooleanProperty COMPASS = BooleanProperty.create("compass");
    public static final int MAX_STAGE = 12;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, MAX_STAGE);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2, 10, 2, 14, 15, 14),
            Block.box(3, 3, 3, 13, 10, 13),
            Block.box(0, 0, 0, 16, 3, 16));

    public AbyssPortalCoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(COMPASS, false).setValue(STAGE, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(MiaArtifactItems.STAR_COMPASS.get()) || state.getValue(COMPASS)
                || level.dimension() != Level.OVERWORLD) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(COMPASS, true).setValue(STAGE, 0), Block.UPDATE_ALL);
            level.levelEvent(1503, pos, 0);
            level.scheduleTick(pos, this, 20);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(COMPASS)) {
            return;
        }
        int stage = state.getValue(STAGE) + 1;
        if (stage < 1 || stage > MAX_STAGE) {
            return;
        }
        level.setBlock(pos, state.setValue(STAGE, stage), Block.UPDATE_ALL);
        BlockPos portalCenter = pos.below(15);
        if (stage < MAX_STAGE) {
            AbyssPortalFeature.clearPortalLayer(level, portalCenter, MAX_STAGE - stage);
            level.scheduleTick(pos, this, 20);
        } else {
            AbyssPortalFeature.createPortal(level, portalCenter);
            level.globalLevelEvent(1038, portalCenter, 0);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(COMPASS) || random.nextBoolean()) {
            return;
        }
        int intensity = MAX_STAGE - state.getValue(STAGE) + 1;
        for (int i = 0; i < intensity * 2; i++) {
            level.addParticle(ParticleTypes.END_ROD,
                    pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat() * 0.1,
                    pos.getZ() + random.nextFloat(), random.nextGaussian() * 0.005,
                    random.nextGaussian() * 0.05 + intensity * 0.05, random.nextGaussian() * 0.005);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return state.getValue(COMPASS) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COMPASS, STAGE);
    }
}
