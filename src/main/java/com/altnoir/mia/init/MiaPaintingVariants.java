package com.altnoir.mia.init;

import com.altnoir.mia.util.MiaUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class MiaPaintingVariants {
    /**
     * 全部画作 key。
     * <p>
     * <b>在类初始化时填充</b>（见 {@link #create(String)}）而不是 bootstrap 期间：
     * datagen 时各 provider 是并发跑的，MIA 的 lang 数据在 Reginth 的 lang provider 构建时就要求值，
     * 那时如果还没轮到 bootstrap，这份列表就是空的 —— 实测会静默丢掉全部
     * {@code painting.mia.*.title/.author} 键。
     */
    public static final List<ResourceKey<PaintingVariant>> PAINTING_VARIANTS = new ArrayList<>();

    public static final ResourceKey<PaintingVariant> ABYSS_MAP = create("abyss_map");
    public static final ResourceKey<PaintingVariant> THE_ABYSS = create("the_abyss");
    public static final ResourceKey<PaintingVariant> THE_ABYSS_2 = create("the_abyss2");
    public static final ResourceKey<PaintingVariant> FOSSIL_TREE = create("fossil_tree");
    public static final ResourceKey<PaintingVariant> FORTITUDE_FLOWER = create("fortitude_flower");

    public static void bootstrap(BootstrapContext<PaintingVariant> context) {
        register(context, ABYSS_MAP, 2, 3);
        register(context, THE_ABYSS, 3, 2);
        register(context, THE_ABYSS_2, 2, 2);
        register(context, FOSSIL_TREE, 3, 2);
        register(context, FORTITUDE_FLOWER, 2, 1);
    }

    private static void register(
            BootstrapContext<PaintingVariant> context,
            ResourceKey<PaintingVariant> key,
            int width,
            int height) {
        context.register(key, new PaintingVariant(width, height, key.location()));
    }

    private static ResourceKey<PaintingVariant> create(String name) {
        ResourceKey<PaintingVariant> key =
                ResourceKey.create(Registries.PAINTING_VARIANT, MiaUtil.miaId(name));
        PAINTING_VARIANTS.add(key);
        return key;
    }
}
