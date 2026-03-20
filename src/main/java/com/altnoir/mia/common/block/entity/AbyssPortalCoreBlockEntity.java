package com.altnoir.mia.common.block.entity;

import com.altnoir.mia.init.MiaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AbyssPortalCoreBlockEntity extends BlockEntity {
    public AbyssPortalCoreBlockEntity(BlockPos pos, BlockState blockState) {
        super(MiaBlockEntities.ABYSS_PORTAL_CORE_ENTITY.get(), pos, blockState);
    }
}
