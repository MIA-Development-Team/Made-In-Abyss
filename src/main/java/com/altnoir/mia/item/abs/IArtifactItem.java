package com.altnoir.mia.item.abs;

import java.util.List;
import java.util.function.UnaryOperator;

import com.altnoir.mia.item.IMiaTooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * 继承关系
 * ICurioItem,IMiaTooltip
 * - IArtifactItem (可以加IBundleable来支持放入ArtifactBundle)
 * - - ArtifactBundle
 * - - EnhanceableArtifact
 * - - - 其他特殊遗物
 */
public interface IArtifactItem extends ICurioItem, IMiaTooltip {

    @Override
    public default boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    /**
     * 1 遗物等级
     * 2 重量（如果有）
     * 3 备注
     */
    @Override
    public default void appendTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(1, Component.translatable(getGradeTranslatable()).withStyle(getGradeStyle()));
        if (stack.getItem() instanceof IBundleable) {
            tooltip.add(2,
                    Component.translatable("tooltip.mia.artifact.weight",
                            Component.literal(Integer.toString(((IBundleable) stack.getItem()).getWeight()))
                                    .withStyle(ChatFormatting.YELLOW))
                            .withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        }
        IMiaTooltip.super.appendTooltip(stack, tooltip);
    }

    public Grade getGrade();

    public default UnaryOperator<Style> getGradeStyle() {
        return switch (getGrade()) {
            case Grade.D -> style -> style.withColor(ChatFormatting.DARK_GRAY);
            case Grade.C -> style -> style.withColor(ChatFormatting.DARK_AQUA);
            case Grade.B -> style -> style.withColor(ChatFormatting.DARK_GREEN);
            case Grade.A -> style -> style.withColor(ChatFormatting.GOLD);
            case Grade.S -> style -> style.withColor(ChatFormatting.DARK_RED);
            default -> style -> style.withColor(ChatFormatting.BLACK);
        };
    }

    public default String getGradeTranslatable() {
        return switch (getGrade()) {
            case Grade.D -> "tooltip.mia.artifact.grade.fourth";
            case Grade.C -> "tooltip.mia.artifact.grade.third";
            case Grade.B -> "tooltip.mia.artifact.grade.second";
            case Grade.A -> "tooltip.mia.artifact.grade.first";
            case Grade.S -> "tooltip.mia.artifact.grade.special";
            default -> "tooltip.mia.artifact.grade.unknown";
        };
    }

    public enum Grade {
        D,
        C,
        B,
        A,
        S,
        UNKNOWN
    }
}
