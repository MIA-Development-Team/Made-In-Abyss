package com.altnoir.mementoinabyss.impl.event;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.client.tooltip.ArtifactEnhancementMaterialTooltip;
import com.altnoir.mementoinabyss.client.WhistleComboHandler;
import com.altnoir.mementoinabyss.client.WhistleKeyMappings;
import com.altnoir.mementoinabyss.client.RopeFreeEndGrabHandler;
import com.altnoir.mementoinabyss.client.tooltip.TooltipModifierRegistry;
import com.altnoir.mementoinabyss.client.screen.ArtifactEnhancementScreen;
import com.altnoir.mementoinabyss.client.screen.WhistleWorkbenchScreen;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodDebugEntry;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodRenderer;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodRenderTypes;
import com.altnoir.mementoinabyss.client.render.EnvironmentCubeSkyboxRenderer;
import com.altnoir.mementoinabyss.client.render.StarCompassOverlay;
import com.altnoir.mementoinabyss.client.render.WhistleComboOverlay;
import com.altnoir.mementoinabyss.impl.rope.minecraft.RopeClimbing;
import com.altnoir.mementoinabyss.content.block.entity.RopeConnectorBlockEntity;
import com.altnoir.mementoinabyss.content.item.RopeItem;
import com.altnoir.mementoinabyss.network.AdjustRopeLengthPayload;
import com.altnoir.mementoinabyss.network.CompassTargetPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodDebugPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodPayload;
import com.altnoir.mementoinabyss.network.RopeSnapshotPayload;
import com.altnoir.mementoinabyss.init.MiaMenus;
import com.altnoir.mementoinabyss.init.MiaDataComponents;
import com.altnoir.mementoinabyss.init.MiaRecipes;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class ClientEvent {
    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MiaMenus.ARTIFACT_ENHANCEMENT.get(), ArtifactEnhancementScreen::new);
        event.register(MiaMenus.WHISTLE_WORKBENCH.get(), WhistleWorkbenchScreen::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(WhistleKeyMappings.CATEGORY);
        event.register(WhistleKeyMappings.SKILL_DIAL);
    }

    @SubscribeEvent
    public static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        CrossDimensionLodRenderTypes.registerPipelines(event);
        EnvironmentCubeSkyboxRenderer.registerPipeline(event);
    }

    @SubscribeEvent
    public static void registerEnvironmentRenderers(RegisterCustomEnvironmentEffectRendererEvent event) {
        event.registerSkyboxRenderer(EnvironmentCubeSkyboxRenderer.ID, EnvironmentCubeSkyboxRenderer.INSTANCE);
    }

    @SubscribeEvent
    public static void renderCrossDimensionLod(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        CrossDimensionLodRenderer.renderPersistent(event);
    }

    @SubscribeEvent
    public static void renderStarCompass(RenderGuiEvent.Post event) {
        StarCompassOverlay.render(event.getGuiGraphics(), event.getPartialTick());
        WhistleComboOverlay.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Pre event) {
        var player = net.minecraft.client.Minecraft.getInstance().player;
        if (player != null) {
            var input = player.input.keyPresses;
            RopeClimbing.tick(player, RopeConnectorBlockEntity.clientInstances(),
                    input.jump(), input.shift());
        }
    }

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(CrossDimensionLodPayload.TYPE,
                (payload, context) -> CrossDimensionLodRenderer.accept(payload));
        event.register(CrossDimensionLodDebugPayload.TYPE,
                (payload, context) -> CrossDimensionLodDebugEntry.accept(payload));
        event.register(CompassTargetPayload.TYPE,
                (payload, context) -> StarCompassOverlay.accept(payload.target()));
        event.register(RopeSnapshotPayload.TYPE, (payload, context) -> {
            var level = net.minecraft.client.Minecraft.getInstance().level;
            if (level != null
                    && level.getBlockEntity(payload.connector())
                    instanceof com.altnoir.mementoinabyss.content.block.entity.RopeConnectorBlockEntity connector) {
                connector.acceptServerSnapshot(payload.points());
            }
        });
    }

    @SubscribeEvent
    public static void registerDebugEntries(RegisterDebugEntriesEvent event) {
        var id = MementoInAbyss.asResource("cross_dimension_lod");
        event.register(id, CrossDimensionLodDebugEntry.INSTANCE);
        event.includeInProfile(id, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.IN_OVERLAY);
        event.includeInProfile(id, DebugScreenProfile.PERFORMANCE, DebugScreenEntryStatus.IN_OVERLAY);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ArtifactEnhancementMaterialTooltip.clear();
        CrossDimensionLodRenderer.disconnect();
        CrossDimensionLodDebugEntry.clear();
        StarCompassOverlay.clear();
        WhistleComboHandler.reset();
        RopeFreeEndGrabHandler.clear();
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        TooltipModifierRegistry.get(event.getItemStack().getItem()).modify(event);
        ArtifactEnhancementMaterialTooltip.append(event);
    }

    @SubscribeEvent
    public static void adjustRopeLength(InputEvent.MouseScrollingEvent event) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.screen != null
                || !minecraft.player.isUsingItem()
                || event.getScrollDeltaY() == 0.0) {
            return;
        }
        var stack = minecraft.player.getUseItem();
        if (!(stack.getItem() instanceof RopeItem)
                || stack.get(MiaDataComponents.ROPE_ENDPOINT.get()) == null) {
            return;
        }
        event.setCanceled(true);
        int direction = event.getScrollDeltaY() > 0.0 ? 1 : -1;
        if (RopeItem.adjustSelectedLength(minecraft.player, stack, direction)) {
            ClientPacketDistributor.sendToServer(new AdjustRopeLengthPayload(direction));
        }
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        CrossDimensionLodRenderer.clientTick();
        WhistleComboHandler.tick();
        RopeFreeEndGrabHandler.tick();
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        if (event.getRecipeTypes().contains(MiaRecipes.ARTIFACT_ENHANCEMENT_TYPE.get())) {
            ArtifactEnhancementMaterialTooltip.update(event.getRecipeMap());
        }
    }

    private ClientEvent() {}
}
