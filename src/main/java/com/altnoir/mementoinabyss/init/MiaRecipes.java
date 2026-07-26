package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.artifact.enhancement.ArtifactEnhancementRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaRecipes {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MementoInAbyss.ID);
    private static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MementoInAbyss.ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ArtifactEnhancementRecipe>>
            ARTIFACT_ENHANCEMENT_SERIALIZER = SERIALIZERS.register(
                    "artifact_enhancement",
                    () -> new RecipeSerializer<>(
                            ArtifactEnhancementRecipe.CODEC,
                            ArtifactEnhancementRecipe.STREAM_CODEC
                    )
            );

    public static final DeferredHolder<RecipeType<?>, RecipeType<ArtifactEnhancementRecipe>>
            ARTIFACT_ENHANCEMENT_TYPE = TYPES.register(
                    "artifact_enhancement",
                    () -> new RecipeType<>() {
                        @Override
                        public String toString() {
                            return MementoInAbyss.ID + ":artifact_enhancement";
                        }
                    }
            );

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
        TYPES.register(bus);
    }

    private MiaRecipes() {}
}
