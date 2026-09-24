package com.altnoir.mia.datagen;

import com.altnoir.mia.init.MiaBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

public class MiaDataMapProvider extends DataMapProvider {
    protected MiaDataMapProvider(
            PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        this.builder(NeoForgeDataMaps.COMPOSTABLES)
                .add(MiaBlocks.FORTITUDE_FLOWER.getId(), new Compostable(0.65F), false)
                .add(MiaBlocks.MYCELIUM_MAT.getId(), new Compostable(0.3F), false);
    }
}
