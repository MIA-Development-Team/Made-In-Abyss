package com.altnoir.mia.common.block.inc;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public interface HurtPlant {
    float getKnockbackForce();

    ResourceKey<DamageType> getDamageTypeKey();

    default void onHurtCollision(BlockState state, Level level, BlockPos pos, Entity entity) {
        VoxelShape collision = state.getShape(level, pos, CollisionContext.empty()).move(pos.getX(), pos.getY(), pos.getZ());
        boolean colliding = entity.getBoundingBox().intersects(collision.bounds().inflate(1.0E-7D));

        if (colliding && entity instanceof LivingEntity living) {
            if (!level.isClientSide && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
                double d0 = Math.abs(entity.getX() - entity.xOld);
                double d1 = Math.abs(entity.getZ() - entity.zOld);
                if (d0 >= (double) 0.003F || d1 >= (double) 0.003F) {
                    if (!entity.isCrouching()) {
                        living.knockback(this.getKnockbackForce(), -Mth.sin((float) (entity.getYRot() * Math.PI / 180.0F)), Mth.cos((float) (entity.getYRot() * Math.PI / 180.0F)));
                    }

                    DamageSource source = level.damageSources().source(this.getDamageTypeKey());
                    entity.hurt(source, 1.0F);
                }
            }
        }
    }

    default PathType getHurtPathType(@Nullable Mob entity) {
        return entity != null ? null : PathType.DAMAGE_OTHER;
    }

    default PathType getHurtAdjacentPathType(@Nullable Mob entity) {
        return entity != null ? null : PathType.DANGER_OTHER;
    }
}
