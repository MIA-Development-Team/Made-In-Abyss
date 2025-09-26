package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.recipe.LampTubeRecipe;
import com.altnoir.mia.recipe.ArtifactSmithingRecipe;
import com.altnoir.mia.recipe.ArtifactBundleUpgradeRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, MIA.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE,
            MIA.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<LampTubeRecipe>> LAMP_TUBE_SERIALIZER = SERIALIZERS
            .register("lamp_tube", LampTubeRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<LampTubeRecipe>> LAMP_TUBE_TYPE = TYPES
            .register("lamp_tube", () -> new RecipeType<LampTubeRecipe>() {
                @Override
                public String toString() {
                    return "lamp_tube";
                }
            });


    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ArtifactBundleUpgradeRecipe>> ARTIFACT_BUNDLE_UPGRADE_SERIALIZER = SERIALIZERS
            .register("artifact_bundle_upgrade", ArtifactBundleUpgradeRecipe.Serializer::new);


    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ArtifactSmithingRecipe>> ARTIFACT_SMITHING_SERIALIZER = SERIALIZERS
            .register("artifact_smithing", ArtifactSmithingRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ArtifactSmithingRecipe>> ARTIFACT_SMITHING_TYPE = TYPES
            .register("artifact_smithing", () -> new RecipeType<ArtifactSmithingRecipe>() {
                @Override
                public String toString() {
                    return "artifact_smithing";
                }
            });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
