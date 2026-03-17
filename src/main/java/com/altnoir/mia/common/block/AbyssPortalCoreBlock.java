package com.altnoir.mia.common.block;

import com.altnoir.mia.common.block.entity.AbyssPortalCoreBlockEntity;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.worldgen.feature.AbyssPortalFeature;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AbyssPortalCoreBlock extends BaseEntityBlock {
    public static final MapCodec<AbyssPortalCoreBlock> CODEC = simpleCodec(AbyssPortalCoreBlock::new);
    public static final BooleanProperty COMPASS = BooleanProperty.create("compass");
    public static final int MAX_STAGE = 12;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, MAX_STAGE);
    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0, 10.0, 2.0, 14.0, 15.0, 14.0),
            Block.box(3, 3, 3, 13, 10, 13),
            Block.box(0, 0, 0, 16, 3, 16)
    );

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public AbyssPortalCoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(COMPASS, Boolean.valueOf(false))
                .setValue(STAGE, 0));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(MiaItems.STAR_COMPASS.get()) && !state.getValue(COMPASS) && level.dimension() == Level.OVERWORLD) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(COMPASS, Boolean.valueOf(true)).setValue(STAGE, 0), 3);
                level.levelEvent(1503, pos, 0);

                level.scheduleTick(pos, this, 20);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(COMPASS)) {
            return;
        }

        int newStage = state.getValue(STAGE) + 1;

        if (newStage > MAX_STAGE || newStage < 1) {
            return;
        }
        level.setBlock(pos, state.setValue(STAGE, newStage), 3);

        ServerLevel serverLevel = level.getServer().getLevel(Level.OVERWORLD);
        BlockPos targetPos = pos.below(15);

        if (newStage < MAX_STAGE) {
            int height = MAX_STAGE - newStage;
            AbyssPortalFeature.createPortalStructure(serverLevel, targetPos, height);
        } else {
            AbyssPortalFeature.createPortalStructure(serverLevel, targetPos, MiaBlocks.ABYSS_PORTAL.get(), 0);
            level.globalLevelEvent(1038, targetPos, 0);
            return;
        }
        level.scheduleTick(pos, this, 20);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int stage = MAX_STAGE - state.getValue(STAGE) + 1;

        if (state.getValue(COMPASS) && random.nextInt(2) == 0) {
            double yOffset = stage * 0.05;

            for (int i = 0; i < stage + stage; i++) {
                double d0 = (double) pos.getX() + random.nextFloat();
                double d1 = (double) pos.getY() + random.nextFloat() * 0.1F;
                double d2 = (double) pos.getZ() + random.nextFloat();

                level.addParticle(
                        ParticleTypes.END_ROD,
                        d0, d1, d2,
                        random.nextGaussian() * 0.005,
                        random.nextGaussian() * 0.05 + yOffset,
                        random.nextGaussian() * 0.005
                );
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(COMPASS, Boolean.valueOf(false))
                .setValue(STAGE, 0);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return blockState.getValue(COMPASS) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COMPASS, STAGE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbyssPortalCoreBlockEntity(pos, state);
    }
}