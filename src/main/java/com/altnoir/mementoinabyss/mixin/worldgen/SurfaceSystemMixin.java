package com.altnoir.mementoinabyss.mixin.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.OverworldAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssHole;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SurfaceSystem.class)
public abstract class SurfaceSystemMixin {
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
            @Local(argsOnly = true) RandomState state) {
        original.call(system, mia$guardColumn(column, x, z, state), x, z, height, chunk);
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
            @Local(argsOnly = true) RandomState state) {
        original.call(
                system, minSurface, biome, mia$guardColumn(column, x, z, state), pos, x, z, height);
    }

    @Unique private static BlockColumn mia$guardColumn(
            BlockColumn column, int x, int z, RandomState state) {
        OverworldAbyssHole hole = OverworldAbyss.holeOf(state);
        if (hole == null || hole.shape().distance(x, z) >= 2.0 * hole.shape().radius())
            return column;
        return new BlockColumn() {
            @Override
            public BlockState getBlock(int y) {
                return column.getBlock(y);
            }

            @Override
            public void setBlock(int y, BlockState block) {
                if (!hole.isOpening(new DensityFunction.SinglePointContext(x, y, z)))
                    column.setBlock(y, block);
            }
        };
    }
}
