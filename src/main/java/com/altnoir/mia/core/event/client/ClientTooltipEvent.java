package com.altnoir.mia.core.event.client;

import com.altnoir.mia.common.item.abs.IMiaTooltip;
import com.altnoir.mia.common.recipe.ArtifactSmithingRecipe;
import com.altnoir.mia.init.MiaAttributes;
import com.altnoir.mia.core.MiaColors;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.init.MiaTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientTooltipEvent {
    public static final String TOOLTIP_HOLD_SHIFT = "tooltip.mia.hold_shift";
    public static final String TOOLTIP_ATTRIBUTE_MODIFIER = "tooltip.mia.attribute_modifier";
    public static final String TOOLTIP_MODIFIERS_ARTIFACT_MATERIAL = "tooltip.mia.modifiers.artifact_material";

    public static void onTooltip(ItemStack stack, List<Component> tooltip) {
        var item = stack.getItem();

        if (item instanceof IMiaTooltip tooltipProvider) {
            if (Screen.hasShiftDown()) {
                tooltipProvider.appendTooltip(stack, tooltip);
            } else {
                holdShiftTooltip(tooltip);
            }
        }
        if (stack.is(MiaTags.Items.ARTIFACT_MODIFIERS_MATERIAL)) {
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
        ).withColor(MiaColors.GREEN.getColor()));
    }

    private static List<Component> materialModifiers(ItemStack material, List<RecipeHolder<ArtifactSmithingRecipe>> recipes) {
        return recipes.stream().map(RecipeHolder::value)
                .filter(recipe -> recipe.getMaterial().getItem() == material.getItem())
                .map(recipe -> {
                    var attribute = recipe.getAttribute();

                    double minValue = recipe.getMinAttributeValue();
                    double maxValue = recipe.getMaxAttributeValue();
                    AttributeModifier.Operation operation = recipe.getAttributeOperation();
                    return formatAttributeModifier(attribute, minValue, maxValue, operation);
                })
                .toList();
    }

    public static Component formatAttributeModifier(Holder<Attribute> attribute, double minValue, double maxValue,
                                                    AttributeModifier.Operation op) {
        boolean isPercent = op == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                || op == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;

        // 4) Format the numeric text
        String valueText;
        if (isPercent || attribute.is(MiaAttributes.CRITICAL_HIT) || attribute.is(MiaAttributes.CRITICAL_HIT_DAMAGE)) {
            // convert to integer percent
            int min = (int) Math.round(minValue * 100);
            int max = (int) Math.round(maxValue * 100);
            valueText = (minValue > 0 ? "+" : "") + min + "%-" + max + "%";
        } else {
            // flat number with two decimals
            String min = BigDecimal.valueOf(minValue).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            String max = BigDecimal.valueOf(maxValue).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            valueText = String.format("%s%s", minValue > 0 ? "+" : "", min + "-" + max);
        }

        Component attributeName = Component.translatable(attribute.value().getDescriptionId());
        return Component.translatable(TOOLTIP_ATTRIBUTE_MODIFIER, valueText, attributeName)
                .withStyle(ChatFormatting.BLUE);
    }
}
