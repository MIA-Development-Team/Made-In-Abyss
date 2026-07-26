package com.altnoir.mementoinabyss.impl.artifact.component;

import com.altnoir.mementoinabyss.impl.artifact.ArtifactProfile;
import com.altnoir.mementoinabyss.init.MiaDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public record ArtifactIdentityComponent(ArtifactProfile profile) implements ArtifactItemComponent {
    @Override
    public void applyDefaults(Item.Properties properties) {
        properties.component(MiaDataComponents.ARTIFACT_PROFILE.get(), profile);
    }

    @Override
    public void appendTooltip(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipFlag flag,
            List<Component> tooltip
    ) {
        ArtifactProfile current = stack.getOrDefault(MiaDataComponents.ARTIFACT_PROFILE.get(), profile);
        tooltip.add(Component.translatable(
                "tooltip.mementoinabyss.artifact.grade." + current.grade().getSerializedName()
        ).withStyle(current.grade().style()));
        tooltip.add(Component.translatable(
                "tooltip.mementoinabyss.artifact.weight",
                Component.literal(Integer.toString(current.weight())).withStyle(ChatFormatting.YELLOW)
        ).withStyle(ChatFormatting.GOLD));
    }
}
