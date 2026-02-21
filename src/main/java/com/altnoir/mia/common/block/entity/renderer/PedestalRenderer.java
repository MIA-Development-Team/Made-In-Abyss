package com.altnoir.mia.common.block.entity.renderer;

import com.altnoir.mia.common.block.entity.PedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity> {
    public PedestalRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(PedestalBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        var level = blockEntity.getLevel();
        if (level == null) return;

        var time = level.getGameTime() + partialTick;

        var inputStack = blockEntity.extractInput(1, true);
        if (!inputStack.isEmpty()) {
            var yOffset = (float) Math.sin(time * 0.1F) * 0.05F;

            poseStack.pushPose();
            poseStack.translate(0.5F, 1.05F + yOffset, 0.5F);
            poseStack.scale(0.5F, 0.5F, 0.5F);

            var inputAngle = time * 4 % 360;
            poseStack.mulPose(Axis.YN.rotationDegrees(inputAngle));

            itemRenderer.renderStatic(inputStack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, level, 0);
            poseStack.popPose();
        }
        var slots = blockEntity.getOutputSlots();
        if (slots == 0) return;

        var nonEmptyStacks = new ArrayList<ItemStack>();
        for (int i = 0; i < slots; i++) {
            var stack = blockEntity.getOutputStack(i);
            if (!stack.isEmpty()) nonEmptyStacks.add(stack);
        }
        int count = nonEmptyStacks.size();
        if (count == 0) return;

        var radius = 0.35F;
        var yBase = 0.3F;
        var rotationSpeed = 2F;

        for (int i = 0; i < count; i++) {
            var stack = nonEmptyStacks.get(i);
            poseStack.pushPose();

            if (count < 1) {
                var yOffset = (float) Math.sin(time * 0.1F) * 0.05F;
                poseStack.translate(0.5F, yBase + yOffset, 0.5F);
            } else {
                var angle = ((2 * Math.PI / count) * i) + Math.toRadians(time * rotationSpeed);
                var x = 0.5F + (float) Math.cos(angle) * radius;
                var z = 0.5F + (float) Math.sin(angle) * radius;
                poseStack.translate(x, yBase, z);
            }

            poseStack.scale(0.35F, 0.35F, 0.35F);

            var itemAngle = time * 6 % 360;
            poseStack.mulPose(Axis.YN.rotationDegrees(itemAngle));

            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, level, 0);

            poseStack.popPose();
        }
    }
}
