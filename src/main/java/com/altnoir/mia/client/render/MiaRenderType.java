package com.altnoir.mia.client.render;

import com.altnoir.mia.util.MiaUtil;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MiaRenderType extends RenderType {
    protected static final ResourceLocation CLOUDS_LOCATION = MiaUtil.miaId("textures/environment/clouds.png");

    public MiaRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
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
    public static final RenderType ABYSS_CLOUDS = createClouds(false);
    public static final RenderType ABYSS_CLOUDS_DEPTH_ONLY = createClouds(true);

    public static RenderType clouds() {
        return ABYSS_CLOUDS;
    }
    public static RenderType cloudsDepthOnly() {
        return ABYSS_CLOUDS_DEPTH_ONLY;
    }

    private static RenderType createClouds(boolean color) {
        return create(
                "abyss_clouds",
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL,
                VertexFormat.Mode.QUADS,
                786432,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_CLOUDS_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(CLOUDS_LOCATION, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setWriteMaskState(color ? DEPTH_WRITE : COLOR_DEPTH_WRITE)
                        .setOutputState(CLOUDS_TARGET)
                        .createCompositeState(true)
        );
    }

}
