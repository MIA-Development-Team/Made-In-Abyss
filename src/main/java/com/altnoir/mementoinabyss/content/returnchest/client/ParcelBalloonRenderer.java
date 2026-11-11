package com.altnoir.mementoinabyss.content.returnchest.client;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.returnchest.BalloonCargo;
import com.altnoir.mementoinabyss.content.returnchest.BalloonItem;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.TreeModelInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.tree.TreeBedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v2.event.RegisterV2BedrockResourcesEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v2.resource.BedrockModelResources;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class ParcelBalloonRenderer
        extends EntityRenderer<BalloonCargo, ParcelBalloonRenderer.State> {
    private static final Identifier MODEL = MementoInAbyss.asResource("parcel_balloon.geo");
    private static final Identifier TEXTURE =
            MementoInAbyss.asResource("textures/entity/parcel/parcel_balloon.png");
    private static final Identifier TINT_TEXTURE =
            MementoInAbyss.asResource("textures/entity/parcel/balloon_tint.png");

    private ParcelBalloonRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @SubscribeEvent
    public static void registerModel(RegisterV2BedrockResourcesEvent event) {
        event.treeModel(MODEL).register();
    }

    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BalloonCargo.PARCEL_BALLOON.get(), ParcelBalloonRenderer::new);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BalloonCargo cargo, State state, float partialTick) {
        super.extractRenderState(cargo, state, partialTick);
        state.yaw = cargo.getYRot(partialTick);
        state.color = cargo.color();
    }

    @Override
    public void submit(
            State state, PoseStack pose, SubmitNodeCollector nodes, CameraRenderState camera) {
        var resources = BedrockModelResources.getInstance();
        var model = resources == null ? null : resources.getTreeModel(MODEL);
        if (model != null) {
            var instance = model.createInstance();
            int light = state.lightCoords;
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-state.yaw - 180.0F));
            nodes.submitCustomGeometry(
                    pose, RenderTypes.entityCutout(TEXTURE), geometry(model, instance, light, 0xFFFFFF));
            if (state.color != DyeColor.RED) {
                nodes.order(1).submitCustomGeometry(
                        pose,
                        RenderTypes.entityCutoutZOffset(TINT_TEXTURE, false),
                        geometry(model, instance, light, BalloonItem.rgb(state.color)));
            }
            pose.popPose();
        }
        super.submit(state, pose, nodes, camera);
    }

    private static SubmitNodeCollector.CustomGeometryRenderer geometry(
            TreeBedrockModel model, TreeModelInstance instance, int light, int rgb) {
        float red = ((rgb >> 16) & 255) / 255.0F;
        float green = ((rgb >> 8) & 255) / 255.0F;
        float blue = (rgb & 255) / 255.0F;
        return (renderPose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.last().set(renderPose);
            model.renderToBuffer(
                    instance, stack, buffer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
        };
    }

    static final class State extends EntityRenderState {
        private DyeColor color;
        private float yaw;
    }
}
