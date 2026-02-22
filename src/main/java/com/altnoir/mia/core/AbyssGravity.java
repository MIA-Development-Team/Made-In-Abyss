package com.altnoir.mia.core;

import com.altnoir.mia.worldgen.MiaHeight;
import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import com.altnoir.mia.worldgen.noise_setting.densityfunction.HopperAbyssHole;
import net.minecraft.world.entity.Entity;

public class AbyssGravity {
    public static boolean isTheAbyssGravity(Entity entity) {
        double distance = Math.sqrt(entity.getX() * entity.getX() + entity.getZ() * entity.getZ());
        return isTheAbyss(entity) && entity.getY() < (double) MiaHeight.THE_ABYSS.midY() && distance <= HopperAbyssHole.getAbyssRadius() / 2;
    }

    public static boolean isInvertedForest(Entity entity) {
        return isTheAbyss(entity) && entity.getY() < (double) MiaHeight.THE_ABYSS.minY() / 2 + 16;
    }

    private static boolean isTheAbyss(Entity entity) {
        return entity.level().dimension() == MiaDimensions.THE_ABYSS_LEVEL;
    }
}
