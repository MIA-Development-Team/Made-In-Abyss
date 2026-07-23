package com.altnoir.mementoinabyss.impl.registrate;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class MiaRegistrate extends AbstractRegistrate<MiaRegistrate> {
    private final Set<String> ignoredCreativeTabEntries = new HashSet<>();
    private ResourceKey<CreativeModeTab> defaultCreativeTab;

    protected MiaRegistrate(String modId) {
        super(modId);
    }

    public static MiaRegistrate create(String modId) {
        return new MiaRegistrate(modId);
    }

    @Override
    public MiaRegistrate defaultCreativeTab(ResourceKey<CreativeModeTab> creativeModeTab) {
        defaultCreativeTab = creativeModeTab;
        return super.defaultCreativeTab(creativeModeTab);
    }

    @Override
    public <T extends Item, P> MiaItemBuilder<T, P> item(
            P parent,
            String name,
            NonNullFunction<Item.Properties, T> factory
    ) {
        return (MiaItemBuilder<T, P>) this
                .<Item, T, P, ItemBuilder<T, P>>entry(name, callback -> {
                    MiaItemBuilder<T, P> builder =
                            MiaItemBuilder.create(this, parent, name, callback, factory);
                    if (defaultCreativeTab != null && !ignoredCreativeTabEntries.contains(name)) {
                        builder.tab(defaultCreativeTab);
                    }
                    return builder;
                });
    }

    @Override
    public <T extends Item> MiaItemBuilder<T, MiaRegistrate> item(
            NonNullFunction<Item.Properties, T> factory
    ) {
        return item(self(), currentName(), factory);
    }

    @Override
    public <T extends Item> MiaItemBuilder<T, MiaRegistrate> item(
            String name,
            NonNullFunction<Item.Properties, T> factory
    ) {
        return item(self(), name, factory);
    }

    @Override
    public <T extends Item, P> MiaItemBuilder<T, P> item(
            P parent,
            NonNullFunction<Item.Properties, T> factory
    ) {
        return item(parent, currentName(), factory);
    }

    @Override
    public <T extends Block, P> MiaBlockBuilder<T, P> block(
            P parent,
            String name,
            NonNullFunction<BlockBehaviour.Properties, T> factory
    ) {
        return (MiaBlockBuilder<T, P>) this
                .<Block, T, P, BlockBuilder<T, P>>entry(
                        name,
                        callback -> MiaBlockBuilder.create(this, parent, name, callback, factory)
                );
    }

    @Override
    public <T extends Block> MiaBlockBuilder<T, MiaRegistrate> block(
            NonNullFunction<BlockBehaviour.Properties, T> factory
    ) {
        return block(self(), currentName(), factory);
    }

    @Override
    public <T extends Block> MiaBlockBuilder<T, MiaRegistrate> block(
            String name,
            NonNullFunction<BlockBehaviour.Properties, T> factory
    ) {
        return block(self(), name, factory);
    }

    @Override
    public <T extends Block, P> MiaBlockBuilder<T, P> block(
            P parent,
            NonNullFunction<BlockBehaviour.Properties, T> factory
    ) {
        return block(parent, currentName(), factory);
    }

    void ignoreCreativeTab(String name) {
        ignoredCreativeTabEntries.add(name);
    }

    Optional<ResourceKey<CreativeModeTab>> defaultCreativeTabKey() {
        return Optional.ofNullable(defaultCreativeTab);
    }

    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, MiaRegistrate> creativeTab(Consumer<CreativeModeTab.Builder> config) {
        return creativeTab(self(), config);
    }

    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, MiaRegistrate> creativeTab(String name) {
        return creativeTab(self(), name);
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> creativeTab(P parent, Consumer<CreativeModeTab.Builder> config) {
        return creativeTab(parent, currentName(), config);
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> creativeTab(P parent, String name) {
        return creativeTab(parent, name, tab -> {
        });
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> creativeTab(P parent, String name, Consumer<CreativeModeTab.Builder> config) {
        return this.generic(parent, name, Registries.CREATIVE_MODE_TAB, () -> {
            var builder = CreativeModeTab.builder()
                    .icon(() -> getAll(Registries.ITEM).stream().findFirst().map(ItemEntry::cast).map(ItemEntry::asStack).orElse(new ItemStack(Items.AIR)))
                    .title(this.addLang("itemGroup", MementoInAbyss.asResource(name), MementoInAbyss.NAME + ": " + RegistrateLangProvider.toEnglishName(name)));
            config.accept(builder);
            return builder.build();
        });
    }
}
