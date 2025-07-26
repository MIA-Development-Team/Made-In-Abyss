package com.altnoir.mia.recipe;

import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.item.abs.AbstractWhistle;
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

public class WhistleUpgradeRecipe extends ShapedRecipe {

    public WhistleUpgradeRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
            ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        // try get old whistle
        ItemStack oldWhistle = ItemStack.EMPTY;
        for (ItemStack stack : inv.items()) {
            if (stack.getItem() instanceof AbstractWhistle) {
                oldWhistle = stack;
                break;
            }
        }
        // extend old whistle level
        ItemStack newWhistle = this.getResultItem(registries).copy();
        if (oldWhistle.has(MiaComponents.WHISTLE_LEVEL) && newWhistle.has(MiaComponents.WHISTLE_LEVEL)) {
            newWhistle.set(MiaComponents.WHISTLE_LEVEL, oldWhistle.get(MiaComponents.WHISTLE_LEVEL).intValue());
        }
        return newWhistle;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MiaRecipes.WHISTLE_UPGRADE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<WhistleUpgradeRecipe> {

        public static final MapCodec<WhistleUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec((codec) -> {
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
                    })).apply(codec, WhistleUpgradeRecipe::new);
        });

        public static final StreamCodec<RegistryFriendlyByteBuf, WhistleUpgradeRecipe> STREAM_CODEC = StreamCodec
                .of(WhistleUpgradeRecipe.Serializer::toNetwork, WhistleUpgradeRecipe.Serializer::fromNetwork);

        @Override
        public MapCodec<WhistleUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WhistleUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static WhistleUpgradeRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String s = buffer.readUtf();
            CraftingBookCategory craftingbookcategory = (CraftingBookCategory) buffer
                    .readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedrecipepattern = (ShapedRecipePattern) ShapedRecipePattern.STREAM_CODEC
                    .decode(buffer);
            ItemStack itemstack = (ItemStack) ItemStack.STREAM_CODEC.decode(buffer);
            boolean flag = buffer.readBoolean();
            return new WhistleUpgradeRecipe(s, craftingbookcategory, shapedrecipepattern, itemstack, flag);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, WhistleUpgradeRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(null));
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
