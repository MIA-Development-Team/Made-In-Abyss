package com.altnoir.mia.common.block.entity;

import com.altnoir.mia.init.MiaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SunStoneBlockEntity extends BlockEntity {
    public SunStoneBlockEntity(BlockPos pos, BlockState blockState) {
        super(MiaBlockEntities.SUN_STONE_ENTITY.get(), pos, blockState);
    }
}
