package com.altnoir.mia.register;

import com.altnoir.mia.MIA;
import com.altnoir.mia.content.items.*;
import com.altnoir.mia.content.items.base.UnlimitedBucketItem;
import com.altnoir.mia.content.items.relics.t1.UnlimitedWaterBucket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MIAItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MIA.MOD_ID);

    public static final RegistryObject<Item> PURIN = ITEMS.register("purin", Purin::new);
    public static final RegistryObject<Item> UNLIMITED_WATER_BUCKET = ITEMS.register("unlimited_water_bucket", UnlimitedWaterBucket::new);

    public static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
