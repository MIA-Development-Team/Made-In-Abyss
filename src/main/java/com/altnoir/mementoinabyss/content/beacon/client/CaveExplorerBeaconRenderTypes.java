package com.altnoir.mementoinabyss.content.beacon.client;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class CaveExplorerBeaconRenderTypes {
    private static final RenderPipeline PIPELINE =
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(MementoInAbyss.asResource("pipeline/cave_explorer_beacon_glow"))
                    .withVertexShader("core/rendertype_lightning")
                    .withFragmentShader("core/rendertype_lightning")
                    .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                    .withDepthStencilState(
                            new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .withCull(false)
                    .build();

    private static final RenderType GLOW =
            RenderType.create(
                    "mementoinabyss_beacon_glow",
                    RenderSetup.builder(PIPELINE).sortOnUpload().createRenderSetup());

    private CaveExplorerBeaconRenderTypes() {}

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }

    public static RenderType glow() {
        return GLOW;
    }
}
