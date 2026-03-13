package com.altnoir.mia.common.block;

import com.altnoir.mia.init.MiaItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class GloomBerryBlock extends DoubleBerryblock {
    public static final MapCodec<GloomBerryBlock> CODEC = simpleCodec(GloomBerryBlock::new);

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    public GloomBerryBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(MiaItems.GLOOM_BERRY.get());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        int i = state.getValue(AGE);
        if (i > 1) {
            popResource(level, pos, new ItemStack(MiaItems.GLOOM_BERRY.get(), 1));
            level.playSound(
                    null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F
            );
            BlockState blockstate = state.setValue(AGE, Integer.valueOf(1));
            if (level instanceof ServerLevel serverLevel) {
                setAgeUpdate(serverLevel, pos, blockstate, 1);

                if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    BlockState upperState = level.getBlockState(pos.above());

                    if (upperState.is(this) && upperState.getValue(HALF) == DoubleBlockHalf.UPPER) {
                        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
                    }
                } else if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                    BlockState lowerState = level.getBlockState(pos.below());

                    if (lowerState.is(this) && lowerState.getValue(HALF) == DoubleBlockHalf.LOWER) {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, blockstate));
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
    }

    public static int getLightLevel(BlockState state) {
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return 0;
        }
        int age = state.getValue(AGE);
        if (age == 3) {
            return 12;
        } else if (age == 2) {
            return 7;
        }
        return 0;
    }
}
