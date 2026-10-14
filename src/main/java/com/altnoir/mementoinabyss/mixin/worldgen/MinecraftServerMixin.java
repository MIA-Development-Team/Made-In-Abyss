package com.altnoir.mementoinabyss.mixin.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.OverworldAbyssSpawn;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @ModifyExpressionValue(
            method = "setInitialSpawn",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/ChunkPos;containing(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/ChunkPos;"))
    private static ChunkPos mia$suggestOuterSlope(
            ChunkPos original, @Local(argsOnly = true) ServerLevel level) {
        return OverworldAbyssSpawn.suggest(level, original);
    }
}
