package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/** Render state for greedy LOD quads whose block sprites repeat in the shader. */
public final class CrossDimensionLodRenderTypes {
    private static final RenderPipeline TILED_BLOCKS_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(MementoInAbyss.asResource("pipeline/cross_dimension_lod"))
            .withVertexShader(MementoInAbyss.asResource("core/cross_dimension_lod"))
            .withFragmentShader(MementoInAbyss.asResource("core/cross_dimension_lod"))
            .withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .build();

    public static final RenderType TILED_BLOCKS = RenderType.create("mementoinabyss_cross_dimension_lod",
            RenderSetup.builder(TILED_BLOCKS_PIPELINE)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
                    .createRenderSetup());

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(TILED_BLOCKS_PIPELINE);
    }

    public static RenderPipeline tiledBlocksPipeline() {
        return TILED_BLOCKS_PIPELINE;
    }

    private CrossDimensionLodRenderTypes() {}
}
