package com.altnoir.mementoinabyss.mixin.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface VerticalMinecraftAccessor {
    @Mutable
    @Accessor("levelRenderer")
    void mia$setLevelRenderer(LevelRenderer renderer);
}
