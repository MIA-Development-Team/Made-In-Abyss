package com.altnoir.mia.client;

import com.altnoir.mia.content.AreaParticle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Map;
import java.util.Queue;

@Mod.EventBusSubscriber
public class RendererUtils {

    public static final ResourceLocation beam = new ResourceLocation("mia:textures/item/white.png");

    static Map<ParticleRenderType, Queue<Particle>> ps = null;

    public static void renderSphere(PoseStack matrix, MultiBufferSource buf, float radius, int gradation, int lx, int ly, float r, float g, float b, float a, RenderType type, float percentage) {
        float PI = 3.141592653589792F;
        VertexConsumer bb = buf.getBuffer(type);
        Matrix4f m = matrix.last().pose();
        float alpha;
        for (alpha = 0.0F; alpha < PI; alpha += PI / gradation) {
            float beta;
            for (beta = 0.0F; beta < PI * 2 * percentage; beta += PI / gradation) {
                float x = (float) (radius * Math.cos(beta) * Math.sin(alpha));
                float y = (float) (radius * Math.sin(beta) * Math.sin(alpha));
                float z = (float) (radius * Math.cos(alpha));
                bb.vertex(m, x, y, z).color(r, g, b, a).uv(0.0F, 1.0F).uv2(lx, ly).endVertex();
                var sin = Math.sin((alpha + PI / gradation));
                x = (float) (radius * Math.cos(beta) * sin);
                y = (float) (radius * Math.sin(beta) * sin);
                z = (float) (radius * Math.cos((alpha + PI / gradation)));
                bb.vertex(m, x, y, z).color(r, g, b, a).uv(0.0F, 1.0F).uv2(lx, ly).endVertex();
            }
        }
    }

    public static void renderSphere(PoseStack matrix, MultiBufferSource buf, float radius, int gradation, int lx, int ly, float r, float g, float b, float a, RenderType type) {
        renderSphere(matrix, buf, radius, gradation, lx, ly, r, g, b, a, type, 1.0F);
    }


    public static void particleRenders(PoseStack matrix, float partialTicks) {
        Vec3 proj = (Minecraft.getInstance()).gameRenderer.getMainCamera().getPosition();
        double d3 = proj.x;
        double d4 = proj.y;
        double d5 = proj.z;
        HashSet<Particle> particles = getNoRenderParticles((Minecraft.getInstance()).particleEngine);
        if (particles.size() > 0)
            for (Particle particle : particles) {
                if (particle instanceof AreaParticle p) {
                    p.tick();
                    AreaParticle.render(p, d3, d4, d5, matrix, partialTicks);
                }
            }
    }

    public static HashSet<Particle> getNoRenderParticles(ParticleEngine manager) {
        if (ps == null)
            ps = manager.particles;
        Queue<Particle> q = ps.get(ParticleRenderType.NO_RENDER);
        if (q != null)
            return new HashSet<>(q);
        return new HashSet<>();
    }


    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void gui(RenderGuiOverlayEvent event) {
        GuiGraphics guiGraphics = new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
        PoseStack stack = new PoseStack();
        stack.pushPose();
        stack.scale(0.01f, 0.01f, 0.01f);
        guiGraphics.renderItem(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), -13, -12);
        stack.popPose();
    }

    @SubscribeEvent()
    public static void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            Entity re = Minecraft.getInstance().getCameraEntity();
            if (re != null) {
                RendererUtils.particleRenders(event.getPoseStack(), event.getPartialTick());

            }
        }
    }


}
