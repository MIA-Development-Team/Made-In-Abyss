package com.altnoir.mia.common.item.abs;

import com.altnoir.mia.common.component.ArtifactEnhancementComponent;
import com.altnoir.mia.init.MiaComponents;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.Optional;

public interface IEArtifact extends IArtifactItem, IBundleable {
    public static final String TOOLTIP_ARTIFACT_LEVEL = "tooltip.mia.artifact.level";
    public static final String TOOLTIP_ARTIFACT_MAX = "tooltip.mia.artifact.max";

    default int getMaxLevel() {
        return switch (getGrade()) {
            case Grade.D -> 0;
            case Grade.C -> 1;
            case Grade.B -> 2;
            case Grade.A -> 4;
            case Grade.S -> 5;
            default -> 0;
        };
    }

    Grade getGrade();

    int getWeight();

    default void appendTooltip(ItemStack stack, List<Component> tooltip) {
        int currentLevel = stack.get(MiaComponents.ARTIFACT_ENHANCEMENT).getLevel();
        int maxLevel = getMaxLevel();
        if (currentLevel < maxLevel) {
            tooltip.add(1, Component.translatable(TOOLTIP_ARTIFACT_LEVEL,
                            Component.literal(Optional
                                            .ofNullable(currentLevel)
                                            .map(Object::toString)
                                            .orElse("0"))
                                    .withStyle(ChatFormatting.YELLOW),
                            Component.literal(Optional
                                            .ofNullable(maxLevel)
                                            .map(Object::toString)
                                            .orElse("0"))
                                    .withStyle(ChatFormatting.YELLOW))
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        } else {
            tooltip.add(1, Component.translatable(TOOLTIP_ARTIFACT_MAX)
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        }

        IArtifactItem.super.appendTooltip(stack, tooltip);
    }

    default Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        return stack.getOrDefault(MiaComponents.ARTIFACT_ENHANCEMENT, ArtifactEnhancementComponent.EMPTY).getAttributeModifiers();
    }
}
