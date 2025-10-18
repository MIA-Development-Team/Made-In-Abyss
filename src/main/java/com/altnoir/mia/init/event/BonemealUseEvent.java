package com.altnoir.mia.init.event;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaTags;
import com.google.common.collect.ImmutableMap.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;

import java.util.Map;

public class BonemealUseEvent {
    public static final Map<Block, Block> COVERGRASS_BLOCKS = new Builder<Block, Block>()
            .put(Blocks.DIRT, Blocks.GRASS_BLOCK)
            .put(Blocks.TUFF, MiaBlocks.COVERGRASS_TUFF.get())
            .build();

    public static void onBonemealUse(BonemealEvent event) {
        BlockState state = event.getState();
        Block block = state.getBlock();
        BlockPos pos = event.getPos();
        Level level = event.getLevel();

        if (COVERGRASS_BLOCKS.containsKey(block)) {
            Block coverGrassBlock = COVERGRASS_BLOCKS.get(block);
            boolean flag = false;

            for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                BlockState blockstate = level.getBlockState(blockpos);
                if (blockstate.is(MiaTags.Blocks.COVERGRASS)) {
                    flag = true;
                    break;
                }
            }

            if (flag) {
                level.setBlock(pos, coverGrassBlock.defaultBlockState(), 3);
                bonemealSuccess(event);
            }
        }
    }

    /**
     * 处理骨粉使用成功后的逻辑
     *
     * @param event 骨粉事件
     */
    private static void bonemealSuccess(BonemealEvent event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getPlayer();
        ItemStack stack = event.getStack();

        if (!level.isClientSide) {
            if (player != null) {
                player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
            level.levelEvent(1505, pos, 15);
            stack.shrink(1);
        } else {
            if (player != null) {
                if (player.getMainHandItem() == stack) {
                    player.swing(InteractionHand.MAIN_HAND);
                } else if (player.getOffhandItem() == stack) {
                    player.swing(InteractionHand.OFF_HAND);
                }
            }
        }
    }
}
