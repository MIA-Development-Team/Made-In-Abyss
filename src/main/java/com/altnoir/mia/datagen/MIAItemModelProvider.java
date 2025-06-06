package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.MIABlocks;
import com.altnoir.mia.item.MIAItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class MIAItemModelProvider extends ItemModelProvider {
    public MIAItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(MIAItems.ENDLESS_CUP.get());
        crossItem(MIABlocks.FORTITUDE_FLOWER);
    }

    private ItemModelBuilder crossItem(DeferredBlock<Block> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "block/" + item.getId().getPath()));
    }
}
