package com.altnoir.mementoinabyss.mixin.client;

import com.altnoir.mementoinabyss.client.render.LodJfrEvents;
import jdk.jfr.Event;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds an exact whole-frame interval to JFR without allocating while no recording is active. */
@Mixin(GameRenderer.class)
public abstract class GameRendererJfrMixin {
    @Unique
    private Event mia$renderFrameEvent;

    @Inject(method = "render", at = @At("HEAD"))
    private void mia$beginRenderFrame(DeltaTracker deltaTracker, boolean rendersLevel, CallbackInfo ci) {
        this.mia$renderFrameEvent = LodJfrEvents.beginClientFrame(rendersLevel);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void mia$endRenderFrame(DeltaTracker deltaTracker, boolean rendersLevel, CallbackInfo ci) {
        LodJfrEvents.endClientFrame(this.mia$renderFrameEvent);
        this.mia$renderFrameEvent = null;
    }
}
