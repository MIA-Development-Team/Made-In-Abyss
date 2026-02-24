package com.altnoir.mia.core.event.client;

import com.altnoir.mia.client.renderer.HookEntityRenderer;
import com.altnoir.mia.common.block.entity.renderer.*;
import com.altnoir.mia.init.MiaBlockEntities;
import com.altnoir.mia.init.MiaEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class RegisterEntityRendererEvent {
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MiaBlockEntities.PEDESTAL_ENTITY.get(), PedestalRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.ABYSS_SPAWNER_ENTITY.get(), AbyssSpawnerRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.BRUSHABLE_ENTITY.get(), MiaBrushableRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.ENDLESS_CUP_ENTITY.get(), EndlessCupRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.CAVE_EXPLORER_BEACON_ENTITY.get(), CaveExplorerBeaconRenerer::new);
        event.registerEntityRenderer(MiaEntities.HOOK.get(), HookEntityRenderer::new);
    }
}
