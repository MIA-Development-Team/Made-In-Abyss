package com.altnoir.mia.common.block;

import com.altnoir.mia.init.MiaBlocks;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class StrippedRotatedPillarBlock extends RotatedPillarBlock {
    private static Supplier<Map<Block, Block>> STRIPPABLES = null;

    private static Map<Block, Block> getStrippables() {
        if (STRIPPABLES == null) {
            STRIPPABLES = () -> ImmutableMap.<Block, Block>builder()
                    .put(MiaBlocks.FOSSILIZED_LOG.get(), MiaBlocks.STRIPPED_FOSSILIZED_LOG.get())
                    .put(MiaBlocks.FOSSILIZED_WOOD.get(), MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get())
                    .put(MiaBlocks.MOSSY_FOSSILIZED_LOG.get(), MiaBlocks.FOSSILIZED_LOG.get())
                    .put(MiaBlocks.MOSSY_FOSSILIZED_WOOD.get(), MiaBlocks.FOSSILIZED_WOOD.get())
                    .put(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_LOG.get(), MiaBlocks.STRIPPED_FOSSILIZED_LOG.get())
                    .put(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD.get(), MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get())
                    .put(MiaBlocks.SKYFOG_LOG.get(), MiaBlocks.STRIPPED_SKYFOG_LOG.get())
                    .put(MiaBlocks.SKYFOG_WOOD.get(), MiaBlocks.STRIPPED_SKYFOG_WOOD.get())
                    .build();
        }
        return STRIPPABLES.get();
    }

    public StrippedRotatedPillarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (context.getItemInHand().getItem() instanceof AxeItem) {
            Block strippedBlock = getStrippables().get(state.getBlock());
            if (strippedBlock != null) {
                return strippedBlock.defaultBlockState().setValue(AXIS, state.getValue(AXIS));
            }
        }
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }
}
