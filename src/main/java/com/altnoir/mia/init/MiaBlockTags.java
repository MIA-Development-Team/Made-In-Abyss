package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class MiaBlockTags {
    // 新增多个自定义方块标签定义
    public static final TagKey<Block> BASE_STONE_ABYSS = create("base_stone_abyss");
    public static final TagKey<Block> ANDESITE_ORE_REPLACEABLES = create("andesite_ore_replaceables");
    public static final TagKey<Block> COVERGRASS = create("covergrass");

    private MiaBlockTags() {
        // 私有构造函数防止实例化
    }

    // 新增标签创建方法
    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, MiaUtil.miaId(name));
    }

    public static TagKey<Block> create(ResourceLocation name) {
        return TagKey.create(Registries.BLOCK, name);
    }
}