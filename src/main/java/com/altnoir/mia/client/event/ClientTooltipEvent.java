package com.altnoir.mia.client.event;

import com.altnoir.mia.init.MiaColors;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.item.abs.IMiaTooltip;
import com.altnoir.mia.recipe.ArtifactSmithingRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientTooltipEvent {
    public static final String TOOLTIP_HOLD_SHIFT = "tooltip.mia.hold_shift";
    public static final String TOOLTIP_ATTRIBUTE_MODIFIER = "tooltip.mia.attribute_modifier";
    public static final String TOOLTIP_MODIFIERS_ARTIFACT_MATERIAL = "tooltip.mia.modifiers.artifact_material";

    public static void onTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        var item = stack.getItem();

        if (item instanceof IMiaTooltip tooltipProvider) {
            var tooltip = event.getToolTip();

            if (Screen.hasShiftDown()) {
                tooltipProvider.appendTooltip(stack, tooltip);
            } else {
                holdShiftTooltip(tooltip);
            }
        }
        if (stack.is(MiaTags.Items.ARTIFACT_MODIFIERS_MATERIAL)) {
            var tooltip = event.getToolTip();
            if (Screen.hasShiftDown()) {
                RecipeManager recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
                if (recipeManager != null) {
                    tooltip.add(1, Component.translatable(TOOLTIP_MODIFIERS_ARTIFACT_MATERIAL)
                            .withStyle(ChatFormatting.GOLD));
                    tooltip.addAll(2, materialModifiers(stack,
                            recipeManager.getAllRecipesFor(MiaRecipes.ARTIFACT_SMITHING_TYPE.get())));
                }
            } else {
                holdShiftTooltip(tooltip);
            }
        }
    }

    private static void holdShiftTooltip(List<Component> tooltip) {
        tooltip.add(1, Component.translatable(
                TOOLTIP_HOLD_SHIFT,
                Component.literal("Shift").withStyle(ChatFormatting.GRAY)
        ).withColor(MiaColors.ABYSS_GREEN));
    }

    private static List<Component> materialModifiers(ItemStack material, List<RecipeHolder<ArtifactSmithingRecipe>> recipes) {
        return recipes.stream().map(RecipeHolder::value)
                .filter(recipe -> recipe.getMaterial().getItem() == material.getItem())
                .map(recipe -> {
                    Attribute attribute = recipe.getAttribute().value();
                    double amount = recipe.getAttributeAmount();
                    AttributeModifier.Operation operation = recipe.getAttributeOperation();
                    return formatAttributeModifier(attribute, amount, operation);
                })
                .toList();
    }

    public static Component formatAttributeModifier(Attribute attribute, double amount,
                                                    AttributeModifier.Operation op) {
        boolean isPercent = op == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                || op == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;

        // 4) Format the numeric text
        String valueText;
        if (isPercent) {
            // convert to integer percent
            int pct = (int) Math.round(amount * 100);
            valueText = (amount > 0 ? "+" : "") + pct + "%";
        } else {
            // flat number with two decimals
            valueText = String.format("%s%s", amount > 0 ? "+" : "", BigDecimal.valueOf(amount)
                    .setScale(2, RoundingMode.HALF_UP)
                    .stripTrailingZeros().toPlainString());
        }

        Component attributeName = Component.translatable(attribute.getDescriptionId());
        return Component.translatable(TOOLTIP_ATTRIBUTE_MODIFIER, valueText, attributeName)
                .withStyle(ChatFormatting.BLUE);
    }
}
