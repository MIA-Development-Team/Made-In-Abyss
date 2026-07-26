package com.altnoir.mementoinabyss.impl.whistle.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record WhistleLoadout(List<PlacedWhistleFragment> fragments) {
    public static final WhistleLoadout EMPTY = new WhistleLoadout(List.of());
    public static final Codec<WhistleLoadout> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlacedWhistleFragment.CODEC.sizeLimitedListOf(64)
                    .optionalFieldOf("fragments", List.of())
                    .forGetter(WhistleLoadout::fragments)
    ).apply(instance, WhistleLoadout::new));

    public WhistleLoadout {
        fragments = List.copyOf(fragments);
    }

    public WhistleLoadout with(PlacedWhistleFragment fragment) {
        List<PlacedWhistleFragment> updated = new ArrayList<>(fragments);
        updated.add(fragment);
        return new WhistleLoadout(updated);
    }

    public WhistleLoadout without(int index) {
        List<PlacedWhistleFragment> updated = new ArrayList<>(fragments);
        updated.remove(index);
        return new WhistleLoadout(updated);
    }
}
