package com.altnoir.mia.content.items;

import com.altnoir.mia.MIA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.chunk.LevelChunk;
import com.altnoir.mia.content.worldgen.worldfilm.WorldFilmUtils;

public class WorldFilmDebugItem extends MIABaseItem {
    public WorldFilmDebugItem(Properties pProperties) {
        super(pProperties);
    }

    @Mod.EventBusSubscriber(modid = MIA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ItemHandler {
        private static final int CHECK_INTERVAL = 10;

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            if (!(event.player instanceof ServerPlayer player)) return;

            var heldItem = player.getMainHandItem();
            if (!(heldItem.getItem() instanceof WorldFilmDebugItem)) return;

            if (player.tickCount % CHECK_INTERVAL != 0) return;

            var radius = 2;
            var centerChunk = player.chunkPosition();
            var level = player.serverLevel();

            for (var dx = -radius; dx <= radius; dx++) {
                for (var dz = -radius; dz <= radius; dz++) {
                    ChunkPos chunkPos = new ChunkPos(
                            centerChunk.x + dx,
                            centerChunk.z + dz
                    );
                    if (!level.hasChunk(chunkPos.x, chunkPos.z)) continue;

                    LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
                    WorldFilmUtils.visualizeChunk(chunk, player);
                }
            }
        }
    }
}
