package com.altnoir.mementoinabyss.infrastructure.worldgen.feature;

import com.altnoir.mementoinabyss.infrastructure.worldgen.layer2.Layer2Runtime;
import com.altnoir.mementoinabyss.init.MiaTags;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class GiantFossilizedStumpFeature extends Feature<GiantFossilizedStumpConfiguration> {
    private static final int EXTENT = 16;
    private static final int WIDTH = EXTENT * 2 + 1;

    public GiantFossilizedStumpFeature(Codec<GiantFossilizedStumpConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GiantFossilizedStumpConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        var runtime = Layer2Runtime.state(level.getLevel().getChunkSource().randomState());
        if (runtime != null) {
            var column = runtime.column(origin.getX(), origin.getZ());
            if (column.pillar() || Math.abs(origin.getY() - column.height()) > 12) return false;
        }
        RandomSource random = context.random();
        var config = context.config();
        int diameter = config.diameter().sample(random);
        int height = diameter + config.extraHeight().sample(random);
        int rootDepth = config.rootDepth().sample(random);
        if (origin.getY() - rootDepth - 4 < level.getMinY()
                || origin.getY() + height + 4 >= level.getMaxY()) return false;

        double radius = diameter / 2.0;
        double phase = random.nextDouble() * Math.PI * 2;
        int[] grounds = new int[WIDTH * WIDTH];
        Arrays.fill(grounds, Integer.MIN_VALUE);
        var blocks =
                new Long2ObjectLinkedOpenHashMap<BlockState>(
                        (int) (radius * radius * height * 3.2));
        var tops = new ArrayList<BlockPos>();
        var cursor = new BlockPos.MutableBlockPos();
        int minGround = Integer.MAX_VALUE, maxGround = Integer.MIN_VALUE;
        int bound = (int) Math.ceil(radius);
        for (int dx = -bound; dx <= bound; dx++) {
            for (int dz = -bound; dz <= bound; dz++) {
                double distance = Math.hypot(dx, dz);
                double angle = Math.atan2(dz, dx);
                double edge =
                        radius
                                - 0.45
                                - 0.35 * Math.sin(5 * angle + phase)
                                - 0.2 * Math.sin(9 * angle - phase);
                if (distance > edge) continue;
                double breakDepth =
                        1.2 * (1 + Math.sin(3 * angle + phase))
                                + 0.6 * (1 + Math.sin(dx * 0.4 + dz * 0.3 + phase));
                double taper = Math.clamp((edge - distance) / (radius * 0.12), 0, 1);
                int top =
                        Math.min(
                                height - (int) Math.round(breakDepth),
                                (int) Math.floor(height * taper));
                if (top < 1) continue;
                int ground = ground(level, origin, dx, dz, grounds, cursor);
                if (ground == Integer.MIN_VALUE) return false;
                minGround = Math.min(minGround, ground);
                maxGround = Math.max(maxGround, ground);
                if (maxGround - minGround > 4) return false;
                int topY = origin.getY() + top - 1;
                for (int y = ground - 1; y <= topY; y++) {
                    cursor.set(origin.getX() + dx, y, origin.getZ() + dz);
                    if (!level.ensureCanWrite(cursor)) return false;
                    BlockState existing = level.getBlockState(cursor);
                    if (y <= ground) {
                        if (!naturalGround(existing)) continue;
                    } else if (!replaceable(existing)) return false;
                    boolean mossy = random.nextFloat() < config.mossyChance();
                    BlockState state =
                            y == topY
                                    ? (mossy ? config.mossyTopWood() : config.topWood())
                                    : (mossy ? config.mossyWood() : config.wood());
                    blocks.put(cursor.asLong(), state);
                }
                if (distance < radius * 0.75 && top > diameter)
                    tops.add(new BlockPos(origin.getX() + dx, topY, origin.getZ() + dz));
            }
        }
        if (tops.isEmpty()) return false;
        int roots = 5 + random.nextInt(3);
        for (int root = 0; root < roots; root++) {
            double angle = phase + root * Math.PI * 2 / roots + (random.nextDouble() - 0.5) * 0.4;
            int length = Math.min(EXTENT - 1, (int) Math.ceil(radius) + 2 + random.nextInt(3));
            for (int step = (int) (radius * 0.5); step <= length; step++) {
                int centerX = (int) Math.round(Math.cos(angle) * step);
                int centerZ = (int) Math.round(Math.sin(angle) * step);
                int width = step < length ? 1 : 0;
                for (int dx = centerX - width; dx <= centerX + width; dx++)
                    for (int dz = centerZ - width; dz <= centerZ + width; dz++) {
                        int ground = ground(level, origin, dx, dz, grounds, cursor);
                        if (ground == Integer.MIN_VALUE) continue;
                        int depth = 2 + (int) Math.round((rootDepth - 2) * (double) step / length);
                        for (int y = ground - depth; y <= ground - 2; y++) {
                            cursor.set(origin.getX() + dx, y, origin.getZ() + dz);
                            if (level.ensureCanWrite(cursor)
                                    && naturalGround(level.getBlockState(cursor))
                                    && !blocks.containsKey(cursor.asLong()))
                                blocks.put(
                                        cursor.asLong(),
                                        random.nextFloat() < config.mossyChance()
                                                ? config.mossyWood()
                                                : config.wood());
                        }
                    }
            }
        }

        // No writes occur until the whole trunk fits; failed candidates cannot leave half a
        // landmark.
        for (Long2ObjectMap.Entry<BlockState> entry : blocks.long2ObjectEntrySet()) {
            cursor.set(entry.getLongKey());
            level.setBlock(cursor, entry.getValue(), Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
        }
        for (int patch = 0; patch < config.mossPatches() && !tops.isEmpty(); patch++) {
            BlockPos top = tops.remove(random.nextInt(tops.size()));
            level.setBlock(
                    top,
                    Blocks.MOSS_BLOCK.defaultBlockState(),
                    Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
            config.mossPatch().value().place(level, context.chunkGenerator(), random, top.above());
        }
        return true;
    }

    private static int ground(
            WorldGenLevel level,
            BlockPos origin,
            int dx,
            int dz,
            int[] grounds,
            BlockPos.MutableBlockPos cursor) {
        if (Math.abs(dx) > EXTENT || Math.abs(dz) > EXTENT) return Integer.MIN_VALUE;
        int index = (dx + EXTENT) * WIDTH + dz + EXTENT;
        if (grounds[index] != Integer.MIN_VALUE) return grounds[index];
        for (int y = origin.getY() + 3; y >= origin.getY() - 4; y--) {
            cursor.set(origin.getX() + dx, y, origin.getZ() + dz);
            if (!level.ensureCanWrite(cursor)) return Integer.MIN_VALUE;
            BlockState state = level.getBlockState(cursor);
            if (naturalGround(state) && replaceable(level.getBlockState(cursor.above()))) {
                grounds[index] = y;
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    private static boolean naturalGround(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(BlockTags.GRASS_BLOCKS)
                || state.is(BlockTags.MOSS_BLOCKS)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(MiaTags.BlockTags.BASE_STONE_ABYSS.tag);
    }

    private static boolean replaceable(BlockState state) {
        return state.getFluidState().isEmpty()
                && (state.canBeReplaced()
                        || state.is(BlockTags.REPLACEABLE_BY_TREES)
                        || state.is(BlockTags.LEAVES));
    }
}
