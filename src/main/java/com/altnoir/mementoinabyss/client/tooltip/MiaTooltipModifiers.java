package com.altnoir.mementoinabyss.client.tooltip;

import com.altnoir.mementoinabyss.init.MiaArtifactItems;
import com.altnoir.mementoinabyss.init.MiaWhistleItems;

public final class MiaTooltipModifiers {
    public static void register() {
        registerArtifact(MiaArtifactItems.TEST_ARTIFACT_1);
        registerArtifact(MiaArtifactItems.TEST_ARTIFACT_2);
        registerArtifact(MiaArtifactItems.TEST_ARTIFACT_3);
        registerArtifact(MiaArtifactItems.HEALTH_JUNKIE);
        registerWhistle(MiaWhistleItems.RED_WHISTLE);
        registerWhistleFragment(MiaWhistleItems.ECHO_REED);
        registerWhistleFragment(MiaWhistleItems.RESONANCE_PRESSURE_FRAGMENT);
    }

    private static void registerArtifact(java.util.function.Supplier<? extends net.minecraft.world.item.Item> item) {
        TooltipModifier modifier = new ItemDescription.Modifier(TooltipPalette.MIA, true, true)
                .andThen(ArtifactTooltipModifier.INSTANCE);
        TooltipModifierRegistry.register(item, modifier);
    }

    private static void registerWhistle(java.util.function.Supplier<? extends net.minecraft.world.item.Item> item) {
        TooltipModifier modifier = new ItemDescription.Modifier(TooltipPalette.MIA, true, true)
                .andThen(WhistleTooltipModifier.INSTANCE);
        TooltipModifierRegistry.register(item, modifier);
    }

    private static void registerWhistleFragment(java.util.function.Supplier<? extends net.minecraft.world.item.Item> item) {
        TooltipModifier modifier = new ItemDescription.Modifier(TooltipPalette.MIA, true, true)
                .andThen(WhistleFragmentTooltipModifier.INSTANCE);
        TooltipModifierRegistry.register(item, modifier);
    }

    private MiaTooltipModifiers() {}
}
