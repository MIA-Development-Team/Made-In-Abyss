package com.altnoir.mementoinabyss.worldgen.dimension;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.clock.WorldClock;

/** Dedicated paused clock used by MIA dimensions so overworld time remains independent. */
public final class MiaWorldClocks {
    public static final ResourceKey<WorldClock> NOON = ResourceKey.create(
            Registries.WORLD_CLOCK, MementoInAbyss.asResource("noon"));
    private static final long NOON_TICKS = 6000L;

    public static void bootstrap(BootstrapContext<WorldClock> context) {
        context.register(NOON, new WorldClock());
    }

    public static void initialize(MinecraftServer server) {
        var clock = server.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(NOON);
        server.clockManager().setTotalTicks(clock, NOON_TICKS);
        server.clockManager().setPaused(clock, true);
    }

    private MiaWorldClocks() {}
}
