package com.altnoir.mementoinabyss.compat;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.irisshaders.iris.api.v0.IrisProgram;

public final class IrisRenderCompat {
    public static boolean isShaderPackInUse() {
        return MiaMods.IRIS.runIfInstalled(() -> IrisApi::isShaderPackInUse).orElse(false);
    }

    public static void assignTerrainSolidPipeline(RenderPipeline pipeline) {
        MiaMods.IRIS.executeIfInstalled(() -> () -> IrisApi.assignTerrainSolidPipeline(pipeline));
    }

    public static void assignSkyPipeline(RenderPipeline pipeline) {
        MiaMods.IRIS.executeIfInstalled(() -> () -> IrisApi.assignSkyPipeline(pipeline));
    }

    private static final class IrisApi {
        private static boolean isShaderPackInUse() {
            return net.irisshaders.iris.api.v0.IrisApi.getInstance().isShaderPackInUse();
        }

        private static void assignTerrainSolidPipeline(RenderPipeline pipeline) {
            net.irisshaders.iris.api.v0.IrisApi.getInstance()
                    .assignPipeline(pipeline, IrisProgram.TERRAIN_SOLID);
        }

        private static void assignSkyPipeline(RenderPipeline pipeline) {
            net.irisshaders.iris.api.v0.IrisApi.getInstance()
                    .assignPipeline(pipeline, IrisProgram.SKY_BASIC);
        }
    }

    private IrisRenderCompat() {}
}
