package com.altnoir.mia.common.block.entity.renderer;

import com.altnoir.mia.common.block.entity.MiaBrushableBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class MiaBrushableRenderer implements BlockEntityRenderer<MiaBrushableBlockEntity> {
    private final ItemRenderer itemRenderer;

    public MiaBrushableRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(MiaBrushableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() != null) {
            int i = blockEntity.getBlockState().getValue(BlockStateProperties.DUSTED);
            if (i > 0) {
                Direction direction = blockEntity.getHitDirection();
                if (direction != null) {
                    ItemStack itemstack = blockEntity.getItem();
                    if (!itemstack.isEmpty()) {
                        poseStack.pushPose();
                        poseStack.translate(0.0F, 0.5F, 0.0F);
                        float[] afloat = this.translations(direction, i);
                        poseStack.translate(afloat[0], afloat[1], afloat[2]);
                        poseStack.mulPose(Axis.YP.rotationDegrees(75.0F));
                        boolean flag = direction == Direction.EAST || direction == Direction.WEST;
                        poseStack.mulPose(Axis.YP.rotationDegrees((float) ((flag ? 90 : 0) + 11)));
                        poseStack.scale(0.5F, 0.5F, 0.5F);
                        int j = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockState(), blockEntity.getBlockPos().relative(direction));
                        this.itemRenderer
                                .renderStatic(itemstack, ItemDisplayContext.FIXED, j, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, blockEntity.getLevel(), 0);
                        poseStack.popPose();
                    }
                }
            }
        }
    }

    private float[] translations(Direction direction, int dustedLevel) {
        float[] afloat = new float[]{0.5F, 0.0F, 0.5F};
        float f = (float) dustedLevel / 10.0F * 0.75F;
        switch (direction) {
            case EAST:
                afloat[0] = 0.73F + f;
                break;
            case WEST:
                afloat[0] = 0.25F - f;
                break;
            case UP:
                afloat[1] = 0.25F + f;
                break;
            case DOWN:
                afloat[1] = -0.23F - f;
                break;
            case NORTH:
                afloat[2] = 0.25F - f;
                break;
            case SOUTH:
                afloat[2] = 0.73F + f;
        }

        return afloat;
    }

    @Override
    public AABB getRenderBoundingBox(MiaBrushableBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new net.minecraft.world.phys.AABB(pos.getX() - .25, pos.getY() - .25, pos.getZ() - .25, pos.getX() + 1.25, pos.getY() + 1.25, pos.getZ() + 1.25);
    }
}
