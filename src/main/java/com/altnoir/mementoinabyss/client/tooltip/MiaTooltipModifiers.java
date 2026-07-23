package com.altnoir.mementoinabyss.client.tooltip;

import com.altnoir.mementoinabyss.init.MiaArtifactItems;

public final class MiaTooltipModifiers {
    public static void register() {
        registerArtifact(MiaArtifactItems.TEST_ARTIFACT_1);
        registerArtifact(MiaArtifactItems.TEST_ARTIFACT_2);
        registerArtifact(MiaArtifactItems.TEST_ARTIFACT_3);
        registerArtifact(MiaArtifactItems.HEALTH_JUNKIE);
    }

    private static void registerArtifact(java.util.function.Supplier<? extends net.minecraft.world.item.Item> item) {
        TooltipModifier modifier = new ItemDescription.Modifier(TooltipPalette.MIA, true)
                .andThen(ArtifactTooltipModifier.INSTANCE);
        TooltipModifierRegistry.register(item, modifier);
    }

    private MiaTooltipModifiers() {}
}
