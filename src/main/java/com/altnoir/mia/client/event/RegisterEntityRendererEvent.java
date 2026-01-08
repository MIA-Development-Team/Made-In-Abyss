package com.altnoir.mia.client.event;

import com.altnoir.mia.block.entity.renderer.AbyssSpawnerRenderer;
import com.altnoir.mia.block.entity.renderer.EndlessCupBlockRenderer;
import com.altnoir.mia.block.entity.renderer.PedestalBlockRenderer;
import com.altnoir.mia.block.entity.renderer.SunStoneBlockRenderer;
import com.altnoir.mia.init.MiaBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@OnlyIn(Dist.CLIENT)
public class RegisterEntityRendererEvent {
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MiaBlockEntities.PEDESTAL_ENTITY.get(), PedestalBlockRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.ENDLESS_CUP_BLOCK_ENTITY.get(), EndlessCupBlockRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.SUN_STONE_BLOCK_ENTITY.get(), SunStoneBlockRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.ABYSS_SPAWNER_ENTITY.get(), AbyssSpawnerRenderer::new);
    }
}
