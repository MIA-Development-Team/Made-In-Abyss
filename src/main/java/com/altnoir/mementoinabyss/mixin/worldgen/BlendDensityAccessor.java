package com.altnoir.mementoinabyss.mixin.worldgen;

import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read the vanilla terrain input without serializing or guessing its private record layout. */
@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$BlendDensity")
public interface BlendDensityAccessor {
    @Accessor("input")
    DensityFunction mia$input();
}
