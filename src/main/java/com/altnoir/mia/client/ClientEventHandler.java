package com.altnoir.mia.client;

import com.altnoir.mia.MIA;
import com.altnoir.mia.EffectsRegister;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = MIA.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {
    private static final Map<Entity, Long> entityScaleStartTimes = new WeakHashMap<>();

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasEffect(EffectsRegister.PERSPECTIVE.get())) {
            entityScaleStartTimes.clear();
            return;
        }

        Entity entity = event.getEntity();
        long currentTime = System.nanoTime();
        if (!entityScaleStartTimes.containsKey(entity)) {
            entityScaleStartTimes.put(entity, currentTime);
        }

        long startTime = entityScaleStartTimes.get(entity);
        long elapsed = currentTime - startTime;
        double progress = Math.min(elapsed / 1e9, 1.0);

        double targetScale = RenderUtils.calculateScale(entity);
        double currentScale = 1.0 + (targetScale - 1.0) * progress;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.scale((float) currentScale, (float) currentScale, (float) currentScale);
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.hasEffect(EffectsRegister.PERSPECTIVE.get())) {
            event.getPoseStack().popPose();
        }
    }
}