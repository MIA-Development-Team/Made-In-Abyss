package com.altnoir.mia.content.items;

import com.altnoir.mia.MIA;
import com.altnoir.mia.content.foods.Purin;
import com.altnoir.mia.content.items.UnlimitedWaterBucket;
import com.altnoir.mia.content.items.test;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ItemsRegister {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MIA.MODID);

    public static final RegistryObject<Item> PURIN = ITEMS.register("purin", Purin::new);
    public static final RegistryObject<Item> ENDLESS_CUP = ITEMS.register("endless_cup", UnlimitedWaterBucket::new);
    public static final RegistryObject<Item> TESTITEM = ITEMS.register("testitem", () ->
            new test(new Item.Properties().durability(10)));

    public static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static List<RegistryObject<Item>> getAll() {
        return ITEMS.getEntries().stream().toList();
    }
}
