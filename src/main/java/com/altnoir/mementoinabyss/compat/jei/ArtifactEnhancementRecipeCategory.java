package com.altnoir.mementoinabyss.compat.jei;

import com.altnoir.mementoinabyss.client.tooltip.ArtifactEnhancementMaterialTooltip;
import com.altnoir.mementoinabyss.content.artifact.ArtifactApi;
import com.altnoir.mementoinabyss.content.artifact.enhancement.ArtifactEnhancementRecipe;
import com.altnoir.mementoinabyss.content.artifact.enhancement.ArtifactEnhancementRecipeInput;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Comparator;
import java.util.List;

public final class ArtifactEnhancementRecipeCategory
        implements IRecipeCategory<RecipeHolder<ArtifactEnhancementRecipe>> {
    private static final int ARROW_X = 76;
    private static final int ARROW_Y = 6;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;

    public static final IRecipeHolderType<ArtifactEnhancementRecipe> TYPE =
            IRecipeHolderType.create(
                    com.altnoir.mementoinabyss.MementoInAbyss.asResource("artifact_enhancement")
            );

    private final IDrawable icon;

    public ArtifactEnhancementRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(MiaBlocks.ARTIFACT_SMITHING_TABLE.get());
    }

    @Override
    public IRecipeHolderType<ArtifactEnhancementRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("container.mementoinabyss.artifact_enhancement");
    }

    @Override
    public int getWidth() {
        return 125;
    }

    @Override
    public int getHeight() {
        return 28;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            RecipeHolder<ArtifactEnhancementRecipe> holder,
            IFocusGroup focuses
    ) {
        ArtifactEnhancementRecipe recipe = holder.value();
        ItemStack material = new ItemStack(recipe.material(), recipe.materialCount());
        List<ItemStack> artifacts = enhanceableArtifacts();
        List<ItemStack> results = artifacts.stream()
                .map(artifact -> recipe.assemble(new ArtifactEnhancementRecipeInput(
                        artifact,
                        material,
                        RandomSource.create(holder.id().identifier().hashCode())
                )))
                .filter(stack -> !stack.isEmpty())
                .toList();

        var artifactSlot = builder.addInputSlot(1, 6)
                .setStandardSlotBackground()
                .addItemStacks(artifacts);
        builder.addInputSlot(50, 6)
                .setStandardSlotBackground()
                .add(material);
        var resultSlot = builder.addOutputSlot(108, 6)
                .setStandardSlotBackground()
                .addItemStacks(results);
        if (artifacts.size() == results.size() && !artifacts.isEmpty()) {
            builder.createFocusLink(artifactSlot, resultSlot);
        }
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            RecipeHolder<ArtifactEnhancementRecipe> recipe,
            IFocusGroup focuses
    ) {
        builder.addRecipePlusSign().setPosition(27, 8);
        builder.addRecipeArrow().setPosition(ARROW_X, ARROW_Y);
    }

    @Override
    public void getTooltip(
            ITooltipBuilder tooltip,
            RecipeHolder<ArtifactEnhancementRecipe> recipe,
            mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY
    ) {
        if (mouseX >= ARROW_X && mouseX < ARROW_X + ARROW_WIDTH
                && mouseY >= ARROW_Y && mouseY < ARROW_Y + ARROW_HEIGHT) {
            tooltip.add(ArtifactEnhancementMaterialTooltip.modifierLine(recipe.value()));
        }
    }

    private static List<ItemStack> enhanceableArtifacts() {
        return BuiltInRegistries.ITEM.stream()
                .map(item -> item.getDefaultInstance())
                .filter(ArtifactApi::isEnhanceable)
                .sorted(Comparator.comparing(stack ->
                        BuiltInRegistries.ITEM.getKey(stack.getItem())))
                .toList();
    }
}
