package com.altnoir.mementoinabyss.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

/** One bounded part of an atomic column transfer. The last part also carries the commit marker. */
public record CrossDimensionLodBatchPayload(CrossDimensionLodTransfer transfer,
        List<Section> sections, boolean commit, int checksum) implements CrossDimensionLodStreamPayload {
    public static final int MAX_ENCODED_BYTES = 32 * 1024;
    public static final int MAX_SECTIONS = 128;
    public static final byte DIRTY_NEG_X = 1;
    public static final byte DIRTY_POS_X = 1 << 1;
    public static final byte DIRTY_NEG_Y = 1 << 2;
    public static final byte DIRTY_POS_Y = 1 << 3;
    public static final byte DIRTY_NEG_Z = 1 << 4;
    public static final byte DIRTY_POS_Z = 1 << 5;
    public static final byte DIRTY_ALL_FACES = 63;
    public static final Type<CrossDimensionLodBatchPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MementoInAbyss.ID, "cross_dimension_lod_batch"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrossDimensionLodBatchPayload> STREAM_CODEC =
            StreamCodec.ofMember(CrossDimensionLodBatchPayload::encode, CrossDimensionLodBatchPayload::decode);

    public CrossDimensionLodBatchPayload(CrossDimensionLodTransfer transfer, List<Section> sections, boolean commit) {
        this(transfer, sections, commit, checksum(transfer, sections, commit));
    }

    public CrossDimensionLodBatchPayload {
        if (transfer == null || sections == null || sections.size() > MAX_SECTIONS
                || sections.size() > transfer.updateCount() || sections.isEmpty() && !commit) {
            throw new IllegalArgumentException("Invalid LOD batch");
        }
        sections = List.copyOf(sections);
        boolean[] seen = new boolean[transfer.sectionCount()];
        int size = 16 / transfer.cellSize();
        for (Section section : sections) {
            if (section.sectionY() >= seen.length || seen[section.sectionY()]
                    || section.revision() <= transfer.baseRevision() || section.revision() > transfer.targetRevision()
                    || section.voxels().length != size * size * size) {
                throw new IllegalArgumentException("Invalid LOD batch section layout/revision");
            }
            seen[section.sectionY()] = true;
        }
        if (encodedSize(transfer, sections) > MAX_ENCODED_BYTES
                || checksum != checksum(transfer, sections, commit)) {
            throw new IllegalArgumentException("Invalid LOD batch size/checksum");
        }
    }

    /** Returns null when the remaining tick budget cannot fit the next section. Never exceeds either bound. */
    public static CrossDimensionLodBatchPayload pack(CrossDimensionLodTransfer transfer, List<Section> updates,
                                                    int offset, int byteBudget) {
        if (updates.size() != transfer.updateCount() || offset < 0 || offset > updates.size()) {
            throw new IllegalArgumentException("Invalid LOD batch cursor");
        }
        int limit = Math.min(byteBudget, MAX_ENCODED_BYTES);
        // Reserve the largest possible section-count VarInt, including when crossing 127 entries.
        int bytes = headerSize(transfer) + varIntSize(MAX_SECTIONS);
        if (bytes > limit) return null;
        int end = offset;
        while (end < updates.size() && end - offset < MAX_SECTIONS) {
            int next = updates.get(end).encodedSize();
            if (bytes + next > limit) break;
            bytes += next;
            end++;
        }
        if (end == offset && offset < updates.size()) return null;
        return new CrossDimensionLodBatchPayload(transfer, updates.subList(offset, end), end == updates.size());
    }

    /** Retained primitive array bytes, separately bounded from the much smaller wire representation. */
    @Override public int retainedBytes() {
        int bytes = encodedSize();
        for (var section : sections) bytes += section.palette().length * 4 + section.voxels().length * 2;
        return bytes;
    }

    public int encodedSize() { return encodedSize(transfer, sections); }

    private static int encodedSize(CrossDimensionLodTransfer t, List<Section> sections) {
        int bytes = headerSize(t) + varIntSize(sections.size());
        for (Section section : sections) bytes += section.encodedSize();
        return bytes;
    }

    private static int headerSize(CrossDimensionLodTransfer t) {
        return t.encodedSize() + 1 + 4; // commit and CRC
    }

    private static int varIntSize(int value) {
        int bytes = 1;
        while ((value & ~127) != 0) { bytes++; value >>>= 7; }
        return bytes;
    }

    private static int checksum(CrossDimensionLodTransfer t, List<Section> sections, boolean commit) {
        if (t == null || sections == null) throw new IllegalArgumentException("Null LOD batch");
        CRC32 crc = new CRC32();
        t.checksum(crc);
        update(crc, commit ? 1 : 0);
        update(crc, sections.size());
        for (Section section : sections) {
            if (section == null) throw new IllegalArgumentException("Null LOD section");
            crc.update(section.encoded);
        }
        return (int) crc.getValue();
    }

    private static void update(CRC32 crc, long value) {
        for (int shift = 56; shift >= 0; shift -= 8) crc.update((int) (value >>> shift));
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        transfer.encode(buffer);
        buffer.writeBoolean(commit);
        buffer.writeVarInt(sections.size());
        for (Section section : sections) {
            buffer.writeBytes(section.encoded);
        }
        buffer.writeInt(checksum);
    }

    private static CrossDimensionLodBatchPayload decode(RegistryFriendlyByteBuf buffer) {
        if (buffer.readableBytes() > MAX_ENCODED_BYTES) throw new IllegalArgumentException("Oversized LOD batch");
        var transfer = CrossDimensionLodTransfer.decode(buffer);
        boolean commit = buffer.readBoolean();
        int count = buffer.readVarInt();
        if (count < 0 || count > MAX_SECTIONS || count > transfer.updateCount() || count == 0 && !commit) {
            throw new IllegalArgumentException("Invalid LOD batch count");
        }
        List<Section> sections = new ArrayList<>(count);
        int size = 16 / transfer.cellSize();
        for (int n = 0; n < count; n++) {
            int start = buffer.readerIndex();
            int index = buffer.readVarInt();
            long revision = buffer.readLong();
            byte faces = buffer.readByte();
            int paletteSize = buffer.readVarInt();
            if (index < 0 || index >= transfer.sectionCount() || paletteSize <= 0 || paletteSize > 4096) {
                throw new IllegalArgumentException("Invalid LOD section header");
            }
            int[] palette = new int[paletteSize];
            for (int i = 0; i < paletteSize; i++) palette[i] = buffer.readVarInt();
            short[] voxels = CrossDimensionLodVoxelCodec.decode(buffer, paletteSize, size * size * size);
            var section = new Section(index, revision, faces, palette, voxels);
            if (buffer.readerIndex() - start != section.encoded.length) throw new IllegalArgumentException("Noncanonical LOD section");
            for (int i = 0; i < section.encoded.length; i++) {
                if (buffer.getByte(start + i) != section.encoded[i]) throw new IllegalArgumentException("Noncanonical LOD section");
            }
            sections.add(section);
        }
        var result = new CrossDimensionLodBatchPayload(transfer, sections, commit, buffer.readInt());
        if (buffer.isReadable()) throw new IllegalArgumentException("Trailing LOD batch data");
        return result;
    }

    /** Owned immutable-by-convention voxel arrays; no transfer header is retained in the client store. */
    public static final class Section {
        private final int sectionY;
        private final long revision;
        private final byte dirtyFaces;
        private final int[] palette;
        private final short[] voxels;
        private final byte[] encoded;

        public Section(int sectionY, long revision, byte dirtyFaces, int[] palette, short[] voxels) {
            if (sectionY < 0 || sectionY >= 1024 || revision <= 0 || (dirtyFaces & ~63) != 0
                    || palette == null || palette.length == 0 || palette.length > 4096
                    || voxels == null || voxels.length == 0 || voxels.length > 4096) {
                throw new IllegalArgumentException("Invalid LOD section");
            }
            for (int state : palette) if (state < 0) throw new IllegalArgumentException("Negative block state");
            for (short voxel : voxels) if (Short.toUnsignedInt(voxel) >= palette.length) {
                throw new IllegalArgumentException("Invalid LOD palette index");
            }
            this.sectionY = sectionY; this.revision = revision; this.dirtyFaces = dirtyFaces;
            this.palette = palette; this.voxels = voxels;
            // Prepared once on the load worker (or network decode), not rescanned for each tick's byte budget/CRC.
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                buffer.writeVarInt(sectionY); buffer.writeLong(revision); buffer.writeByte(dirtyFaces);
                buffer.writeVarInt(palette.length);
                for (int state : palette) buffer.writeVarInt(state);
                CrossDimensionLodVoxelCodec.encode(buffer, palette.length, voxels);
                encoded = new byte[buffer.readableBytes()];
                buffer.readBytes(encoded);
            } finally { buffer.release(); }
        }

        public int sectionY() { return sectionY; }
        public long revision() { return revision; }
        public byte dirtyFaces() { return dirtyFaces; }
        public int[] palette() { return palette; }
        public short[] voxels() { return voxels; }
        public int encodedSize() { return encoded.length; }

        public boolean sameContent(Section other) {
            return sectionY == other.sectionY && revision == other.revision && dirtyFaces == other.dirtyFaces
                    && Arrays.equals(palette, other.palette) && Arrays.equals(voxels, other.voxels);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
