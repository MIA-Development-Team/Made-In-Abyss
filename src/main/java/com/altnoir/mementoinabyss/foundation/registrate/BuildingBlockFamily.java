package com.altnoir.mementoinabyss.foundation.registrate;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.Nullable;

/**
 * A registered building palette: one existing base and an ordered, typed set of variants.
 *
 * <p>The base is never registered/generated again. Models, loot, tags and recipes belong to each
 * variant, so variants can be omitted, renamed or configured without duplicating a family.
 */
public final class BuildingBlockFamily {
    private final BlockEntry<? extends Block> base;
    private final Map<BuildingBlockVariant<?>, BlockEntry<?>> variants;

    private BuildingBlockFamily(
            BlockEntry<? extends Block> base,
            Map<BuildingBlockVariant<?>, BlockEntry<?>> variants) {
        this.base = base;
        this.variants = Collections.unmodifiableMap(new LinkedHashMap<>(variants));
    }

    public BlockEntry<? extends Block> base() {
        return base;
    }

    /** The key carries the block type; requesting an omitted variant fails immediately. */
    @SuppressWarnings("unchecked")
    public <B extends Block> BlockEntry<B> get(BuildingBlockVariant<B> variant) {
        var entry = variants.get(variant);
        if (entry == null) {
            throw new IllegalArgumentException(
                    "Missing " + variant.suffix() + " in " + base.getId());
        }
        return (BlockEntry<B>) entry;
    }

    public Map<BuildingBlockVariant<?>, BlockEntry<?>> variants() {
        return variants;
    }

    public static Builder stone(MiaRegistrate registrate, BlockEntry<? extends Block> base) {
        return new Builder(new Context(registrate, base, base.getId().getPath(), null))
                .variant(BuildingBlockVariant.STAIRS)
                .variant(BuildingBlockVariant.SLAB)
                .variant(BuildingBlockVariant.WALL);
    }

    public static Builder wooden(
            MiaRegistrate registrate,
            String prefix,
            BlockEntry<? extends Block> planks,
            WoodType woodType) {
        return new Builder(
                        new Context(registrate, planks, prefix, Objects.requireNonNull(woodType)))
                .variant(BuildingBlockVariant.STAIRS)
                .variant(BuildingBlockVariant.SLAB)
                .variant(BuildingBlockVariant.FENCE)
                .variant(BuildingBlockVariant.FENCE_GATE)
                .variant(BuildingBlockVariant.DOOR)
                .variant(BuildingBlockVariant.TRAPDOOR)
                .variant(BuildingBlockVariant.PRESSURE_PLATE)
                .variant(BuildingBlockVariant.BUTTON);
    }

    public record Context(
            MiaRegistrate registrate,
            BlockEntry<? extends Block> base,
            String prefix,
            @Nullable WoodType woodType) {
        public boolean wooden() {
            return woodType != null;
        }

        public WoodType requireWoodType() {
            return Objects.requireNonNull(woodType, "This variant needs a wooden family");
        }
    }

    public static final class Builder {
        private final Context context;
        private final Map<BuildingBlockVariant<?>, Registration<?>> variants =
                new LinkedHashMap<>();
        private boolean registered;

        private Builder(Context context) {
            this.context = context;
        }

        /** Adds a variant, or restores its defaults, without changing its position in the family. */
        public <B extends Block> Builder variant(BuildingBlockVariant<B> variant) {
            return variant(variant, context.prefix() + "_" + variant.suffix(), builder -> {});
        }

        /**
         * Customizes the block builder after family defaults, before its item and block are registered.
         * The family owns item creation; do not call item(), build() or register() in this callback.
         * Client model callbacks must retain Registrate's supplier boundary.
         */
        public <B extends Block> Builder variant(
                BuildingBlockVariant<B> variant,
                NonNullConsumer<BlockBuilder<B, MiaRegistrate>> configure) {
            return variant(variant, context.prefix() + "_" + variant.suffix(), configure);
        }

        public <B extends Block> Builder variant(
                BuildingBlockVariant<B> variant,
                String name,
                NonNullConsumer<BlockBuilder<B, MiaRegistrate>> configure) {
            ensureMutable();
            Objects.requireNonNull(variant, "variant");
            Objects.requireNonNull(configure, "configure");
            if (name == null || !name.matches("[a-z0-9/._-]+")) {
                throw new IllegalArgumentException("Invalid family variant name: " + name);
            }
            variants.put(variant, new Registration<>(variant, name, configure));
            return this;
        }

        public Builder without(BuildingBlockVariant<?> variant) {
            ensureMutable();
            variants.remove(variant);
            return this;
        }

        public BuildingBlockFamily register() {
            ensureMutable();
            // Validate the entire plan before any registrations have side effects.
            var names = new HashSet<String>();
            names.add(context.base().getId().getPath());
            for (var registration : variants.values()) {
                if (!names.add(registration.name())) {
                    throw new IllegalArgumentException(
                            "Duplicate family variant name: " + registration.name());
                }
                registration.variant().validate(context);
            }
            registered = true;
            Map<BuildingBlockVariant<?>, BlockEntry<?>> entries = new LinkedHashMap<>();
            variants.forEach(
                    (variant, registration) ->
                            entries.put(variant, registration.register(context)));
            return new BuildingBlockFamily(context.base(), entries);
        }

        private void ensureMutable() {
            if (registered)
                throw new IllegalStateException("Family already registered: " + context.prefix());
        }
    }

    private record Registration<B extends Block>(
            BuildingBlockVariant<B> variant,
            String name,
            NonNullConsumer<BlockBuilder<B, MiaRegistrate>> configure) {
        BlockEntry<B> register(Context context) {
            var builder = variant.create(context, name);
            configure.accept(builder);
            return variant.withItem(builder, context).register();
        }
    }
}
