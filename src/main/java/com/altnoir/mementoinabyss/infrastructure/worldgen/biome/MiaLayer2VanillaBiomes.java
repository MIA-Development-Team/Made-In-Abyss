package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.Layer2FeatureConfiguration;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.MiaAbyssPlacements;
import com.altnoir.mementoinabyss.infrastructure.worldgen.placement.Layer2SurfacePlacement;
import com.altnoir.mementoinabyss.init.MiaSoundEvents;
import com.altnoir.mementoinabyss.init.MiaWorldgenFeatures;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
import org.jspecify.annotations.Nullable;

public final class MiaLayer2VanillaBiomes {
    private static List<Holder<PlacedFeature>> vegetation(Biome biome) {
        return biome
                .getGenerationSettings()
                .features()
                .get(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal())
                .stream()
                .filter(
                        holder ->
                                holder.value().placement().stream()
                                        .anyMatch(HeightmapPlacement.class::isInstance))
                .toList();
    }

    private static ResourceKey<PlacedFeature> key(Holder<PlacedFeature> original) {
        return ResourceKey.create(
                Registries.PLACED_FEATURE,
                MementoInAbyss.asResource(
                        "layer2/vanilla/"
                                + original.unwrapKey().orElseThrow().identifier().getPath()));
    }

    public static void bootstrapPlacements(
            BootstrapContext<PlacedFeature> context, HolderLookup.Provider originals) {
        var registered = new HashSet<ResourceKey<PlacedFeature>>();
        for (var original : placements(originals)) {
            var target = key(original);
            if (!registered.add(target)) continue;
            var source = original.value();
            int maxWaterDepth = -1;
            for (var modifier : source.placement()) {
                if (modifier instanceof SurfaceWaterDepthFilter depth) {
                    maxWaterDepth =
                            SurfaceWaterDepthFilter.CODEC
                                    .codec()
                                    .encodeStart(JsonOps.INSTANCE, depth)
                                    .getOrThrow()
                                    .getAsJsonObject()
                                    .get("max_water_depth")
                                    .getAsInt();
                }
            }
            var placement = new ArrayList<PlacementModifier>();
            for (var modifier : source.placement()) {
                if (modifier instanceof SurfaceWaterDepthFilter) continue;
                if (modifier instanceof HeightmapPlacement heightmap) {
                    var json =
                            HeightmapPlacement.CODEC
                                    .codec()
                                    .encodeStart(JsonOps.INSTANCE, heightmap)
                                    .getOrThrow();
                    var type =
                            Heightmap.Types.CODEC
                                    .parse(
                                            JsonOps.INSTANCE,
                                            json.getAsJsonObject().get("heightmap"))
                                    .getOrThrow();
                    placement.add(Layer2SurfacePlacement.of(type, maxWaterDepth));
                } else placement.add(modifier);
            }
            var configured =
                    context.lookup(Registries.CONFIGURED_FEATURE)
                            .getOrThrow(source.feature().unwrapKey().orElseThrow());
            if (localOceanFloor(original.value().feature().value()))
                configured =
                        context.lookup(Registries.CONFIGURED_FEATURE)
                                .getOrThrow(aquaticKey(original.value().feature()));
            context.register(target, new PlacedFeature(configured, List.copyOf(placement)));
        }
    }

    private static List<Holder<PlacedFeature>> placements(HolderLookup.Provider originals) {
        var biomes = originals.lookupOrThrow(Registries.BIOME);
        var features = new ArrayList<Holder<PlacedFeature>>();
        for (var biome :
                List.of(Biomes.MANGROVE_SWAMP, Biomes.PALE_GARDEN, Biomes.WARM_OCEAN, Biomes.OCEAN))
            features.addAll(vegetation(biomes.getOrThrow(biome).value()));
        features.add(
                originals
                        .lookupOrThrow(Registries.PLACED_FEATURE)
                        .getOrThrow(AquaticPlacements.KELP_WARM));
        return features;
    }

    private static boolean localOceanFloor(ConfiguredFeature<?, ?> feature) {
        return feature.feature() == Feature.SEAGRASS
                || feature.feature() == Feature.KELP
                || feature.feature() == Feature.SEA_PICKLE;
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> aquaticKey(
            Holder<ConfiguredFeature<?, ?>> original) {
        return ResourceKey.create(
                Registries.CONFIGURED_FEATURE,
                MementoInAbyss.asResource(
                        "layer2/vanilla/"
                                + original.unwrapKey().orElseThrow().identifier().getPath()));
    }

    public static void bootstrapFeatures(
            BootstrapContext<ConfiguredFeature<?, ?>> context, HolderLookup.Provider originals) {
        var registered = new HashSet<ResourceKey<ConfiguredFeature<?, ?>>>();
        var configured = context.lookup(Registries.CONFIGURED_FEATURE);
        for (var placed : placements(originals)) {
            var original = placed.value().feature();
            if (!localOceanFloor(original.value())) continue;
            var key = aquaticKey(original);
            if (registered.add(key))
                context.register(
                        key,
                        new ConfiguredFeature<>(
                                MiaWorldgenFeatures.LAYER2_FEATURE.get(),
                                new Layer2FeatureConfiguration(
                                        configured.getOrThrow(original.unwrapKey().orElseThrow()),
                                        false,
                                        true)));
        }
    }

    public static Biome create(
            ResourceKey<Biome> biome, BiomeGenerationSettings.Builder generation, Biome original) {
        for (var feature : vegetation(original))
            generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, key(feature));
        if (biome.equals(MiaBiomes.WARM_OCEAN))
            generation.addFeature(
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(
                            Registries.PLACED_FEATURE,
                            MementoInAbyss.asResource("layer2/vanilla/kelp_warm")));
        generation.addFeature(
                GenerationStep.Decoration.UNDERGROUND_STRUCTURES, MiaAbyssPlacements.SUN_STONE);
        var effects = original.getSpecialEffects();
        if (biome.equals(MiaBiomes.TEMPTATION_OCEAN)) {
            effects =
                    new BiomeSpecialEffects(
                            MiaBiomes.ABYSS_WATER_COLOR,
                            effects.foliageColorOverride(),
                            effects.dryFoliageColorOverride(),
                            effects.grassColorOverride(),
                            effects.grassColorModifier());
        }
        var builder =
                new Biome.BiomeBuilder()
                        .hasPrecipitation(false)
                        .temperature(original.getBaseTemperature())
                        .downfall(
                                biome.equals(MiaBiomes.MANGROVE_SWAMP)
                                        ? .9F
                                        : biome.equals(MiaBiomes.PALE_GARDEN) ? .8F : .5F)
                        .specialEffects(effects)
                        .mobSpawnSettings(original.getMobSettings())
                        .generationSettings(generation.build())
                        .putAttributes(original.getAttributes())
                        .setAttribute(
                                EnvironmentAttributes.BACKGROUND_MUSIC,
                                new BackgroundMusic(MiaSoundEvents.MUSIC_THE_FIRST_LAYER))
                        .setAttribute(EnvironmentAttributes.MUSIC_VOLUME, 1.0F);
        if (biome.equals(MiaBiomes.PALE_GARDEN))
            builder.setAttribute(EnvironmentAttributes.CREAKING_ACTIVE, true);
        return builder.build();
    }

    static @Nullable ResourceKey<Biome> template(ResourceKey<Biome> biome) {
        if (biome.equals(MiaBiomes.MANGROVE_SWAMP)) return Biomes.MANGROVE_SWAMP;
        if (biome.equals(MiaBiomes.PALE_GARDEN)) return Biomes.PALE_GARDEN;
        if (biome.equals(MiaBiomes.WARM_OCEAN)) return Biomes.WARM_OCEAN;
        if (biome.equals(MiaBiomes.TEMPTATION_OCEAN)) return Biomes.OCEAN;
        return null;
    }

    private MiaLayer2VanillaBiomes() {}
}
