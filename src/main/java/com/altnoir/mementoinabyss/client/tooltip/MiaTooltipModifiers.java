package com.altnoir.mementoinabyss.client.tooltip;

import com.altnoir.mementoinabyss.content.returnchest.BalloonItem;
import com.altnoir.mementoinabyss.init.MiaArtifactItems;
import com.altnoir.mementoinabyss.init.MiaItems;
import com.altnoir.mementoinabyss.init.MiaWhistleItems;
import java.util.function.Supplier;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public final class MiaTooltipModifiers {
    public static void register() {
        registerArtifact(MiaArtifactItems.TEST_ARTIFACT_1);
        registerArtifact(MiaArtifactItems.TEST_ARTIFACT_2);
        registerArtifact(MiaArtifactItems.TEST_ARTIFACT_3);
        registerArtifact(MiaArtifactItems.HEALTH_JUNKIE);
        registerWhistle(MiaWhistleItems.RED_WHISTLE);
        registerWhistleFragment(MiaWhistleItems.ECHO_REED);
        registerWhistleFragment(MiaWhistleItems.RESONANCE_PRESSURE_FRAGMENT);
        for (DyeColor color : DyeColor.VALUES) {
            TooltipModifierRegistry.register(
                    MiaItems.balloon(color),
                    event -> {
                        var resonance =
                                BalloonItem.colorName(BalloonItem.color(event.getItemStack()));
                        var description =
                                new ItemDescription.Builder(TooltipPalette.MIA)
                                        .summary(I18n.get("tooltip.mementoinabyss.balloon.summary"))
                                        .behaviour(
                                                I18n.get(
                                                        "tooltip.mementoinabyss.balloon.resonance",
                                                        resonance.getString()),
                                                I18n.get("tooltip.mementoinabyss.balloon.delivery"))
                                        .build();
                        var tooltip = event.getToolTip();
                        tooltip.addAll(Math.min(1, tooltip.size()), description.currentLines());
                    });
        }
    }

    private static void registerArtifact(Supplier<? extends Item> item) {
        TooltipModifier modifier =
                new ItemDescription.Modifier(TooltipPalette.MIA, true, true)
                        .andThen(ArtifactTooltipModifier.INSTANCE);
        TooltipModifierRegistry.register(item, modifier);
    }

    private static void registerWhistle(Supplier<? extends Item> item) {
        TooltipModifier modifier =
                new ItemDescription.Modifier(TooltipPalette.MIA, true, true)
                        .andThen(WhistleTooltipModifier.INSTANCE);
        TooltipModifierRegistry.register(item, modifier);
    }

    private static void registerWhistleFragment(Supplier<? extends Item> item) {
        TooltipModifier modifier =
                new ItemDescription.Modifier(TooltipPalette.MIA, true, true)
                        .andThen(WhistleFragmentTooltipModifier.INSTANCE);
        TooltipModifierRegistry.register(item, modifier);
    }

    private MiaTooltipModifiers() {}
}
