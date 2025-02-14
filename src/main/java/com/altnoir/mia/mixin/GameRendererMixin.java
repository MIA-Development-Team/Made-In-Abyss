package com.altnoir.mia.mixin;

import com.altnoir.mia.Time;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    public ItemInHandRenderer itemInHandRenderer;
    @Shadow
    @Nullable
    PostChain postEffect;
    @Shadow
    @Final
    Minecraft minecraft;
    @Shadow
    private float itemActivationOffY;
    @Shadow
    @Final
    private RenderBuffers renderBuffers;
    @Shadow
    private boolean panoramicMode;
    @Shadow
    @Final
    private LightTexture lightTexture;

    protected GameRendererMixin() {
    }

    @Shadow
    public abstract void resetProjectionMatrix(Matrix4f p_253668_);

    @Shadow
    public abstract Matrix4f getProjectionMatrix(double p_254507_);

    @Shadow
    protected abstract double getFov(Camera p_109142_, float p_109143_, boolean p_109144_);

    @Shadow
    protected abstract void bobHurt(PoseStack p_109118_, float p_109119_);

    @Shadow
    protected abstract void bobView(PoseStack p_109139_, float p_109140_);

    @Shadow
    public abstract LightTexture lightTexture();

    @Inject(method = "loadEffect", at = @At("HEAD"), cancellable = true)
    public void loadEffect(ResourceLocation p_109129_, CallbackInfo ci) {
        if (Time.get()) {
            p_109129_ = new ResourceLocation("shaders/post/the_world.json");
        }
        if (postEffect != null && postEffect.getName().endsWith("the_world.json"))
            ci.cancel();
    }

    @Inject(method = "shutdownEffect", at = @At("HEAD"), cancellable = true)
    public void se(CallbackInfo ci) {
        if (Time.get() && minecraft.level != null && postEffect != null && postEffect.getName().endsWith("the_world.json"))
            ci.cancel();
    }

    @Inject(method = "checkEntityPostEffect", at = @At("HEAD"), cancellable = true)
    public void checkEntityPostEffect(Entity p_109107_, CallbackInfo ci) {
        if (Time.get()) {
            if (minecraft.level != null && postEffect != null && postEffect.getName().endsWith("the_world.json"))
                ci.cancel();
        }
    }


    @ModifyVariable(method = "renderItemInHand", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public float partials(float val) {
        return Time.timer.partialTick;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;F)V"), ordinal = 0, argsOnly = true)
    public float render(float val) {
        return Time.timer.partialTick;
    }

}