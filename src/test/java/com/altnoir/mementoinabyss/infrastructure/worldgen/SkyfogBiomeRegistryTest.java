package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.*;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.MiaTreeFeatures;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.*;
import org.junit.jupiter.api.Test;

class SkyfogBiomeRegistryTest {
    @Test
    void actualBiomeBootstrapContainsForest() {
        var registries = registries();
        var biome =
                registries.lookupOrThrow(Registries.BIOME).getOrThrow(MiaBiomes.OVERWORLD_ABYSS);
        var forest =
                registries
                        .lookupOrThrow(Registries.PLACED_FEATURE)
                        .getOrThrow(MiaAbyssPlacements.OVERWORLD_TREES_SKYFOG);
        assertTrue(biome.value().getGenerationSettings().hasFeature(forest.value()));
    }

    static HolderLookup.Provider registries() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        var vanilla = VanillaRegistries.createLookup();
        return RegistryPatchGenerator.createLookup(
                        java.util.concurrent.CompletableFuture.completedFuture(vanilla),
                        new RegistrySetBuilder()
                                .add(
                                        Registries.CONFIGURED_FEATURE,
                                        c -> {
                                            MiaTreeFeatures.bootstrap(c);
                                            MiaAbyssFeatures.bootstrap(c);
                                            GreatFaultFeatures.bootstrap(c);
                                        })
                                .add(
                                        Registries.PLACED_FEATURE,
                                        c -> {
                                            MiaAbyssPlacements.bootstrap(c);
                                            GreatFaultPlacements.bootstrap(c);
                                        })
                                .add(Registries.BIOME, MiaBiomes::bootstrap))
                .join()
                .full();
    }
}
