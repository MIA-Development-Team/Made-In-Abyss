package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.item.RopeItem;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class MiaItems {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    static {
        REGISTRATE.defaultCreativeSection(MiaItemGroups.BASE_ITEMS);
    }

    public static final ItemEntry<BlockItem> GLOOM_BERRY = REGISTRATE
            .item("gloom_berry", p -> new BlockItem(MiaBlocks.GLOOM_BERRY_PLANT.get(), p))
            .properties(p -> p.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build()))
            .register();

    public static final ItemEntry<BlockItem> DREAM_LICHEE = REGISTRATE
            .item("dream_lichee", p -> new BlockItem(MiaBlocks.DREAM_LICHEE_PLANT.get(), p))
            .properties(p -> p.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.4F).build()))
            .register();

    public static final ItemEntry<Item> MISTFUZZ_PEACH = REGISTRATE
            .item("mistfuzz_peach", Item::new)
            .properties(p -> p.food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.4F).build()))
            .register();

    public static final ItemEntry<Item> RAW_CHLOROPHYTE = REGISTRATE.item("raw_chlorophyte", Item::new).register();
    public static final ItemEntry<Item> PRASIOLITE_SHARD = REGISTRATE.item("prasiolite_shard", Item::new).register();
    public static final ItemEntry<Item> CAERULITE_SHARD = REGISTRATE.item("caerulite_shard", Item::new).register();
    public static final ItemEntry<RopeItem> ROPE = REGISTRATE
            .item("rope", RopeItem::new)
            .model(() -> (context, provider) -> {
                var model = ModelTemplates.FLAT_ITEM.create(
                        context.get(),
                        TextureMapping.layer0(new Material(provider.modLoc("item/rope"))),
                        provider.modelOutput
                );
                provider.createWithExistingModel(context.getEntry(), model);
            })
            .register();

    public static void register() {}
}
