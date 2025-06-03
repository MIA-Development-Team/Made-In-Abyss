package com.altnoir.mia.item;

import com.altnoir.mia.MIA;
import com.altnoir.mia.item.c.UnlimitedBucket;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MIAItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MIA.MOD_ID);

    public static final DeferredItem<Item> ENDLESS_CUP = ITEMS.register("endless_cup", () ->
            new UnlimitedBucket(Fluids.WATER, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
