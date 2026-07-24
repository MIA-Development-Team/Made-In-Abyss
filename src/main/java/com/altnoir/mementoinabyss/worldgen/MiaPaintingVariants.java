package com.altnoir.mementoinabyss.worldgen;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;

import java.util.Optional;

public final class MiaPaintingVariants {
    public static final ResourceKey<PaintingVariant> ABYSS_MAP = key("abyss_map");
    public static final ResourceKey<PaintingVariant> THE_ABYSS = key("the_abyss");
    public static final ResourceKey<PaintingVariant> THE_ABYSS_2 = key("the_abyss2");
    public static final ResourceKey<PaintingVariant> FOSSIL_TREE = key("fossil_tree");
    public static final ResourceKey<PaintingVariant> FORTITUDE_FLOWER = key("fortitude_flower");

    public static void bootstrap(BootstrapContext<PaintingVariant> context) {
        register(context, ABYSS_MAP, 2, 3);
        register(context, THE_ABYSS, 3, 2);
        register(context, THE_ABYSS_2, 2, 2);
        register(context, FOSSIL_TREE, 3, 2);
        register(context, FORTITUDE_FLOWER, 2, 1);
    }

    private static void register(BootstrapContext<PaintingVariant> context,
                                 ResourceKey<PaintingVariant> key, int width, int height) {
        context.register(key, new PaintingVariant(width, height, key.identifier(),
                Optional.empty(), Optional.empty()));
    }

    private static ResourceKey<PaintingVariant> key(String path) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, MementoInAbyss.asResource(path));
    }

    private MiaPaintingVariants() {
    }
}
