package com.altnoir.mia.client.event;

import com.altnoir.mia.block.entity.renderer.EndlessCupBlockRenderer;
import com.altnoir.mia.block.entity.renderer.PedestalBlockRenderer;
import com.altnoir.mia.init.MiaBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@OnlyIn(Dist.CLIENT)
public class RegisterEntityRendererEvent {
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MiaBlockEntities.PEDESTAL.get(), PedestalBlockRenderer::new);
        event.registerBlockEntityRenderer(MiaBlockEntities.ENDLESS_CUP_BLOCK_ENTITY.get(), EndlessCupBlockRenderer::new);
    }
}
