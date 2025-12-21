package com.altnoir.mia.mixin;

import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {
    @Redirect(method = "lambda$createFluidPicker$4", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private static int modify(int a, int b) {
        return Math.min(-64, b);
    }
}