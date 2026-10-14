package com.altnoir.mementoinabyss.impl.lod.client;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodCacheOfferPayload;
import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodCacheScope;
import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodTransfer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CrossDimensionLodContextsTest {
    @Test
    @SuppressWarnings("unchecked")
    void packetsAndEpochsRemainIndependentAtIdenticalChunkCoordinates() throws Exception {
        var contextsField = CrossDimensionLodRenderer.class.getDeclaredField("CONTEXTS");
        contextsField.setAccessible(true);
        var contexts = (Map<String, CrossDimensionLodRenderContext>) contextsField.get(null);
        var saved = new HashMap<>(contexts);
        var ingressField = CrossDimensionLodRenderContext.class.getDeclaredField("INGRESS");
        ingressField.setAccessible(true);
        var epochField = CrossDimensionLodRenderContext.class.getDeclaredField("WORK_EPOCH");
        epochField.setAccessible(true);
        try {
            contexts.clear();
            var upper =
                    offer("mementoinabyss:overworld_in_the_abyss", 448, 10, "minecraft:overworld");
            var lower = offer("mementoinabyss:great_fault", -640, 11, "mementoinabyss:great_fault");
            CrossDimensionLodRenderer.accept(upper);
            CrossDimensionLodRenderer.accept(lower);
            var upperContext = contexts.get(upper.transfer().linkId());
            var lowerContext = contexts.get(lower.transfer().linkId());
            assertNotSame(upperContext, lowerContext);
            var upperIngress = (CrossDimensionLodIngress) ingressField.get(upperContext);
            var lowerIngress = (CrossDimensionLodIngress) ingressField.get(lowerContext);
            assertSame(upper, upperIngress.poll().payload());
            assertSame(lower, lowerIngress.poll().payload());
            assertNotSame(epochField.get(upperContext), epochField.get(lowerContext));
            CrossDimensionLodRenderer.accept(
                    offer(upper.transfer().linkId(), 448, 12, "minecraft:overworld"));
            CrossDimensionLodRenderer.accept(lower);
            assertEquals(12, upperIngress.poll().payload().transfer().streamEpoch());
            assertEquals(11, lowerIngress.poll().payload().transfer().streamEpoch());
        } finally {
            contexts.clear();
            contexts.putAll(saved);
        }
    }

    private static CrossDimensionLodCacheOfferPayload offer(
            String link, int offset, long epoch, String dimension) {
        return new CrossDimensionLodCacheOfferPayload(
                new CrossDimensionLodTransfer(
                        link,
                        offset,
                        256,
                        epoch,
                        1,
                        0,
                        1,
                        3,
                        -4,
                        16,
                        -64,
                        1,
                        1,
                        new CrossDimensionLodCacheScope(new UUID(1, 2), dimension)),
                "a".repeat(64));
    }
}
