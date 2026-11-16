package com.altnoir.mementoinabyss.content.returnchest.client;

import com.altnoir.mementoinabyss.content.returnchest.ReturnChestBlockEntity;
import com.altnoir.mementoinabyss.content.returnchest.ReturnChests;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class ReturnChestAmbience {
    private static final int INTERVAL = 80;
    private static final Map<ReturnChestBlockEntity, SimpleSoundInstance> PLAYING =
            new IdentityHashMap<>();

    public static void tick(
            Level level, BlockPos pos, BlockState state, ReturnChestBlockEntity chest) {
        if (!ReturnChests.isCore(state) || !ReturnChests.isBound(state)) {
            stop(chest);
            return;
        }
        if (level.getGameTime() % INTERVAL != 0L) return;
        SimpleSoundInstance sound =
                new SimpleSoundInstance(
                        SoundEvents.BEACON_AMBIENT,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F,
                        level.getRandom(),
                        pos);
        PLAYING.put(chest, sound);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    public static void stop(ReturnChestBlockEntity chest) {
        SimpleSoundInstance sound = PLAYING.remove(chest);
        if (sound != null) {
            Minecraft.getInstance().getSoundManager().stop(sound);
        }
    }

    private ReturnChestAmbience() {}
}
