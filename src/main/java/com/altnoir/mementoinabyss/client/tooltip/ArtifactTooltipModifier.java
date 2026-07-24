package com.altnoir.mementoinabyss.client.tooltip;

import com.altnoir.mementoinabyss.content.artifact.ArtifactItem;
import com.altnoir.mementoinabyss.content.artifact.component.ArtifactItemComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

public final class ArtifactTooltipModifier implements TooltipModifier {
    public static final ArtifactTooltipModifier INSTANCE = new ArtifactTooltipModifier();

    @Override
    public void modify(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof ArtifactItem artifact)
                || !Minecraft.getInstance().hasControlDown()) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        for (ArtifactItemComponent component : artifact.artifactComponents()) {
            component.appendTooltip(
                    event.getItemStack(),
                    event.getContext(),
                    event.getFlags(),
                    lines
            );
        }
        if (!lines.isEmpty()
                && !event.getToolTip().isEmpty()
                && !event.getToolTip().getLast().getString().isEmpty()) {
            lines.addFirst(CommonComponents.EMPTY);
        }
        event.getToolTip().addAll(lines);
    }

    private ArtifactTooltipModifier() {}
}
