package com.altnoir.mementoinabyss.infrastructure.worldgen.placement;

import com.altnoir.mementoinabyss.init.MiaPlacementModifiers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class Layer2SurfacePlacement extends PlacementModifier {
    private static final Layer2SurfacePlacement INSTANCE =
            new Layer2SurfacePlacement(Heightmap.Types.WORLD_SURFACE_WG, -1);
    public static final MapCodec<Layer2SurfacePlacement> CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(
                                            Heightmap.Types.CODEC
                                                    .optionalFieldOf(
                                                            "heightmap",
                                                            Heightmap.Types.WORLD_SURFACE_WG)
                                                    .forGetter(Layer2SurfacePlacement::heightmap),
                                            Codec.intRange(-1, DimensionType.Y_SIZE)
                                                    .optionalFieldOf("max_water_depth", -1)
                                                    .forGetter(value -> value.maxWaterDepth))
                                    .apply(instance, Layer2SurfacePlacement::of));

    private final Heightmap.Types heightmap;
    private final int maxWaterDepth;

    private Layer2SurfacePlacement(Heightmap.Types heightmap, int maxWaterDepth) {
        this.heightmap = heightmap;
        this.maxWaterDepth = maxWaterDepth;
    }

    public static Layer2SurfacePlacement of() {
        return INSTANCE;
    }

    public static Layer2SurfacePlacement of(Heightmap.Types heightmap) {
        return of(heightmap, -1);
    }

    public static Layer2SurfacePlacement of(Heightmap.Types heightmap, int maxWaterDepth) {
        return heightmap == Heightmap.Types.WORLD_SURFACE_WG && maxWaterDepth == -1
                ? INSTANCE
                : new Layer2SurfacePlacement(heightmap, maxWaterDepth);
    }

    public Heightmap.Types heightmap() {
        return heightmap;
    }

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext context, RandomSource random, BlockPos origin) {
        var positions = Stream.<BlockPos>builder();
        boolean oceanFloor =
                heightmap == Heightmap.Types.OCEAN_FLOOR
                        || heightmap == Heightmap.Types.OCEAN_FLOOR_WG;
        var surface =
                oceanFloor
                        ? LayeredPlacementScan.Surface.LAYER2_FLOOR
                        : LayeredPlacementScan.Surface.LAYER2;
        var scan = new LayeredPlacementScan(context, surface, -128, 0);
        var level = context.getLevel();
        int x = origin.getX(), z = origin.getZ();
        for (int layer = 0; ; layer++) {
            int y = scan.find(x, z, layer);
            if (y == Integer.MAX_VALUE) break;
            BlockPos position = new BlockPos(x, y, z);
            BlockPos support = position.below();
            BlockState floor = level.getBlockState(support);
            boolean supported =
                    (!oceanFloor && floor.is(Blocks.WATER))
                            || floor.isFaceSturdy(level, support, Direction.UP);
            if (!supported) continue;
            if (maxWaterDepth >= 0) {
                var cursor = position.mutable();
                int depth = 0;
                while (depth <= maxWaterDepth && level.getFluidState(cursor).is(FluidTags.WATER)) {
                    depth++;
                    cursor.move(Direction.UP);
                }
                if (depth > maxWaterDepth) continue;
            }
            BlockState above = level.getBlockState(position.above());
            if (oceanFloor
                    ? LayeredPlacementScan.Surface.waterOrReplaceable(above)
                    : headroom(above)) positions.add(position);
        }
        return positions.build();
    }

    private static boolean headroom(BlockState state) {
        return state.getFluidState().isEmpty() && (state.isAir() || state.canBeReplaced());
    }

    @Override
    public PlacementModifierType<?> type() {
        return MiaPlacementModifiers.LAYER2_SURFACE.get();
    }
}
