package com.altnoir.mementoinabyss.content.returnchest.client;

import com.altnoir.mementoinabyss.content.returnchest.BalloonCargo;
import com.altnoir.mementoinabyss.content.returnchest.BalloonItem;
import com.altnoir.mementoinabyss.init.MiaItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class BalloonCargoRenderer
        extends EntityRenderer<BalloonCargo, BalloonCargoRenderer.State> {
    private final ItemModelResolver models;

    private BalloonCargoRenderer(EntityRendererProvider.Context context) {
        super(context);
        models = context.getItemModelResolver();
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BalloonCargo.TYPE.get(), BalloonCargoRenderer::new);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BalloonCargo cargo, State state, float partialTick) {
        super.extractRenderState(cargo, state, partialTick);
        ItemStack balloon = new ItemStack(MiaItems.BALLOON.get());
        balloon.set(DataComponents.DYED_COLOR, new DyedItemColor(BalloonItem.rgb(cargo.color())));
        models.updateForNonLiving(state.balloon, balloon, ItemDisplayContext.NONE, cargo);
        models.updateForNonLiving(
                state.chest, new ItemStack(Items.CHEST), ItemDisplayContext.NONE, cargo);
        state.yaw = cargo.getYRot(partialTick);
    }

    @Override
    public void submit(
            State state, PoseStack pose, SubmitNodeCollector nodes, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.0, 0.5, 0.0);
        pose.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        state.chest.submit(
                pose, nodes, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();

        pose.pushPose();
        pose.translate(0.0, 1.375, 0.0);
        pose.mulPose(Axis.YP.rotationDegrees(45.0F));
        state.balloon.submit(
                pose, nodes, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        state.balloon.submit(
                pose, nodes, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
        super.submit(state, pose, nodes, camera);
    }

    static final class State extends EntityRenderState {
        private final ItemStackRenderState balloon = new ItemStackRenderState();
        private final ItemStackRenderState chest = new ItemStackRenderState();
        private float yaw;
    }
}
