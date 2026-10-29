package com.altnoir.mementoinabyss.impl.vertical;

import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLink;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class VerticalRaytrace {
    public static @Nullable BlockHitResult trace(
            Level current,
            BlockGetter remote,
            CrossDimensionLodLink link,
            Entity player,
            double reach) {
        Vec3 eye = player.getEyePosition(), direction = player.getViewVector(1);
        int boundary = link.displayYOffset() < 0 ? current.getMinY() : current.getMaxY() + 1;
        if (Math.abs(direction.y) < 1e-8) return null;
        double distance = (boundary - eye.y) / direction.y;
        if (distance < 0 || distance >= reach) return null;
        Vec3 crossing = eye.add(direction.scale(distance));
        var front =
                current.clip(
                        new ClipContext(
                                eye,
                                crossing,
                                ClipContext.Block.OUTLINE,
                                ClipContext.Fluid.NONE,
                                player));
        if (front.getType() != HitResult.Type.MISS) return null;
        Vec3 shift = new Vec3(0, -link.displayYOffset(), 0);
        var hit =
                remote.clip(
                        new ClipContext(
                                crossing.add(shift).add(direction.scale(.0001)),
                                eye.add(direction.scale(reach)).add(shift),
                                ClipContext.Block.OUTLINE,
                                ClipContext.Fluid.NONE,
                                player));
        return hit.getType() == HitResult.Type.BLOCK ? hit : null;
    }

    private VerticalRaytrace() {}
}
