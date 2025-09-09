package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MiaItemModelProvider extends ItemModelProvider {
    public MiaItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(MiaItems.RED_WHISTLE.get());
        basicItem(MiaItems.BLUE_WHISTLE.get());
        basicItem(MiaItems.MOON_WHISTLE.get());
        basicItem(MiaItems.BLACK_WHISTLE.get());
        basicItem(MiaItems.WHITE_WHISTLE.get());

        basicItem(MiaItems.MISTFUZZ_PEACH.get());
        basicItem(MiaItems.PRASIOLITE_SHARD.get());
        basicItem(MiaItems.GRAY_ARTIFACT_BUNDLE.get());
        basicItem(MiaItems.FANCY_ARTIFACT_BUNDLE.get());
        basicItem(MiaItems.TEST_ARTIFACT_1.get());
        basicItem(MiaItems.TEST_ARTIFACT_2.get());
        basicItem(MiaItems.TEST_ARTIFACT_3.get());

        handheldItem(MiaItems.PRASIOLITE_COMPOSITE.get());
        handheldItem(MiaItems.PRASIOLITE_HOE.get());

    }
}
