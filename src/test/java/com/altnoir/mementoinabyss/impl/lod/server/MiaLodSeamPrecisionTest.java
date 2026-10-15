package com.altnoir.mementoinabyss.impl.lod.server;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.MiaHeight;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import java.util.List;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class MiaLodSeamPrecisionTest {
    private static MiaLodView view(double x, double y, double z) {
        return new MiaLodView(
                x,
                y,
                z,
                640,
                List.of(
                        new MiaLodView.Plane(1, 0, 0, 1000),
                        new MiaLodView.Plane(-1, 0, 0, 1000),
                        new MiaLodView.Plane(0, 0, 1, 1000),
                        new MiaLodView.Plane(0, 0, -1, 1000)));
    }

    @Test
    void nearbySelectedWallColumnsUseFullPrecisionInBothDirections() {
        var down =
                new CrossDimensionLodLink(
                        MiaDimensions.THE_ABYSS_LEVEL.identifier(),
                        MiaDimensions.THE_ABYSS_LEVEL,
                        Level.OVERWORLD,
                        MiaHeight.THE_ABYSS,
                        -448);
        var up =
                new CrossDimensionLodLink(
                        Level.OVERWORLD.identifier(),
                        Level.OVERWORLD,
                        MiaDimensions.THE_ABYSS_LEVEL,
                        new MiaHeight(-64, 384, 320),
                        448);
        var demand = new MiaLodViewPlanner.Demand(CrossDimensionLodKey.pack(21, 0), 16, 0, 123);
        var fine = MiaLodViewPlanner.refineAbyssSeam(demand, view(344, -60, 0), down, 320);
        assertEquals(1, fine.cellSize());
        assertEquals(demand.key(), fine.key());
        assertEquals(123, fine.priority());
        assertEquals(
                1,
                MiaLodViewPlanner.refineAbyssSeam(demand, view(344, 380, 0), up, 320).cellSize());
        assertSame(demand, MiaLodViewPlanner.refineAbyssSeam(demand, view(344, 20, 0), down, 320));
        assertSame(demand, MiaLodViewPlanner.refineAbyssSeam(demand, view(0, -64, 0), down, 320));
        var outside = new MiaLodViewPlanner.Demand(CrossDimensionLodKey.pack(40, 0), 16, 0, 123);
        assertSame(
                outside, MiaLodViewPlanner.refineAbyssSeam(outside, view(640, -64, 0), down, 320));
        var culled = new MiaLodViewPlanner.Demand(demand.key(), 16, 2, 123);
        assertSame(culled, MiaLodViewPlanner.refineAbyssSeam(culled, view(344, -64, 0), down, 320));
    }
}
