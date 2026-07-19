package com.altnoir.mementoinabyss.content.block.plant;

import com.altnoir.mementoinabyss.init.MiaItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class GloomBerryBlock extends DoubleBerryBlock {
    public GloomBerryBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) <= 1) return super.useWithoutItem(state, level, pos, player, hit);
        popResource(level, pos, new ItemStack(MiaItems.GLOOM_BERRY.get()));
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);
        if (level instanceof ServerLevel server) setAge(server, pos, state, 1);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state.setValue(AGE, 1)));
        return InteractionResult.SUCCESS;
    }

    public static int getLightLevel(BlockState state) {
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) return 0;
        return state.getValue(AGE) == 3 ? 15 : state.getValue(AGE) == 2 ? 12 : 0;
    }
}
