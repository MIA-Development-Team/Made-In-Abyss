package com.altnoir.mia.content.blocks.seventhcrucible;

import com.altnoir.mia.BlockEntityRegister;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SeventhCrucibleBlock extends BaseEntityBlock {
    // TODO:模型的大小，等待render的TODO
    // public static final VoxelShape SHAPE = Block.box(TODO);
    private static final Logger LOGGER = LogUtils.getLogger();

    public SeventhCrucibleBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SeventhCrucibleEntity(blockPos,blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide){
            SeventhCrucibleEntity seventhCrucibleEntity = (SeventhCrucibleEntity) pLevel.getBlockEntity(pPos);
            FluidUtil.interactWithFluidHandler(pPlayer,pHand, seventhCrucibleEntity.getFluidTank());
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, BlockEntityRegister.SEVENTH_CRUCIBLE_ENTITY.get(), SeventhCrucibleEntity::serverTick);
    }
}
