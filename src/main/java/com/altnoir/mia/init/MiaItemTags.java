package com.altnoir.mia.init;

import com.altnoir.mia.MIA;

import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MiaItemTags {
    public static final TagKey<Item> ARTIFACT_GRADE_4 = create("artifact_grade_4");
    public static final TagKey<Item> ARTIFACT_GRADE_3 = create("artifact_grade_3");
    public static final TagKey<Item> ARTIFACT_GRADE_2 = create("artifact_grade_2");
    public static final TagKey<Item> ARTIFACT_GRADE_1 = create("artifact_grade_2");
    public static final TagKey<Item> ARTIFACT_GRADE_S = create("artifact_grade_s");
    public static final TagKey<Item> ARTIFACT_GRADE_U = create("artifact_grade_u");
    // 用于合成
    public static final TagKey<Item> ENHANCEABLE_ARTIFACT = create("enhanceable_artifact");
    // 用于tooltip
    public static final TagKey<Item> ARTIFACT_ENHANCE_MATERIAL = create("artifact_enhance_material");

    private MiaItemTags() {
        // 私有构造函数防止实例化
    }

    // 新增标签创建方法
    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, MiaUtil.miaId(name));
    }
}
