package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class MiaItems {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    static {
        REGISTRATE.defaultCreativeTab(MiaItemGroups.BASE.getKey());
    }

    public static final ItemEntry<BlockItem> GLOOM_BERRY = REGISTRATE
            .item("gloom_berry", p -> new BlockItem(MiaBlocks.GLOOM_BERRY_PLANT.get(), p))
            .properties(p -> p.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build()))
            .register();

    public static final ItemEntry<BlockItem> DREAM_LICHEE = REGISTRATE
            .item("dream_lichee", p -> new BlockItem(MiaBlocks.DREAM_LICHEE_PLANT.get(), p))
            .properties(p -> p.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.4F).build()))
            .register();

    public static final ItemEntry<Item> RAW_CHLOROPHYTE = REGISTRATE.item("raw_chlorophyte", Item::new).register();
    public static final ItemEntry<Item> PRASIOLITE_SHARD = REGISTRATE.item("prasiolite_shard", Item::new).register();

    public static void register() {}
}
