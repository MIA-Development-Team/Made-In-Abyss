package com.altnoir.mementoinabyss.impl.lod.client;

import com.altnoir.mementoinabyss.impl.lod.client.CrossDimensionLodMesher.Side;

/** One-block contact AO, using a frozen rendered neighbourhood rather than pending network data. */
final class CrossDimensionLodAmbientOcclusion {
    static final int UNOCCLUDED = 255;

    static final class View {
        final int chunkX, chunkZ, width;
        final Side[][] columns;
        private final CrossDimensionLodColumn local;

        View(int chunkX, int chunkZ, int width, Side[][] columns) {
            this(chunkX, chunkZ, width, columns, null);
        }

        private View(
                int chunkX,
                int chunkZ,
                int width,
                Side[][] columns,
                CrossDimensionLodColumn local) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.width = width;
            this.columns = columns;
            this.local = local;
        }

        View withSource(CrossDimensionLodColumn source) {
            return new View(chunkX, chunkZ, width, columns, source);
        }

        Side[] column(int x, int z) {
            x -= chunkX;
            z -= chunkZ;
            return x < 0 || z < 0 || x >= width || z >= width ? null : columns[z * width + x];
        }

        boolean occupied(int worldX, int worldY, int worldZ) {
            int cx = Math.floorDiv(worldX, 16), cz = Math.floorDiv(worldZ, 16);
            int x = Math.floorMod(worldX, 16), z = Math.floorMod(worldZ, 16);
            if (local != null && cx == local.chunkX() && cz == local.chunkZ()) {
                int cell = local.cellSize();
                if (cell > 4) return false;
                int y = Math.floorDiv(worldY - local.minY() - local.displayYOffset(), cell);
                if (y < 0 || y >= local.yCells()) return false;
                return local.voxels()[((z / cell) * (16 / cell) + x / cell) * local.yCells() + y]
                        != 0;
            }
            Side[] sides = column(cx, cz);
            if (sides == null || sides[0].cellSize() > 4) return false;
            // Every nonlocal AO probe is in the outermost block layer of a column.
            int cell = sides[0].cellSize();
            if (x < cell) return sides[0].state(z, worldY) >= 0;
            if (x >= 16 - cell) return sides[1].state(z, worldY) >= 0;
            if (z < cell) return sides[2].state(x, worldY) >= 0;
            if (z >= 16 - cell) return sides[3].state(x, worldY) >= 0;
            return false;
        }
    }

    /** Corner order in face-local (u,v): (0,0), (1,0), (1,1), (0,1). */
    static int corners(View view, int face, int x, int y, int z, int width, int height) {
        int normal = face < 2 ? x : face < 4 ? y : z;
        if ((face & 1) == 0) normal--;
        int u = face < 2 ? z : x, v = face < 2 ? y : face < 4 ? z : y;
        int packed = 0;
        for (int corner = 0; corner < 4; corner++) {
            boolean highU = corner == 1 || corner == 2, highV = corner >= 2;
            int insideU = u + (highU ? width - 1 : 0), outsideU = u + (highU ? width : -1);
            int insideV = v + (highV ? height - 1 : 0), outsideV = v + (highV ? height : -1);
            boolean a = sample(view, face, normal, outsideU, insideV);
            boolean b = sample(view, face, normal, insideU, outsideV);
            boolean diagonal = !(a && b) && sample(view, face, normal, outsideU, outsideV);
            int light = a && b ? 0 : 3 - (a ? 1 : 0) - (b ? 1 : 0) - (diagonal ? 1 : 0);
            packed |= light << (corner * 2);
        }
        return packed;
    }

    private static boolean sample(View view, int face, int normal, int u, int v) {
        return face < 2
                ? view.occupied(normal, v, u)
                : face < 4 ? view.occupied(u, normal, v) : view.occupied(u, v, normal);
    }

    static boolean constantU(int ao) {
        return (ao & 3) == (ao >> 2 & 3) && (ao >> 6 & 3) == (ao >> 4 & 3);
    }

    static boolean constantV(int ao) {
        return (ao & 3) == (ao >> 6 & 3) && (ao >> 2 & 3) == (ao >> 4 & 3);
    }

    static int outwardOrder(int face, int ao) {
        int order =
                switch (face) {
                    case 0, 5 -> 0x39; // 1,2,3,0
                    case 1, 4 -> 0x6C; // 0,3,2,1
                    case 2 -> 0xE4; // 0,1,2,3
                    case 3 -> 0x1B; // 3,2,1,0
                    default -> throw new IllegalArgumentException("Invalid LOD face");
                };
        int result = 0;
        for (int i = 0; i < 4; i++) result |= (ao >> ((order >> (i * 2) & 3) * 2) & 3) << (i * 2);
        return result;
    }

    static boolean flipDiagonal(int ao) {
        return (ao & 3) + (ao >> 4 & 3) > (ao >> 2 & 3) + (ao >> 6 & 3);
    }

    static float shade(int ao, int corner) {
        return .55F + (ao >> (corner * 2) & 3) * .15F;
    }

    private CrossDimensionLodAmbientOcclusion() {}
}
