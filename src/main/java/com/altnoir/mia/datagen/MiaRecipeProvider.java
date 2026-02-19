package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.common.recipe.ArtifactBundleUpgradeRecipeBuilder;
import com.altnoir.mia.common.recipe.ArtifactSmithingRecipeBuilder;
import com.altnoir.mia.common.recipe.DoubleRange;
import com.altnoir.mia.common.recipe.LampTubeRecipeBuilder;
import com.altnoir.mia.init.MiaAttributes;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.init.MiaTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MiaRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public MiaRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        //安山岩
        stoneBlocks(recipeOutput, MiaBlocks.ABYSS_COBBLED_ANDESITE.get(),
                MiaBlocks.ABYSS_COBBLED_ANDESITE_STAIRS.get(), MiaBlocks.ABYSS_COBBLED_ANDESITE_SLAB.get(), MiaBlocks.ABYSS_COBBLED_ANDESITE_WALL.get());

        shapeless2B1(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get(), MiaBlocks.ABYSS_COBBLED_ANDESITE.get(), Blocks.MOSS_BLOCK);
        shapeless2B1(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get(), MiaBlocks.ABYSS_COBBLED_ANDESITE.get(), Blocks.VINE);
        stoneBlocks(recipeOutput, MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get(),
                MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS.get(), MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_SLAB.get(), MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_WALL.get());

        smeltingResultFromBase(recipeOutput, MiaBlocks.ABYSS_ANDESITE.get(), MiaBlocks.ABYSS_COBBLED_ANDESITE.get());
        stoneBlocks(recipeOutput, MiaBlocks.ABYSS_ANDESITE.get(),
                MiaBlocks.ABYSS_ANDESITE_STAIRS.get(), MiaBlocks.ABYSS_ANDESITE_SLAB.get(), MiaBlocks.ABYSS_ANDESITE_WALL.get());

        // 磨制安山岩
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.POLISHED_ABYSS_ANDESITE.get(), MiaBlocks.ABYSS_ANDESITE.get());
        stoneCutter(recipeOutput, MiaBlocks.POLISHED_ABYSS_ANDESITE.get(), MiaBlocks.ABYSS_ANDESITE.get());
        stoneBlocks(recipeOutput, Arrays.asList(MiaBlocks.POLISHED_ABYSS_ANDESITE.get(), MiaBlocks.ABYSS_ANDESITE.get()),
                MiaBlocks.POLISHED_ABYSS_ANDESITE_STAIRS.get(), MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB.get(), MiaBlocks.POLISHED_ABYSS_ANDESITE_WALL.get());

        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.ABYSS_ANDESITE_PILLAR.get(), MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB.get());
        stoneCutter(recipeOutput, MiaBlocks.ABYSS_ANDESITE_PILLAR.get(), Arrays.asList(MiaBlocks.POLISHED_ABYSS_ANDESITE.get(), MiaBlocks.ABYSS_ANDESITE.get()));
        // 安山岩砖
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.ABYSS_ANDESITE_BRICKS.get(), MiaBlocks.POLISHED_ABYSS_ANDESITE.get());
        stoneCutter(recipeOutput, MiaBlocks.ABYSS_ANDESITE_BRICKS.get(), Arrays.asList(MiaBlocks.POLISHED_ABYSS_ANDESITE.get(), MiaBlocks.ABYSS_ANDESITE.get()));
        stoneBlocks(recipeOutput, Arrays.asList(MiaBlocks.ABYSS_ANDESITE_BRICKS.get(), MiaBlocks.POLISHED_ABYSS_ANDESITE.get(), MiaBlocks.ABYSS_ANDESITE.get()),
                MiaBlocks.ABYSS_ANDESITE_BRICKS_STAIRS.get(), MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB.get(), MiaBlocks.ABYSS_ANDESITE_BRICKS_WALL.get());

        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.CHISLED_ABYSS_ANDESITE.get(), MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB.get());
        stoneCutter(recipeOutput, MiaBlocks.CHISLED_ABYSS_ANDESITE.get(), Arrays.asList(MiaBlocks.ABYSS_ANDESITE_BRICKS.get(), MiaBlocks.POLISHED_ABYSS_ANDESITE.get(), MiaBlocks.ABYSS_ANDESITE.get()));
        // 安山岩苔石砖
        shapeless2B1(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get(), MiaBlocks.ABYSS_ANDESITE_BRICKS.get(), Blocks.MOSS_BLOCK);
        shapeless2B1(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get(), MiaBlocks.ABYSS_ANDESITE_BRICKS.get(), Blocks.VINE);
        stoneBlocks(recipeOutput, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get(),
                MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS.get(), MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB.get(), MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_WALL.get());

        // 化石树
        twoByTwoPacker(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.FOSSILIZED_WOOD.get(), MiaBlocks.FOSSILIZED_LOG.get(), 3);
        twoByTwoPacker(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get(), MiaBlocks.STRIPPED_FOSSILIZED_LOG.get(), 3);

        stoneFromLog(recipeOutput, MiaBlocks.POLISHED_FOSSILIZED_WOOD.get(), MiaTags.Items.UNSTRIPPED_FOSSILIZED_LOGS);
        stoneCutter(recipeOutput, MiaBlocks.POLISHED_FOSSILIZED_WOOD.get(), Arrays.asList(MiaBlocks.FOSSILIZED_WOOD.get(), MiaBlocks.FOSSILIZED_LOG.get()));
        stoneBlocks(recipeOutput, MiaBlocks.POLISHED_FOSSILIZED_WOOD.get(),
                MiaBlocks.POLISHED_FOSSILIZED_WOOD_STAIRS.get(), MiaBlocks.POLISHED_FOSSILIZED_WOOD_SLAB.get(), MiaBlocks.POLISHED_FOSSILIZED_WOOD_WALL.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.FOSSILIZED_WOOD_BRICKS.get(), MiaBlocks.POLISHED_FOSSILIZED_WOOD.get());
        stoneCutter(recipeOutput, MiaBlocks.FOSSILIZED_WOOD_BRICKS.get(), Arrays.asList(MiaBlocks.POLISHED_FOSSILIZED_WOOD.get(), MiaBlocks.FOSSILIZED_WOOD.get(), MiaBlocks.FOSSILIZED_LOG.get()));
        stoneBlocks(recipeOutput, MiaBlocks.FOSSILIZED_WOOD_BRICKS.get(),
                MiaBlocks.FOSSILIZED_WOOD_BRICKS_STAIRS.get(), MiaBlocks.FOSSILIZED_WOOD_BRICKS_SLAB.get(), MiaBlocks.FOSSILIZED_WOOD_BRICKS_WALL.get());

        stoneFromLog(recipeOutput, MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get(), MiaTags.Items.STRIPPED_FOSSILIZED_LOGS);
        stoneCutter(recipeOutput, MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get(), Arrays.asList(MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get(), MiaBlocks.STRIPPED_FOSSILIZED_LOG.get()));
        stoneBlocks(recipeOutput, MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get(),
                MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_STAIRS.get(), MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_SLAB.get(), MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_WALL.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.CHISLED_STRIPPED_FOSSILIZED_WOOD.get(), MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_SLAB.get());
        stoneCutter(recipeOutput, MiaBlocks.CHISLED_STRIPPED_FOSSILIZED_WOOD.get(), Arrays.asList(MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get(), MiaBlocks.STRIPPED_FOSSILIZED_LOG.get()));
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get(), MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get());
        stoneCutter(recipeOutput, MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get(), Arrays.asList(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get(), MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get(), MiaBlocks.STRIPPED_FOSSILIZED_LOG.get()));
        stoneBlocks(recipeOutput, MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get(),
                MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS.get(), MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB.get(), MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL.get());


        // 工具
        pickaxe(recipeOutput, MiaItems.PRASIOLITE_PICKAXE.get(), MiaItems.PRASIOLITE_SHARD.get());
        hoe(recipeOutput, MiaItems.PRASIOLITE_HOE.get(), MiaItems.PRASIOLITE_SHARD.get());


        // 木头
        planksFromLog(recipeOutput, MiaBlocks.SKYFOG_PLANKS.get(), MiaTags.Items.SKYFOG_LOGS, 4);
        twoByTwoPacker(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.SKYFOG_WOOD.get(), MiaBlocks.SKYFOG_LOG.get(), 3);
        twoByTwoPacker(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.STRIPPED_SKYFOG_WOOD.get(), MiaBlocks.STRIPPED_SKYFOG_LOG.get(), 3);
        woodBlocks(recipeOutput, MiaBlocks.SKYFOG_PLANKS.get(),
                MiaBlocks.SKYFOG_STAIRS.get(), MiaBlocks.SKYFOG_SLAB.get(), MiaBlocks.SKYFOG_FENCE.get(), MiaBlocks.SKYFOG_FENCE_GATE.get(),
                MiaBlocks.SKYFOG_DOOR.get(), MiaBlocks.SKYFOG_TRAPDOOR.get(), MiaBlocks.SKYFOG_PRESSURE_PLATE.get(), MiaBlocks.SKYFOG_BUTTON.get());

        planksFromLog(recipeOutput, MiaBlocks.INVERTED_PLANKS.get(), MiaTags.Items.INVERTED_LOGS, 4);
        twoByTwoPacker(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.INVERTED_WOOD.get(), MiaBlocks.INVERTED_LOG.get(), 3);
        twoByTwoPacker(recipeOutput, RecipeCategory.BUILDING_BLOCKS, MiaBlocks.STRIPPED_INVERTED_WOOD.get(), MiaBlocks.STRIPPED_INVERTED_LOG.get(), 3);
        woodBlocks(recipeOutput, MiaBlocks.INVERTED_PLANKS.get(),
                MiaBlocks.INVERTED_STAIRS.get(), MiaBlocks.INVERTED_SLAB.get(), MiaBlocks.INVERTED_FENCE.get(), MiaBlocks.INVERTED_FENCE_GATE.get(),
                MiaBlocks.INVERTED_DOOR.get(), MiaBlocks.INVERTED_TRAPDOOR.get(), MiaBlocks.INVERTED_PRESSURE_PLATE.get(), MiaBlocks.INVERTED_BUTTON.get());


        // 模组工作台
        lampTube(recipeOutput, Items.STONE, 2, Items.DEEPSLATE);
        lampTube(recipeOutput, Items.ANDESITE, 2, MiaBlocks.ABYSS_ANDESITE);
        lampTube(recipeOutput, MiaTags.Items.FOSSILIZED_LOGS, Items.COAL, "fossilized_logs");
        ArtifactBundleUpgrade(recipeOutput);
        ArtifactEnhancement(recipeOutput);
    }

    private static void ArtifactBundleUpgrade(RecipeOutput recipeOutput) {
        ArtifactBundleUpgradeRecipeBuilder.shaped(RecipeCategory.TOOLS, MiaItems.FANCY_ARTIFACT_BUNDLE, 1)
                .define('#', Items.IRON_INGOT)
                .define('$', MiaItems.GRAY_ARTIFACT_BUNDLE.get())
                .pattern("###")
                .pattern("#$#")
                .pattern("###")
                .unlockedBy(getHasName(MiaItems.GRAY_ARTIFACT_BUNDLE.get()), has(MiaItems.GRAY_ARTIFACT_BUNDLE.get()))
                .save(recipeOutput);
    }

    private static void ArtifactEnhancement(RecipeOutput recipeOutput) {
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.SMITHING_ARTIFACT),
                new ItemStack(MiaItems.PRASIOLITE_SHARD.get(), 8),
                Attributes.BLOCK_BREAK_SPEED, DoubleRange.between(0.1, 0.3),
                Operation.ADD_MULTIPLIED_TOTAL
        ).unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.SMITHING_ARTIFACT),
                new ItemStack(Items.IRON_INGOT, 2),
                Attributes.KNOCKBACK_RESISTANCE, DoubleRange.between(0.2, 0.4),
                Operation.ADD_MULTIPLIED_BASE
        ).unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.SMITHING_ARTIFACT),
                new ItemStack(Items.COPPER_INGOT, 3),
                Attributes.ARMOR, DoubleRange.between(0.5, 1.0),
                Operation.ADD_VALUE
        ).unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.SMITHING_ARTIFACT),
                new ItemStack(Items.GOLD_INGOT, 2),
                Attributes.ATTACK_DAMAGE, DoubleRange.between(0.1, 5.0),
                Operation.ADD_VALUE
        ).unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.SMITHING_ARTIFACT),
                new ItemStack(Items.LAPIS_LAZULI, 1),
                Attributes.ATTACK_SPEED, DoubleRange.between(0.5, 1.5),
                Operation.ADD_MULTIPLIED_BASE
        ).unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.SMITHING_ARTIFACT),
                new ItemStack(Items.NETHERITE_INGOT, 1),
                Attributes.GRAVITY, DoubleRange.between(-0.5, 0.5),
                Operation.ADD_VALUE
        ).unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.SMITHING_ARTIFACT),
                new ItemStack(Items.DIAMOND, 1),
                MiaAttributes.CRITICAL_HIT_DAMAGE, DoubleRange.between(0.2, 1.0),
                Operation.ADD_VALUE
        ).unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.SMITHING_ARTIFACT),
                new ItemStack(Items.EMERALD, 1),
                MiaAttributes.CRITICAL_HIT, DoubleRange.between(0.5, 0.9),
                Operation.ADD_VALUE
        ).unlockedByMaterial().save(recipeOutput);
    }

    private static void lampTube(RecipeOutput recipeOutput, ItemLike input, Integer count, ItemLike output) {
        lampTube(recipeOutput, input, count, output, 1);
    }

    private static void lampTube(RecipeOutput recipeOutput, ItemLike input, Integer count, ItemLike output, Integer resultCount) {
        LampTubeRecipeBuilder.lampTube(input, count, output, resultCount)
                .unlockedBy(getHasName(input), has(output))
                .save(recipeOutput);
    }

    private static void lampTube(RecipeOutput recipeOutput, ItemLike input, ItemLike output, Integer count, String id) {
        LampTubeRecipeBuilder.lampTube(input, output, count)
                .unlockedBy(getHasName(input), has(output))
                .save(recipeOutput, getItemName(output) + "_from_" + id);
    }

    private static void lampTube(RecipeOutput recipeOutput, TagKey<Item> tag, ItemLike output, String hasName) {
        lampTube(recipeOutput, tag, output, 1, hasName);
    }

    private static void lampTube(RecipeOutput recipeOutput, TagKey<Item> tag, ItemLike output, Integer resultCount, String hasName) {
        LampTubeRecipeBuilder.lampTube(tag, output, resultCount)
                .unlockedBy("has_" + hasName, has(output))
                .save(recipeOutput);
    }

    private static void woodBlocks(RecipeOutput recipeOutput, ItemLike baseBlock,
                                   ItemLike stairs, ItemLike slab, ItemLike fence, ItemLike fenceGate,
                                   ItemLike door, ItemLike trapdoor, ItemLike plate, ItemLike button) {
        stair(recipeOutput, stairs, baseBlock);
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, slab, baseBlock);
        fenceBuilder(fence, Ingredient.of(baseBlock)).group("wooden_fence").unlockedBy(getHasName(baseBlock), has(baseBlock)).save(recipeOutput);
        fenceGateBuilder(fenceGate, Ingredient.of(baseBlock)).group("wooden_fence_gate").unlockedBy(getHasName(baseBlock), has(baseBlock)).save(recipeOutput);
        doorBuilder(door, Ingredient.of(baseBlock)).group("wooden_door").unlockedBy(getHasName(baseBlock), has(baseBlock)).save(recipeOutput);
        trapdoorBuilder(trapdoor, Ingredient.of(baseBlock)).group("wooden_trapdoor").unlockedBy(getHasName(baseBlock), has(baseBlock)).save(recipeOutput);
        pressurePlate(recipeOutput, plate, baseBlock);
        buttonBuilder(button, Ingredient.of(baseBlock)).unlockedBy(getHasName(baseBlock), has(baseBlock)).save(recipeOutput);
    }

    private static void stoneBlocks(RecipeOutput recipeOutput, ItemLike baseBlock, ItemLike stairs, ItemLike slab, ItemLike wall) {
        stoneBlocks(recipeOutput, Collections.singletonList(baseBlock), stairs, slab, wall);
    }

    private static void stoneBlocks(RecipeOutput recipeOutput, List<ItemLike> baseBlocks, ItemLike stairs, ItemLike slab, ItemLike wall) {
        stair(recipeOutput, stairs, baseBlocks.getFirst());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, slab, baseBlocks.getFirst());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, wall, baseBlocks.getFirst());
        for (ItemLike baseBlock : baseBlocks) {
            stoneCutter(recipeOutput, stairs, baseBlock);
            stoneCutter(recipeOutput, slab, baseBlock, 2);
            stoneCutter(recipeOutput, wall, baseBlock);
        }
    }

    private static void stoneCutter(RecipeOutput recipeOutput, ItemLike result, List<ItemLike> materials) {
        for (ItemLike material : materials) {
            stoneCutter(recipeOutput, result, material, 1);
        }
    }

    private static void stoneCutter(RecipeOutput recipeOutput, ItemLike result, ItemLike material) {
        stoneCutter(recipeOutput, result, material, 1);
    }

    private static void stoneCutter(RecipeOutput recipeOutput, ItemLike result, ItemLike material, int resultCount) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(material), RecipeCategory.BUILDING_BLOCKS, result, resultCount)
                .unlockedBy(getHasName(material), has(material))
                .save(recipeOutput, MIA.MOD_ID + ":stonecutting/" + getConversionRecipeName(result, material));
    }

    private static void stoneFromLog(RecipeOutput recipeOutput, ItemLike stones, TagKey<Item> logs) {
        stoneFromLog(recipeOutput, stones, logs, 1);
    }

    private static void stoneFromLog(RecipeOutput recipeOutput, ItemLike stones, TagKey<Item> logs, int resultCount) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, stones, resultCount)
                .requires(logs)
                .unlockedBy("has_log", has(logs))
                .save(recipeOutput);
    }

    private static void pickaxe(RecipeOutput recipeOutput, ItemLike output, ItemLike input) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, output)
                .define('#', input)
                .define('S', Items.STICK)
                .pattern("###")
                .pattern(" S ")
                .pattern(" S ")
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput);
    }

    private static void hoe(RecipeOutput recipeOutput, ItemLike output, ItemLike input) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, output)
                .define('#', input)
                .define('S', Items.STICK)
                .pattern("##")
                .pattern(" S")
                .pattern(" S")
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput);
    }

    private static void twoByTwoPacker(RecipeOutput recipeOutput, RecipeCategory category, ItemLike packed, ItemLike unpacked, int count) {
        ShapedRecipeBuilder.shaped(category, packed, count)
                .define('#', unpacked)
                .pattern("##").pattern("##")
                .unlockedBy(getHasName(unpacked), has(unpacked))
                .save(recipeOutput);
    }

    private static void stair(RecipeOutput recipeOutput, ItemLike output, ItemLike input) {
        stairBuilder(output, Ingredient.of(input))
                .group("stairs")
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput);
    }

    private static void shapeless2B1(RecipeOutput recipeOutput, RecipeCategory category, ItemLike output, ItemLike input, ItemLike input2) {
        shapeless2B1Builder(category, output, Ingredient.of(input), Ingredient.of(input2))
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput, MIA.MOD_ID + ":" + getConversionRecipeName(output, input2));
    }

    private static RecipeBuilder shapeless2B1Builder(RecipeCategory category, ItemLike output, Ingredient input, Ingredient input2) {
        return ShapelessRecipeBuilder.shapeless(category, output)
                .requires(input).requires(input2);
    }
}
