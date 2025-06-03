package com.altnoir.mia.item.c;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class UnlimitedBucket extends BucketItem {

    public UnlimitedBucket(Fluid content, Properties properties) {
        super(content, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = hitResult.getBlockPos();
            Direction direction = hitResult.getDirection();
            BlockPos blockPos1 = blockPos.relative(direction);
            BlockState blockState = level.getBlockState(blockPos);
            Block block = blockState.getBlock();
            BlockState blockstate = level.getBlockState(blockPos);
            BlockPos blockPos2 = canBlockContainFluid(player, level, blockPos, blockstate) ? blockPos : blockPos1;

            if (block instanceof BucketPickup bucketPickup) {
                ItemStack fluidBucket = bucketPickup.pickupBlock(player, level, blockPos, blockState);
                if (!fluidBucket.isEmpty()) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                    level.gameEvent(player, GameEvent.FLUID_PICKUP, blockPos);
                    level.playSound(player, blockPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                }
            } else if (this.emptyContents(player, level, blockPos2, hitResult, itemStack)) {
                this.checkExtraContent(player, level, itemStack, blockPos2);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos2, itemStack);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
                ItemStack itemStack1 = ItemUtils.createFilledResult(itemStack, player, getEmptySuccessItem(itemStack, player));
                return InteractionResultHolder.sidedSuccess(itemStack1, level.isClientSide());
            }
        }
        return InteractionResultHolder.fail(itemStack);
    }
}
