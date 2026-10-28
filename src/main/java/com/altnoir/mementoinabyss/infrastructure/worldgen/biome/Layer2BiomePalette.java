package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.altnoir.mementoinabyss.api.worldgen.Layer2BiomeGroup;
import com.altnoir.mementoinabyss.api.worldgen.Layer2BiomeSelection;
import com.altnoir.mementoinabyss.api.worldgen.Layer2Biomes;
import com.altnoir.mementoinabyss.api.worldgen.Layer2TerrainKind;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.Layer2Layout;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

final class Layer2BiomePalette {
    private final EnumMap<Layer2TerrainKind, Candidates> candidates =
            new EnumMap<>(Layer2TerrainKind.class);
    private final List<Entry> entries;
    private final Holder<Biome> defaultRiver;

    Layer2BiomePalette(
            HolderLookup.RegistryLookup<Layer2BiomeGroup> lookup, Holder<Biome> defaultRiver) {
        this.defaultRiver = defaultRiver;
        entries =
                lookup.listElements()
                        .filter(h -> h.value().weight() > 0)
                        .sorted(Comparator.comparing(h -> h.key().identifier().toString()))
                        .map(h -> new Entry(h.key(), h.value()))
                        .toList();
        for (var kind : Layer2TerrainKind.values()) {
            var available =
                    entries.stream()
                            .filter(e -> e.value().terrainKinds().contains(kind))
                            .toArray(Entry[]::new);
            if (available.length == 0)
                throw new IllegalArgumentException("No enabled Layer2 biome groups for " + kind);
            long[] cumulative = new long[available.length];
            long total = 0;
            for (int i = 0; i < available.length; i++)
                cumulative[i] = total = Math.addExact(total, available[i].value().weight());
            candidates.put(kind, new Candidates(available, cumulative, total));
        }
    }

    private Entry entry(Layer2Layout.Region region) {
        var available = candidates.get(Layer2Layout.terrainKind(region));
        long hash = region.id() ^ 0x651ca947d23b860fL;
        hash = (hash ^ hash >>> 30) * 0xbf58476d1ce4e5b9L;
        hash = (hash ^ hash >>> 27) * 0x94d049bb133111ebL;
        hash ^= hash >>> 31;
        long ticket = Long.remainderUnsigned(hash, available.total()) + 1;
        int index = Arrays.binarySearch(available.cumulative(), ticket);
        return available.entries()[index < 0 ? -index - 1 : index];
    }

    private Identifier zone(Layer2BiomeGroup group, Layer2Layout.Region region, boolean river) {
        if (river) return Layer2Biomes.RIVER;
        if (!region.primary() || group.primary().isEmpty()) return Layer2Biomes.COMPANIONS;
        if (group.transition().isPresent()
                && region.primaryDistance()
                        < Math.min(group.transitionWidth(), region.primaryExtent() * .5))
            return Layer2Biomes.TRANSITION;
        return Layer2Biomes.PRIMARY;
    }

    private Holder<Biome> biome(
            Layer2BiomeGroup group, Layer2Layout.Region region, Identifier zone) {
        if (zone.equals(Layer2Biomes.RIVER)) return group.river().orElse(defaultRiver);
        if (zone.equals(Layer2Biomes.PRIMARY)) return group.primary().orElseThrow();
        if (zone.equals(Layer2Biomes.TRANSITION))
            return group.transition().orElseThrow().get(region.sector());
        return group.companions().get(region.sector());
    }

    Holder<Biome> resolve(Layer2Layout.Region region, boolean river) {
        var group = entry(region).value();
        return biome(group, region, zone(group, region, river));
    }

    Layer2BiomeSelection selection(Layer2Layout.Region region, boolean river) {
        var entry = entry(region);
        var zone = zone(entry.value(), region, river);
        return new Layer2BiomeSelection(
                Optional.of(entry.key()),
                zone,
                region.sector(),
                biome(entry.value(), region, zone));
    }

    Stream<Holder<Biome>> possibleBiomes() {
        return Stream.concat(
                Stream.of(defaultRiver), entries.stream().flatMap(e -> e.value().possibleBiomes()));
    }

    private record Entry(ResourceKey<Layer2BiomeGroup> key, Layer2BiomeGroup value) {}

    private record Candidates(Entry[] entries, long[] cumulative, long total) {}
}
