package com.altnoir.mia;

import com.altnoir.mia.content.AreaParticle;
import com.altnoir.mia.client.RendererUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class Time {
    public static long millis = 0L;
    public static int times = 0;
    public static Timer timer = new Timer(20.0F, 0L);
    private static boolean isTimeStop = false;

    public static void abs(Player e) {
        if (!Time.get()) {
            e.playSound(SoundsRegister.STOP.get(), 1f, 1f);
            AreaParticle p = new AreaParticle((Minecraft.getInstance()).level, e.getX(), e.getY(), e.getZ(), 0.4F, 0.0D, 0.0D, 0.0D, RendererUtils.beam.toString(), 0.62F, 0.3F, 0.3F, 0.3F, 0.4F, false, 1.4d) {
                @Override
                public Vec3 getPos() {
                    return e.position();
                }
            };
            p.setLifetime(80);
            Minecraft.getInstance().particleEngine.add(p);
        }
        isTimeStop = !isTimeStop;
        Minecraft.getInstance().pause = !Minecraft.getInstance().pause;
        if (!Time.get()) {
            Minecraft.getInstance().gameRenderer.shutdownEffect();
        }
        Minecraft.getInstance().particleEngine.tick();
    }

    public static boolean get() {
        return isTimeStop;
    }
}