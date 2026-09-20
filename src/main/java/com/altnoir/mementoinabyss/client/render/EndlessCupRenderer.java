package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.client.render.state.EndlessCupRenderState;
import com.altnoir.mementoinabyss.content.block.EndlessCupBlock;
import com.altnoir.mementoinabyss.content.block.entity.EndlessCupBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EndlessCupRenderer
        implements BlockEntityRenderer<EndlessCupBlockEntity, EndlessCupRenderState> {
    private static final Identifier WATER_STILL = Identifier.withDefaultNamespace("block/water_still");
    private static final float SIZE_OUTER = 0.875F;
    private static final float SIZE_INNER = 1.0F - SIZE_OUTER;
    private static final float WATER_Y = -0.11F;

    public EndlessCupRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public EndlessCupRenderState createRenderState() {
        return new EndlessCupRenderState();
    }

    @Override
    public void extractRenderState(
            EndlessCupBlockEntity blockEntity,
            EndlessCupRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.showWater = !blockEntity.getBlockState().getValue(EndlessCupBlock.WATERLOGGED);
        if (!state.showWater || !(blockEntity.getLevel() instanceof ClientLevel clientLevel)) {
            state.waterSprite = null;
            return;
        }

        state.waterColor = BiomeColors.getAverageWaterColor(clientLevel, blockEntity.getBlockPos());
        state.waterSprite = Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS)
                .getSprite(WATER_STILL);
    }

    @Override
    public void submit(
            EndlessCupRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        TextureAtlasSprite sprite = state.waterSprite;
        if (!state.showWater || sprite == null) {
            return;
        }

        float red = ((state.waterColor >> 16) & 0xFF) / 255.0F;
        float green = ((state.waterColor >> 8) & 0xFF) / 255.0F;
        float blue = (state.waterColor & 0xFF) / 255.0F;
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        submitNodeCollector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(), (pose, buffer) -> {
            putVertex(buffer, pose, SIZE_INNER, WATER_Y, SIZE_OUTER, u0, v1, red, green, blue, state.lightCoords);
            putVertex(buffer, pose, SIZE_OUTER, WATER_Y, SIZE_OUTER, u1, v1, red, green, blue, state.lightCoords);
            putVertex(buffer, pose, SIZE_OUTER, WATER_Y, SIZE_INNER, u1, v0, red, green, blue, state.lightCoords);
            putVertex(buffer, pose, SIZE_INNER, WATER_Y, SIZE_INNER, u0, v0, red, green, blue, state.lightCoords);
        });
    }

    private static void putVertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            float red,
            float green,
            float blue,
            int light) {
        buffer.addVertex(pose, x, y + 1.0F, z)
                .setColor(red, green, blue, 1.0F)
                .setUv(u, v)
                .setLight(light)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
