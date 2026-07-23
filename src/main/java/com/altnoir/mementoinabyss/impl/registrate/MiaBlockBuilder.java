package com.altnoir.mementoinabyss.impl.registrate;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Block builder with MIA-specific registration conveniences. */
public class MiaBlockBuilder<T extends Block, P> extends BlockBuilder<T, P> {
    protected MiaBlockBuilder(
            MiaRegistrate owner,
            P parent,
            String name,
            BuilderCallback callback,
            NonNullFunction<BlockBehaviour.Properties, T> factory
    ) {
        super(owner, parent, name, callback, factory, BlockBehaviour.Properties::of);
    }

    static <T extends Block, P> MiaBlockBuilder<T, P> create(
            MiaRegistrate owner,
            P parent,
            String name,
            BuilderCallback callback,
            NonNullFunction<BlockBehaviour.Properties, T> factory
    ) {
        MiaBlockBuilder<T, P> builder = new MiaBlockBuilder<>(owner, parent, name, callback, factory);
        builder.defaultBlockstate().defaultLoot().defaultLang();
        return builder;
    }

    /** Removes this block's item from the currently configured default creative tab. */
    public MiaBlockBuilder<T, P> ignore() {
        getOwner().ignoreCreativeTab(getName());
        return this;
    }

    @Override
    public MiaRegistrate getOwner() {
        return (MiaRegistrate) super.getOwner();
    }
}
