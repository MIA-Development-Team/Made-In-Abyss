package com.altnoir.mementoinabyss.impl.lod.server;

import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodBatchPayload.Section;
import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodTransfer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/** Portable, canonical voxel data. No runtime block IDs, source revisions or connection epochs on disk. */
public record MiaLodCacheData(
        int cellSize, int minY, int sectionCount, String[] palette, short[] voxels) {
    public static final int FORMAT = 1;

    public MiaLodCacheData {
        validateLayout(cellSize, minY, sectionCount);
        if (palette == null || palette.length == 0 || palette.length > 4096 || voxels == null) {
            throw new IllegalArgumentException("Invalid cached LOD layout");
        }
        int size = 16 / cellSize;
        if (voxels.length != sectionCount * size * size * size)
            throw new IllegalArgumentException("Invalid cached LOD size");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < palette.length; i++) {
            String state = palette[i];
            if (state == null
                    || state.isBlank()
                    || state.length() > 1024
                    || !seen.add(state)
                    || i > 1 && palette[i - 1].compareTo(state) >= 0)
                throw new IllegalArgumentException("Invalid cached LOD palette");
        }
        boolean[] used = new boolean[palette.length];
        used[0] = true; // Air is always retained, even in completely solid columns.
        for (short voxel : voxels) {
            int index = Short.toUnsignedInt(voxel);
            if (index >= palette.length)
                throw new IllegalArgumentException("Invalid cached LOD index");
            used[index] = true;
        }
        for (boolean value : used)
            if (!value) throw new IllegalArgumentException("Non-canonical cached LOD palette");
    }

    private static void validateLayout(int cellSize, int minY, int sectionCount) {
        if (cellSize < 1
                || cellSize > 16
                || Integer.bitCount(cellSize) != 1
                || minY % 16 != 0
                || minY < -65536
                || minY > 65536
                || sectionCount < 1
                || sectionCount > 1024) {
            throw new IllegalArgumentException("Invalid cached LOD layout");
        }
        int size = 16 / cellSize;
        if (sectionCount * size > 1024 || sectionCount * size * size * size > 262144) {
            throw new IllegalArgumentException("Oversized cached LOD column");
        }
    }

    /** Shared probe/write policy: do not queue disk writes for previews the server will never probe. */
    public static boolean worthCaching(List<Section> sections) {
        long bytes = 0;
        for (var section : sections) {
            bytes += section.encodedSize();
            if (bytes >= 8 * 1024) return true;
        }
        return false;
    }

    public static MiaLodCacheData fromSections(
            int cellSize, int minY, List<Section> sections, IntFunction<String> names) {
        validateLayout(cellSize, minY, sections.size());
        Map<Integer, String> named = new HashMap<>();
        String air = names.apply(sections.getFirst().palette()[0]);
        SortedSet<String> states = new TreeSet<>();
        int size = 16 / cellSize, volume = size * size * size;
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            if (section.sectionY() != i
                    || section.voxels().length != volume
                    || !air.equals(names.apply(section.palette()[0])))
                throw new IllegalArgumentException("Inconsistent cache sections");
            boolean[] used = new boolean[section.palette().length];
            for (short voxel : section.voxels()) used[Short.toUnsignedInt(voxel)] = true;
            for (int p = 0; p < used.length; p++)
                if (used[p]) {
                    states.add(named.computeIfAbsent(section.palette()[p], names::apply));
                }
        }
        states.remove(air);
        String[] palette = new String[states.size() + 1];
        palette[0] = air;
        int index = 1;
        for (String state : states) palette[index++] = state;
        Map<String, Short> lookup = new HashMap<>();
        for (int i = 0; i < palette.length; i++) lookup.put(palette[i], (short) i);
        short[] voxels = new short[sections.size() * volume];
        for (int i = 0; i < sections.size(); i++) {
            var section = sections.get(i);
            short[] remap = new short[section.palette().length];
            for (int p = 0; p < remap.length; p++) {
                String name = named.get(section.palette()[p]);
                if (name != null) remap[p] = lookup.get(name);
            }
            for (int j = 0; j < volume; j++) {
                voxels[i * volume + j] = remap[Short.toUnsignedInt(section.voxels()[j])];
            }
        }
        return new MiaLodCacheData(cellSize, minY, sections.size(), palette, voxels);
    }

    public boolean matchesLayout(CrossDimensionLodTransfer t) {
        return t.cellSize() == cellSize && t.minY() == minY && t.sectionCount() == sectionCount;
    }

    /** Client thread: resolve named states against this connection's registry, never default unknown states to air. */
    public List<Section> sections(CrossDimensionLodTransfer t, ToIntFunction<String> resolve) {
        if (!t.replacement() || !matchesLayout(t))
            throw new IllegalArgumentException("Incompatible cached LOD layout");
        int[] ids = Arrays.stream(palette).mapToInt(resolve).toArray();
        int size = 16 / cellSize, volume = size * size * size;
        List<Section> result = new ArrayList<>(sectionCount);
        short[] remap = new short[ids.length];
        int[] localPalette = new int[Math.min(ids.length, volume + 1)];
        for (int i = 0; i < sectionCount; i++) {
            // Primitive remapping only: no per-voxel boxed hash lookup, and no repeated 4096-entry
            // column palette in every section's retained wire image after cache restoration.
            Arrays.fill(remap, (short) -1);
            remap[0] = 0;
            localPalette[0] = ids[0];
            int used = 1;
            short[] data = new short[volume];
            for (int j = 0; j < volume; j++) {
                int global = Short.toUnsignedInt(voxels[i * volume + j]);
                short local = remap[global];
                if (local < 0) {
                    local = (short) used++;
                    localPalette[local] = ids[global];
                    remap[global] = local;
                }
                data[j] = local;
            }
            result.add(
                    new Section(
                            i,
                            t.targetRevision(),
                            (byte) 63,
                            Arrays.copyOf(localPalette, used),
                            data));
        }
        return List.copyOf(result);
    }

    public String digest() {
        MessageDigest digest = sha256();
        for (int value : new int[] {FORMAT, cellSize, minY, sectionCount, palette.length})
            update(digest, value);
        for (String state : palette) {
            byte[] bytes = state.getBytes(StandardCharsets.UTF_8);
            update(digest, bytes.length);
            digest.update(bytes);
        }
        byte[] block = new byte[Math.min(voxels.length * 2, 8192)];
        for (int start = 0; start < voxels.length; ) {
            int length = Math.min(block.length / 2, voxels.length - start);
            for (int i = 0; i < length; i++) {
                short voxel = voxels[start + i];
                block[i * 2] = (byte) (voxel >>> 8);
                block[i * 2 + 1] = (byte) voxel;
            }
            digest.update(block, 0, length * 2);
            start += length;
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    public static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    private static void update(MessageDigest digest, int value) {
        for (int shift = 24; shift >= 0; shift -= 8) digest.update((byte) (value >>> shift));
    }
}
