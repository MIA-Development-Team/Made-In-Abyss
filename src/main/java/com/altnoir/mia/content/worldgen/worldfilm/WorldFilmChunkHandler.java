package com.altnoir.mia.content.worldgen.worldfilm;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldFilmChunkHandler {
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) return;

        var chunk = (LevelChunk) event.getChunk();
        chunk.getCapability(WorldFilmCapability.CAPABILITY).ifPresent(data -> {
            if (!data.isInitialized()) {
                generateWorldFilmData(chunk, data);
                data.markInitialized();
                chunk.setUnsaved(true);
            }
        });
    }

    private void generateWorldFilmData(LevelChunk chunk, WorldFilmData data) {
        if (!(chunk.getLevel() instanceof ServerLevel serverLevel)) return;

        var seed = serverLevel.getSeed();
        var chunkPos = chunk.getPos();
        var chunkRandom = RandomSource.create(seed ^ chunkPos.toLong());

        var minY = serverLevel.getMinBuildHeight();
        var maxY = serverLevel.getMaxBuildHeight();
        var heightRange = maxY - minY;

        var temperatureFunc = serverLevel.getChunkSource().randomState().sampler().temperature();

        for (var x = 0; x < 16; x++) {
            for (var z = 0; z < 16; z++) {
                var worldPos = chunkPos.getBlockAt(x, minY, z);

                var height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                var heightFactor = (height - minY) / (float) heightRange;

                var noiseRaw = temperatureFunc.compute(
                        new DensityFunction.SinglePointContext(worldPos.getX(), height, worldPos.getZ())
                );
                var noise = (float) Mth.clamp(noiseRaw * 2.0, -1.0, 1.0);
                noise = (noise + 1.0f) / 2.0f;
                var randomFactor = chunkRandom.nextFloat() * 0.4f;

                var value = Mth.clamp(
                        noise * 0.8f +
                                heightFactor * 0.6f +
                                randomFactor,
                        0.0f, 1.0f
                );

                data.setThickness(x, z, value);
            }
        }
    }

}
