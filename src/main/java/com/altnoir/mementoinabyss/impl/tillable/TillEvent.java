package com.altnoir.mementoinabyss.impl.tillable;

import com.altnoir.mementoinabyss.content.block.base.TillableBlock;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoeItem;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class TillEvent {
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        var level = event.getLevel();
        var pos = event.getPos();
        var stack = event.getItemStack();

        if (!(stack.getItem() instanceof HoeItem)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        var state = level.getBlockState(pos);
        if (state.isAir()) return;
        if (!(state.getBlock() instanceof TillableBlock tillable)) return;

        var newBlock = tillable.getTilledState(state, level, pos, player);
        if (newBlock == null) return;

        if (!level.isClientSide()) {
            level.setBlock(pos, newBlock, 3);
            stack.hurtAndBreak(1, player, event.getHand());
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
