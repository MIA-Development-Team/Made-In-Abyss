package com.altnoir.mementoinabyss.mixin.worldgen;

import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$Ap2")
public interface DensityPairAccessor {
    @Accessor("argument1")
    DensityFunction mia$first();

    @Accessor("argument2")
    DensityFunction mia$second();
}
