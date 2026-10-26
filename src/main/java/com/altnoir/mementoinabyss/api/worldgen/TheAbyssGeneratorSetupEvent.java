package com.altnoir.mementoinabyss.api.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.layer2.Layer2Runtime;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.bus.api.Event;

/**
 * Fired synchronously on the NeoForge game bus after native Layer2 initialization and before
 * structure state creation. Extensions must be installed during this event, before generation starts.
 */
public final class TheAbyssGeneratorSetupEvent extends Event {
    private final ServerLevel level;
    private final ChunkGenerator generator;
    private final RandomState randomState;
    private final Layer2Runtime.State layer2State;

    public TheAbyssGeneratorSetupEvent(
            ServerLevel level,
            ChunkGenerator generator,
            RandomState randomState,
            Layer2Runtime.State layer2State) {
        this.level = level;
        this.generator = generator;
        this.randomState = randomState;
        this.layer2State = layer2State;
    }

    public ServerLevel level() {
        return level;
    }

    public ChunkGenerator generator() {
        return generator;
    }

    public RandomState randomState() {
        return randomState;
    }

    public Layer2Runtime.State layer2State() {
        return layer2State;
    }
}
