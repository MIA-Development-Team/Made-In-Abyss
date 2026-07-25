package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.client.render.state.PedestalRenderState;
import com.altnoir.mementoinabyss.content.block.entity.PedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public final class PedestalRenderer
        implements BlockEntityRenderer<PedestalBlockEntity, PedestalRenderState> {
    private static final float INPUT_SCALE = 0.5F;
    private static final float OUTPUT_SCALE = 0.35F;
    private static final float OUTPUT_RADIUS = 0.45F;
    private final ItemModelResolver itemModelResolver;

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public PedestalRenderState createRenderState() {
        return new PedestalRenderState();
    }

    @Override
    public void extractRenderState(
            PedestalBlockEntity pedestal,
            PedestalRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                pedestal, state, partialTicks, cameraPosition, breakProgress);

        state.animationTime = (pedestal.getLevel() == null ? 0 : pedestal.getLevel().getGameTime())
                + partialTicks;
        int seed = Long.hashCode(pedestal.getBlockPos().asLong());
        state.inputItem = resolve(pedestal.getItem(PedestalBlockEntity.INPUT_SLOT), pedestal, seed);

        var outputItems = new ArrayList<ItemStackRenderState>();
        for (int slot = PedestalBlockEntity.OUTPUT_SLOT_START;
             slot < pedestal.getContainerSize();
             slot++) {
            ItemStack stack = pedestal.getItem(slot);
            if (!stack.isEmpty()) {
                outputItems.add(resolve(stack, pedestal, seed + slot));
            }
        }
        state.outputItems = outputItems;
    }

    private ItemStackRenderState resolve(ItemStack stack, PedestalBlockEntity pedestal, int seed) {
        var state = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(
                state,
                stack,
                ItemDisplayContext.FIXED,
                pedestal.getLevel(),
                null,
                seed);
        return state;
    }

    @Override
    public void submit(
            PedestalRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        submitInput(state, poseStack, submitNodeCollector);
        submitOutputs(state, poseStack, submitNodeCollector);
    }

    private static void submitInput(
            PedestalRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector) {
        if (state.inputItem.isEmpty()) {
            return;
        }

        float bob = (float) Math.sin(state.animationTime * 0.1F) * 0.05F;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.875F + bob, 0.5F);
        poseStack.scale(INPUT_SCALE, INPUT_SCALE, INPUT_SCALE);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.animationTime * 4.0F));
        state.inputItem.submit(
                poseStack,
                submitNodeCollector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0);
        poseStack.popPose();
    }

    private static void submitOutputs(
            PedestalRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector) {
        int count = state.outputItems.size();
        if (count == 0) {
            return;
        }

        for (int index = 0; index < count; index++) {
            poseStack.pushPose();
            if (count == 1) {
                float bob = (float) Math.sin(state.animationTime * 0.1F) * 0.05F;
                poseStack.translate(0.5F, 0.3F + bob, 0.5F);
            } else {
                double angle = Math.TAU * index / count
                        + Math.toRadians(state.animationTime * 2.0F);
                poseStack.translate(
                        0.5F + (float) Math.cos(angle) * OUTPUT_RADIUS,
                        0.3F,
                        0.5F + (float) Math.sin(angle) * OUTPUT_RADIUS);
            }
            poseStack.scale(OUTPUT_SCALE, OUTPUT_SCALE, OUTPUT_SCALE);
            poseStack.mulPose(Axis.YN.rotationDegrees(state.animationTime * 6.0F));
            state.outputItems.get(index).submit(
                    poseStack,
                    submitNodeCollector,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0);
            poseStack.popPose();
        }
    }
}
