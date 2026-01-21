package com.altnoir.mia.event.server;

import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.init.MiaPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;

public class BrewingRecipesEvent {
    public static void onBrewingRecipe(PotionBrewing.Builder builder) {
        builder.addMix(Potions.AWKWARD, MiaItems.MISTFUZZ_PEACH.get(), MiaPotions.HASTE);
        builder.addMix(MiaPotions.STRONG_HASTE, Items.REDSTONE, MiaPotions.LONG_HASTE);
        builder.addMix(MiaPotions.HASTE, Items.GLOWSTONE_DUST, MiaPotions.STRONG_HASTE);
    }
}
