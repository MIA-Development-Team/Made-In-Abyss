package com.altnoir.mia.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RenderUtils {
    private static final double BASE_DISTANCE = 5.0;

    public static double calculateScale(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 entityPos = entity.position();
        double distance = cameraPos.distanceTo(entityPos);
        double scale = distance / BASE_DISTANCE;
        return Math.max(scale, 1);
    }
}
