package com.altnoir.mia.worldgen.biome.abyss_brink;

import com.altnoir.mia.worldgen.MiaPlacementUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class AbyssBrinkPlacements {
    public static final ResourceKey<PlacedFeature> SPRING_WATER = MiaPlacementUtils.resourceKey("spring_water");
    public static final ResourceKey<PlacedFeature> TREES_SKYFOG_AND_AZALEA = MiaPlacementUtils.resourceKey("trees_skyfog_and_azalea");

    public static final PlacementModifier ABYSS_BRINK_HEIGHT = HeightRangePlacement.uniform(
            VerticalAnchor.bottom(), VerticalAnchor.absolute(360)
    );

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> spring_water = holdergetter.getOrThrow(AbyssBrinkFeatures.SPRING_WATER);
        Holder<ConfiguredFeature<?, ?>> skyfog_and_azalea = holdergetter.getOrThrow(AbyssBrinkFeatures.TREES_SKYFOG_AND_AZALEA);

        MiaPlacementUtils.register(
                context,
                SPRING_WATER,
                spring_water,
                CountPlacement.of(10),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(16), VerticalAnchor.absolute(300)),
                BiomeFilter.biome()
        );

        MiaPlacementUtils.register(context, TREES_SKYFOG_AND_AZALEA, skyfog_and_azalea, abyssTreePlace(8));
    }

    private static ImmutableList.Builder<PlacementModifier> abyssTreePlacementBase(int count) {
        return ImmutableList.<PlacementModifier>builder()
                .add(CountOnEveryLayerPlacement.of(count))
                .add(BiomeFilter.biome());

    }

    public static List<PlacementModifier> abyssTreePlace(int count) {
        return abyssTreePlacementBase(count).build();
    }

    private static ImmutableList.Builder<PlacementModifier> treePlacementBase(PlacementModifier placement) {
        return ImmutableList.<PlacementModifier>builder()
                .add(placement)
                .add(InSquarePlacement.spread())
                .add(ABYSS_BRINK_HEIGHT)
                .add(RandomOffsetPlacement.vertical(ConstantInt.of(-1)))
                .add(BiomeFilter.biome());
    }

    public static List<PlacementModifier> treePlace(PlacementModifier placement) {
        return treePlacementBase(placement).build();
    }

    public static List<PlacementModifier> treePlace(PlacementModifier placement, Block saplingBlock) {
        return treePlacementBase(placement)
                .add(BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(saplingBlock.defaultBlockState(), BlockPos.ZERO)))
                .build();
    }
}
