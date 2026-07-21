package com.altnoir.mementoinabyss.impl.event;

import com.altnoir.mementoinabyss.client.render.CrossDimensionLodRenderer;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodRenderTypes;
import com.altnoir.mementoinabyss.network.CrossDimensionLodPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvent {
    @SubscribeEvent
    public static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        CrossDimensionLodRenderTypes.registerPipelines(event);
    }

    @SubscribeEvent
    public static void submitCustomGeometry(SubmitCustomGeometryEvent event) {
        CrossDimensionLodRenderer.submit(event);
    }

    @SubscribeEvent
    public static void renderCrossDimensionLod(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        CrossDimensionLodRenderer.renderPersistent(event);
    }

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(CrossDimensionLodPayload.TYPE,
                (payload, context) -> CrossDimensionLodRenderer.accept(payload));
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CrossDimensionLodRenderer.clear();
    }
}
