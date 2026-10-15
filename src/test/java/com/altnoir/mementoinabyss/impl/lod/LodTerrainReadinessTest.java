package com.altnoir.mementoinabyss.impl.lod;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodViewPayload;
import com.altnoir.mementoinabyss.impl.lod.server.MiaLodView;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Test;

class LodTerrainReadinessTest {
    @Test
    void requiresNinetyPercentAndEveryImmediateNeighborWithoutLoadingAnything() {
        assertTrue(LodTerrainReadiness.sample(0, 0, 8, (x, z) -> true).mostlyReady());
        assertFalse(LodTerrainReadiness.sample(0, 0, 8, (x, z) -> x != 0 || z != 0).mostlyReady());
        assertFalse(
                LodTerrainReadiness.sample(
                                0, 0, 8, (x, z) -> x < 0 || Math.abs(x) <= 1 && Math.abs(z) <= 1)
                        .mostlyReady());
        assertTrue(LodTerrainReadiness.sample(0, 0, 8, (x, z) -> x != 8).mostlyReady());
        assertTrue(LodTerrainReadiness.sample(-100, 200, 8, (x, z) -> true).mostlyReady());
    }

    @Test
    void readinessMustRemainStableAndImmediatelyClosesWhenTerrainFallsBehind() {
        var gate = new LodTerrainReadiness.Gate();
        assertFalse(gate.update(0, true));
        assertFalse(gate.update(9, true));
        assertTrue(gate.update(10, true));
        assertFalse(gate.update(11, false));
        assertFalse(gate.update(12, true));
        assertTrue(gate.update(22, true));
        gate.reset();
        assertFalse(gate.update(30, true));
        assertFalse(gate.update(0, true));
    }

    @Test
    void cameraProtocolCarriesBothClientReadinessStates() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var view =
                new MiaLodView(
                        0,
                        128,
                        0,
                        640,
                        List.of(
                                new MiaLodView.Plane(1, 0, 0, 1000),
                                new MiaLodView.Plane(-1, 0, 0, 1000),
                                new MiaLodView.Plane(0, 0, 1, 1000),
                                new MiaLodView.Plane(0, 0, -1, 1000)));
        for (boolean ready : new boolean[] {true, false}) {
            var buffer =
                    new RegistryFriendlyByteBuf(
                            io.netty.buffer.Unpooled.buffer(),
                            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
            try {
                var payload =
                        new CrossDimensionLodViewPayload(
                                "mementoinabyss:test", 1, 128, view, ready);
                CrossDimensionLodViewPayload.STREAM_CODEC.encode(buffer, payload);
                assertEquals(payload, CrossDimensionLodViewPayload.STREAM_CODEC.decode(buffer));
                assertEquals(0, buffer.readableBytes());
            } finally {
                buffer.release();
            }
        }
    }
}
