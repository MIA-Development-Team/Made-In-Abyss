package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.mementoinabyss.content.lamptube.LampTubeRecipeBuilder;
import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatType;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class MiaLampTubeRecipeProvider extends RecipeProvider {
    private MiaLampTubeRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        lampTube(Items.STONE, 2, Items.DEEPSLATE, "deepslate");
        lampTube(Items.ANDESITE, 2, MiaBlocks.ABYSS_ANDESITE, "abyss_andesite");
        LampTubeRecipeBuilder.lampTube(
                        Ingredient.of(
                                MiaBlocks.FOSSILIZED_LOG.get(),
                                MiaBlocks.MOSSY_FOSSILIZED_LOG.get()),
                        1,
                        Items.COAL)
                .heat(HeatType.UNIT)
                .save(output, "coal");
    }

    private void lampTube(ItemLike input, int count, ItemLike result, String path) {
        LampTubeRecipeBuilder.lampTube(input, count, result).save(output, path);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(
                HolderLookup.Provider registries, RecipeOutput output) {
            return new MiaLampTubeRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Memento in Abyss Lamp Tube Recipes";
        }
    }
}
