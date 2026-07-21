package com.altnoir.mementoinabyss.worldgen.lod;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.network.CrossDimensionLodDebugPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.function.Predicate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Generates terrain-only ProtoChunks for LOD capture without loading them into the server world. */
public final class CrossDimensionLazyChunkGenerator {
    private static final int GENERATION_INTERVAL_TICKS = 5;
    private static final int MAX_IN_FLIGHT = 2;
    private static final int MAX_CANDIDATE_CHECKS_PER_TICK = 64;
    private static final int DEBUG_SYNC_INTERVAL_TICKS = 20;
    private static final Map<CrossDimensionLodLink, State> STATES = new HashMap<>();
    private static final ExecutorService REQUESTER = Executors.newFixedThreadPool(MAX_IN_FLIGHT, runnable -> {
        Thread thread = new Thread(runnable, "MIA lazy chunk requester");
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    });
    private static final java.util.concurrent.atomic.AtomicInteger IN_FLIGHT =
            new java.util.concurrent.atomic.AtomicInteger();
    private static long nextGenerationTick;
    private static long nextDebugSyncTick;

    public static void tick(MinecraftServer server) {
        long gameTime = server.overworld().getGameTime();
        if (gameTime >= nextDebugSyncTick) {
            nextDebugSyncTick = gameTime + DEBUG_SYNC_INTERVAL_TICKS;
            CrossDimensionLodDebugPayload.send(server);
        }
        if (gameTime < nextGenerationTick || IN_FLIGHT.get() >= MAX_IN_FLIGHT) return;
        for (CrossDimensionLodLink link : CrossDimensionLodLinks.all()) {
            ServerLevel source = server.getLevel(link.source());
            if (source == null || server.getPlayerList().getPlayers().stream().noneMatch(
                    player -> player.level().dimension().equals(link.target()))) continue;

            State state = STATES.computeIfAbsent(link, ignored -> new State());
            String phase = "center";
            ChunkPos candidate = state.nextCentral(link, source);
            if (candidate == null && state.centralComplete()) {
                phase = "player-view";
                candidate = state.nextPlayerVisible(server, link, source);
            }
            if (candidate == null) continue;

            nextGenerationTick = gameTime + GENERATION_INTERVAL_TICKS;
            requestAsync(server, source, link, state, candidate, phase);
            break;
        }
    }

    private static void requestAsync(MinecraftServer server, ServerLevel source,
                                     CrossDimensionLodLink link, State state, ChunkPos pos, String phase) {
        long key = ChunkPos.pack(pos.x(), pos.z());
        state.requested.add(key);
        state.activePos = pos;
        state.activePhase = phase;
        state.startedNanos = System.nanoTime();
        IN_FLIGHT.incrementAndGet();
        REQUESTER.execute(() -> {
            try {
                ProtoChunk protoChunk = new ProtoChunk(
                        pos, UpgradeData.EMPTY, source, source.palettedContainerFactory(), null);
                ChunkGenerator generator = source.getChunkSource().getGenerator();
                var randomState = source.getChunkSource().randomState();
                StructureManager structureManager = terrainOnlyStructureManager(source);

                // No ticket, LevelChunk, entities, lighting, server save, or ticking is created.
                // After surface generation, addTrees runs only this mod's tree placed-features.
                generator.createBiomes(randomState, Blender.empty(), structureManager, protoChunk)
                        .thenApply(chunk -> advanceStatus(chunk, ChunkStatus.BIOMES))
                        .thenCompose(chunk -> generator.fillFromNoise(
                                Blender.empty(), randomState, structureManager, chunk))
                        .thenApply(chunk -> advanceStatus(chunk, ChunkStatus.NOISE))
                        .thenApplyAsync(chunk -> buildSurface(
                                source, generator, structureManager, randomState, chunk), REQUESTER)
                        .thenApplyAsync(chunk -> addTrees(source, generator, chunk), REQUESTER)
                        .thenComposeAsync(chunk -> MiaLodStorage.ingest(link, source, chunk, true), REQUESTER)
                        .whenComplete((ignored, throwable) -> server.execute(() -> {
                            try {
                                if (throwable != null) {
                                    state.requested.remove(key);
                                    state.failed++;
                                    state.lastResult = "failed";
                                    MementoInAbyss.LOGGER.warn(
                                            "Unable to lazily generate cross-dimension LOD terrain {}", pos, throwable);
                                } else {
                                    state.generated++;
                                    state.lastResult = "stored";
                                    MiaLodSampler.notifyAvailable(link, pos);
                                }
                            } finally {
                                state.finish(pos);
                                IN_FLIGHT.decrementAndGet();
                            }
                        }));
            } catch (Throwable throwable) {
                server.execute(() -> {
                    state.requested.remove(key);
                    state.failed++;
                    state.lastResult = "failed";
                    state.finish(pos);
                    IN_FLIGHT.decrementAndGet();
                    MementoInAbyss.LOGGER.warn(
                            "Unable to request lazy cross-dimension chunk {}", pos, throwable);
                });
            }
        });
    }

    private static ChunkAccess buildSurface(ServerLevel source, ChunkGenerator generator,
                                            StructureManager structureManager,
                                            net.minecraft.world.level.levelgen.RandomState randomState,
                                            ChunkAccess chunk) {
        if (generator instanceof NoiseBasedChunkGenerator noiseGenerator) {
            noiseGenerator.buildSurface(chunk, new WorldGenerationContext(generator, source), randomState,
                    structureManager, source.getBiomeManager(),
                    source.registryAccess().lookupOrThrow(Registries.BIOME), Blender.empty());
        }
        return advanceStatus(chunk, ChunkStatus.SURFACE);
    }

    private static ChunkAccess advanceStatus(ChunkAccess chunk, ChunkStatus status) {
        if (chunk instanceof ProtoChunk protoChunk && protoChunk.getPersistedStatus().isBefore(status)) {
            protoChunk.setPersistedStatus(status);
        }
        return chunk;
    }

    /** Places only this mod's tree placed-features, without running the rest of FEATURES. */
    private static ChunkAccess addTrees(ServerLevel source, ChunkGenerator generator, ChunkAccess chunk) {
        Map<String, Holder<PlacedFeature>> trees = new LinkedHashMap<>();
        Set<Holder<Biome>> biomes = new HashSet<>();
        for (var section : chunk.getSections()) section.getBiomes().getAll(biomes::add);
        int vegetationStep = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();
        for (Holder<Biome> biome : biomes) {
            var features = generator.getBiomeGenerationSettings(biome).features();
            if (vegetationStep >= features.size()) continue;
            for (Holder<PlacedFeature> feature : features.get(vegetationStep)) {
                feature.unwrapKey().ifPresent(key -> {
                    var id = key.identifier();
                    String path = id.getPath();
                    String name = path.substring(path.lastIndexOf('/') + 1);
                    if (id.getNamespace().equals(MementoInAbyss.ID)
                            && (name.startsWith("trees_") || name.startsWith("dense_trees_"))) {
                        trees.putIfAbsent(id.toString(), feature);
                    }
                });
            }
        }
        if (trees.isEmpty()) return chunk;

        Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.MOTION_BLOCKING,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR,
                Heightmap.Types.WORLD_SURFACE));
        ChunkPos center = chunk.getPos();
        StaticCache2D<GenerationChunkHolder> cache = StaticCache2D.create(center.x(), center.z(), 1,
                (x, z) -> new TemporaryChunkHolder(x == center.x() && z == center.z()
                        ? chunk : new ProtoChunk(new ChunkPos(x, z), UpgradeData.EMPTY, source,
                        source.palettedContainerFactory(), null)));
        var featureStep = ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FEATURES);
        WorldGenRegion region = new TemporaryWorldGenRegion(source, cache, featureStep, chunk);
        BlockPos origin = new BlockPos(center.getMinBlockX(), source.getMinY(), center.getMinBlockZ());
        WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(0L));
        long decorationSeed = random.setDecorationSeed(source.getSeed(), origin.getX(), origin.getZ());
        List<Map.Entry<String, Holder<PlacedFeature>>> ordered = new ArrayList<>(trees.entrySet());
        ordered.sort(Comparator.comparing(Map.Entry::getKey));
        for (int index = 0; index < ordered.size(); index++) {
            random.setFeatureSeed(decorationSeed, index, vegetationStep);
            ordered.get(index).getValue().value().placeWithBiomeCheck(region, generator, random, origin);
        }
        return chunk;
    }

    private static StructureManager terrainOnlyStructureManager(ServerLevel source) {
        return new StructureManager(source,
                source.getServer().getWorldGenSettings().options(), null) {
            @Override
            public List<StructureStart> startsForStructure(
                    ChunkPos pos, Predicate<Structure> matcher) {
                return List.of();
            }
        };
    }

    public static void clear() {
        STATES.clear();
        IN_FLIGHT.set(0);
        nextGenerationTick = 0;
        nextDebugSyncTick = 0;
    }

    public static DebugSnapshot debugSnapshot(CrossDimensionLodLink link) {
        State state = STATES.get(link);
        int radius = Mth.ceil(CrossDimensionLodLinks.centralGenerationRadius() / 16.0);
        int total = (radius * 2 + 1) * (radius * 2 + 1);
        if (state == null) return new DebugSnapshot("center", false, 0, total,
                0, 0, 0, 0, 0, 0, 0, 0L, "none");
        boolean generating = state.activePos != null;
        long elapsed = generating ? (System.nanoTime() - state.startedNanos) / 1_000_000L : state.lastMillis;
        return new DebugSnapshot(state.activePhase, generating, state.centralCursor, total,
                state.requested.size(), state.generated, state.failed,
                generating ? state.activePos.x() : 0, generating ? state.activePos.z() : 0,
                state.lastPos == null ? 0 : state.lastPos.x(), state.lastPos == null ? 0 : state.lastPos.z(),
                elapsed, state.lastResult);
    }

    public record DebugSnapshot(String phase, boolean generating, int centralCursor, int centralTotal,
                                int requested, int generated, int failed, int activeX, int activeZ,
                                int lastX, int lastZ, long elapsedMillis, String lastResult) {}

    private static final class TemporaryChunkHolder extends GenerationChunkHolder {
        private final ChunkAccess chunk;

        private TemporaryChunkHolder(ChunkAccess chunk) {
            super(chunk.getPos());
            this.chunk = chunk;
        }

        @Override
        public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus status) {
            return chunk;
        }

        @Override
        protected void addSaveDependency(CompletableFuture<?> sync) {}

        @Override
        public int getTicketLevel() {
            return 33;
        }

        @Override
        public int getQueueLevel() {
            return 33;
        }
    }

    /** Prevents temporary tree placement from touching the real world's POI manager. */
    private static final class TemporaryWorldGenRegion extends WorldGenRegion {
        private TemporaryWorldGenRegion(ServerLevel level, StaticCache2D<GenerationChunkHolder> cache,
                                        net.minecraft.world.level.chunk.status.ChunkStep step,
                                        ChunkAccess center) {
            super(level, cache, step, center);
        }

        @Override
        public boolean setBlock(BlockPos pos, BlockState state, @Block.UpdateFlags int flags, int updateLimit) {
            if (!ensureCanWrite(pos)) return false;
            getChunk(pos).setBlockState(pos, state, flags);
            return true;
        }

        @Override
        public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ) {
            // Neighbor chunks are write-only placeholders. Resolve biome checks directly from the source.
            return getUncachedNoiseBiome(quartX, quartY, quartZ);
        }
    }

    private static final class State {
        private final Set<Long> requested = new HashSet<>();
        private int centralCursor;
        private int centralRadius = -1;
        private int playerCursor;
        private int generated;
        private int failed;
        private ChunkPos activePos;
        private ChunkPos lastPos;
        private String activePhase = "center";
        private String lastResult = "none";
        private long startedNanos;
        private long lastMillis;

        private void finish(ChunkPos pos) {
            lastMillis = (System.nanoTime() - startedNanos) / 1_000_000L;
            lastPos = pos;
            activePos = null;
        }

        private ChunkPos nextCentral(CrossDimensionLodLink link, ServerLevel source) {
            int radiusBlocks = CrossDimensionLodLinks.centralGenerationRadius();
            int radius = Mth.ceil(radiusBlocks / 16.0);
            if (centralRadius != radiusBlocks) {
                centralRadius = radiusBlocks;
                centralCursor = 0;
            }
            int diameter = radius * 2 + 1;
            int total = diameter * diameter;
            int checked = 0;
            while (centralCursor < total && checked++ < MAX_CANDIDATE_CHECKS_PER_TICK) {
                ChunkPos pos = squareSpiral(centralCursor++);
                long centerX = pos.getMiddleBlockX();
                long centerZ = pos.getMiddleBlockZ();
                if (centerX * centerX + centerZ * centerZ > (long) radiusBlocks * radiusBlocks) continue;
                if (needsGeneration(source, pos)) return pos;
            }
            return null;
        }

        private boolean centralComplete() {
            int radius = Mth.ceil(centralRadius / 16.0);
            int diameter = radius * 2 + 1;
            return centralRadius >= 0 && centralCursor >= diameter * diameter;
        }

        private ChunkPos nextPlayerVisible(MinecraftServer server, CrossDimensionLodLink link,
                                           ServerLevel source) {
            int radius = Mth.ceil(CrossDimensionLodLinks.radius(link) / 16.0);
            int diameter = radius * 2 + 1;
            int area = diameter * diameter;
            int checked = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!player.level().dimension().equals(link.target())) continue;
                ChunkPos center = player.chunkPosition();
                while (checked++ < MAX_CANDIDATE_CHECKS_PER_TICK) {
                    ChunkPos offset = squareSpiral(Math.floorMod(playerCursor++, area));
                    long offsetX = offset.x() * 16L;
                    long offsetZ = offset.z() * 16L;
                    if (offsetX * offsetX + offsetZ * offsetZ
                            > (long) radius * radius * 16L * 16L) continue;
                    ChunkPos pos = new ChunkPos(center.x() + offset.x(), center.z() + offset.z());
                    if (needsGeneration(source, pos)) return pos;
                }
            }
            return null;
        }

        private boolean needsGeneration(ServerLevel source, ChunkPos pos) {
            return !requested.contains(ChunkPos.pack(pos.x(), pos.z()))
                    && !MiaLodStorage.contains(source, pos);
        }

        private static ChunkPos squareSpiral(int index) {
            if (index == 0) return new ChunkPos(0, 0);
            int ring = (int) Math.ceil((Math.sqrt(index + 1) - 1) / 2);
            int side = ring * 2;
            int offset = index - (side - 1) * (side - 1);
            if (offset < side) return new ChunkPos(-ring + offset, -ring);
            offset -= side;
            if (offset < side) return new ChunkPos(ring, -ring + offset);
            offset -= side;
            if (offset < side) return new ChunkPos(ring - offset, ring);
            offset -= side;
            return new ChunkPos(-ring, ring - offset);
        }
    }

    private CrossDimensionLazyChunkGenerator() {}
}
