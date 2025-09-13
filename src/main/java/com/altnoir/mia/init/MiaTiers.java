package com.altnoir.mia.init;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class MiaTiers {
    public static final Tier PRASIOLITE = new SimpleTier(MiaTags.Blocks.INCORRECT_FOR_PRASIOLITE_TOOL,
            768, 8.0f, 0.0f, 22, () -> Ingredient.of(MiaItems.PRASIOLITE_SHARD));
}
