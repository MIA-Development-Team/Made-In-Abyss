package com.altnoir.mia.common.item.abs;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * 继承关系
 * ICurioItem,IMiaTooltip
 * - IArtifactItem (可以加IBundleable来支持放入ArtifactBundle)
 * - - ArtifactBundle
 * - - EnhanceableArtifact
 * - - - 其他特殊遗物
 */
public interface IArtifactItem extends ICurioItem, IMiaTooltip {
    String TOOLTIP_ARTIFACT_WEIGHT = "tooltip.mia.artifact.weight";
    String TOOLTIP_ARTIFACT_GRADE_D = "tooltip.mia.artifact.grade.d";
    String TOOLTIP_ARTIFACT_GRADE_C = "tooltip.mia.artifact.grade.c";
    String TOOLTIP_ARTIFACT_GRADE_B = "tooltip.mia.artifact.grade.b";
    String TOOLTIP_ARTIFACT_GRADE_A = "tooltip.mia.artifact.grade.a";
    String TOOLTIP_ARTIFACT_GRADE_S = "tooltip.mia.artifact.grade.s";
    String TOOLTIP_ARTIFACT_GRADE_UNKNOWN = "tooltip.mia.artifact.grade.unknown";

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
                    Component.translatable(TOOLTIP_ARTIFACT_WEIGHT,
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
            case Grade.B -> style -> style.withColor(ChatFormatting.DARK_PURPLE);
            case Grade.A -> style -> style.withColor(ChatFormatting.YELLOW);
            case Grade.S -> style -> style.withColor(ChatFormatting.DARK_RED);
            default -> style -> style.withColor(ChatFormatting.BLACK);
        };
    }

    public default String getGradeTranslatable() {
        return switch (getGrade()) {
            case Grade.D -> TOOLTIP_ARTIFACT_GRADE_D;
            case Grade.C -> TOOLTIP_ARTIFACT_GRADE_C;
            case Grade.B -> TOOLTIP_ARTIFACT_GRADE_B;
            case Grade.A -> TOOLTIP_ARTIFACT_GRADE_A;
            case Grade.S -> TOOLTIP_ARTIFACT_GRADE_S;
            default -> TOOLTIP_ARTIFACT_GRADE_UNKNOWN;
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
