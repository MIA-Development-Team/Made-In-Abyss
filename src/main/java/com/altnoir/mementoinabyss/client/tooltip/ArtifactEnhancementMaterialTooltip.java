package com.altnoir.mementoinabyss.client.tooltip;

import com.altnoir.mementoinabyss.client.ArtifactEnhancementClientRecipes;
import com.altnoir.mementoinabyss.impl.artifact.enhancement.ArtifactEnhancementRecipe;
import com.altnoir.mementoinabyss.init.MiaAttributes;
import com.altnoir.mementoinabyss.init.MiaRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ArtifactEnhancementMaterialTooltip {
    private static Map<Item, List<ArtifactEnhancementRecipe>> recipesByMaterial = Map.of();

    public static void update(RecipeMap recipes) {
        ArtifactEnhancementClientRecipes.update(recipes);
        Map<Item, List<ArtifactEnhancementRecipe>> updated = new IdentityHashMap<>();
        ArtifactEnhancementClientRecipes.all().forEach(holder -> {
            ArtifactEnhancementRecipe recipe = holder.value();
            updated.computeIfAbsent(recipe.material().value(), ignored -> new ArrayList<>()).add(recipe);
        });
        updated.replaceAll((item, itemRecipes) -> List.copyOf(itemRecipes));
        recipesByMaterial = Map.copyOf(updated);
    }

    public static void clear() {
        ArtifactEnhancementClientRecipes.clear();
        recipesByMaterial = Map.of();
    }

    public static void append(ItemTooltipEvent event) {
        List<ArtifactEnhancementRecipe> recipes = recipesByMaterial.get(event.getItemStack().getItem());
        if (recipes == null || recipes.isEmpty()) {
            return;
        }

        int index = Math.min(1, event.getToolTip().size());
        boolean expanded = Minecraft.getInstance().hasControlDown();
        Component key = Component.translatable("tooltip.mementoinabyss.key_control")
                .withStyle(expanded ? TooltipPalette.MIA.highlight() : TooltipPalette.MIA.muted());
        event.getToolTip().add(index++, Component.translatable(
                "tooltip.mementoinabyss.hold_for_enhancements",
                key
        ).withStyle(TooltipPalette.MIA.primary()));
        if (!expanded) {
            return;
        }

        event.getToolTip().add(index++, CommonComponents.EMPTY);
        event.getToolTip().add(index++, Component.translatable(
                "tooltip.mementoinabyss.artifact.enhancement_material"
        ).withStyle(TooltipPalette.MIA.highlight()));
        for (ArtifactEnhancementRecipe recipe : recipes) {
            event.getToolTip().add(index++, modifierLine(recipe));
        }
    }

    public static Component modifierLine(ArtifactEnhancementRecipe recipe) {
        boolean percentage = recipe.operation() != AttributeModifier.Operation.ADD_VALUE
                || recipe.attribute().is(MiaAttributes.CRITICAL_HIT)
                || recipe.attribute().is(MiaAttributes.CRITICAL_HIT_DAMAGE);
        String range = formatValue(recipe.value().min(), percentage)
                + " \u2013 "
                + formatValue(recipe.value().max(), percentage);
        return Component.translatable(
                "tooltip.mementoinabyss.artifact.enhancement_material.modifier",
                Component.literal(range).withStyle(TooltipPalette.MIA.highlight()),
                Component.translatable(recipe.attribute().value().getDescriptionId())
                        .withStyle(TooltipPalette.MIA.primary())
        ).withStyle(TooltipPalette.MIA.muted());
    }

    private static String formatValue(double value, boolean percentage) {
        double displayed = percentage ? value * 100.0 : value;
        String number = BigDecimal.valueOf(displayed)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
        return (displayed > 0.0 ? "+" : "") + number + (percentage ? "%" : "");
    }

    private ArtifactEnhancementMaterialTooltip() {}
}
