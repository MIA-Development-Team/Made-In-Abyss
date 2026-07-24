package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.compat.iris.IrisRenderCompat;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/** Render state for greedy LOD quads whose block sprites repeat in the shader. */
public final class CrossDimensionLodRenderTypes {
    /** Compact native format containing packed sprite bounds for atlas-safe repetition. */
    public static final VertexFormat LOD_VERTEX_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("UV1", VertexFormatElement.UV1)
            .add("UV2", VertexFormatElement.UV2)
            .add("Normal", VertexFormatElement.NORMAL)
            .padding(1)
            .build();
    /**
     * Terrain-compatible format used while an Iris shader pack is active.
     * Light coordinates and normals are required by terrain gbuffers; omitting
     * either makes many shader packs treat the LOD as unlit textured geometry.
     */
    public static final VertexFormat IRIS_VERTEX_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV0)
            .add("UV2", VertexFormatElement.UV2)
            .add("Normal", VertexFormatElement.NORMAL)
            .padding(1)
            .build();

    private static final RenderPipeline TILED_BLOCKS_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(MementoInAbyss.asResource("pipeline/cross_dimension_lod"))
            .withVertexShader(MementoInAbyss.asResource("core/cross_dimension_lod"))
            .withFragmentShader(MementoInAbyss.asResource("core/cross_dimension_lod"))
            .withSampler("Sampler0")
            .withUniform("LodFog", UniformType.UNIFORM_BUFFER)
            .withUniform("LodLight", UniformType.UNIFORM_BUFFER)
            .withVertexFormat(LOD_VERTEX_FORMAT, VertexFormat.Mode.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .build();
    private static final RenderPipeline IRIS_BLOCKS_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(MementoInAbyss.asResource("pipeline/cross_dimension_lod_iris"))
            .withVertexShader("core/position_tex_color")
            .withFragmentShader("core/position_tex_color")
            .withSampler("Sampler0")
            .withVertexFormat(IRIS_VERTEX_FORMAT, VertexFormat.Mode.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .build();

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(TILED_BLOCKS_PIPELINE);
        event.registerPipeline(IRIS_BLOCKS_PIPELINE);
        IrisRenderCompat.assignTerrainSolidPipeline(IRIS_BLOCKS_PIPELINE);
    }

    public static RenderPipeline tiledBlocksPipeline() {
        return TILED_BLOCKS_PIPELINE;
    }

    public static RenderPipeline irisBlocksPipeline() {
        return IRIS_BLOCKS_PIPELINE;
    }

    private CrossDimensionLodRenderTypes() {}
}
