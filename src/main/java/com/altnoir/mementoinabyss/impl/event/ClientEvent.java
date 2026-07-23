package com.altnoir.mementoinabyss.impl.event;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.client.tooltip.TooltipModifierRegistry;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodDebugEntry;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodRenderer;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodRenderTypes;
import com.altnoir.mementoinabyss.client.render.EnvironmentCubeSkyboxRenderer;
import com.altnoir.mementoinabyss.client.render.StarCompassOverlay;
import com.altnoir.mementoinabyss.network.CompassTargetPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodDebugPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodPayload;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class ClientEvent {
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
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        CrossDimensionLodRenderer.clientTick();
    }

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(CrossDimensionLodPayload.TYPE,
                (payload, context) -> CrossDimensionLodRenderer.accept(payload));
        event.register(CrossDimensionLodDebugPayload.TYPE,
                (payload, context) -> CrossDimensionLodDebugEntry.accept(payload));
        event.register(CompassTargetPayload.TYPE,
                (payload, context) -> StarCompassOverlay.accept(payload.target()));
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
        CrossDimensionLodRenderer.disconnect();
        CrossDimensionLodDebugEntry.clear();
        StarCompassOverlay.clear();
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        TooltipModifierRegistry.get(event.getItemStack().getItem()).modify(event);
    }

    private ClientEvent() {}
}
