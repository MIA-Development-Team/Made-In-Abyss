package com.altnoir.mia.core;

import com.altnoir.mia.init.MiaSounds;
import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public class AbyssPortal {
    public static boolean abyssPortal(Level level, Entity entity) {
        if (level.dimension() == MiaDimensions.THE_ABYSS_LEVEL && entity.getY() < (double) (level.getMinBuildHeight() - 16)) {
            if (level instanceof ServerLevel serverLevel) {
                ServerLevel level3 = serverLevel.getServer().getLevel(MiaDimensions.GREAT_FAULT_LEVEL);
                if (level3 != null) {
                    Vec3 pos = new Vec3(entity.getX() + 0.5, level3.getMaxBuildHeight(), entity.getZ() + 0.5);
                    DimensionTransition transition = dimTransition(level3, entity, pos);
                    entity.changeDimension(transition);
                }
            }
        }
        return false;
    }

    private static DimensionTransition dimTransition(ServerLevel level, Entity entity, Vec3 pos) {
        DimensionTransition.PostDimensionTransition portalSound = playerEntity -> {
            if (playerEntity instanceof ServerPlayer serverplayer) {
                serverplayer.playNotifySound(MiaSounds.ABYSS_PORTAL_TRAVEL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        };
        return new DimensionTransition(
                level, pos,
                entity.getDeltaMovement(),
                entity.getYRot(), entity.getXRot(),
                portalSound.then(DimensionTransition.PLACE_PORTAL_TICKET)
        );
    }
}
