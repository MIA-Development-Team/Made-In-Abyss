package com.altnoir.mia;

import com.altnoir.mia.content.foods.Purin;
import com.altnoir.mia.content.items.ClockItem;
import com.altnoir.mia.content.items.KnifeItem;
import com.altnoir.mia.content.items.relics.t1.UnlimitedWaterBucket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemsRegister {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MIA.MOD_ID);

    public static final RegistryObject<Item> PURIN = ITEMS.register("purin", Purin::new);
    public static final RegistryObject<Item> ENDLESS_CUP = ITEMS.register("endless_cup", UnlimitedWaterBucket::new);

    public static final RegistryObject<Item> CLOCK = ITEMS.register("time_clock", () ->
            new ClockItem(new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant().stacksTo(1)));
    public static final RegistryObject<Item> Knife = ITEMS.register("knife", () ->
            new KnifeItem(new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant().stacksTo(16)));

    public static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
