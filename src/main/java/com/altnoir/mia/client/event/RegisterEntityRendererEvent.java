package com.altnoir.mia.client.event;

import com.altnoir.mia.block.entity.renderer.*;
import com.altnoir.mia.init.MiaBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@OnlyIn(Dist.CLIENT)
public class RegisterEntityRendererEvent {
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MiaBlockEntities.PEDESTAL_ENTITY.get(), PedestalRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.ENDLESS_CUP_ENTITY.get(), EndlessCupRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.SUN_STONE_ENTITY.get(), SunStoneRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.ABYSS_SPAWNER_ENTITY.get(), AbyssSpawnerRenderer::new);
    }
}
