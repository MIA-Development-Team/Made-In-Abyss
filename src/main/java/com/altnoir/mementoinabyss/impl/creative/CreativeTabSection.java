package com.altnoir.mementoinabyss.impl.creative;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Registration target for one section inside a sectioned creative tab.
 *
 * <p>Entries are stored by id so builders may assign their section before the
 * item registry has finished loading.</p>
 */
public final class CreativeTabSection {
    private final ResourceKey<CreativeModeTab> tab;
    private final Identifier id;
    private final Component title;
    private final @Nullable Identifier bannerSprite;
    private final Set<Identifier> itemIds = new LinkedHashSet<>();

    public CreativeTabSection(ResourceKey<CreativeModeTab> tab, Identifier id, Component title) {
        this(tab, id, title, null);
    }

    /**
     * Creates a section with an optional custom 162x18 GUI banner sprite.
     *
     * <p>The sprite id follows vanilla GUI sprite conventions and resolves to
     * {@code assets/<namespace>/textures/gui/sprites/<path>.png}. When omitted,
     * the renderer uses the built-in MIA theme banner.</p>
     */
    public CreativeTabSection(
            ResourceKey<CreativeModeTab> tab,
            Identifier id,
            Component title,
            @Nullable Identifier bannerSprite
    ) {
        this.tab = tab;
        this.id = id;
        this.title = title;
        this.bannerSprite = bannerSprite;
    }

    public ResourceKey<CreativeModeTab> tab() {
        return tab;
    }

    public Identifier id() {
        return id;
    }

    public Component title() {
        return title;
    }

    public Optional<Identifier> bannerSprite() {
        return Optional.ofNullable(bannerSprite);
    }

    public void add(Identifier itemId) {
        itemIds.add(itemId);
    }

    public List<ItemStack> itemStacks() {
        return itemIds.stream()
                .map(BuiltInRegistries.ITEM::get)
                .flatMap(java.util.Optional::stream)
                .map(Holder.Reference::value)
                .filter(item -> item != Items.AIR)
                .map(Item::getDefaultInstance)
                .toList();
    }
}
