package com.altnoir.mementoinabyss.impl.registrate;

import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.world.item.Item;

/** Item builder with MIA-specific registration conveniences. */
public class MiaItemBuilder<T extends Item, P> extends ItemBuilder<T, P> {
    protected MiaItemBuilder(
            MiaRegistrate owner,
            P parent,
            String name,
            BuilderCallback callback,
            NonNullFunction<Item.Properties, T> factory
    ) {
        super(owner, parent, name, callback, factory);
    }

    static <T extends Item, P> MiaItemBuilder<T, P> create(
            MiaRegistrate owner,
            P parent,
            String name,
            BuilderCallback callback,
            NonNullFunction<Item.Properties, T> factory
    ) {
        MiaItemBuilder<T, P> builder = new MiaItemBuilder<>(owner, parent, name, callback, factory);
        builder.defaultModel().defaultLang();
        return builder;
    }

    /** Removes this item from the currently configured default creative tab. */
    public MiaItemBuilder<T, P> ignore() {
        getOwner().ignoreCreativeTab(getName());
        getOwner().defaultCreativeTabKey().ifPresent(this::removeTab);
        return this;
    }

    @Override
    public MiaRegistrate getOwner() {
        return (MiaRegistrate) super.getOwner();
    }
}
