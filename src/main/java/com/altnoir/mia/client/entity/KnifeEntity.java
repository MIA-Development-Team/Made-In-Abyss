package com.altnoir.mia.client.entity;

import com.altnoir.mia.ItemsRegister;
import com.altnoir.mia.Time;
import com.altnoir.mia.EntityRegister;
import com.altnoir.mia.SoundsRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class KnifeEntity extends Arrow {
    private Vec3 timeStopHitMotion;

    public KnifeEntity(EntityType<? extends KnifeEntity> entityEntityType, Level level) {
        super(entityEntityType, level);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundsRegister.KNIFE_HIT.get();
    }

    public static KnifeEntity create(Level level, Player player) {
        KnifeEntity obj = new KnifeEntity(EntityRegister.flyingSwordEntity.get(), level);
        obj.setOwner(player);
        obj.setPos(player.position().add(0D,player.getEyeHeight(player.getPose()), 0D));
        return obj;
    }

    @Override
    public void tick() {
        if (!Time.get() || timeStopHitMotion == null && tickCount < 5) {
            super.tick();
            if (!onGround() && !level().isClientSide()) {
                Vec3 posVec = position();
                BlockHitResult rayTraceResult = level().clip(new ClipContext(posVec, posVec.add(getDeltaMovement()), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
                if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos blockPos = rayTraceResult.getBlockPos();
                    Block block = level().getBlockState(blockPos).getBlock();
                    if (block.equals(Blocks.COBWEB)) {
                        level().destroyBlock(blockPos, true);
                        setDeltaMovement(getDeltaMovement().scale(0.8D));
                    }
                    if (block.equals(Blocks.TRIPWIRE)) {
                        level().destroyBlock(blockPos, true);
                    }
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult rayTraceResult) {
        if (!Time.get()) {
            if (rayTraceResult instanceof EntityHitResult r) {
                if (r.getEntity() instanceof LivingEntity l) {
                    l.invulnerableTime = 0;
                }
                super.onHit(rayTraceResult);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ItemsRegister.Knife.get());
    }
}
