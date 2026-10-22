package com.altnoir.mementoinabyss.client.render.sky;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.OverworldAbyss;
import net.minecraft.world.level.Level;

public final class OverworldAbyssSky {
    public static boolean visible(Level level, double x, double z) {
        return Level.OVERWORLD.equals(level.dimension())
                && MementoInAbyss.CONFIGS.worldGenSection.overworldAbyssEnabled.get()
                && OverworldAbyss.withinConnectionRange(x, z, OverworldAbyss.configuredRadius());
    }

    private OverworldAbyssSky() {}
}
