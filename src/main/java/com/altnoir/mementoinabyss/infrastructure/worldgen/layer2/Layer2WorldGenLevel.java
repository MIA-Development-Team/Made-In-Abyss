package com.altnoir.mementoinabyss.infrastructure.worldgen.layer2;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jspecify.annotations.Nullable;

public final class Layer2WorldGenLevel implements WorldGenLevel {
    private final WorldGenLevel original;
    private final Layer2Runtime.State state;
    private final boolean structure;
    private final int minY;
    private final int maxY;
    private final boolean allowFluids;

    public Layer2WorldGenLevel(
            WorldGenLevel original,
            Layer2Runtime.State state,
            boolean structure,
            int minY,
            int maxY) {
        this(original, state, structure, minY, maxY, false);
    }

    public Layer2WorldGenLevel(
            WorldGenLevel original,
            Layer2Runtime.State state,
            boolean structure,
            int minY,
            int maxY,
            boolean allowFluids) {
        this.original = original;
        this.state = state;
        this.structure = structure;
        this.minY = minY;
        this.maxY = maxY;
        this.allowFluids = allowFluids;
    }

    public WorldGenLevel original() {
        return original;
    }

    public Layer2WorldGenLevel withFluidWrites() {
        return allowFluids
                ? this
                : new Layer2WorldGenLevel(original, state, structure, minY, maxY, true);
    }

    @Override
    public int getHeight(Heightmap.Types type, int x, int z) {
        return Layer2GeneratorView.surfaceHeight(
                state, original, structure, type, x, z, minY, maxY);
    }

    @Override
    public int getHeight(Heightmap.Types type, BlockPos pos) {
        return getHeight(type, pos.getX(), pos.getZ());
    }

    @Override
    public BlockPos getHeightmapPos(Heightmap.Types type, BlockPos pos) {
        return pos.atY(getHeight(type, pos));
    }

    @Override
    public int getMinY() {
        return structure ? original.getMinY() : minY;
    }

    @Override
    public int getMaxY() {
        return structure ? original.getMaxY() : maxY;
    }

    @Override
    public int getHeight() {
        return getMaxY() - getMinY() + 1;
    }

    @Override
    public int getSeaLevel() {
        return -112;
    }

    @Override
    public boolean ensureCanWrite(BlockPos pos) {
        return original.isInsideBuildHeight(pos)
                && (structure || pos.getY() >= minY && pos.getY() <= maxY)
                && original.ensureCanWrite(pos);
    }

    private boolean canReplace(BlockPos pos) {
        return ensureCanWrite(pos)
                && (structure || allowFluids || original.getFluidState(pos).isEmpty());
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState blockState, int flags, int updateLimit) {
        if (!ensureCanWrite(pos)) return false;
        var originalFluid = original.getFluidState(pos);
        boolean preservesWater =
                !originalFluid.isEmpty()
                        && blockState.getFluidState().getType() == originalFluid.getType();
        boolean freezesWater =
                originalFluid.is(FluidTags.WATER)
                        && (blockState.is(Blocks.ICE) || blockState.is(Blocks.FROSTED_ICE));
        return (structure
                        || allowFluids
                        || blockState.getFluidState().isEmpty() && originalFluid.isEmpty()
                        || preservesWater
                        || freezesWater)
                && original.setBlock(pos, blockState, flags, updateLimit);
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean movedByPiston) {
        return canReplace(pos) && original.removeBlock(pos, movedByPiston);
    }

    @Override
    public boolean destroyBlock(
            BlockPos pos, boolean dropResources, @Nullable Entity breaker, int updateLimit) {
        return canReplace(pos) && original.destroyBlock(pos, dropResources, breaker, updateLimit);
    }

    @Override
    public ServerLevel getLevel() {
        return original.getLevel();
    }

    @Override
    public long getSeed() {
        return original.getSeed();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        return original.getCurrentDifficultyAt(pos);
    }

    @Override
    public long nextSubTickCount() {
        return original.nextSubTickCount();
    }

    @Override
    public LevelData getLevelData() {
        return original.getLevelData();
    }

    @Override
    public @Nullable MinecraftServer getServer() {
        return original.getServer();
    }

    @Override
    public ChunkSource getChunkSource() {
        return original.getChunkSource();
    }

    @Override
    public RandomSource getRandom() {
        return original.getRandom();
    }

    @Override
    public void playSound(
            @Nullable Entity except,
            BlockPos pos,
            SoundEvent sound,
            SoundSource source,
            float volume,
            float pitch) {
        original.playSound(except, pos, sound, source, volume, pitch);
    }

    @Override
    public void addParticle(
            ParticleOptions particle,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd) {
        original.addParticle(particle, x, y, z, xd, yd, zd);
    }

    @Override
    public void levelEvent(@Nullable Entity source, int type, BlockPos pos, int data) {
        original.levelEvent(source, type, pos, data);
    }

    @Override
    public void gameEvent(Holder<GameEvent> event, Vec3 pos, GameEvent.Context context) {
        original.gameEvent(event, pos, context);
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return original.getBlockTicks();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return original.getFluidTicks();
    }

    @Override
    public @Nullable ChunkAccess getChunk(
            int x, int z, ChunkStatus status, boolean loadOrGenerate) {
        return original.getChunk(x, z, status, loadOrGenerate);
    }

    @Override
    public ChunkAccess getChunk(int x, int z) {
        return original.getChunk(x, z);
    }

    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus status) {
        return original.getChunk(x, z, status);
    }

    @Override
    public ChunkAccess getChunk(BlockPos pos) {
        return original.getChunk(pos);
    }

    @Override
    public boolean hasChunk(int x, int z) {
        return original.hasChunk(x, z);
    }

    @Override
    public int getSkyDarken() {
        return original.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return original.getBiomeManager();
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos) {
        return original.getBiome(pos);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z) {
        return original.getNoiseBiome(x, y, z);
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return original.getUncachedNoiseBiome(x, y, z);
    }

    @Override
    public boolean isClientSide() {
        return original.isClientSide();
    }

    @Override
    public DimensionType dimensionType() {
        return original.dimensionType();
    }

    @Override
    public RegistryAccess registryAccess() {
        return original.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return original.enabledFeatures();
    }

    @Override
    public EnvironmentAttributeReader environmentAttributes() {
        return original.environmentAttributes();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return original.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return original.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return original.getFluidState(pos);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return original.getLightEngine();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return original.getWorldBorder();
    }

    @Override
    public List<Entity> getEntities(
            @Nullable Entity except, AABB bounds, Predicate<? super Entity> selector) {
        return original.getEntities(except, bounds, selector);
    }

    @Override
    public <T extends Entity> List<T> getEntities(
            EntityTypeTest<Entity, T> type, AABB bounds, Predicate<? super T> selector) {
        return original.getEntities(type, bounds, selector);
    }

    @Override
    public List<? extends Player> players() {
        return original.players();
    }

    @Override
    public @Nullable Player getNearestPlayer(
            double x,
            double y,
            double z,
            double maxDistance,
            @Nullable Predicate<Entity> predicate) {
        return original.getNearestPlayer(x, y, z, maxDistance, predicate);
    }

    @Override
    public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> predicate) {
        return original.isStateAtPosition(pos, predicate);
    }

    @Override
    public boolean isFluidAtPosition(BlockPos pos, Predicate<FluidState> predicate) {
        return original.isFluidAtPosition(pos, predicate);
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        return original.addFreshEntity(entity);
    }

    @Override
    public void setCurrentlyGenerating(@Nullable Supplier<String> currentlyGenerating) {
        original.setCurrentlyGenerating(currentlyGenerating);
    }
}
