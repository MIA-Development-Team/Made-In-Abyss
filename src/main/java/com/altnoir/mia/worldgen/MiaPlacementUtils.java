package com.altnoir.mia.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.biome.the_abyss.TheAbyssPlacements;
import com.altnoir.mia.worldgen.feature.tree.MiaTreePlacements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public class MiaPlacementUtils {
    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        MiaTreePlacements.bootstrap(context);
        TheAbyssPlacements.bootstrap(context);
    }

    public static ResourceKey<PlacedFeature> resourceKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, MiaUtil.id(MIA.MOD_ID, name));
    }

    public static ResourceKey<PlacedFeature> treeKey(String name) {
        return resourceKey("tree/" + name);
    }

    public static ResourceKey<PlacedFeature> abyssEdgeKey(String name) {
        return resourceKey("the_abyss/" + name);
    }

    public static void register(
            BootstrapContext<PlacedFeature> context,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> configuration,
            List<PlacementModifier> modifiers
    ) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

    public static void register(
            BootstrapContext<PlacedFeature> context,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> configuredFeature,
            PlacementModifier... placements
    ) {
        register(context, key, configuredFeature, List.of(placements));
    }

    public static BlockPredicateFilter filteredByBlockSurvival(Block block) {
        return BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(block.defaultBlockState(), BlockPos.ZERO));
    }
}
