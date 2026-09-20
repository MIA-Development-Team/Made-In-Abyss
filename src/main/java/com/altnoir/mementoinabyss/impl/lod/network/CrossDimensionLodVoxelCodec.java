package com.altnoir.mementoinabyss.impl.lod.network;

import java.util.Arrays;
import net.minecraft.network.FriendlyByteBuf;

/** Lossless palette indices: choose the smaller of bit packing and maximal same-value runs. */
final class CrossDimensionLodVoxelCodec {
    static void encode(FriendlyByteBuf out, int paletteSize, short[] voxels) {
        int bits = 32 - Integer.numberOfLeadingZeros(paletteSize - 1);
        if (bits == 0) {
            out.writeByte(0);
            return;
        }
        int packedBytes = (voxels.length * bits + 7) / 8, runBytes = 0;
        for (int i = 0; i < voxels.length; ) {
            int end = i + 1;
            while (end < voxels.length && voxels[end] == voxels[i]) end++;
            runBytes += varIntSize(end - i) + varIntSize(Short.toUnsignedInt(voxels[i]));
            i = end;
        }
        boolean runs = runBytes < packedBytes;
        out.writeByte(runs ? 1 : 0);
        if (runs) {
            for (int i = 0; i < voxels.length; ) {
                int end = i + 1;
                while (end < voxels.length && voxels[end] == voxels[i]) end++;
                out.writeVarInt(end - i);
                out.writeVarInt(Short.toUnsignedInt(voxels[i]));
                i = end;
            }
        } else if (bits != 0) {
            int pending = 0, count = 0;
            for (short voxel : voxels) {
                pending |= Short.toUnsignedInt(voxel) << count;
                count += bits;
                while (count >= 8) {
                    out.writeByte(pending);
                    pending >>>= 8;
                    count -= 8;
                }
            }
            if (count != 0) out.writeByte(pending);
        }
    }

    static short[] decode(FriendlyByteBuf in, int paletteSize, int volume) {
        short[] voxels = new short[volume];
        int mode = in.readUnsignedByte();
        if (mode == 1) {
            for (int i = 0; i < volume; ) {
                int run = in.readVarInt(), value = in.readVarInt();
                if (run < 1 || run > volume - i || value < 0 || value >= paletteSize) {
                    throw new IllegalArgumentException("Invalid LOD voxel run");
                }
                Arrays.fill(voxels, i, i + run, (short) value);
                i += run;
            }
        } else if (mode == 0) {
            int bits = 32 - Integer.numberOfLeadingZeros(paletteSize - 1);
            int mask = (1 << bits) - 1, pending = 0, count = 0;
            for (int i = 0; bits != 0 && i < volume; i++) {
                while (count < bits) {
                    pending |= in.readUnsignedByte() << count;
                    count += 8;
                }
                int value = pending & mask;
                if (value >= paletteSize)
                    throw new IllegalArgumentException("Invalid packed LOD palette index");
                voxels[i] = (short) value;
                pending >>>= bits;
                count -= bits;
            }
            if (pending != 0) throw new IllegalArgumentException("Nonzero LOD bit padding");
        } else throw new IllegalArgumentException("Invalid LOD voxel encoding");
        return voxels;
    }

    private static int varIntSize(int value) {
        int bytes = 1;
        while ((value & ~127) != 0) {
            bytes++;
            value >>>= 7;
        }
        return bytes;
    }

    private CrossDimensionLodVoxelCodec() {}
}
