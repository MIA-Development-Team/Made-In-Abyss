package com.altnoir.mia.common.block.entity.renderer;

import com.altnoir.mia.common.block.AbyssPortalCoreBlock;
import com.altnoir.mia.common.block.entity.AbyssPortalCoreBlockEntity;
import com.altnoir.mia.init.MiaItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AbyssPortalCoreRenderer implements BlockEntityRenderer<AbyssPortalCoreBlockEntity> {
    public AbyssPortalCoreRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AbyssPortalCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        var level = blockEntity.getLevel();
        if (level == null) return;

        if (blockEntity.getBlockState().getValue(AbyssPortalCoreBlock.COMPASS)) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.0375F, 0.5F);
            poseStack.scale(0.5F, 0.5F, 0.5F);

            var player = Minecraft.getInstance().player;
            if (player != null) {
                var blockPos = blockEntity.getBlockPos();
                var playerPos = player.getEyePosition(partialTick);
                double dx = playerPos.x - (blockPos.getX() + 0.5);
                double dz = playerPos.z - (blockPos.getZ() + 0.5);
                float angle = (float) (Math.atan2(dz, dx) * 180 / Math.PI - 90);

                poseStack.mulPose(Axis.YN.rotationDegrees(angle));
            }

            itemRenderer.renderStatic(new ItemStack(MiaItems.STAR_COMPASS.get()), ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, level, 0);
            poseStack.popPose();
        }
    }
}
