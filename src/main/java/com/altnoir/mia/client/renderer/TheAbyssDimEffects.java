package com.altnoir.mia.client.renderer;

import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.MiaHeight;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class TheAbyssDimEffects extends DimensionSpecialEffects {
    protected static final ResourceLocation THE_ABYSS = MiaUtil.miaId("textures/skybox/the_abyss.png");

    public static final float SIZE = 2048.0F;
    public static final float HEIGHT = 1536.0F;
    public static final float CLOUD_0 = -64.0F;
    public static final float CLOUD_1 = 128.0F;
    public static final float CLOUD_2 = MiaHeight.THE_ABYSS.maxY();

    private int prevCloudX = Integer.MIN_VALUE;
    private int prevCloudY = Integer.MIN_VALUE;
    private int prevCloudZ = Integer.MIN_VALUE;
    private Vec3 prevCloudColor = Vec3.ZERO;
    private boolean generateClouds = true;
    @Nullable
    private CloudStatus prevCloudsType;
    @Nullable
    private VertexBuffer cloudBuffer;

    public TheAbyssDimEffects() {
        super(Float.NaN, true, SkyType.NONE, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public @Nullable float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        var oldProjectionMatrix = RenderSystem.getProjectionMatrix();
        var projection = new Matrix4f(oldProjectionMatrix);

        float m22 = projection.m22();
        float m32 = projection.m32();

        float near = m32 / (m22 - 1.0F);
        float far = 1.0E6F;

        projection.m22((far + near) / (near - far));
        projection.m32((2.0F * far * near) / (near - far));

        RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, THE_ABYSS);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);

        float playerX = (float) camera.getPosition().x;
        float playerY = (float) camera.getPosition().y;
        float playerZ = (float) camera.getPosition().z;
        float height = HEIGHT - playerY;

        Matrix4f pose = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferbuilder.addVertex(pose, -SIZE - playerX, height, -SIZE - playerZ).setUv(0.0F, 0.0F);
        bufferbuilder.addVertex(pose, SIZE - playerX, height, -SIZE - playerZ).setUv(1.0F, 0.0F);
        bufferbuilder.addVertex(pose, SIZE - playerX, height, SIZE - playerZ).setUv(1.0F, 1.0F);
        bufferbuilder.addVertex(pose, -SIZE - playerX, height, SIZE - playerZ).setUv(0.0F, 1.0F);

        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

        RenderSystem.setProjectionMatrix(oldProjectionMatrix, VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.disableBlend();

        return true;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        float[] cloudHeights = {CLOUD_0, CLOUD_1, CLOUD_2};
        int[] cloudOffsets = {0, 250, -250};

        for (int i = 0; i < cloudHeights.length; i++) {
            cloudsRender(poseStack, modelViewMatrix, projectionMatrix, partialTick, camX, camY, camZ, level, ticks, cloudHeights[i], cloudOffsets[i]);
        }

        return true;
    }

    private boolean cloudsRender(PoseStack poseStack, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ,
                                 ClientLevel level, int ticks, float cloudY, int cloudOffset) {

        double d1 = ((float) ticks + partialTick) * 0.03F;

        double d2 = (camX + d1 + (double) cloudOffset) / 12.0;
        double d3 = cloudY - (float) camY + 0.33F;
        double d4 = (camZ + cloudOffset * 0.5) / 12.0 + 0.33F;
        d2 -= Mth.floor(d2 / 2048.0) * 2048;
        d4 -= Mth.floor(d4 / 2048.0) * 2048;
        float f3 = (float) (d2 - (double) Mth.floor(d2));
        float f4 = (float) (d3 / 4.0 - (double) Mth.floor(d3 / 4.0)) * 4.0F;
        float f5 = (float) (d4 - (double) Mth.floor(d4));
        Vec3 vec3 = level.getCloudColor(partialTick);
        int i = (int) Math.floor(d2);
        int j = (int) Math.floor(d3 / 4.0);
        int k = (int) Math.floor(d4);
        if (i != this.prevCloudX || j != this.prevCloudY || k != this.prevCloudZ || Minecraft.getInstance().options.getCloudsType() != this.prevCloudsType || this.prevCloudColor.distanceToSqr(vec3) > 2.0E-4) {
            this.prevCloudX = i;
            this.prevCloudY = j;
            this.prevCloudZ = k;
            this.prevCloudColor = vec3;
            this.prevCloudsType = Minecraft.getInstance().options.getCloudsType();
            this.generateClouds = true;
        }

        if (this.generateClouds) {
            this.generateClouds = false;
            if (this.cloudBuffer != null) {
                this.cloudBuffer.close();
            }

            this.cloudBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            this.cloudBuffer.bind();
            this.cloudBuffer.upload(this.buildClouds(Tesselator.getInstance(), d2, d3, d4, vec3));
            VertexBuffer.unbind();
        }

        FogRenderer.levelFogColor();
        poseStack.pushPose();
        poseStack.mulPose(frustumMatrix);
        poseStack.scale(12.0F, 1.0F, 12.0F);
        poseStack.translate(-f3, f4, -f5);
        if (this.cloudBuffer != null) {
            this.cloudBuffer.bind();
            int l = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

            for (int i1 = l; i1 < 2; i1++) {
                RenderType rendertype = i1 == 0 ? RenderType.cloudsDepthOnly() : RenderType.clouds();
                rendertype.setupRenderState();
                ShaderInstance shaderinstance = RenderSystem.getShader();
                this.cloudBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shaderinstance);
                rendertype.clearRenderState();
            }

            VertexBuffer.unbind();
        }

        poseStack.popPose();
        return false;
    }

    private MeshData buildClouds(Tesselator tesselator, double x, double y, double z, Vec3 cloudColor) {
        float f = 4.0F;
        float f1 = 0.00390625F;
        int i = 8;
        int j = 4;
        float f2 = 9.765625E-4F;
        float f3 = (float) Mth.floor(x) * 0.00390625F;
        float f4 = (float) Mth.floor(z) * 0.00390625F;
        float f5 = (float) cloudColor.x;
        float f6 = (float) cloudColor.y;
        float f7 = (float) cloudColor.z;
        float f8 = f5 * 0.9F;
        float f9 = f6 * 0.9F;
        float f10 = f7 * 0.9F;
        float f11 = f5 * 0.7F;
        float f12 = f6 * 0.7F;
        float f13 = f7 * 0.7F;
        float f14 = f5 * 0.8F;
        float f15 = f6 * 0.8F;
        float f16 = f7 * 0.8F;
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        float f17 = (float) Math.floor(y / 4.0) * 4.0F;
        if (this.prevCloudsType == CloudStatus.FANCY) {
            for (int k = -3; k <= 4; k++) {
                for (int l = -3; l <= 4; l++) {
                    float f18 = (float) (k * 8);
                    float f19 = (float) (l * 8);
                    if (f17 > -5.0F) {
                        bufferbuilder.addVertex(f18 + 0.0F, f17 + 0.0F, f19 + 8.0F)
                                .setUv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4)
                                .setColor(f11, f12, f13, 0.8F)
                                .setNormal(0.0F, -1.0F, 0.0F);
                        bufferbuilder.addVertex(f18 + 8.0F, f17 + 0.0F, f19 + 8.0F)
                                .setUv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4)
                                .setColor(f11, f12, f13, 0.8F)
                                .setNormal(0.0F, -1.0F, 0.0F);
                        bufferbuilder.addVertex(f18 + 8.0F, f17 + 0.0F, f19 + 0.0F)
                                .setUv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4)
                                .setColor(f11, f12, f13, 0.8F)
                                .setNormal(0.0F, -1.0F, 0.0F);
                        bufferbuilder.addVertex(f18 + 0.0F, f17 + 0.0F, f19 + 0.0F)
                                .setUv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4)
                                .setColor(f11, f12, f13, 0.8F)
                                .setNormal(0.0F, -1.0F, 0.0F);
                    }

                    if (f17 <= 5.0F) {
                        bufferbuilder.addVertex(f18 + 0.0F, f17 + 4.0F - 9.765625E-4F, f19 + 8.0F)
                                .setUv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4)
                                .setColor(f5, f6, f7, 0.8F)
                                .setNormal(0.0F, 1.0F, 0.0F);
                        bufferbuilder.addVertex(f18 + 8.0F, f17 + 4.0F - 9.765625E-4F, f19 + 8.0F)
                                .setUv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4)
                                .setColor(f5, f6, f7, 0.8F)
                                .setNormal(0.0F, 1.0F, 0.0F);
                        bufferbuilder.addVertex(f18 + 8.0F, f17 + 4.0F - 9.765625E-4F, f19 + 0.0F)
                                .setUv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4)
                                .setColor(f5, f6, f7, 0.8F)
                                .setNormal(0.0F, 1.0F, 0.0F);
                        bufferbuilder.addVertex(f18 + 0.0F, f17 + 4.0F - 9.765625E-4F, f19 + 0.0F)
                                .setUv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4)
                                .setColor(f5, f6, f7, 0.8F)
                                .setNormal(0.0F, 1.0F, 0.0F);
                    }

                    if (k > -1) {
                        for (int i1 = 0; i1 < 8; i1++) {
                            bufferbuilder.addVertex(f18 + (float) i1 + 0.0F, f17 + 0.0F, f19 + 8.0F)
                                    .setUv((f18 + (float) i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4)
                                    .setColor(f8, f9, f10, 0.8F)
                                    .setNormal(-1.0F, 0.0F, 0.0F);
                            bufferbuilder.addVertex(f18 + (float) i1 + 0.0F, f17 + 4.0F, f19 + 8.0F)
                                    .setUv((f18 + (float) i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4)
                                    .setColor(f8, f9, f10, 0.8F)
                                    .setNormal(-1.0F, 0.0F, 0.0F);
                            bufferbuilder.addVertex(f18 + (float) i1 + 0.0F, f17 + 4.0F, f19 + 0.0F)
                                    .setUv((f18 + (float) i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4)
                                    .setColor(f8, f9, f10, 0.8F)
                                    .setNormal(-1.0F, 0.0F, 0.0F);
                            bufferbuilder.addVertex(f18 + (float) i1 + 0.0F, f17 + 0.0F, f19 + 0.0F)
                                    .setUv((f18 + (float) i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4)
                                    .setColor(f8, f9, f10, 0.8F)
                                    .setNormal(-1.0F, 0.0F, 0.0F);
                        }
                    }

                    if (k <= 1) {
                        for (int j2 = 0; j2 < 8; j2++) {
                            bufferbuilder.addVertex(f18 + (float) j2 + 1.0F - 9.765625E-4F, f17 + 0.0F, f19 + 8.0F)
                                    .setUv((f18 + (float) j2 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4)
                                    .setColor(f8, f9, f10, 0.8F)
                                    .setNormal(1.0F, 0.0F, 0.0F);
                            bufferbuilder.addVertex(f18 + (float) j2 + 1.0F - 9.765625E-4F, f17 + 4.0F, f19 + 8.0F)
                                    .setUv((f18 + (float) j2 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4)
                                    .setColor(f8, f9, f10, 0.8F)
                                    .setNormal(1.0F, 0.0F, 0.0F);
                            bufferbuilder.addVertex(f18 + (float) j2 + 1.0F - 9.765625E-4F, f17 + 4.0F, f19 + 0.0F)
                                    .setUv((f18 + (float) j2 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4)
                                    .setColor(f8, f9, f10, 0.8F)
                                    .setNormal(1.0F, 0.0F, 0.0F);
                            bufferbuilder.addVertex(f18 + (float) j2 + 1.0F - 9.765625E-4F, f17 + 0.0F, f19 + 0.0F)
                                    .setUv((f18 + (float) j2 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4)
                                    .setColor(f8, f9, f10, 0.8F)
                                    .setNormal(1.0F, 0.0F, 0.0F);
                        }
                    }

                    if (l > -1) {
                        for (int k2 = 0; k2 < 8; k2++) {
                            bufferbuilder.addVertex(f18 + 0.0F, f17 + 4.0F, f19 + (float) k2 + 0.0F)
                                    .setUv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float) k2 + 0.5F) * 0.00390625F + f4)
                                    .setColor(f14, f15, f16, 0.8F)
                                    .setNormal(0.0F, 0.0F, -1.0F);
                            bufferbuilder.addVertex(f18 + 8.0F, f17 + 4.0F, f19 + (float) k2 + 0.0F)
                                    .setUv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float) k2 + 0.5F) * 0.00390625F + f4)
                                    .setColor(f14, f15, f16, 0.8F)
                                    .setNormal(0.0F, 0.0F, -1.0F);
                            bufferbuilder.addVertex(f18 + 8.0F, f17 + 0.0F, f19 + (float) k2 + 0.0F)
                                    .setUv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float) k2 + 0.5F) * 0.00390625F + f4)
                                    .setColor(f14, f15, f16, 0.8F)
                                    .setNormal(0.0F, 0.0F, -1.0F);
                            bufferbuilder.addVertex(f18 + 0.0F, f17 + 0.0F, f19 + (float) k2 + 0.0F)
                                    .setUv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float) k2 + 0.5F) * 0.00390625F + f4)
                                    .setColor(f14, f15, f16, 0.8F)
                                    .setNormal(0.0F, 0.0F, -1.0F);
                        }
                    }

                    if (l <= 1) {
                        for (int l2 = 0; l2 < 8; l2++) {
                            bufferbuilder.addVertex(f18 + 0.0F, f17 + 4.0F, f19 + (float) l2 + 1.0F - 9.765625E-4F)
                                    .setUv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float) l2 + 0.5F) * 0.00390625F + f4)
                                    .setColor(f14, f15, f16, 0.8F)
                                    .setNormal(0.0F, 0.0F, 1.0F);
                            bufferbuilder.addVertex(f18 + 8.0F, f17 + 4.0F, f19 + (float) l2 + 1.0F - 9.765625E-4F)
                                    .setUv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float) l2 + 0.5F) * 0.00390625F + f4)
                                    .setColor(f14, f15, f16, 0.8F)
                                    .setNormal(0.0F, 0.0F, 1.0F);
                            bufferbuilder.addVertex(f18 + 8.0F, f17 + 0.0F, f19 + (float) l2 + 1.0F - 9.765625E-4F)
                                    .setUv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float) l2 + 0.5F) * 0.00390625F + f4)
                                    .setColor(f14, f15, f16, 0.8F)
                                    .setNormal(0.0F, 0.0F, 1.0F);
                            bufferbuilder.addVertex(f18 + 0.0F, f17 + 0.0F, f19 + (float) l2 + 1.0F - 9.765625E-4F)
                                    .setUv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float) l2 + 0.5F) * 0.00390625F + f4)
                                    .setColor(f14, f15, f16, 0.8F)
                                    .setNormal(0.0F, 0.0F, 1.0F);
                        }
                    }
                }
            }
        } else {
            int j1 = 1;
            int k1 = 32;

            for (int l1 = -32; l1 < 32; l1 += 32) {
                for (int i2 = -32; i2 < 32; i2 += 32) {
                    bufferbuilder.addVertex((float) (l1 + 0), f17, (float) (i2 + 32))
                            .setUv((float) (l1 + 0) * 0.00390625F + f3, (float) (i2 + 32) * 0.00390625F + f4)
                            .setColor(f5, f6, f7, 0.8F)
                            .setNormal(0.0F, -1.0F, 0.0F);
                    bufferbuilder.addVertex((float) (l1 + 32), f17, (float) (i2 + 32))
                            .setUv((float) (l1 + 32) * 0.00390625F + f3, (float) (i2 + 32) * 0.00390625F + f4)
                            .setColor(f5, f6, f7, 0.8F)
                            .setNormal(0.0F, -1.0F, 0.0F);
                    bufferbuilder.addVertex((float) (l1 + 32), f17, (float) (i2 + 0))
                            .setUv((float) (l1 + 32) * 0.00390625F + f3, (float) (i2 + 0) * 0.00390625F + f4)
                            .setColor(f5, f6, f7, 0.8F)
                            .setNormal(0.0F, -1.0F, 0.0F);
                    bufferbuilder.addVertex((float) (l1 + 0), f17, (float) (i2 + 0))
                            .setUv((float) (l1 + 0) * 0.00390625F + f3, (float) (i2 + 0) * 0.00390625F + f4)
                            .setColor(f5, f6, f7, 0.8F)
                            .setNormal(0.0F, -1.0F, 0.0F);
                }
            }
        }

        return bufferbuilder.buildOrThrow();
    }
}