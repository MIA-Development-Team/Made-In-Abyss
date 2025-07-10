package com.altnoir.mia.init;

import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class MiaPaintingVariants {
    public static final ResourceKey<PaintingVariant> ABYSS_MAP = create("abyss_map");
    public static final ResourceKey<PaintingVariant> ABYSS = create("abyss");
    public static final ResourceKey<PaintingVariant> FOSSIL_TREE = create("fossil_tree");
    public static final ResourceKey<PaintingVariant> FORTITUDE_FLOWER = create("fortitude_flower");

    public static void bootstrap(BootstrapContext<PaintingVariant> context) {
        register(context, ABYSS_MAP, 2, 3);
        register(context, ABYSS, 2, 2);
        register(context, FOSSIL_TREE, 3, 2);
        register(context, FORTITUDE_FLOWER, 2, 1);
    }

    private static void register(BootstrapContext<PaintingVariant> context, ResourceKey<PaintingVariant> key, int width, int height) {
        context.register(key, new PaintingVariant(width, height, key.location()));
    }

    private static ResourceKey<PaintingVariant> create(String name) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, MiaUtil.miaId(name));
    }
}
