package com.altnoir.mementoinabyss.content.artifact.component;

import com.altnoir.mementoinabyss.content.artifact.ArtifactApi;
import com.altnoir.mementoinabyss.content.artifact.ArtifactAttribute;
import com.altnoir.mementoinabyss.content.artifact.ArtifactEnhancement;
import com.altnoir.mementoinabyss.content.artifact.ArtifactProfile;
import com.altnoir.mementoinabyss.init.MiaDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.CurioAttributeModifiers;

import java.util.List;

public final class ArtifactEnhancementComponent implements ArtifactItemComponent {
    public static final ArtifactEnhancementComponent INSTANCE = new ArtifactEnhancementComponent();

    private ArtifactEnhancementComponent() {}

    @Override
    public void applyDefaults(Item.Properties properties) {
        properties.component(MiaDataComponents.ARTIFACT_ENHANCEMENT.get(), ArtifactEnhancement.EMPTY);
    }

    @Override
    public void appendTooltip(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipFlag flag,
            List<Component> tooltip
    ) {
        ArtifactProfile profile = stack.get(MiaDataComponents.ARTIFACT_PROFILE.get());
        if (profile == null) {
            return;
        }
        int level = stack.getOrDefault(MiaDataComponents.ARTIFACT_ENHANCEMENT.get(), ArtifactEnhancement.EMPTY).level();
        if (level >= profile.maxEnhancementLevel()) {
            tooltip.add(Component.translatable("tooltip.mementoinabyss.artifact.enhancement.max")
                    .withStyle(ChatFormatting.GOLD));
            return;
        }
        tooltip.add(Component.translatable(
                "tooltip.mementoinabyss.artifact.enhancement.level",
                Component.literal(Integer.toString(level)).withStyle(ChatFormatting.YELLOW),
                Component.literal(Integer.toString(profile.maxEnhancementLevel())).withStyle(ChatFormatting.YELLOW)
        ).withStyle(ChatFormatting.GOLD));
    }

    @Override
    public void addAttributeModifiers(
            ItemStack stack,
            CurioAttributeModifiers.Builder modifiers
    ) {
        ArtifactEnhancement enhancement =
                stack.getOrDefault(MiaDataComponents.ARTIFACT_ENHANCEMENT.get(), ArtifactEnhancement.EMPTY);
        for (ArtifactAttribute attribute : enhancement.attributes()) {
            modifiers.addModifier(
                    attribute.attribute(),
                    attribute.modifier(),
                    ArtifactApi.SLOT_ID
            );
        }
    }
}
