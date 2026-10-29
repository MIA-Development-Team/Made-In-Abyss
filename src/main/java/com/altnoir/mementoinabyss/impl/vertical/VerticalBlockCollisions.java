package com.altnoir.mementoinabyss.impl.vertical;

import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLink;
import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLinks;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.google.common.collect.Iterables;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class VerticalBlockCollisions {
    private static ClientView clientView = (level, link) -> null;

    public static void setClientView(ClientView provider) {
        clientView = provider;
    }

    public static Iterable<VoxelShape> extend(
            Level level, CollisionContext context, AABB box, Iterable<VoxelShape> original) {
        if (!(context instanceof EntityCollisionContext entityContext)
                || !(entityContext.getEntity() instanceof Player player)
                || player.level() != level
                || !MiaDimensions.isMiaDimension(level.dimension())
                || (box.minY >= level.getMinY() + 1 && box.maxY <= level.getMaxY()))
            return original;
        Iterable<VoxelShape> result = original;
        for (var link : CrossDimensionLodLinks.forTargets(level)) {
            if (!MiaDimensions.isMiaDimension(link.source())) continue;
            int offset = link.displayYOffset();
            double minY = link.sourceHeight().minY() + offset;
            double maxY = minY + link.sourceHeight().height();
            if (box.maxY <= minY - 1 || box.minY >= maxY + 1) continue;
            CollisionGetter remote;
            if (level instanceof ServerLevel server)
                remote = server.getServer().getLevel(link.source());
            else remote = clientView.get(level, link);
            if (remote == null) continue;
            AABB remoteBox = box.move(0, -offset, 0);
            CollisionContext remoteContext = new TranslatedContext(context, player, offset);
            result =
                    Iterables.concat(
                            result,
                            () ->
                                    new BlockCollisions<>(
                                            remote,
                                            remoteContext,
                                            remoteBox,
                                            false,
                                            (pos, shape) -> shape.move(0, offset, 0)));
        }
        return result;
    }

    @FunctionalInterface
    public interface ClientView {
        @Nullable CollisionGetter get(Level level, CrossDimensionLodLink link);
    }

    private static final class TranslatedContext extends EntityCollisionContext {
        private final CollisionContext original;
        private final int offset;

        TranslatedContext(CollisionContext original, Player player, int offset) {
            super(
                    original.isDescending(),
                    original.isPlacement(),
                    player.getY() - offset,
                    player.getMainHandItem(),
                    original.alwaysCollideWithFluid(),
                    player);
            this.original = original;
            this.offset = offset;
        }

        @Override
        public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
            return original.isAbove(shape, pos.offset(0, offset, 0), defaultValue);
        }
    }

    private VerticalBlockCollisions() {}
}
