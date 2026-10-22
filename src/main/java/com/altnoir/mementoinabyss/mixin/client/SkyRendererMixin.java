package com.altnoir.mementoinabyss.mixin.client;

import com.altnoir.mementoinabyss.client.render.sky.OverworldAbyssSky;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @Inject(
            method = "shouldRenderDarkDisc(FLnet/minecraft/client/multiplayer/ClientLevel;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void mia$keepSkyAboveAbyssConnection(
            float deltaPartialTick, ClientLevel level, CallbackInfoReturnable<Boolean> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (OverworldAbyssSky.visible(level, minecraft.player.getX(), minecraft.player.getZ())) {
            cir.setReturnValue(false);
        }
    }
}
