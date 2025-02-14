package com.altnoir.mia.content.items.base;

import com.altnoir.mia.mixin.BucketItemMixin;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class UnlimitedBucketItem extends BucketItem {
    public int defaultRechargeTime;

    public UnlimitedBucketItem(Fluid pContent, Properties builder, int defaultRechargeTime) {
        super(pContent, builder);
        this.defaultRechargeTime = defaultRechargeTime;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, Level pLevel, @Nullable Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        var nbt = checkOrCreateNbt(pStack);
        int have = nbt.getInt(HAVE_RECHARGE_TIME_KEY), need = nbt.getInt(NEED_RECHARGE_TIME_KEY);
        if(have < need) {
            nbt.putInt(HAVE_RECHARGE_TIME_KEY, have + 1);
            pStack.setTag(nbt);
        }
    }

    public static String NEED_RECHARGE_TIME_KEY = "unlimited_bucket_need_recharge_time";
    public static String HAVE_RECHARGE_TIME_KEY = "unlimited_bucket_have_recharge_time";
    protected CompoundTag checkOrCreateNbt(ItemStack pStack) {
        var nbt = pStack.getOrCreateTag();
        if(!nbt.contains(NEED_RECHARGE_TIME_KEY, Tag.TAG_ANY_NUMERIC)) {
            nbt.putInt(NEED_RECHARGE_TIME_KEY, this.defaultRechargeTime);
        }
        if(!nbt.contains(HAVE_RECHARGE_TIME_KEY, Tag.TAG_ANY_NUMERIC)) {
            nbt.putInt(HAVE_RECHARGE_TIME_KEY, 0);
        }
        pStack.setTag(nbt);
        return nbt;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        var stack = pPlayer.getItemInHand(pHand);
        var nbt = checkOrCreateNbt(stack);
        int have = nbt.getInt(HAVE_RECHARGE_TIME_KEY), need = nbt.getInt(NEED_RECHARGE_TIME_KEY);
        if(have >= need) {
            var res = modifiedSuperUse(pLevel, pPlayer, pHand);
            if(res.getResult().equals(InteractionResult.SUCCESS) || res.getResult().equals(InteractionResult.CONSUME)) {
                nbt.putInt(HAVE_RECHARGE_TIME_KEY, 0);
                stack.setTag(nbt);
            }
            return res;
        }
        return InteractionResultHolder.pass(stack);
    }

    protected InteractionResultHolder<ItemStack> modifiedSuperUse(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        BucketItemMixin pMixin = (BucketItemMixin)this;
        BlockHitResult blockhitresult = getPlayerPOVHitResult(pLevel, pPlayer, pMixin.getContent() == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onBucketUse(pPlayer, pLevel, itemstack, blockhitresult);
        if (ret != null) return ret;
        if (blockhitresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        } else if (blockhitresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            BlockPos blockpos = blockhitresult.getBlockPos();
            Direction direction = blockhitresult.getDirection();
            BlockPos blockpos1 = blockpos.relative(direction);
            if (pLevel.mayInteract(pPlayer, blockpos) && pPlayer.mayUseItemAt(blockpos1, direction, itemstack)) {
                if (pMixin.getContent() == Fluids.EMPTY) {
                    BlockState blockstate1 = pLevel.getBlockState(blockpos);
                    if (blockstate1.getBlock() instanceof BucketPickup) {
                        BucketPickup bucketpickup = (BucketPickup)blockstate1.getBlock();
                        ItemStack itemstack1 = bucketpickup.pickupBlock(pLevel, blockpos, blockstate1);
                        if (!itemstack1.isEmpty()) {
                            pPlayer.awardStat(Stats.ITEM_USED.get(this));
                            bucketpickup.getPickupSound(blockstate1).ifPresent((p_150709_) -> {
                                pPlayer.playSound(p_150709_, 1.0F, 1.0F);
                            });
                            pLevel.gameEvent(pPlayer, GameEvent.FLUID_PICKUP, blockpos);
                            ItemStack itemstack2 = ItemUtils.createFilledResult(itemstack, pPlayer, itemstack1);
                            if (!pLevel.isClientSide) {
                                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)pPlayer, itemstack1);
                            }

                            return InteractionResultHolder.sidedSuccess(itemstack2, pLevel.isClientSide());
                        }
                    }

                    return InteractionResultHolder.fail(itemstack);
                } else {
                    BlockState blockstate = pLevel.getBlockState(blockpos);
                    BlockPos blockpos2 = canBlockContainFluid(pLevel, blockpos, blockstate) ? blockpos : blockpos1;
                    if (this.emptyContents(pPlayer, pLevel, blockpos2, blockhitresult, itemstack)) {
                        this.checkExtraContent(pPlayer, pLevel, itemstack, blockpos2);
                        if (pPlayer instanceof ServerPlayer) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)pPlayer, blockpos2, itemstack);
                        }
                        pPlayer.awardStat(Stats.ITEM_USED.get(this));
                        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
                    } else {
                        return InteractionResultHolder.fail(itemstack);
                    }
                }
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        }
    }
}
