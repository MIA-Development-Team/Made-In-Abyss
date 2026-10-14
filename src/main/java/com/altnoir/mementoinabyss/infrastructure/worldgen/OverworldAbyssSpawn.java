package com.altnoir.mementoinabyss.infrastructure.worldgen;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = MementoInAbyss.ID)
public final class OverworldAbyssSpawn {
    /** Clearance the rings keep past the carved slope, where the terrain is the original one. */
    private static final double RING_CLEARANCE = 0.5;

    private static final double RING_STEP = 0.5;
    private static final int RINGS = 6;
    private static final int ANGLES = 24;

    /** Block step of the in-chunk walk, so one chunk covers its 16x16 area with an 8x8 grid. */
    private static final int STEP = 2;

    /** Extra clearance past the abyss the fallback uses, where terrain is the original one. */
    private static final int FALLBACK_CLEARANCE = 64;

    /** Move only a new world's climate suggestion; vanilla still validates the actual spawn. */
    public static ChunkPos suggest(ServerLevel level, ChunkPos original) {
        OverworldAbyssShape shape = OverworldAbyss.shapeOf(level.getChunkSource().randomState());
        if (shape == null) return original;
        // Only a suggestion that landed on the modified terrain is moved.
        if (shape.distance(original.getMiddleBlockX(), original.getMiddleBlockZ())
                >= shape.outerLimit() + 32.0) {
            return original;
        }
        return suggested(shape);
    }

    /**
     * Radius of the first search ring: past the carved slope, so every candidate stands on original
     * terrain. It is derived from the shape rather than written as a multiple of the radius, because
     * the slope band is configurable and a fixed multiple drifts back inside the carve as soon as it
     * is widened.
     */
    private static double firstRing(OverworldAbyssShape shape) {
        return shape.outerLimit() + RING_CLEARANCE * shape.radius();
    }

    /** The chunk this mod proposes when the climate suggestion lands on the modified terrain. */
    public static ChunkPos suggested(OverworldAbyssShape shape) {
        return ChunkPos.containing(BlockPos.containing(firstRing(shape), 0, 0));
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        OverworldAbyssShape shape = OverworldAbyss.shapeOf(level.getChunkSource().randomState());
        if (shape == null) return;
        LevelData.RespawnData spawn =
                event.getServer().getWorldData().overworldData().getRespawnData();
        if (!spawn.dimension().equals(Level.OVERWORLD) || safe(level, spawn.pos())) return;

        // Only a hazardous global default is changed. Player beds/anchors are never touched.
        // Fixed order, bounded work: the search never crosses the abyss, where there is no ground.
        BlockPos safe = search(level, shape);
        if (safe == null) {
            BlockPos fallback = fallback(level, shape);
            if (fallback == null) {
                MementoInAbyss.LOGGER.warn(
                        "Could not find safe ground for the overworld abyss spawn within the"
                                + " bounded search; retained {}",
                        spawn.pos());
                return;
            }
            safe = fallback;
        }
        level.setRespawnData(
                LevelData.RespawnData.of(Level.OVERWORLD, safe, spawn.yaw(), spawn.pitch()));
        MementoInAbyss.LOGGER.info(
                "Moved unsafe overworld abyss spawn from {} to {}", spawn.pos(), safe);
    }

    /** The bounded ring search, starting past the carved slope so every step is dry land. */
    private static BlockPos search(ServerLevel level, OverworldAbyssShape shape) {
        double start = firstRing(shape);
        for (int ring = 0; ring < RINGS; ring++) {
            double radius = start + ring * RING_STEP * shape.radius();
            for (int angle = 0; angle < ANGLES; angle++) {
                double radians = angle * (Math.PI * 2.0 / ANGLES);
                ChunkPos chunk =
                        ChunkPos.containing(
                                BlockPos.containing(
                                        radius * Math.cos(radians), 0, radius * Math.sin(radians)));
                // Cheap rejection first: one column decides whether the whole chunk is worth
                // sampling, so a candidate in the ocean costs one heightmap instead of sixty-four.
                if (!safe(
                        level, heightPos(level, chunk.getMiddleBlockX(), chunk.getMiddleBlockZ())))
                    continue;
                for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x += STEP) {
                    for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z += STEP) {
                        BlockPos candidate = heightPos(level, x, z);
                        if (safe(level, candidate)) return candidate;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Last resort past the modified terrain. It is only reached when the ring search saw nothing dry
     * at all, which means the world is water everywhere around the abyss; the center of the last ring
     * is at least dry ground in that case because the outer slope there is above the sea.
     */
    private static BlockPos fallback(ServerLevel level, OverworldAbyssShape shape) {
        int distance = (int) Math.ceil(4.0 * shape.radius()) + FALLBACK_CLEARANCE;
        for (int[] direction : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            BlockPos candidate = heightPos(level, distance * direction[0], distance * direction[1]);
            if (safe(level, candidate)) return candidate;
        }
        return null;
    }

    private static BlockPos heightPos(ServerLevel level, int x, int z) {
        return level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
    }

    /**
     * Dry, completely solid floor with two blocks of free space above it. Lava and other liquids are
     * rejected both as the floor and as the standing space, so a spawn is never dropped into a pool
     * even when the floor below it would carry a player.
     */
    private static boolean safe(ServerLevel level, BlockPos pos) {
        if (pos.getY() <= level.getMinY()
                || pos.getY() + 1 >= level.getMaxY()
                || !level.getWorldBorder().isWithinBounds(pos)) return false;
        BlockPos floorPos = pos.below();
        BlockState floor = level.getBlockState(floorPos);
        if (!floor.getFluidState().isEmpty()
                || !Block.isFaceFull(floor.getCollisionShape(level, floorPos), Direction.UP)) {
            return false;
        }
        return passable(level.getBlockState(pos)) && passable(level.getBlockState(pos.above()));
    }

    private static boolean passable(BlockState state) {
        return !state.is(Blocks.LAVA)
                && !state.is(Blocks.FIRE)
                && !state.is(Blocks.SOUL_FIRE)
                && !state.is(Blocks.MAGMA_BLOCK)
                && !state.is(Blocks.CAMPFIRE)
                && !state.is(Blocks.SOUL_CAMPFIRE)
                && !state.is(Blocks.POWDER_SNOW)
                && !state.is(Blocks.CACTUS)
                && !state.is(Blocks.SWEET_BERRY_BUSH)
                && !state.is(Blocks.WITHER_ROSE)
                && state.getFluidState().isEmpty();
    }

    private OverworldAbyssSpawn() {}
}
