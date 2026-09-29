/* Portions Copyright (c) 2022 The Create Team. SPDX-License-Identifier: MIT
 * Adapted from Catnip PonderRenderTypes; See META-INF/licenses/ponder-MIT.txt. */
package com.mementoinabyss.recall.client.render;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/** Only the solid cuboid outline material is needed by Recall. */
public final class RecallRenderTypes {
    private static final RenderType OUTLINE_SOLID =
            RenderType.create(
                    "recall:outline_solid",
                    RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
                            .bufferSize(256)
                            .withTexture(
                                    "Sampler0",
                                    Identifier.fromNamespaceAndPath("recall", "textures/blank.png"))
                            .useLightmap()
                            .useOverlay()
                            .createRenderSetup());

    public static RenderType outlineSolid() {
        return OUTLINE_SOLID;
    }

    private RecallRenderTypes() {}
}
