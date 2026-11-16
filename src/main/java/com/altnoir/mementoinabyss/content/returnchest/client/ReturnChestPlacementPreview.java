package com.altnoir.mementoinabyss.content.returnchest.client;

import com.altnoir.mementoinabyss.content.returnchest.ReturnChestItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class ReturnChestPlacementPreview {
    @SubscribeEvent
    public static void extract(ExtractBlockOutlineRenderStateEvent event) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) return;
        InteractionHand hand =
                player.getMainHandItem().getItem() instanceof ReturnChestItem
                        ? InteractionHand.MAIN_HAND
                        : InteractionHand.OFF_HAND;
        var stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ReturnChestItem item)) return;

        var context = new BlockPlaceContext(player, hand, stack, event.getHitResult());
        var base = item.getPlacementBase(context).immutable();
        int color = item.canPlaceStructure(context) ? 0xFFA9EB3C : 0xFFFF4444;
        event.addCustomRenderer(
                (outline, buffers, pose, translucent, renderState) -> {
                    if (outline.isTranslucent() == translucent) {
                        Vec3 camera = renderState.cameraRenderState.pos;
                        ShapeRenderer.renderShape(
                                pose,
                                buffers.getBuffer(RenderTypes.lines()),
                                ReturnChestItem.previewShape(),
                                base.getX() - camera.x,
                                base.getY() - camera.y,
                                base.getZ() - camera.z,
                                color,
                                2.5F);
                    }
                    return true;
                });
    }

    private ReturnChestPlacementPreview() {}
}
