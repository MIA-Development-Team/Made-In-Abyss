package com.altnoir.mementoinabyss.impl.rope.minecraft;

import com.altnoir.mementoinabyss.impl.rope.RopeAnchor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Convenience adapters for common Minecraft attachment sources.
 */
public final class MinecraftRopeAnchors {
    public static RopeAnchor blockCenter(BlockPos position) {
        Objects.requireNonNull(position, "position");
        return RopeAnchor.fixed(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5);
    }

    public static RopeAnchor entity(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        return destination -> destination.set(entity.getX(), entity.getY(), entity.getZ());
    }

    public static RopeAnchor entity(Entity entity, double offsetX, double offsetY, double offsetZ) {
        Objects.requireNonNull(entity, "entity");
        return destination -> destination.set(
                entity.getX() + offsetX,
                entity.getY() + offsetY,
                entity.getZ() + offsetZ
        );
    }

    public static RopeAnchor supplied(Supplier<Vec3> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        return destination -> {
            Vec3 position = Objects.requireNonNull(supplier.get(), "anchor supplier result");
            destination.set(position.x(), position.y(), position.z());
        };
    }

    private MinecraftRopeAnchors() {
    }
}
