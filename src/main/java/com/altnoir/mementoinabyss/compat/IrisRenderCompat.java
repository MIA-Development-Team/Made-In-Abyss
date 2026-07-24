package com.altnoir.mementoinabyss.compat;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.irisshaders.iris.api.v0.IrisProgram;

public final class IrisRenderCompat {
    public static boolean isShaderPackInUse() {
        return MiaMods.IRIS.runIfInstalled(() -> IrisApi::isShaderPackInUse).orElse(false);
    }

    public static void assignTexturedPipeline(RenderPipeline pipeline) {
        MiaMods.IRIS.executeIfInstalled(() -> () -> IrisApi.assignTexturedPipeline(pipeline));
    }

    public static void assignSkyPipeline(RenderPipeline pipeline) {
        MiaMods.IRIS.executeIfInstalled(() -> () -> IrisApi.assignSkyPipeline(pipeline));
    }

    private static final class IrisApi {
        private static boolean isShaderPackInUse() {
            return net.irisshaders.iris.api.v0.IrisApi.getInstance().isShaderPackInUse();
        }

        private static void assignTexturedPipeline(RenderPipeline pipeline) {
            net.irisshaders.iris.api.v0.IrisApi.getInstance()
                    .assignPipeline(pipeline, IrisProgram.TEXTURED);
        }

        private static void assignSkyPipeline(RenderPipeline pipeline) {
            net.irisshaders.iris.api.v0.IrisApi.getInstance()
                    .assignPipeline(pipeline, IrisProgram.SKY_BASIC);
        }
    }

    private IrisRenderCompat() {}
}
