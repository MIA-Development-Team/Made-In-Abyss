package com.altnoir.mementoinabyss.mixin.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.OverworldAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssInterpolated;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SurfaceSystem.class)
public abstract class SurfaceSystemMixin {
    @ModifyVariable(method = "buildSurface", at = @At("HEAD"), argsOnly = true)
    private SurfaceRules.RuleSource mia$addOverworldHabitatSurface(
            SurfaceRules.RuleSource original, @Local(argsOnly = true) RandomState state) {
        return OverworldAbyss.holeOf(state) == null
                ? original
                : com.altnoir.mementoinabyss.infrastructure.worldgen.noise
                        .OverworldAbyssSurfaceRules.withOriginal(original);
    }

    @Inject(method = "buildSurface", at = @At("RETURN"))
    private void mia$replaceInnerBedrockWithDeepslate(
            RandomState state,
            BiomeManager biomeManager,
            Registry<Biome> biomes,
            boolean legacy,
            WorldGenerationContext context,
            ChunkAccess chunk,
            NoiseChunk noiseChunk,
            SurfaceRules.RuleSource rules,
            CallbackInfo ci) {
        var hole = OverworldAbyss.holeOf(state);
        if (hole == null || !hole.composed()) return;
        var shape = hole.shape();
        int minX = chunk.getPos().getMinBlockX(), minZ = chunk.getPos().getMinBlockZ();
        double limit = 2.0 * shape.radius();
        if (shape.distance(Math.clamp(0, minX, minX + 15), Math.clamp(0, minZ, minZ + 15)) > limit)
            return;
        double limitSquared = limit * limit;
        var pos = new BlockPos.MutableBlockPos();
        var sections = chunk.getSections();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            var section = sections[sectionIndex];
            // Custom surface rules can place bedrock above the ordinary five-layer floor.
            // Skip palettes without bedrock rather than scanning the full world height.
            if (!section.maybeHas(block -> block.is(Blocks.BEDROCK))) continue;
            int minY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;
            for (int x = minX; x < minX + 16; x++)
                for (int z = minZ; z < minZ + 16; z++) {
                    if ((double) x * x + (double) z * z > limitSquared) continue;
                    for (int y = minY; y < minY + 16; y++) {
                        pos.set(x, y, z);
                        if (chunk.getBlockState(pos).is(Blocks.BEDROCK))
                            chunk.setBlockState(pos, Blocks.DEEPSLATE.defaultBlockState());
                    }
                }
        }
    }

    @WrapOperation(
            method = "buildSurface",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/SurfaceSystem;erodedBadlandsExtension(Lnet/minecraft/world/level/chunk/BlockColumn;IIILnet/minecraft/world/level/LevelHeightAccessor;)V"))
    private void mia$protectBadlandsOpening(
            SurfaceSystem system,
            BlockColumn column,
            int x,
            int z,
            int height,
            LevelHeightAccessor chunk,
            Operation<Void> original,
            @Local(argsOnly = true) RandomState state,
            @Local(argsOnly = true) NoiseChunk noiseChunk) {
        original.call(
                system, mia$guardColumn(column, x, z, state, noiseChunk), x, z, height, chunk);
    }

    @WrapOperation(
            method = "buildSurface",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/SurfaceSystem;frozenOceanExtension(ILnet/minecraft/world/level/biome/Biome;Lnet/minecraft/world/level/chunk/BlockColumn;Lnet/minecraft/core/BlockPos$MutableBlockPos;III)V"))
    private void mia$protectIcebergOpening(
            SurfaceSystem system,
            int minSurface,
            Biome biome,
            BlockColumn column,
            BlockPos.MutableBlockPos pos,
            int x,
            int z,
            int height,
            Operation<Void> original,
            @Local(argsOnly = true) RandomState state,
            @Local(argsOnly = true) NoiseChunk noiseChunk) {
        original.call(
                system,
                minSurface,
                biome,
                mia$guardColumn(column, x, z, state, noiseChunk),
                pos,
                x,
                z,
                height);
    }

    @Unique private static BlockColumn mia$guardColumn(
            BlockColumn column, int x, int z, RandomState state, NoiseChunk noiseChunk) {
        OverworldAbyssHole hole = OverworldAbyss.holeOf(state);
        var seam =
                com.altnoir.mementoinabyss.infrastructure.worldgen.AbyssSeamRuntime.seamOf(state);
        if (seam == null
                && (hole == null || hole.shape().distance(x, z) >= 2.0 * hole.shape().radius()))
            return column;
        return new BlockColumn() {
            @Override
            public BlockState getBlock(int y) {
                return column.getBlock(y);
            }

            @Override
            public void setBlock(int y, BlockState block) {
                if (seam != null
                        && com.altnoir.mementoinabyss.infrastructure.worldgen.AbyssSeamRuntime
                                .isOpening(
                                        seam,
                                        new net.minecraft.world.level.levelgen.DensityFunction
                                                .SinglePointContext(x, y, z))) return;
                if (hole == null
                        || !hole.isOpening(
                                OverworldAbyssInterpolated.context(
                                        x, y, z, noiseChunk.getBlender())))
                    column.setBlock(y, block);
            }
        };
    }

    @WrapOperation(
            method = "buildSurface",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/chunk/BlockColumn;setBlock(ILnet/minecraft/world/level/block/state/BlockState;)V"))
    private void mia$protectSurfaceOpening(
            BlockColumn column,
            int y,
            BlockState block,
            Operation<Void> original,
            @Local(argsOnly = true) RandomState state,
            @Local(ordinal = 0) BlockPos.MutableBlockPos columnPos) {
        var seam =
                com.altnoir.mementoinabyss.infrastructure.worldgen.AbyssSeamRuntime.seamOf(state);
        if (seam != null
                && com.altnoir.mementoinabyss.infrastructure.worldgen.AbyssSeamRuntime.isOpening(
                        seam,
                        new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(
                                columnPos.getX(), y, columnPos.getZ()))) return;
        original.call(column, y, block);
    }
}
