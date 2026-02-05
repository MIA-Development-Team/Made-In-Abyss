package com.altnoir.mia.mixin;

import com.altnoir.mia.init.MiaBlocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {
    @Inject(method = "createFluidPicker", at = @At("RETURN"), cancellable = true)
    private static void injectCreateFluidPicker(NoiseGeneratorSettings settings, CallbackInfoReturnable<Aquifer.FluidPicker> cir) {
        if (settings.defaultBlock() == MiaBlocks.ABYSS_ANDESITE.get().defaultBlockState()) {
            Aquifer.FluidStatus fluidStatus = new Aquifer.FluidStatus(settings.seaLevel(), settings.defaultFluid());
            Aquifer.FluidPicker modify = (x, y, z) -> fluidStatus;
            cir.setReturnValue(modify);
        }
    }
}