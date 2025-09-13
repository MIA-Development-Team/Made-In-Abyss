package com.altnoir.mia.init;

import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class MiaTags {
    public static class Blocks {
        // 新增多个自定义方块标签定义
        public static final TagKey<Block> BASE_STONE_ABYSS = create("base_stone_abyss");
        public static final TagKey<Block> ANDESITE_ORE_REPLACEABLES = create("andesite_ore_replaceables");
        public static final TagKey<Block> COVERGRASS = create("covergrass");
        // 用于tooltip
        public static final TagKey<Block> NEED_PRASIOLITE_TOOL = create("need_prasiolite_tool");
        public static final TagKey<Block> INCORRECT_FOR_PRASIOLITE_TOOL = create("incorrect_for_prasiolite_tool");
        public static final TagKey<Block> MINEABLE_WITH_COMPOSITE = create("mineable/composite");

        private static TagKey<Block> create(String name) {
            return TagKey.create(Registries.BLOCK, MiaUtil.miaId(name));
        }
    }


    public static class Items {
        public static final TagKey<Item> ARTIFACT_GRADE_D = create("artifact_grade_d");
        public static final TagKey<Item> ARTIFACT_GRADE_C = create("artifact_grade_c");
        public static final TagKey<Item> ARTIFACT_GRADE_B = create("artifact_grade_b");
        public static final TagKey<Item> ARTIFACT_GRADE_A = create("artifact_grade_a");
        public static final TagKey<Item> ARTIFACT_GRADE_S = create("artifact_grade_s");
        public static final TagKey<Item> ARTIFACT_GRADE_U = create("artifact_grade_u");
        // 用于合成
        public static final TagKey<Item> ENHANCEABLE_ARTIFACT = create("enhanceable_artifact");
        // 用于tooltip
        public static final TagKey<Item> ARTIFACT_ENHANCE_MATERIAL = create("artifact_enhance_material");

        private static TagKey<Item> create(String name) {
            return TagKey.create(Registries.ITEM, MiaUtil.miaId(name));
        }
    }

    private MiaTags() {
        // 私有构造函数防止实例化
    }
}
