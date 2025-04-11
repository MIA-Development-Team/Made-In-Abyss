package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.ItemsRegister;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class MIAItemModelProvider extends ItemModelProvider {
    public MIAItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MIA.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ItemsRegister.PURIN);
        simpleItem(ItemsRegister.ENDLESS_CUP);
        simpleItem(ItemsRegister.UNHEARD_BELL);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(MIA.MODID,"item/" + item.getId().getPath()));
    }
}
