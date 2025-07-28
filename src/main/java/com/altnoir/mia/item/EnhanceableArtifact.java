package com.altnoir.mia.item;

import java.util.List;
import java.util.Optional;

import com.altnoir.mia.component.ArtifactEnhancementComponent;
import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.item.abs.IArtifactItem;
import com.altnoir.mia.item.abs.IBundleable;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class EnhanceableArtifact extends Item implements IArtifactItem, IBundleable {
    private final int weight;
    private final Grade grade;

    public EnhanceableArtifact(Properties properties, Grade grade, int weight) {
        super(properties.component(MiaComponents.ARTIFACT_ENHANCEMENT, ArtifactEnhancementComponent.EMPTY));
        this.weight = weight;
        this.grade = grade;
    }

    public int getMaxLevel() {
        switch (this.getGrade()) {
            case Grade.FOURTH:
                return 0;
            case Grade.THIRD:
                return 1;
            case Grade.SECOND:
                return 2;
            case Grade.FIRST:
                return 4;
            case Grade.SPECIAL:
                return 8;
            default:
                return 0;
        }
    }

    public Grade getGrade() {
        return this.grade;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip) {
        int currentLevel = stack.get(MiaComponents.ARTIFACT_ENHANCEMENT).getLevel();
        int maxLevel = getMaxLevel();
        if (currentLevel < maxLevel) {
            tooltip.add(1, Component.translatable("tooltip.mia.artifact.enhancement.level",
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
            tooltip.add(1, Component.translatable("tooltip.mia.artifact.enhancement.no_more_enhancement")
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        }

        IArtifactItem.super.appendTooltip(stack, tooltip);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        return ((ArtifactEnhancementComponent) stack.getOrDefault(MiaComponents.ARTIFACT_ENHANCEMENT,
                ArtifactEnhancementComponent.EMPTY)).getAttributeModifiers();
    }

    // public static class Builder {
    // private final Properties props;
    // private int weight;
    // private int maxLevel;
    // private final List<SimpleArtifactStat> mods = new ArrayList<>();

    // public Builder(Properties props) {
    // this.props = props;
    // }

    // public Builder weight(int w) {
    // this.weight = w;
    // return this;
    // }

    // public Builder maxLevel(int l) {
    // this.maxLevel = l;
    // return this;
    // }

    // public Builder addModifier(Holder<Attribute> attribute, double amount,
    // AttributeModifier.Operation operation) {
    // mods.add(new SimpleArtifactStat(attribute, amount, operation));
    // return this;
    // }

    // public EnhanceableArtifact build() {
    // return new EnhanceableArtifact(this);
    // }

    // }

    // public record SimpleArtifactStat(
    // Holder<Attribute> attribute,
    // double amount,
    // AttributeModifier.Operation operation) {
    // }

}
