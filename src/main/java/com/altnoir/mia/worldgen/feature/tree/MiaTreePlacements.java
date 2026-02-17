package com.altnoir.mia.worldgen.feature.tree;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.worldgen.MiaPlacementUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class MiaTreePlacements {
    public static final ResourceKey<PlacedFeature> AZALEA_CHECKED = MiaPlacementUtils.treeKey("azalea_checked");
    public static final ResourceKey<PlacedFeature> SKYFOG_BEES = MiaPlacementUtils.treeKey("skyfog_bee_002");
    public static final ResourceKey<PlacedFeature> FANCY_SKYFOG_BEES_002 = MiaPlacementUtils.treeKey("fancy_skyfog_bee_002");
    public static final ResourceKey<PlacedFeature> MEGA_SKYFOG = MiaPlacementUtils.treeKey("maga_skyfog");

    public static final ResourceKey<PlacedFeature> VERDANT_FUNGUS = MiaPlacementUtils.treeKey("verdant_fungus");
    public static final ResourceKey<PlacedFeature> INVERTED = MiaPlacementUtils.treeKey("inverted");
    public static final ResourceKey<PlacedFeature> MAGA_INVERTED = MiaPlacementUtils.treeKey("maga_inverted");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> azalea = holdergetter.getOrThrow(TreeFeatures.AZALEA_TREE);
        Holder<ConfiguredFeature<?, ?>> skyfog = holdergetter.getOrThrow(MiaTreeFeatures.SKYFOG_TREE);
        Holder<ConfiguredFeature<?, ?>> fancy_skyfog_bee = holdergetter.getOrThrow(MiaTreeFeatures.FANCY_SKYFOG_TREE_BEES);
        Holder<ConfiguredFeature<?, ?>> maga_skyfog = holdergetter.getOrThrow(MiaTreeFeatures.MEGA_SKYFOG_TREE);
        Holder<ConfiguredFeature<?, ?>> verdant_fungus = holdergetter.getOrThrow(MiaTreeFeatures.VERDANT_FUNGUS);
        Holder<ConfiguredFeature<?, ?>> inverted = holdergetter.getOrThrow(MiaTreeFeatures.INVERTED_TREE);
        Holder<ConfiguredFeature<?, ?>> maga_inverted = holdergetter.getOrThrow(MiaTreeFeatures.MEGA_INVERTED_TREE);

        MiaPlacementUtils.register(context, AZALEA_CHECKED, azalea, MiaPlacementUtils.filteredByBlockSurvival(Blocks.AZALEA));
        MiaPlacementUtils.register(context, SKYFOG_BEES, skyfog, MiaPlacementUtils.filteredByBlockSurvival(MiaBlocks.SKYFOG_SAPLING.get()));
        MiaPlacementUtils.register(context, FANCY_SKYFOG_BEES_002, fancy_skyfog_bee, MiaPlacementUtils.filteredByBlockSurvival(MiaBlocks.SKYFOG_SAPLING.get()));
        MiaPlacementUtils.register(context, MEGA_SKYFOG, maga_skyfog, MiaPlacementUtils.filteredByBlockSurvival(MiaBlocks.SKYFOG_SAPLING.get()));
        MiaPlacementUtils.register(context, VERDANT_FUNGUS, verdant_fungus, MiaPlacementUtils.filteredByBlockSurvival(MiaBlocks.VERDANT_FUNGUS.get()));
        MiaPlacementUtils.register(context, INVERTED, inverted, MiaPlacementUtils.filteredByBlockSurvival(MiaBlocks.INVERTED_SAPLING.get()));
        MiaPlacementUtils.register(context, MAGA_INVERTED, maga_inverted, MiaPlacementUtils.filteredByBlockSurvival(MiaBlocks.INVERTED_SAPLING.get()));

    }
}
