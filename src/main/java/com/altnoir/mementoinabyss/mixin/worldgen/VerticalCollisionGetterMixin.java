package com.altnoir.mementoinabyss.mixin.worldgen;

import com.altnoir.mementoinabyss.impl.vertical.VerticalBlockCollisions;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CollisionGetter.class)
public interface VerticalCollisionGetterMixin {
    // This shared query also covers vanilla sneak-edge checks and server movement validation.
    @ModifyReturnValue(method = "getBlockCollisionsFromContext", at = @At("RETURN"))
    private Iterable<VoxelShape> mia$connectedBlockCollisions(
            Iterable<VoxelShape> original, CollisionContext context, AABB box) {
        return (Object) this instanceof Level level
                ? VerticalBlockCollisions.extend(level, context, box, original)
                : original;
    }
}
