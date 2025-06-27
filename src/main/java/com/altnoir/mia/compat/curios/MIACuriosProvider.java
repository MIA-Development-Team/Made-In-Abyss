package com.altnoir.mia.compat.curios;

// 新增导入Curios API相关类

import com.altnoir.mia.MIA;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;
import top.theillusivec4.curios.api.type.data.IEntitiesData;
import top.theillusivec4.curios.api.type.data.ISlotData;

import java.util.concurrent.CompletableFuture;

public class MiaCuriosProvider extends CuriosDataProvider {

    public MiaCuriosProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(MIA.MOD_ID, output, fileHelper, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        ISlotData whistleSlot = createSlot("whistle");
        whistleSlot.size(1);
        whistleSlot.icon(ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "slot/empty_whistle_slot"));
        whistleSlot.addCosmetic(true);

        IEntitiesData entitiesData = createEntities("entities");
        entitiesData.addEntities(EntityType.PLAYER, EntityType.ARMOR_STAND);
        entitiesData.addSlots("whistle", "head");
    }
}