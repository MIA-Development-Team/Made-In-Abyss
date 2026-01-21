package com.altnoir.mia.common.recipe;

import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.common.item.abs.AbsArtifactBundle;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class ArtifactBundleUpgradeRecipe extends ShapedRecipe {

    public ArtifactBundleUpgradeRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
            ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        // try get old bundle
        ItemStack oldBundle = ItemStack.EMPTY;
        for (ItemStack stack : inv.items()) {
            if (stack.getItem() instanceof AbsArtifactBundle) {
                oldBundle = stack;
                break;
            }
        }
        // extend old whistle level
        ItemStack newBundle = this.getResultItem(registries).copy();
        if (oldBundle.has(MiaComponents.ARTIFACT_BUNDLE_INVENTORY)
                && newBundle.has(MiaComponents.ARTIFACT_BUNDLE_INVENTORY)) {
            newBundle.set(MiaComponents.ARTIFACT_BUNDLE_INVENTORY,
                    oldBundle.get(MiaComponents.ARTIFACT_BUNDLE_INVENTORY));
        }
        return newBundle;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MiaRecipes.ARTIFACT_BUNDLE_UPGRADE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<ArtifactBundleUpgradeRecipe> {

        public static final MapCodec<ArtifactBundleUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec((codec) -> {
            return codec.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter((recipe) -> {
                        return recipe.getGroup();
                    }),
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                            .forGetter((recipe) -> {
                                return recipe.category();
                            }),
                    ShapedRecipePattern.MAP_CODEC.forGetter((recipe) -> {
                        return recipe.pattern;
                    }),
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter((recipe) -> {
                        return recipe.getResultItem(null);
                    }),
                    Codec.BOOL.optionalFieldOf("show_notification", true).forGetter((recipe) -> {
                        return recipe.showNotification();
                    })).apply(codec, ArtifactBundleUpgradeRecipe::new);
        });

        public static final StreamCodec<RegistryFriendlyByteBuf, ArtifactBundleUpgradeRecipe> STREAM_CODEC = StreamCodec
                .of(ArtifactBundleUpgradeRecipe.Serializer::toNetwork,
                        ArtifactBundleUpgradeRecipe.Serializer::fromNetwork);

        @Override
        public MapCodec<ArtifactBundleUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ArtifactBundleUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ArtifactBundleUpgradeRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String s = buffer.readUtf();
            CraftingBookCategory craftingbookcategory = (CraftingBookCategory) buffer
                    .readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedrecipepattern = (ShapedRecipePattern) ShapedRecipePattern.STREAM_CODEC
                    .decode(buffer);
            ItemStack itemstack = (ItemStack) ItemStack.STREAM_CODEC.decode(buffer);
            boolean flag = buffer.readBoolean();
            return new ArtifactBundleUpgradeRecipe(s, craftingbookcategory, shapedrecipepattern, itemstack, flag);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ArtifactBundleUpgradeRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(null));
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
