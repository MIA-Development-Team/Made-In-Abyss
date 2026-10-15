package com.altnoir.mementoinabyss.mixin.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.OverworldAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.OverworldAbyssCarvers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.function.Supplier;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorCarverMixin {
    @WrapOperation(
            method = "applyCarvers",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/chunk/ChunkAccess;carverBiome(Ljava/util/function/Supplier;)Lnet/minecraft/world/level/biome/BiomeGenerationSettings;"))
    private BiomeGenerationSettings mia$restoreOverworldCarverSource(
            ChunkAccess sourceChunk,
            Supplier<BiomeGenerationSettings> settings,
            Operation<BiomeGenerationSettings> original,
            @Local(argsOnly = true) RandomState state) {
        var generator = (NoiseBasedChunkGenerator) (Object) this;
        var visibleSource = generator.getBiomeSource();
        var position = sourceChunk.getPos();
        var carverSource =
                OverworldAbyssCarvers.sourceForCarvers(
                        visibleSource,
                        OverworldAbyss.shapeOf(state),
                        position.getMinBlockX(),
                        position.getMinBlockZ());
        if (carverSource == visibleSource) return original.call(sourceChunk, settings);
        var sampler = OverworldAbyssCarvers.samplerForCarvers(state);
        // Preserve ChunkAccess's lazy cache, the generator's settings resolver and other mods'
        // source biome carvers. Restore the original climate sampler as well as source biome
        // selection; placed biome identity stays intact.
        return original.call(
                sourceChunk,
                (Supplier<BiomeGenerationSettings>)
                        () ->
                                generator.getBiomeGenerationSettings(
                                        carverSource.getNoiseBiome(
                                                QuartPos.fromBlock(position.getMinBlockX()),
                                                0,
                                                QuartPos.fromBlock(position.getMinBlockZ()),
                                                sampler)));
    }
}
