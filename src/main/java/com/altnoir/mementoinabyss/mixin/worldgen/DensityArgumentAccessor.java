package com.altnoir.mementoinabyss.mixin.worldgen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$MulOrAdd")
public interface DensityArgumentAccessor {
    @Accessor("argument")
    double mia$argument();
}
