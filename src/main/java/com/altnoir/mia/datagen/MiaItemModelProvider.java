package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Objects;

public class MiaItemModelProvider extends ItemModelProvider {
    public MiaItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(MiaItems.RED_WHISTLE.get());
        basicItem(MiaItems.MISTFUZZ_PEACH.get());
    }
}
