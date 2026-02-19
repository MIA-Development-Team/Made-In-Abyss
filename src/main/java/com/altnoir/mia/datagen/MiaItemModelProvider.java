package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MiaItemModelProvider extends ItemModelProvider {
    public MiaItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(MiaItems.RED_WHISTLE.get());
        basicItem(MiaItems.BLUE_WHISTLE.get());
//        basicItem(MiaItems.MOON_WHISTLE.get());
//        basicItem(MiaItems.BLACK_WHISTLE.get());
//        basicItem(MiaItems.WHITE_WHISTLE.get());

        basicItem(MiaItems.MISTFUZZ_PEACH.get());
        basicItem(MiaItems.PRASIOLITE_SHARD.get());
        basicItem(MiaItems.GRAY_ARTIFACT_BUNDLE.get());
        basicItem(MiaItems.FANCY_ARTIFACT_BUNDLE.get());
        basicItem(MiaItems.TEST_ARTIFACT_1.get());
        basicItem(MiaItems.TEST_ARTIFACT_2.get());
        basicItem(MiaItems.TEST_ARTIFACT_3.get());
        basicItem(MiaItems.HEALTH_JUNKIE.get());

        basicItem(MiaItems.STAR_COMPASS.get());

        skillItem(MiaItems.ARTIFACT_HASTE.get());

        hookItem(MiaItems.HOOK.get());
        handheldItem(MiaItems.GROW_SWORD.get());
        handheldItem(MiaItems.PRASIOLITE_PICKAXE.get());
        handheldItem(MiaItems.PRASIOLITE_HOE.get());

    }

    public ItemModelBuilder skillItem(Item item) {
        return getBuilder(MiaUtil.getItemKey(item).toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", MiaUtil.id(MiaUtil.getItemKey(item).getNamespace(), "item/skill/" + MiaUtil.getItemKey(item).getPath()));
    }

    public ItemModelBuilder hookItem(Item item) {
        return getBuilder(MiaUtil.getItemKey(item).toString())
                .parent(new ModelFile.UncheckedModelFile(modLoc("item/template/hook")))
                .texture("layer0", MiaUtil.id(MiaUtil.getItemNamespace(item), "item/" + MiaUtil.getItemPath(item)));
    }
}
