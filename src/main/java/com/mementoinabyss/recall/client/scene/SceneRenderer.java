/* Portions Copyright (c) 2022 The Create Team. SPDX-License-Identifier: MIT
 * Adapted from PonderSceneRenderer; See META-INF/licenses/ponder-MIT.txt. */
package com.mementoinabyss.recall.client.scene;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

/** Ponder's isolated PiP/feature render path, without its registry, level or mod entry points. */
public final class SceneRenderer extends PictureInPictureRenderer<SceneRenderState> {
    public SceneRenderer(BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    protected void renderToTexture(SceneRenderState state, PoseStack pose) {
        var gameRenderer = Minecraft.getInstance().gameRenderer;
        gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        var dispatcher = gameRenderer.getFeatureRenderDispatcher();
        pose.pushPose();
        try {
            pose.setIdentity();
            pose.mulPose(state.transform().matrix());
            pose.last().normal().set(state.transform().normalMatrix());
            state.scene().renderScene(state.frame(), dispatcher.getSubmitNodeStorage(), pose);
            dispatcher.renderAllFeatures();
            // Complete the block depth buffer before drawing the slightly expanded cuboid edges.
            bufferSource.endBatch();
            state.scene()
                    .renderOutline(
                            state.frame(),
                            state.selection(),
                            state.highlightAlpha(),
                            bufferSource,
                            pose);
            bufferSource.endBatch();
        } finally {
            pose.popPose();
        }
    }

    @Override
    protected String getTextureLabel() {
        return "recall scene";
    }

    @Override
    public Class<SceneRenderState> getRenderStateClass() {
        return SceneRenderState.class;
    }
}
