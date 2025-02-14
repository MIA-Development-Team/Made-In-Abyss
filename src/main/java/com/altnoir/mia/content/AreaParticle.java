package com.altnoir.mia.content;

import com.altnoir.mia.client.RendererUtils;
import com.altnoir.mia.client.render.CullWrappedRenderLayer;
import com.altnoir.mia.client.render.GlowRenderLayer;
import com.altnoir.mia.client.render.MegaRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class AreaParticle extends Particle {
    public static int MAX_LIFE = 100;
    public boolean growing = true;
    public String loc;
    public float[] rgba = new float[4];
    public float softness = 0.01F;
    public float rotation = 0.0F;
    public float sz;
    public double slow;
    public double grow = 0.4D;
    public boolean shaders = false;
    public int image;

    public AreaParticle(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, float scale, double vx, double vy, double vz, String loc, float sz, float r, float g, float b, float a, boolean shaders, double grow) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, vx, vy, vz);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 0.4F;
        this.lifetime = 100;
        MAX_LIFE = lifetime;
        this.rgba[0] = r;
        this.rgba[1] = g;
        this.rgba[2] = b;
        this.rgba[3] = a;
        this.loc = loc;
        this.sz = sz;
        this.image = this.random.nextInt(6) + 1;
        this.shaders = shaders;
        this.slow = 0.6D;
        this.grow = grow;
    }

    public static void render(AreaParticle particle, double d3, double d4, double d5, PoseStack matrix, float partialTicks) {
        double d0 = particle.xo + (particle.x - particle.xo) * partialTicks;
        double d1 = particle.yo + (particle.y - particle.yo) * partialTicks;
        double d2 = particle.zo + (particle.z - particle.zo) * partialTicks;
        matrix.pushPose();
        matrix.translate(d0 - d3, d1 - d4, d2 - d5);
        Entity e = Minecraft.getInstance().getCameraEntity();
        if (e != null) {
            float r = particle.rgba[0];
            float g = particle.rgba[1];
            float b = particle.rgba[2];
            float a = particle.rgba[3];
            MultiBufferSource.BufferSource buf = Minecraft.getInstance().renderBuffers().bufferSource();
            ResourceLocation loc = new ResourceLocation(particle.loc);
            GlowRenderLayer glowRenderLayer = new GlowRenderLayer(new CullWrappedRenderLayer(MegaRenderType.createSphereRenderType(loc, 0)), particle.rgba, particle.softness, particle.shaders);
            double size = particle.growing ? Math.max(0.0D, particle.sz - particle.grow + particle.grow * partialTicks) : Math.max(0.0D, particle.sz + particle.grow - particle.grow * partialTicks);
            RendererUtils.renderSphere(matrix, buf, (float) size, 20, 240, 240, r, g, b, a, glowRenderLayer);
            //RendererUtils.renderSphere(matrix, buf, 0.00001F, 20, 240, 240, r, g, b, a, RenderType.glint());
            buf.endBatch(glowRenderLayer);
        }
        matrix.popPose();
    }

    @Override
    public void setLifetime(int p_107258_) {
        super.setLifetime(p_107258_);
        MAX_LIFE = p_107258_;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime)
            remove();
        if (this.rgba[3] <= 0.0F)
            remove();
        this.xd *= this.slow;
        this.yd *= this.slow;
        this.zd *= this.slow;
        if (age > MAX_LIFE / 2 + 10)
            growing = false;
        if (growing)
            this.sz = (float) (this.sz + this.grow);
        else this.sz = (float) (this.sz - this.grow);
        this.oRoll = this.roll;
        setPos(getPos().x, getPos().y, getPos().z);
    }

    @Override
    public void render(VertexConsumer p_107261_, Camera p_107262_, float p_107263_) {
    }

    public Vec3 getPos() {
        return new Vec3(x, y, z);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.NO_RENDER;
    }
}
