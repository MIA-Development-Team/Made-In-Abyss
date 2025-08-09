package com.altnoir.mia.block.abs;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.item.PrasioliteHoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

public abstract class ACanFarmBlock extends Block {
    public ACanFarmBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (context.getItemInHand().getItem() instanceof PrasioliteHoeItem) {
            if (state.is(this)) {
                return MiaBlocks.HOPPER_FARMLAND.get().defaultBlockState().setValue(BlockStateProperties.MOISTURE, 0);
            }
        }
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }
}
