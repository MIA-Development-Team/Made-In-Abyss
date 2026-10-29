package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.api.worldgen.Layer2BiomeGroup;
import com.altnoir.mementoinabyss.api.worldgen.Layer2Biomes;
import com.altnoir.mementoinabyss.api.worldgen.Layer2TerrainKind;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public final class MiaLayer2BiomeGroups {
    public static void registerRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(Layer2Biomes.REGISTRY_KEY, Layer2BiomeGroup.CODEC);
    }

    public static void bootstrap(BootstrapContext<Layer2BiomeGroup> context) {
        add(
                context,
                "primo",
                MiaBiomes.PRIMO_FOREST,
                MiaBiomes.PRIMO_PLAINS,
                List.of(Layer2TerrainKind.WOODLAND));
        standalone(
                context,
                "temptation_forest",
                MiaBiomes.TEMPTATION_FOREST,
                Layer2TerrainKind.WOODLAND,
                false);
        standalone(
                context,
                "temptation_plains",
                MiaBiomes.TEMPTATION_PLAINS,
                Layer2TerrainKind.WOODLAND,
                false);
        context.register(
                ResourceKey.create(
                        Layer2Biomes.REGISTRY_KEY, MementoInAbyss.asResource("warm_ocean")),
                new Layer2BiomeGroup(
                        Optional.of(
                                context.lookup(Registries.BIOME).getOrThrow(MiaBiomes.WARM_OCEAN)),
                        List.of(),
                        List.of(Layer2TerrainKind.OCEAN),
                        1,
                        Optional.empty(),
                        Optional.empty(),
                        64,
                        true,
                        List.of(MementoInAbyss.asResource("temptation_ocean"))));
        standalone(
                context,
                "temptation_ocean",
                MiaBiomes.TEMPTATION_OCEAN,
                Layer2TerrainKind.OCEAN,
                false);
        add(
                context,
                "giant_fossilized",
                MiaBiomes.GIANT_FOSSILIZED_FOREST,
                MiaBiomes.SPARSE_GIANT_FOSSILIZED_FOREST,
                List.of(Layer2TerrainKind.HIGHLAND, Layer2TerrainKind.PLATEAU));
        add(
                context,
                "fossilized_wetland",
                MiaBiomes.UNDER_RICH_FOSSILIZED_FOREST,
                MiaBiomes.UNDER_WATER_FOSSILIZED_FOREST,
                List.of(Layer2TerrainKind.WETLAND));
        add(
                context,
                "mangrove_pale",
                MiaBiomes.MANGROVE_SWAMP,
                MiaBiomes.PALE_GARDEN,
                List.of(Layer2TerrainKind.WETLAND_WOODLAND, Layer2TerrainKind.WETLAND_HIGHLAND));
    }

    private static void standalone(
            BootstrapContext<Layer2BiomeGroup> context,
            String name,
            ResourceKey<Biome> biome,
            Layer2TerrainKind terrain,
            boolean primaryOnly) {
        context.register(
                ResourceKey.create(Layer2Biomes.REGISTRY_KEY, MementoInAbyss.asResource(name)),
                new Layer2BiomeGroup(
                        Optional.of(context.lookup(Registries.BIOME).getOrThrow(biome)),
                        List.of(),
                        List.of(terrain),
                        1,
                        Optional.empty(),
                        Optional.empty(),
                        64,
                        primaryOnly));
    }

    private static void add(
            BootstrapContext<Layer2BiomeGroup> context,
            String name,
            ResourceKey<Biome> primary,
            ResourceKey<Biome> companion,
            List<Layer2TerrainKind> kinds) {
        var biomes = context.lookup(Registries.BIOME);
        context.register(
                ResourceKey.create(Layer2Biomes.REGISTRY_KEY, MementoInAbyss.asResource(name)),
                new Layer2BiomeGroup(
                        Optional.of(biomes.getOrThrow(primary)),
                        List.of(biomes.getOrThrow(companion)),
                        kinds,
                        1,
                        Optional.empty()));
    }

    private MiaLayer2BiomeGroups() {}
}
