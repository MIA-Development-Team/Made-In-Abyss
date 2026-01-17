package com.altnoir.mia.block;

import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.worldgen.feature.AbyssPortalFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class AbyssPortalFrameBlock extends Block {
    public static final BooleanProperty COMPASS = BooleanProperty.create("compass");

    public AbyssPortalFrameBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(COMPASS, Boolean.valueOf(false)));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(MiaItems.STAR_COMPASS.get()) && !state.getValue(COMPASS) && level.dimension() == Level.OVERWORLD) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(COMPASS, Boolean.valueOf(true)), 3);
                level.levelEvent(1503, pos, 0);

                ServerLevel serverLevel = level.getServer().getLevel(Level.OVERWORLD);
                BlockPos targetPos = pos.below(7);
                AbyssPortalFeature.createPortalStructure(serverLevel, targetPos, 5, 8, true);

                level.globalLevelEvent(1038, targetPos, 0);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(COMPASS, Boolean.valueOf(false));
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
        builder.add(COMPASS);
    }
}
