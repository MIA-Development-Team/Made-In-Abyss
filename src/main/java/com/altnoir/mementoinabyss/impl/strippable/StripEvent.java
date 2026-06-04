package com.altnoir.mementoinabyss.impl.strippable;

import com.altnoir.mementoinabyss.content.block.base.StrippableBlock;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class StripEvent {
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        var level = event.getLevel();
        var pos = event.getPos();
        var stack = event.getItemStack();

        if (!(stack.getItem() instanceof AxeItem)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        var state = level.getBlockState(pos);
        if (state.isAir()) return;
        if (!(state.getBlock() instanceof StrippableBlock strippable)) return;

        var newBlock = strippable.getStrippedState(state, level, pos, player);
        if (newBlock == null) return;

        if (!level.isClientSide()) {
            level.setBlock(pos, newBlock, 3);
            stack.hurtAndBreak(1, player, event.getHand());
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
