package com.altnoir.mia.common.block;

import com.altnoir.mia.common.block.inc.HurtPlant;
import com.altnoir.mia.datagen.MiaDamageTypes;
import com.altnoir.mia.init.MiaItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
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

public class DreamLicheeBlock extends DoubleBerryblock implements HurtPlant {
    public static final MapCodec<DreamLicheeBlock> CODEC = simpleCodec(DreamLicheeBlock::new);

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    public DreamLicheeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(MiaItems.DREAM_LICHEE.get());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        int i = state.getValue(AGE);
        boolean flag = i == MAX_AGE;
        if (i > 1) {
            int j = 1 + level.random.nextInt(2);
            popResource(level, pos, new ItemStack(MiaItems.DREAM_LICHEE.get(), j + (flag ? 1 : 0)));
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

    @Override
    public float getKnockbackForce() {
        return 0.65F;
    }

    @Override
    public ResourceKey<DamageType> getDamageTypeKey() {
        return MiaDamageTypes.DREAM_LICHEE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        this.onHurtCollision(state, level, pos, entity);
    }
}
