package com.altnoir.mementoinabyss.content.abyss.plant;

import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

final class DuskveilVines {
    static boolean supportedByLeaves(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(MiaBlocks.DUSKVEIL_LEAVES.get());
    }

    static InteractionResult harvest(BlockState state, Level level, BlockPos pos, Player player) {
        if (!state.getValue(CaveVines.BERRIES)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            Block.popResource(server, pos, new ItemStack(MiaItems.DUSKVEIL_BERRIES.get()));
            server.playSound(
                    null,
                    pos,
                    SoundEvents.CAVE_VINES_PICK_BERRIES,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.8F + server.getRandom().nextFloat() * 0.4F);
            BlockState harvested = state.setValue(CaveVines.BERRIES, false);
            server.setBlock(pos, harvested, Block.UPDATE_CLIENTS);
            server.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, harvested));
        }
        return InteractionResult.SUCCESS;
    }

    private DuskveilVines() {}
}
