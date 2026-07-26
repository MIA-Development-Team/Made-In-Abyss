package com.altnoir.mementoinabyss.client.tooltip;

import com.altnoir.mementoinabyss.impl.whistle.grid.GridCell;
import com.altnoir.mementoinabyss.impl.whistle.grid.SkillShape;
import com.altnoir.mementoinabyss.content.item.whistle.fragment.WhistleFragmentItem;
import com.altnoir.mementoinabyss.impl.whistle.fragment.amplifier.WhistleAmplifierDefinition;
import com.altnoir.mementoinabyss.impl.whistle.skill.WhistleSkillDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Locale;

public final class WhistleFragmentTooltipModifier implements TooltipModifier {
    public static final WhistleFragmentTooltipModifier INSTANCE = new WhistleFragmentTooltipModifier();

    @Override
    public void modify(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof WhistleFragmentItem<?> fragment)
                || !Minecraft.getInstance().hasControlDown()) {
            return;
        }

        var definition = fragment.getDefinition();
        SkillShape shape = definition.shape();
        event.getToolTip().add(CommonComponents.EMPTY);
        event.getToolTip().add(Component.translatable(
                "tooltip.mementoinabyss.whistle.fragment.type",
                Component.translatable(definition.typeTranslationKey())
                        .withStyle(TooltipPalette.MIA.highlight())
        ).withStyle(TooltipPalette.MIA.primary()));
        event.getToolTip().add(Component.translatable(
                "tooltip.mementoinabyss.whistle.fragment.size",
                Component.literal(Integer.toString(shape.cells().size()))
                        .withStyle(TooltipPalette.MIA.highlight())
        ).withStyle(TooltipPalette.MIA.primary()));
        if (definition.unique()) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.mementoinabyss.whistle.fragment.unique"
            ).withStyle(TooltipPalette.MIA.highlight()));
        }

        if (definition instanceof WhistleSkillDefinition skill) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.mementoinabyss.whistle.skill.category",
                    Component.translatable(skill.category().translationKey())
                            .withStyle(TooltipPalette.MIA.highlight())
            ).withStyle(TooltipPalette.MIA.primary()));
            event.getToolTip().add(Component.translatable(
                    "tooltip.mementoinabyss.whistle.skill.sequence",
                    Component.literal(skill.sequence().stream()
                                    .map(note -> note.symbol())
                                    .reduce("", String::concat))
                            .withStyle(TooltipPalette.MIA.highlight())
            ).withStyle(TooltipPalette.MIA.primary()));
            event.getToolTip().add(Component.translatable(
                    "tooltip.mementoinabyss.whistle.skill.cooldown",
                    Component.literal(formatNumber(skill.cooldownTicks() / 20.0))
                            .withStyle(TooltipPalette.MIA.highlight())
            ).withStyle(TooltipPalette.MIA.primary()));
        } else if (definition instanceof WhistleAmplifierDefinition amplifier) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.mementoinabyss.whistle.amplifier.adjacent"
            ).withStyle(TooltipPalette.MIA.muted()));
            event.getToolTip().add(Component.translatable(
                    "tooltip.mementoinabyss.whistle.amplifier.power",
                    Component.literal(formatPercent(amplifier.powerMultiplier()))
                            .withStyle(TooltipPalette.MIA.highlight())
            ).withStyle(TooltipPalette.MIA.primary()));
            event.getToolTip().add(Component.translatable(
                    "tooltip.mementoinabyss.whistle.amplifier.cooldown",
                    Component.literal(formatPercent(amplifier.cooldownMultiplier()))
                            .withStyle(TooltipPalette.MIA.highlight())
            ).withStyle(TooltipPalette.MIA.primary()));
        }

        for (int y = 0; y < shape.height(); y++) {
            MutableComponent line = Component.empty();
            for (int x = 0; x < shape.width(); x++) {
                boolean filled = shape.cells().contains(new GridCell(x, y));
                line.append(Component.literal(filled ? "\u25a0 " : "\u00b7 ")
                        .withStyle(filled
                                ? TooltipPalette.MIA.highlight()
                                : TooltipPalette.MIA.muted()));
            }
            event.getToolTip().add(line);
        }
    }

    private static String formatPercent(double multiplier) {
        return String.format(Locale.ROOT, "%+.0f%%", (multiplier - 1.0) * 100.0);
    }

    private static String formatNumber(double value) {
        return value == Math.rint(value)
                ? String.format(Locale.ROOT, "%.0f", value)
                : String.format(Locale.ROOT, "%.1f", value);
    }

    private WhistleFragmentTooltipModifier() {}
}
