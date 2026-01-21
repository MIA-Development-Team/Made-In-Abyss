package com.altnoir.mia.event.client;

import com.altnoir.mia.block.entity.renderer.AbyssSpawnerRenderer;
import com.altnoir.mia.block.entity.renderer.EndlessCupRenderer;
import com.altnoir.mia.block.entity.renderer.PedestalRenderer;
import com.altnoir.mia.block.entity.renderer.SunStoneRenderer;
import com.altnoir.mia.client.renderer.HookEntityRenderer;
import com.altnoir.mia.init.MiaBlockEntities;
import com.altnoir.mia.init.MiaEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class RegisterEntityRendererEvent {
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MiaBlockEntities.PEDESTAL_ENTITY.get(), PedestalRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.ENDLESS_CUP_ENTITY.get(), EndlessCupRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.SUN_STONE_ENTITY.get(), SunStoneRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.ABYSS_SPAWNER_ENTITY.get(), AbyssSpawnerRenderer::new);
        event.registerEntityRenderer(MiaEntities.HOOK.get(), HookEntityRenderer::new);
    }
}
