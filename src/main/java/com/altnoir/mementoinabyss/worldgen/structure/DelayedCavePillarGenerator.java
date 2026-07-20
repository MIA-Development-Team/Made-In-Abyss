package com.altnoir.mementoinabyss.worldgen.structure;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.worldgen.density.HopperAbyssHole;
import com.altnoir.mementoinabyss.worldgen.dimension.MiaDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Places cave pillars after world generation. Chunk load only queues work: actual block access is
 * deferred to a later server tick, when every chunk touched by the pillar is FULL and loaded.
 */
public final class DelayedCavePillarGenerator {
    private static final int CELL_SIZE_CHUNKS = 4;
    private static final int CANDIDATES_PER_TICK = 16;
    private static final int PLACEMENT_OPERATIONS_PER_TICK = 1024;
    // Keep main's floor_to_ceiling_search_range and ABYSS_BRINK_HEIGHT placement limits.
    private static final int MAIN_SEARCH_RANGE = 188;
    private static final int MAIN_MIN_HEIGHT_ABOVE_BOTTOM = 128;
    private static final int MAIN_MAX_HEIGHT_BELOW_TOP = 8;
    private static final int MIN_CAVITY_HEIGHT = 8;
    private static final int MAX_ENDPOINT_DRIFT = 32;
    private static final int EMBED_DEPTH = 3;
    private static final int MAX_SINGLE_PILLAR_HEIGHT = 48;
    private static final int MIN_DISCONNECTED_GAP = 8;
    private static final double MIN_ENDPOINT_SUPPORT = 0.5;
    private static final long SALT = 0x43C6A4A7935BD1E5L;
    private static final Map<ServerLevel, PendingCandidates> PENDING = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, PlacementTask> ACTIVE = new ConcurrentHashMap<>();

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(MiaDimensions.THE_ABYSS_LEVEL)) return;

        ChunkPos loaded = event.getChunk().getPos();
        int cellX = Math.floorDiv(loaded.x(), CELL_SIZE_CHUNKS);
        int cellZ = Math.floorDiv(loaded.z(), CELL_SIZE_CHUNKS);
        PENDING.computeIfAbsent(level, ignored -> new PendingCandidates())
                .add(ChunkPos.pack(cellX, cellZ));
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().getLevel(MiaDimensions.THE_ABYSS_LEVEL);
        if (level == null) return;

        PlacementTask active = ACTIVE.get(level);
        if (active != null) {
            if (!allChunksFull(level, active.pillar.bounds())) {
                ACTIVE.remove(level, active);
                PENDING.computeIfAbsent(level, ignored -> new PendingCandidates()).add(active.id);
                return;
            }
            if (placeBatch(level, active, PLACEMENT_OPERATIONS_PER_TICK)) {
                CavePillarSavedData savedData = level.getDataStorage().computeIfAbsent(CavePillarSavedData.TYPE);
                savedData.markProcessed(active.id);
                ACTIVE.remove(level, active);
                MementoInAbyss.LOGGER.debug("Placed delayed cave pillar at {}, {}, mode {}, height {}, blocks {}",
                        active.candidate.x(), active.candidate.z(), active.pillar.mode(),
                        active.pillar.ceiling() - active.pillar.floor(), active.placed);
            }
            return;
        }

        PendingCandidates pending = PENDING.get(level);
        if (pending == null || pending.isEmpty()) return;
        CavePillarSavedData savedData = level.getDataStorage().computeIfAbsent(CavePillarSavedData.TYPE);
        int attempts = Math.min(CANDIDATES_PER_TICK, pending.size());
        for (int i = 0; i < attempts; i++) {
            long id = pending.removeFirst();
            if (savedData.isProcessed(id)) continue;
            Result result = tryPlace(level, savedData, id);
            if (result == Result.RETRY) pending.add(id);
            if (result == Result.STARTED) break;
        }
        if (pending.isEmpty()) PENDING.remove(level);
    }

    public static void clearPending() {
        PENDING.clear();
        ACTIVE.clear();
    }

    private static Result tryPlace(ServerLevel level, CavePillarSavedData savedData, long id) {
        int cellX = ChunkPos.getX(id);
        int cellZ = ChunkPos.getZ(id);
        Candidate candidate = candidate(level.getSeed(), cellX, cellZ);
        if (Math.hypot(candidate.x(), candidate.z()) < HopperAbyssHole.abyssRadius()) {
            savedData.markProcessed(id);
            return Result.DONE;
        }

        LevelChunk centerChunk = fullChunk(level, candidate.chunkX(), candidate.chunkZ());
        if (centerChunk == null) return Result.RETRY;

        List<Cavity> cavities = findCavities(level, candidate.x(), candidate.z());
        if (cavities.isEmpty()) {
            savedData.markProcessed(id);
            return Result.DONE;
        }

        RandomSource random = candidate.random();
        List<Cavity> connectedCavities = cavities.stream().filter(Cavity::isConnected).toList();
        List<Cavity> choices = connectedCavities.isEmpty() ? cavities : connectedCavities;
        choices = choices.stream().sorted(Comparator.comparingInt(Cavity::height).reversed()).toList();
        Cavity cavity = choices.get(random.nextInt(Math.min(3, choices.size())));
        Pillar pillar = createPillar(candidate, cavity, random);
        if (intersectsCenter(pillar.bounds(), HopperAbyssHole.abyssRadius())) {
            savedData.markProcessed(id);
            return Result.DONE;
        }
        if (!allChunksFull(level, pillar.bounds())) return Result.RETRY;

        if (pillar.mode().hasBothEndpoints()) {
            boolean floorSupported = hasEndpointSupport(level, pillar, false);
            boolean ceilingSupported = hasEndpointSupport(level, pillar, true);
            if (!floorSupported || !ceilingSupported) {
                if (!floorSupported && !ceilingSupported) {
                    savedData.markProcessed(id);
                    return Result.DONE;
                }
                pillar = toSinglePillar(pillar, ceilingSupported);
            }
        } else if (!hasEndpointSupport(level, pillar, pillar.mode() == PillarMode.CEILING_SPIKE)) {
            savedData.markProcessed(id);
            return Result.DONE;
        }

        if (intersectsCenter(pillar.bounds(), HopperAbyssHole.abyssRadius())) {
            savedData.markProcessed(id);
            return Result.DONE;
        }
        if (!allChunksFull(level, pillar.bounds())) return Result.RETRY;

        ACTIVE.put(level, createPlacementTask(id, candidate, pillar));
        return Result.STARTED;
    }

    private static Candidate candidate(long worldSeed, int cellX, int cellZ) {
        long seed = mix64(worldSeed ^ SALT
                ^ (long)cellX * 341873128712L
                ^ (long)cellZ * 132897987541L);
        RandomSource random = RandomSource.create(seed);
        int chunkX = cellX * CELL_SIZE_CHUNKS + random.nextInt(CELL_SIZE_CHUNKS);
        int chunkZ = cellZ * CELL_SIZE_CHUNKS + random.nextInt(CELL_SIZE_CHUNKS);
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        return new Candidate(ChunkPos.pack(cellX, cellZ), chunkX, chunkZ, x, z, random);
    }

    private static List<Cavity> findCavities(ServerLevel level, int x, int z) {
        List<Cavity> result = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, level.getMinY() + 1, z);
        int minY = level.getMinY() + 1;
        int maxY = level.getMaxY() - 1;
        int y = minY;
        while (y <= maxY) {
            cursor.setY(y);
            if (!isOpen(level.getBlockState(cursor))) {
                y++;
                continue;
            }
            int start = y;
            while (y <= maxY) {
                cursor.setY(y);
                if (!isOpen(level.getBlockState(cursor))) break;
                y++;
            }
            boolean hasFloor = start > minY;
            boolean hasCeiling = y <= maxY;
            int floor = hasFloor ? start - 1 : minY - 1;
            int ceiling = hasCeiling ? y : maxY + 1;
            boolean floorCanEmbed = !hasFloor || floor - EMBED_DEPTH >= level.getMinY();
            boolean ceilingCanEmbed = !hasCeiling || ceiling + EMBED_DEPTH < level.getMaxY();
            Cavity cavity = new Cavity(floor, ceiling, hasFloor && floorCanEmbed, hasCeiling && ceilingCanEmbed);
            if ((cavity.hasFloor() || cavity.hasCeiling())
                    && cavity.height() >= MIN_CAVITY_HEIGHT
                    && matchesMainHeightLimits(level, cavity)) {
                result.add(cavity);
            }
        }
        return result;
    }

    private static boolean matchesMainHeightLimits(ServerLevel level, Cavity cavity) {
        int minOriginY = level.getMinY() + MAIN_MIN_HEIGHT_ABOVE_BOTTOM;
        int maxOriginY = level.getMaxY() - 1 - MAIN_MAX_HEIGHT_BELOW_TOP;
        int minValidOrigin;
        int maxValidOrigin;
        if (cavity.hasFloor() && cavity.hasCeiling()) {
            minValidOrigin = Math.max(cavity.floor() + 1, cavity.ceiling() - MAIN_SEARCH_RANGE);
            maxValidOrigin = Math.min(cavity.ceiling() - 1, cavity.floor() + MAIN_SEARCH_RANGE);
        } else if (cavity.hasFloor()) {
            minValidOrigin = cavity.floor() + 1;
            maxValidOrigin = cavity.floor() + MAIN_SEARCH_RANGE;
        } else {
            minValidOrigin = cavity.ceiling() - MAIN_SEARCH_RANGE;
            maxValidOrigin = cavity.ceiling() - 1;
        }
        return Math.max(minValidOrigin, minOriginY) <= Math.min(maxValidOrigin, maxOriginY);
    }

    private static Pillar createPillar(Candidate candidate, Cavity cavity, RandomSource random) {
        int maxRadius = Math.min(15, Math.max(5, cavity.height() / 3));
        int radius = 5 + random.nextInt(maxRadius - 4);
        double upperScale = 0.5 + random.nextDouble() * 2.5;
        double lowerScale = 0.5 + random.nextDouble() * 2.5;
        double upperBluntness = 0.3 + random.nextDouble() * 0.3;
        double lowerBluntness = 0.4 + random.nextDouble() * 0.6;
        int windOriginY = (cavity.floor() + cavity.ceiling()) / 2;
        int endpointDistance = Math.max(windOriginY - cavity.floor(), cavity.ceiling() - windOriginY);
        double wind = radius >= 6 ? random.nextDouble() * 0.3 : 0.0;
        if (endpointDistance > 0) wind = Math.min(wind, MAX_ENDPOINT_DRIFT / (double)endpointDistance);
        double angle = random.nextDouble() * Math.PI * 2.0;
        double windX = Math.cos(angle) * wind;
        double windZ = Math.sin(angle) * wind;
        PillarMode mode;
        if (cavity.isConnected()) {
            mode = cavity.height() > maxConnectedHeight(radius)
                    ? PillarMode.DISCONNECTED : PillarMode.CONNECTED;
        } else {
            mode = cavity.hasFloor() ? PillarMode.FLOOR_SPIKE : PillarMode.CEILING_SPIKE;
        }
        Pillar pillar = new Pillar(candidate.x(), candidate.z(), cavity.floor(), cavity.ceiling(), radius,
                upperScale, lowerScale, upperBluntness, lowerBluntness,
                windX, windZ, windOriginY, mode, null);
        if (mode == PillarMode.FLOOR_SPIKE || mode == PillarMode.CEILING_SPIKE) {
            pillar = toSinglePillar(pillar, mode == PillarMode.CEILING_SPIKE);
        }
        return withBounds(pillar);
    }

    private static int maxConnectedHeight(int radius) {
        return Math.min(128, Math.max(48, radius * 8));
    }

    private static Pillar toSinglePillar(Pillar pillar, boolean ceilingAnchor) {
        int availableHeight = pillar.ceiling() - pillar.floor();
        int height = singlePillarHeight(pillar, ceilingAnchor, availableHeight);
        int floor = ceilingAnchor ? pillar.ceiling() - height : pillar.floor();
        int ceiling = ceilingAnchor ? pillar.ceiling() : pillar.floor() + height;
        return withBounds(new Pillar(pillar.centerX(), pillar.centerZ(), floor, ceiling, pillar.radius(),
                pillar.upperScale(), pillar.lowerScale(), pillar.upperBluntness(), pillar.lowerBluntness(),
                pillar.windX(), pillar.windZ(), pillar.windOriginY(),
                ceilingAnchor ? PillarMode.CEILING_SPIKE : PillarMode.FLOOR_SPIKE, null));
    }

    private static Pillar withBounds(Pillar pillar) {
        int minY = pillar.mode() == PillarMode.CEILING_SPIKE
                ? pillar.floor() : pillar.floor() - EMBED_DEPTH;
        int maxY = pillar.mode() == PillarMode.FLOOR_SPIKE
                ? pillar.ceiling() : pillar.ceiling() + EMBED_DEPTH;
        int floorX = axisX(pillar, minY);
        int floorZ = axisZ(pillar, minY);
        int ceilingX = axisX(pillar, maxY);
        int ceilingZ = axisZ(pillar, maxY);
        BoundingBox bounds = new BoundingBox(
                Math.min(floorX, ceilingX) - pillar.radius(), minY,
                Math.min(floorZ, ceilingZ) - pillar.radius(),
                Math.max(floorX, ceilingX) + pillar.radius(), maxY,
                Math.max(floorZ, ceilingZ) + pillar.radius());
        return new Pillar(pillar.centerX(), pillar.centerZ(), pillar.floor(), pillar.ceiling(), pillar.radius(),
                pillar.upperScale(), pillar.lowerScale(), pillar.upperBluntness(), pillar.lowerBluntness(),
                pillar.windX(), pillar.windZ(), pillar.windOriginY(), pillar.mode(), bounds);
    }

    private static boolean hasEndpointSupport(ServerLevel level, Pillar pillar, boolean ceiling) {
        int surfaceY = ceiling ? pillar.ceiling() : pillar.floor();
        int direction = ceiling ? 1 : -1;
        int centerX = axisX(pillar, surfaceY);
        int centerZ = axisZ(pillar, surfaceY);
        int supportRadius = Math.max(3, pillar.radius() * 2 / 3);
        int supportedColumns = 0;
        int sampledColumns = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -supportRadius; dx <= supportRadius; dx++) {
            for (int dz = -supportRadius; dz <= supportRadius; dz++) {
                if (dx * dx + dz * dz > supportRadius * supportRadius) continue;
                sampledColumns++;
                boolean supported = false;
                for (int depth = 0; depth <= EMBED_DEPTH; depth++) {
                    pos.set(centerX + dx, surfaceY + direction * depth, centerZ + dz);
                    if (level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) {
                        supported = true;
                        break;
                    }
                }
                if (supported) supportedColumns++;
            }
        }
        return supportedColumns >= Math.ceil(sampledColumns * MIN_ENDPOINT_SUPPORT);
    }

    private static boolean allChunksFull(ServerLevel level, BoundingBox bounds) {
        int minChunkX = Math.floorDiv(bounds.minX(), 16);
        int maxChunkX = Math.floorDiv(bounds.maxX(), 16);
        int minChunkZ = Math.floorDiv(bounds.minZ(), 16);
        int maxChunkZ = Math.floorDiv(bounds.maxZ(), 16);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (fullChunk(level, chunkX, chunkZ) == null) return false;
            }
        }
        return true;
    }

    private static boolean intersectsCenter(BoundingBox bounds, double radius) {
        int closestX = Math.max(bounds.minX(), Math.min(0, bounds.maxX()));
        int closestZ = Math.max(bounds.minZ(), Math.min(0, bounds.maxZ()));
        return Math.hypot(closestX, closestZ) < radius;
    }

    private static LevelChunk fullChunk(ServerLevel level, int chunkX, int chunkZ) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
        return chunk != null && chunk.getFullStatus().isOrAfter(FullChunkStatus.FULL) ? chunk : null;
    }

    private static PlacementTask createPlacementTask(long id, Candidate candidate, Pillar pillar) {
        List<BlockPos> positions = new ArrayList<>();
        int minY = pillar.mode() == PillarMode.CEILING_SPIKE
                ? pillar.floor() : pillar.floor() - EMBED_DEPTH;
        int maxY = pillar.mode() == PillarMode.FLOOR_SPIKE
                ? pillar.ceiling() : pillar.ceiling() + EMBED_DEPTH;
        double connectionRadius = Math.max(2.0, Math.ceil(pillar.radius() * 0.25));
        int[] disconnectedReaches = pillar.mode() == PillarMode.DISCONNECTED
                ? disconnectedReaches(pillar) : null;
        for (int y = minY; y <= maxY; y++) {
            int axisX = axisX(pillar, y);
            int axisZ = axisZ(pillar, y);
            int fromFloor = y - pillar.floor();
            int fromCeiling = pillar.ceiling() - y;
            boolean embedded = (pillar.mode() != PillarMode.CEILING_SPIKE && y <= pillar.floor())
                    || (pillar.mode() != PillarMode.FLOOR_SPIKE && y >= pillar.ceiling());
            int layerRadius = embedded ? embeddedRadius(pillar, y) : pillar.radius();
            if (!embedded && pillar.mode() == PillarMode.DISCONNECTED) {
                int floorRadius = fromFloor <= disconnectedReaches[0]
                        ? spikeRadius(pillar.radius(), fromFloor, disconnectedReaches[0]) : 0;
                int ceilingRadius = fromCeiling <= disconnectedReaches[1]
                        ? spikeRadius(pillar.radius(), fromCeiling, disconnectedReaches[1]) : 0;
                layerRadius = Math.max(floorRadius, ceilingRadius);
                if (layerRadius == 0) continue;
            } else if (!embedded && pillar.mode() != PillarMode.CONNECTED) {
                layerRadius = spikeRadius(pillar, y);
            }
            for (int dx = -layerRadius; dx <= layerRadius; dx++) {
                for (int dz = -layerRadius; dz <= layerRadius; dz++) {
                    double radialDistance = Math.sqrt(dx * dx + dz * dz);
                    if (radialDistance > layerRadius) continue;
                    if (!embedded && pillar.mode() == PillarMode.CONNECTED) {
                        int lowerHeight = pillarHeight(radialDistance, pillar.radius(),
                                pillar.lowerScale(), pillar.lowerBluntness());
                        int upperHeight = pillarHeight(radialDistance, pillar.radius(),
                                pillar.upperScale(), pillar.upperBluntness());
                        if (radialDistance > connectionRadius
                                && fromFloor > lowerHeight && fromCeiling > upperHeight) continue;
                    }
                    positions.add(new BlockPos(axisX + dx, y, axisZ + dz));
                }
            }
        }
        return new PlacementTask(id, candidate, pillar, positions);
    }

    private static boolean placeBatch(ServerLevel level, PlacementTask task, int operationBudget) {
        BlockState state = MiaBlocks.FOSSILIZED_LOG.get().defaultBlockState();
        int end = Math.min(task.positions.size(), task.index + operationBudget);
        while (task.index < end) {
            BlockPos pos = task.positions.get(task.index++);
            int y = pos.getY();
            Pillar pillar = task.pillar;
            boolean embedded = (pillar.mode() != PillarMode.CEILING_SPIKE && y <= pillar.floor())
                    || (pillar.mode() != PillarMode.FLOOR_SPIKE && y >= pillar.ceiling());
            boolean surfaceLayer = (pillar.mode() != PillarMode.CEILING_SPIKE && y == pillar.floor())
                    || (pillar.mode() != PillarMode.FLOOR_SPIKE && y == pillar.ceiling());
            BlockState current = level.getBlockState(pos);
            if (canReplace(level, pos, current, embedded, surfaceLayer)
                    && level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE)) {
                task.placed++;
            }
        }
        return task.index >= task.positions.size();
    }

    private static int axisX(Pillar pillar, int y) {
        return pillar.centerX() + (int)Math.floor(pillar.windX() * (pillar.windOriginY() - y));
    }

    private static int axisZ(Pillar pillar, int y) {
        return pillar.centerZ() + (int)Math.floor(pillar.windZ() * (pillar.windOriginY() - y));
    }

    private static int embeddedRadius(Pillar pillar, int y) {
        int depth = pillar.mode() != PillarMode.CEILING_SPIKE && y <= pillar.floor()
                ? pillar.floor() - y : y - pillar.ceiling();
        double scale = (EMBED_DEPTH + 1 - depth) / (double)(EMBED_DEPTH + 1);
        return Math.max(1, (int)Math.ceil(pillar.radius() * scale));
    }

    private static int spikeRadius(Pillar pillar, int y) {
        int anchorY = pillar.mode() == PillarMode.FLOOR_SPIKE ? pillar.floor() : pillar.ceiling();
        return spikeRadius(pillar.radius(), Math.abs(y - anchorY), pillar.ceiling() - pillar.floor());
    }

    private static int spikeRadius(int radius, int distance, int reach) {
        double progress = distance / (double)reach;
        return Math.max(1, (int)Math.ceil(radius * Math.pow(1.0 - progress, 1.25)));
    }

    private static int singlePillarHeight(Pillar pillar, boolean ceiling, int availableHeight) {
        double scale = ceiling ? pillar.upperScale() : pillar.lowerScale();
        return Math.min(availableHeight, Math.min(MAX_SINGLE_PILLAR_HEIGHT,
                Math.max(8, (int)Math.ceil(pillar.radius() * scale * 2.0))));
    }

    private static int[] disconnectedReaches(Pillar pillar) {
        int availableHeight = pillar.ceiling() - pillar.floor();
        int lower = singlePillarHeight(pillar, false, availableHeight);
        int upper = singlePillarHeight(pillar, true, availableHeight);
        int maxCombinedReach = availableHeight - MIN_DISCONNECTED_GAP - 1;
        if (lower + upper > maxCombinedReach) {
            double ratio = maxCombinedReach / (double)(lower + upper);
            lower = Math.max(1, (int)Math.floor(lower * ratio));
            upper = Math.max(1, maxCombinedReach - lower);
        }
        return new int[]{lower, upper};
    }

    private static boolean canReplace(ServerLevel level, BlockPos pos, BlockState state,
                                      boolean embedded, boolean surfaceLayer) {
        if (!embedded) return isOpen(state);
        if (surfaceLayer && isOpen(state)) return true;
        return state.isCollisionShapeFullBlock(level, pos) && state.getDestroySpeed(level, pos) >= 0.0F;
    }

    private static int pillarHeight(double distance, double maxRadius, double scale, double bluntness) {
        double sampledRadius = Math.max(distance, bluntness);
        double normalized = sampledRadius / maxRadius * 0.384;
        double shape = 0.75 * Math.pow(normalized, 4.0 / 3.0)
                - Math.pow(normalized, 2.0 / 3.0)
                - Math.log(normalized) / 3.0;
        return (int)Math.max(0.0, scale * shape / 0.384 * maxRadius);
    }

    private static boolean isOpen(BlockState state) {
        return state.isAir() || !state.getFluidState().isEmpty() || state.canBeReplaced();
    }

    private static long mix64(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private enum Result { DONE, RETRY, STARTED }

    private enum PillarMode {
        CONNECTED,
        DISCONNECTED,
        FLOOR_SPIKE,
        CEILING_SPIKE;

        boolean hasBothEndpoints() {
            return this == CONNECTED || this == DISCONNECTED;
        }
    }

    private record Candidate(long id, int chunkX, int chunkZ, int x, int z, RandomSource random) {}

    private record Cavity(int floor, int ceiling, boolean hasFloor, boolean hasCeiling) {
        int height() {
            return ceiling - floor - 1;
        }

        boolean isConnected() {
            return hasFloor && hasCeiling;
        }
    }

    private record Pillar(int centerX, int centerZ, int floor, int ceiling, int radius,
                          double upperScale, double lowerScale,
                          double upperBluntness, double lowerBluntness,
                          double windX, double windZ, int windOriginY,
                          PillarMode mode, BoundingBox bounds) {}

    private static final class PlacementTask {
        private final long id;
        private final Candidate candidate;
        private final Pillar pillar;
        private final List<BlockPos> positions;
        private int index;
        private int placed;

        private PlacementTask(long id, Candidate candidate, Pillar pillar, List<BlockPos> positions) {
            this.id = id;
            this.candidate = candidate;
            this.pillar = pillar;
            this.positions = positions;
        }
    }

    private static final class PendingCandidates {
        private final ConcurrentLinkedDeque<Long> queue = new ConcurrentLinkedDeque<>();
        private final Set<Long> queued = ConcurrentHashMap.newKeySet();

        void add(long id) {
            if (queued.add(id)) queue.addLast(id);
        }

        long removeFirst() {
            long id = queue.removeFirst();
            queued.remove(id);
            return id;
        }

        int size() {
            return queue.size();
        }

        boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    private DelayedCavePillarGenerator() {}
}
