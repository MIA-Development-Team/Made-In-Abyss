package com.altnoir.mia.mixin;

import com.altnoir.mia.Time;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public class CameraMixin {
    @ModifyVariable(method = "setup", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public float render(float val) {
        return Time.get() ? Time.timer.partialTick : Minecraft.getInstance().realPartialTick;
    }
}