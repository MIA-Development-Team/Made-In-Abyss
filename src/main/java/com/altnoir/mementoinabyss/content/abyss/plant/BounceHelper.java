package com.altnoir.mementoinabyss.content.abyss.plant;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Shared bounce maths for primo cap / fungus blocks. */
public final class BounceHelper {
    private BounceHelper() {}

    public static boolean shouldBounce(Entity entity) {
        return !entity.isSuppressingBounce();
    }

    public static void bounceUp(Entity entity) {
        Vec3 movement = entity.getDeltaMovement();
        if (movement.y < 0.0) {
            double scale = entity instanceof LivingEntity ? 1.0 : 0.8;
            entity.setDeltaMovement(movement.x, -movement.y * 0.66 * scale, movement.z);
        }
    }
}
