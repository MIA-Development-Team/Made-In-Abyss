package com.altnoir.mia.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class GlowRenderLayer extends RenderType {
    public static RenderTarget fbo = null;
    private final RenderType delegate;

    public GlowRenderLayer(RenderType delegate, float[] rgba, float softness) {
        this(delegate, rgba, softness, true);
    }

    public GlowRenderLayer(RenderType delegate, float[] rgba, float softness, boolean shaders) {
        super("magic" + delegate.toString() + "_with_framebuffer", delegate.format(), delegate.mode(), delegate.bufferSize(), true, delegate.isOutline(), () -> {
            delegate.setupRenderState();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            GL11.glDepthFunc(513);
            GL11.glDepthMask(false);
        }, () -> {
        });
        this.delegate = delegate;
    }

    public Optional<RenderType> outline() {
        return this.delegate.outline();
    }

    public boolean equals(@Nullable Object other) {
        return (other instanceof GlowRenderLayer && this.delegate
                .equals(((GlowRenderLayer) other).delegate));
    }

    public int hashCode() {
        return Objects.hash(this.delegate);
    }
}