package com.altnoir.mementoinabyss.impl.whistle.component;

import com.altnoir.mementoinabyss.impl.whistle.grid.GridCell;
import com.altnoir.mementoinabyss.impl.whistle.grid.GridRotation;
import com.altnoir.mementoinabyss.content.item.whistle.fragment.WhistleFragmentItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Set;
import java.util.stream.Collectors;

public record PlacedWhistleFragment(
        ItemStackTemplate fragment,
        int x,
        int y,
        GridRotation rotation
) {
    public static final Codec<PlacedWhistleFragment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStackTemplate.CODEC.fieldOf("fragment").forGetter(PlacedWhistleFragment::fragment),
            Codec.intRange(0, 15).fieldOf("x").forGetter(PlacedWhistleFragment::x),
            Codec.intRange(0, 15).fieldOf("y").forGetter(PlacedWhistleFragment::y),
            GridRotation.CODEC.optionalFieldOf("rotation", GridRotation.NONE)
                    .forGetter(PlacedWhistleFragment::rotation)
    ).apply(instance, PlacedWhistleFragment::new));

    public PlacedWhistleFragment {
        fragment = fragment.withCount(1);
    }

    public static PlacedWhistleFragment of(ItemStack stack, int x, int y, GridRotation rotation) {
        return new PlacedWhistleFragment(
                ItemStackTemplate.fromNonEmptyStack(stack.copyWithCount(1)),
                x,
                y,
                rotation
        );
    }

    public ItemStack createStack() {
        return fragment.create();
    }

    public Set<GridCell> occupiedCells() {
        if (!(fragment.item().value() instanceof WhistleFragmentItem<?> fragmentItem)) {
            return Set.of();
        }
        return fragmentItem.getDefinition().shape().rotate(rotation).cells().stream()
                .map(cell -> cell.offset(x, y))
                .collect(Collectors.toUnmodifiableSet());
    }
}
