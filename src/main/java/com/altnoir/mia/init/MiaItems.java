package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.item.RedWhistle;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MIA.MOD_ID);

    public static final DeferredItem<Item> ENDLESS_CUP = ITEMS.register("endless_cup", () ->
            new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> RED_WHISTLE = ITEMS.register("red_whistle", () ->
            new RedWhistle(new Item.Properties().stacksTo(1).component(MiaComponents.WHISTLE_LEVEL, 1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
