package com.altnoir.mia.worldgen.tree;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.worldgen.MiaPlacementUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public class MiaTreePlacements {
    public static final ResourceKey<PlacedFeature> AZALEA_CHECKED = MiaPlacementUtils.resourceKey("azalea_checked");
    public static final ResourceKey<PlacedFeature> SKYFOG_BEES_002 = MiaPlacementUtils.resourceKey("skyfog_bee_002");
    public static final ResourceKey<PlacedFeature> FANCY_SKYFOG_BEES_005 = MiaPlacementUtils.resourceKey("fancy_skyfog_bee_005");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> azalea = holdergetter.getOrThrow(TreeFeatures.AZALEA_TREE);
        Holder<ConfiguredFeature<?, ?>> skyfog_bee = holdergetter.getOrThrow(MiaTreeFeatures.SKYFOG_TREE_BEES);
        Holder<ConfiguredFeature<?, ?>> fancy_skyfog_bee = holdergetter.getOrThrow(MiaTreeFeatures.FANCY_SKYFOG_TREE_BEES);

        MiaPlacementUtils.register(context, AZALEA_CHECKED, azalea, MiaPlacementUtils.filteredByBlockSurvival(Blocks.AZALEA));
        MiaPlacementUtils.register(context, SKYFOG_BEES_002, skyfog_bee, MiaPlacementUtils.filteredByBlockSurvival(MiaBlocks.SKYFOG_SAPLING.get()));
        MiaPlacementUtils.register(context, FANCY_SKYFOG_BEES_005, fancy_skyfog_bee, MiaPlacementUtils.filteredByBlockSurvival(MiaBlocks.SKYFOG_SAPLING.get()));
    }
}
