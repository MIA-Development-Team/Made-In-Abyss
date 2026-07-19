package com.altnoir.mementoinabyss.content.block.plant;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class DreamLicheeBlock extends DoubleBerryBlock {
    public static final ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, MementoInAbyss.asResource("dream_lichee"));
    public DreamLicheeBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        int age = state.getValue(AGE);
        if (age <= 1) return super.useWithoutItem(state, level, pos, player, hit);
        popResource(level, pos, new ItemStack(MiaItems.DREAM_LICHEE.get(), 1 + level.getRandom().nextInt(2) + (age == MAX_AGE ? 1 : 0)));
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);
        if (level instanceof ServerLevel server) setAge(server, pos, state, 1);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state.setValue(AGE, 1)));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!level.isClientSide() && entity.isAlive() && !entity.isCrouching()
                && (Math.abs(entity.getX() - entity.xOld) >= 0.003 || Math.abs(entity.getZ() - entity.zOld) >= 0.003)) {
            entity.hurt(level.damageSources().source(DAMAGE_TYPE), 1.0F);
        }
    }
}
