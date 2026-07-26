package com.altnoir.mementoinabyss.content.item.whistle.fragment;

import com.altnoir.mementoinabyss.impl.whistle.fragment.WhistleFragmentDefinition;
import lombok.Getter;
import net.minecraft.world.item.Item;

@Getter
public abstract class WhistleFragmentItem<D extends WhistleFragmentDefinition> extends Item {
    private final D definition;

    protected WhistleFragmentItem(Properties properties, D definition) {
        super(properties.stacksTo(1));
        this.definition = definition;
    }
}
