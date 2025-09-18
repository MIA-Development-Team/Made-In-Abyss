package com.altnoir.mia.block.entity.renderer;

import com.altnoir.mia.block.entity.PedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class PedestalBlockRenderer implements BlockEntityRenderer<PedestalBlockEntity> {
    public PedestalBlockRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(PedestalBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack stack = blockEntity.INVENTORY.getStackInSlot(0);

        if (stack.isEmpty()) {return;}
        double time = blockEntity.getLevel().getGameTime() + partialTick;
        float yOffset = (float) Math.sin(time * 0.1F) * 0.05F;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.65F, 0.5F);
        poseStack.scale(0.5F, 0.5F, 0.5F);

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockPos blockPos = blockEntity.getBlockPos();

        double dx = cameraPos.x - (blockPos.getX() + 0.5);
        double dy = cameraPos.y - (blockPos.getY() + 0.75);
        double dz = cameraPos.z - (blockPos.getZ() + 0.5);

        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= 16D) {
            float yRot = (float) Mth.atan2(dz, dx);
            poseStack.mulPose(Axis.YN.rotationDegrees((float) Math.toDegrees(yRot) + 90F));

            float pitch = (float) Math.atan2(dy, distance);
            poseStack.mulPose(Axis.XN.rotationDegrees((float) Math.toDegrees(-pitch)));
        }

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, blockEntity.getLevel(), 0);
        poseStack.popPose();
    }
}
