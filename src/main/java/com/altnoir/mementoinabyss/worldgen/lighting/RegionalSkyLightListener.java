package com.altnoir.mementoinabyss.worldgen.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import org.jspecify.annotations.Nullable;

/** A read-compatible sky-light listener for light engine replacements such as Starlight. */
public final class RegionalSkyLightListener implements LayerLightEventListener {
    private final LayerLightEventListener delegate;
    private final RegionalSkyLight.Region region;

    public RegionalSkyLightListener(LayerLightEventListener delegate, RegionalSkyLight.Region region) {
        this.delegate = delegate;
        this.region = region;
    }

    public boolean wraps(LayerLightEventListener listener) {
        return this.delegate == listener;
    }

    @Override
    public @Nullable DataLayer getDataLayerData(SectionPos pos) {
        DataLayer source = this.delegate.getDataLayerData(pos);
        if (source == null) return this.region.intersectsChunk(pos.x(), pos.z()) ? null : new DataLayer();
        return RegionalSkyLight.maskDataLayer(this.region, pos, source);
    }

    @Override
    public int getLightValue(BlockPos pos) {
        return this.region.clampSkyLight(
                pos.getX(), pos.getZ(), this.delegate.getLightValue(pos));
    }

    @Override
    public void checkBlock(BlockPos pos) {
        if (this.region.allowsSkyLight(pos.getX(), pos.getZ())) {
            this.delegate.checkBlock(pos);
        }
    }

    @Override
    public boolean hasLightWork() {
        return this.delegate.hasLightWork();
    }

    @Override
    public int runLightUpdates() {
        return this.delegate.runLightUpdates();
    }

    @Override
    public void updateSectionStatus(SectionPos pos, boolean sectionEmpty) {
        if (this.region.intersectsChunk(pos.x(), pos.z())) {
            this.delegate.updateSectionStatus(pos, sectionEmpty);
        }
    }

    @Override
    public void setLightEnabled(ChunkPos pos, boolean enable) {
        if (!enable || this.region.intersectsChunk(pos.x(), pos.z())) {
            this.delegate.setLightEnabled(pos, enable);
        }
    }

    @Override
    public void propagateLightSources(ChunkPos pos) {
        if (this.region.intersectsChunk(pos.x(), pos.z())) {
            this.delegate.propagateLightSources(pos);
        }
    }
}
