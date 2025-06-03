package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.MIAConfigureFeatures;
import com.altnoir.mia.worldgen.MIAPlacedFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MIADatapackProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUIDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, MIAConfigureFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, MIAPlacedFeatures::bootstrap);

    public MIADatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUIDER, Set.of(MIA.MOD_ID));
    }
}
