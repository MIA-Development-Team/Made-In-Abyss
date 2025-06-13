package com.altnoir.mia.compat.curios;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CuriosItemTags {
    public static final TagKey<Item> WHISTLE = createItemTag("whistle");
    public static TagKey<Item> createItemTag(String id) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("curios", id));
    }
}
