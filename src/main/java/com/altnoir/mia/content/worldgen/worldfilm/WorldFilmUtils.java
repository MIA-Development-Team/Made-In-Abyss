package com.altnoir.mia.content.worldgen.worldfilm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class WorldFilmUtils {
    public static WorldFilmData getData(LevelChunk chunk) {
        return chunk.getCapability(WorldFilmCapability.CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Missing WorldFilmData capability"));
    }

    public static void visualizeChunk(LevelChunk chunk, ServerPlayer player) {
        ServerLevel serverLevel = player.serverLevel();

        if (chunk.isEmpty()) return;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                WorldFilmData data = chunk.getCapability(WorldFilmCapability.CAPABILITY).orElse(null);

                float thickness = data.getThickness(x, z);
                float hue = thickness * 0.35f;
                int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);

                ParticleOptions particle = new DustParticleOptions(
                        Vec3.fromRGB24(rgb).toVector3f(),
                        1.0f
                );

                BlockPos groundPos = serverLevel.getHeightmapPos(
                        Heightmap.Types.WORLD_SURFACE,
                        new BlockPos(
                                chunk.getPos().getMinBlockX() + x,
                                0,
                                chunk.getPos().getMinBlockZ() + z
                        )
                );

                serverLevel.sendParticles(
                        player,
                        particle,
                        true,
                        groundPos.getX() + 0.5,
                        groundPos.getY() + 1.5,
                        groundPos.getZ() + 0.5,
                        1,
                        0.0, 0.0, 0.0,
                        0.0
                );
            }
        }
    }
}
