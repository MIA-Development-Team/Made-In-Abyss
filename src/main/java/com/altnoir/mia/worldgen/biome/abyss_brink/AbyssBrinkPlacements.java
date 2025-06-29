package com.altnoir.mia.worldgen.biome.abyss_brink;

import com.altnoir.mia.worldgen.MiaPlacementUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

public class AbyssBrinkPlacements {
    public static final ResourceKey<PlacedFeature> SPRING_WATER = MiaPlacementUtils.resourceKey("spring_water");
    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> holder6 = holdergetter.getOrThrow(AbyssBrinkFeatures.SPRING_WATER);

        MiaPlacementUtils.register(
                context,
                SPRING_WATER,
                holder6,
                CountPlacement.of(10),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(16), VerticalAnchor.absolute(300)),
                BiomeFilter.biome()
        );
    }
}
