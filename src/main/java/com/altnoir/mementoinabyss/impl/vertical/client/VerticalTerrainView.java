package com.altnoir.mementoinabyss.impl.vertical.client;

import com.altnoir.mementoinabyss.impl.vertical.network.VerticalSectionPayload;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

final class VerticalTerrainView implements BlockAndTintGetter, CollisionGetter {
    private final Map<VerticalNearTerrain.Key, VerticalSectionPayload> sections;
    private final VerticalSectionPayload anchor;

    VerticalTerrainView(
            Map<VerticalNearTerrain.Key, VerticalSectionPayload> sections,
            VerticalSectionPayload anchor) {
        this.sections = sections;
        this.anchor = anchor;
    }

    private VerticalSectionPayload section(BlockPos pos) {
        return sections.get(
                new VerticalNearTerrain.Key(
                        anchor.source(),
                        Math.floorDiv(pos.getX(), 16),
                        Math.floorDiv(pos.getY(), 16),
                        Math.floorDiv(pos.getZ(), 16)));
    }

    private static int index(BlockPos pos) {
        return (pos.getY() & 15) << 8 | (pos.getZ() & 15) << 4 | (pos.getX() & 15);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return Minecraft.getInstance().level.getWorldBorder();
    }

    @Override
    public BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return this;
    }

    @Override
    public List<VoxelShape> getEntityCollisions(Entity source, AABB testArea) {
        return List.of();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        var s = section(pos);
        return s == null
                ? Blocks.AIR.defaultBlockState()
                : Block.stateById(s.palette()[Short.toUnsignedInt(s.cells()[index(pos)])]);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getHeight() {
        return anchor.height();
    }

    @Override
    public int getMinY() {
        return anchor.minY();
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return CardinalLighting.DEFAULT;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        var s = section(pos);
        if (s == null) return layer == LightLayer.SKY ? 15 : 0;
        int light = Byte.toUnsignedInt(s.light()[index(pos)]);
        return layer == LightLayer.SKY ? light >>> 4 : light & 15;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int darkening) {
        return Math.max(
                getBrightness(LightLayer.BLOCK, pos),
                getBrightness(LightLayer.SKY, pos) - darkening);
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        var s = section(pos);
        if (s == null || Minecraft.getInstance().level == null) return -1;
        int index =
                ((pos.getY() & 15) >> 2) << 4
                        | ((pos.getZ() & 15) >> 2) << 2
                        | ((pos.getX() & 15) >> 2);
        var biome =
                Minecraft.getInstance()
                        .level
                        .registryAccess()
                        .lookupOrThrow(Registries.BIOME)
                        .byId(s.biomes()[index]);
        return biome == null ? -1 : resolver.getColor(biome, pos.getX(), pos.getZ());
    }
}
