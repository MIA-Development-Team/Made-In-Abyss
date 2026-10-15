package com.altnoir.mementoinabyss.mixin.worldgen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$YClampedGradient")
public interface DensityGradientAccessor {
    @Accessor("fromY")
    int mia$fromY();

    @Accessor("toY")
    int mia$toY();

    @Accessor("fromValue")
    double mia$fromValue();

    @Accessor("toValue")
    double mia$toValue();
}
