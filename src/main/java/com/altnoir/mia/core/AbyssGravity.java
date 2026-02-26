package com.altnoir.mia.core;

import com.altnoir.mia.compat.curios.CuriosTags;
import com.altnoir.mia.worldgen.MiaHeight;
import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import com.altnoir.mia.worldgen.noise_setting.densityfunction.HopperAbyssHole;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;

public class AbyssGravity {
    public static boolean isTheAbyssGravity(Entity entity) {
        double distance = Math.sqrt(entity.getX() * entity.getX() + entity.getZ() * entity.getZ());
        return isTheAbyss(entity) && distance <= HopperAbyssHole.getAbyssRadius() * 0.3;
    }

    public static boolean isInvertedForest(Entity entity) {
        return isTheAbyss(entity) && entity.getY() < (double) MiaHeight.THE_ABYSS.minY() / 2 + 16;
    }

    private static boolean isTheAbyss(Entity entity) {
        return entity.level().dimension() == MiaDimensions.THE_ABYSS_LEVEL;
    }

    public static double getAbyssGravity(Entity entity, double value) {
        double entityY = entity.getY();
        double midY = MiaHeight.THE_ABYSS.middleY();

        final double RANGE = 8.0;
        double finalGravity;

        if (entity instanceof Player p && hasWhistle(p)) {
            finalGravity = value * 0.5;
        } else if (entityY >= midY) {
            double distanceAbove = entityY - midY;
            double gravityFactor = Mth.clamp(distanceAbove / RANGE, 0.05, 1.0);
            finalGravity = value * gravityFactor;
        } else {
            double distanceBelow = midY - entityY;
            double gravityFactor = Mth.clamp(distanceBelow / RANGE, 0.05, 1.0);
            finalGravity = -value * gravityFactor;
        }
        return finalGravity;
    }

    public static boolean hasWhistle(Player entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(inventory -> {
                    Map<String, ICurioStacksHandler> curios = inventory.getCurios();
                    ICurioStacksHandler whistleHandler = curios.get("whistle");

                    if (whistleHandler != null && whistleHandler.getSlots() > 0) {
                        ItemStack stack = whistleHandler.getStacks().getStackInSlot(0);
                        return !stack.isEmpty() && stack.getItemHolder().is(CuriosTags.WHISTLE);
                    }
                    return false;
                })
                .orElse(false);
    }
}
