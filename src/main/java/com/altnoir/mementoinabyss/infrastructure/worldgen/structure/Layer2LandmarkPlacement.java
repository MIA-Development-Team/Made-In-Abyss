package com.altnoir.mementoinabyss.infrastructure.worldgen.structure;

import com.altnoir.mementoinabyss.api.worldgen.Layer2Biomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.Layer2Layout;
import com.altnoir.mementoinabyss.infrastructure.worldgen.layer2.Layer2Runtime;
import com.altnoir.mementoinabyss.init.MiaStructurePlacementTypes;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public final class Layer2LandmarkPlacement extends StructurePlacement {
    public static final MapCodec<Layer2LandmarkPlacement> CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(
                                            Codec.BOOL
                                                    .fieldOf("primary")
                                                    .forGetter(Layer2LandmarkPlacement::primary),
                                            Codec.INT
                                                    .optionalFieldOf("salt", 0)
                                                    .forGetter(
                                                            Layer2LandmarkPlacement::placementSalt))
                                    .apply(instance, Layer2LandmarkPlacement::new));

    private static final int SEARCH_RADIUS = 12;
    private static final Cache<Layer2Runtime.State, Cache<AnchorKey, Optional<ChunkPos>>> ANCHORS =
            CacheBuilder.newBuilder().weakKeys().maximumSize(16).build();

    private final boolean primary;

    public Layer2LandmarkPlacement(boolean primary, int salt) {
        super(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1, salt, Optional.empty());
        this.primary = primary;
    }

    public boolean primary() {
        return primary;
    }

    public int placementSalt() {
        return salt();
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState structureState, int x, int z) {
        var state = Layer2Runtime.state(structureState.randomState());
        if (state == null) return false;
        var region = state.layout().sample((x << 4) + 8, (z << 4) + 8);
        if (!state.biomeSelection((x << 4) + 8, (z << 4) + 8)
                .zone()
                .equals(primary ? Layer2Biomes.PRIMARY : Layer2Biomes.COMPANIONS)) return false;
        Optional<ChunkPos> anchor = anchorFor(state, region);
        return anchor.isPresent() && anchor.get().x() == x && anchor.get().z() == z;
    }

    public List<ChunkPos> candidates(
            ChunkGeneratorStructureState structureState, ChunkPos center, int chunkRadius) {
        var state = Layer2Runtime.state(structureState.randomState());
        if (state == null || chunkRadius < 0) return List.of();
        Set<AnchorKey> visited = new HashSet<>();
        List<ChunkPos> candidates = new ArrayList<>();
        for (long x = (long) center.x() - chunkRadius; x <= (long) center.x() + chunkRadius; x++) {
            for (long z = (long) center.z() - chunkRadius;
                    z <= (long) center.z() + chunkRadius;
                    z++) {
                var region = state.layout().sample(((int) x << 4) + 8, ((int) z << 4) + 8);
                if (!state.biomeSelection(((int) x << 4) + 8, ((int) z << 4) + 8)
                        .zone()
                        .equals(primary ? Layer2Biomes.PRIMARY : Layer2Biomes.COMPANIONS)) continue;
                var key = new AnchorKey(region.id(), primary ? -1 : region.sector(), salt());
                if (!visited.add(key)) continue;
                anchorFor(state, region)
                        .ifPresent(
                                anchor -> {
                                    if (Math.abs((long) anchor.x() - center.x()) <= chunkRadius
                                            && Math.abs((long) anchor.z() - center.z())
                                                    <= chunkRadius
                                            && isStructureChunk(
                                                    structureState, anchor.x(), anchor.z()))
                                        candidates.add(anchor);
                                });
            }
        }
        return candidates;
    }

    private Optional<ChunkPos> anchorFor(Layer2Runtime.State state, Layer2Layout.Region region) {
        int slot = primary ? -1 : region.sector();
        var cache =
                ANCHORS.asMap()
                        .computeIfAbsent(
                                state,
                                ignored -> CacheBuilder.newBuilder().maximumSize(4096).build());
        var key = new AnchorKey(region.id(), slot, salt());
        return cache.asMap()
                .computeIfAbsent(key, ignored -> findAnchor(state, region, slot, salt()));
    }

    private static Optional<ChunkPos> findAnchor(
            Layer2Runtime.State state, Layer2Layout.Region region, int slot, int salt) {
        var layout = state.layout();
        double targetX = region.siteX(), targetZ = region.siteZ();
        double radius = layout.cellSize() * .35;
        if (slot == 0) targetZ -= radius;
        else if (slot == 1) targetX += radius;
        else if (slot == 2) targetZ += radius;
        else if (slot == 3) targetX -= radius;
        if (!Double.isFinite(targetX) || !Double.isFinite(targetZ)) return Optional.empty();

        // Invert the same low-frequency warp used by the partition, rather than anchoring to
        // the caller's chunk, so every query for a region starts from the same world point.
        double worldX = targetX, worldZ = targetZ;
        for (int step = 0; step < 16; step++) {
            var point =
                    new DensityFunction.SinglePointContext(
                            (int) Math.floor(worldX), 0, (int) Math.floor(worldZ));
            double dx = targetX - worldX - layout.warpX().compute(point);
            double dz = targetZ - worldZ - layout.warpZ().compute(point);
            worldX += .75 * dx;
            worldZ += .75 * dz;
            if (Math.abs(dx) + Math.abs(dz) < .25) break;
        }
        int anchorX = Math.floorDiv((int) Math.floor(worldX), 16);
        int anchorZ = Math.floorDiv((int) Math.floor(worldZ), 16);
        ChunkPos fallback = null;
        for (int ring = 0; ring <= SEARCH_RADIUS; ring++) {
            ChunkPos preferred = null, secondary = null;
            double bestPreferred = Double.POSITIVE_INFINITY,
                    bestSecondary = Double.POSITIVE_INFINITY;
            for (int dx = -ring; dx <= ring; dx++)
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                    int chunkX = anchorX + dx, chunkZ = anchorZ + dz;
                    int x = (chunkX << 4) + 8, z = (chunkZ << 4) + 8;
                    var candidate = layout.sample(x, z);
                    if (candidate.id() != region.id()
                            || (slot >= 0 && candidate.sector() != slot)
                            || candidate.edgeDistance() <= layout.riverBankDistance() + 16
                            || state.opening(x, z)
                            || !state.biomeSelection(x, z)
                                    .zone()
                                    .equals(
                                            slot < 0
                                                    ? Layer2Biomes.PRIMARY
                                                    : Layer2Biomes.COMPANIONS)) continue;
                    var column = state.column(x, z);
                    if (column.pillar()
                            || column.height() < -114
                            || column.height() > -64
                            || column.roof() - column.height() < 48) continue;
                    double distance = (x - worldX) * (x - worldX) + (z - worldZ) * (z - worldZ);
                    double score =
                            distance
                                    + ((mix(region.id() ^ salt ^ ChunkPos.pack(chunkX, chunkZ))
                                                            >>> 48)
                                                    & 65535)
                                            / 65536.0;
                    if (column.height() >= layout.waterLevel()) {
                        if (score < bestPreferred) {
                            preferred = new ChunkPos(chunkX, chunkZ);
                            bestPreferred = score;
                        }
                    } else if (score < bestSecondary) {
                        secondary = new ChunkPos(chunkX, chunkZ);
                        bestSecondary = score;
                    }
                }
            if (preferred != null) return Optional.of(preferred);
            if (fallback == null) fallback = secondary;
        }
        return Optional.ofNullable(fallback);
    }

    private static long mix(long value) {
        value = (value ^ value >>> 30) * 0xbf58476d1ce4e5b9L;
        value = (value ^ value >>> 27) * 0x94d049bb133111ebL;
        return value ^ value >>> 31;
    }

    @Override
    public StructurePlacementType<?> type() {
        return MiaStructurePlacementTypes.LAYER2_LANDMARK.get();
    }

    private record AnchorKey(long region, int slot, int salt) {}
}
