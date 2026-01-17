package com.altnoir.mia.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class MiaRenderTypes extends RenderType {
    public MiaRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
    
    public static final RenderType ABYSS_ORB = create(
        "mia_abyss_orb",
        DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
        VertexFormat.Mode.TRIANGLES,
        1536,
        false,
        true,
        CompositeState.builder()
            .setShaderState(POSITION_COLOR_LIGHTMAP_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .createCompositeState(false)
    );
    
    public static final RenderType ABYSS_ORB_GLOW = create(
        "mia_abyss_orb_glow",
        DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
        VertexFormat.Mode.TRIANGLES,
        1536,
        false,
        true,
        CompositeState.builder()
            .setShaderState(POSITION_COLOR_LIGHTMAP_SHADER)
            .setTransparencyState(ADDITIVE_TRANSPARENCY)
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .createCompositeState(false)
    );
}
