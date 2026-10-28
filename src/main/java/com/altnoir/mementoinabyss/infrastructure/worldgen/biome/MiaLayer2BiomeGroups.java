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
        add(
                context,
                "temptation",
                MiaBiomes.TEMPTATION_PLAINS,
                MiaBiomes.TEMPTATION_FOREST,
                List.of(Layer2TerrainKind.WOODLAND));
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
