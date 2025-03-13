package com.altnoir.mia.content.ability;

import com.altnoir.mia.core.registries.SoundsRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class TimeStop {
    public static long millis = 0L;
    private static boolean isTimeStop = false;

    public static void abs(Player e) {
        if (!TimeStop.get()) {
            e.playSound(SoundsRegister.STOP.get(), 1f, 1f);
        }
        isTimeStop = !isTimeStop;
        Minecraft.getInstance().pause = !Minecraft.getInstance().pause;
        if (!TimeStop.get()) {
            Minecraft.getInstance().gameRenderer.shutdownEffect();
        }
        Minecraft.getInstance().particleEngine.tick();
    }

    public static boolean get() {
        return isTimeStop;
    }
}