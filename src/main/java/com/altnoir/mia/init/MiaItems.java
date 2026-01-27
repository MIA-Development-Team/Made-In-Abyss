package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.common.component.MiaFoods;
import com.altnoir.mia.common.item.*;
import com.altnoir.mia.common.item.abs.IArtifactItem.Grade;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MIA.MOD_ID);

    public static final DeferredItem<Item> RED_WHISTLE = ITEMS.register("red_whistle", () -> new SimpleWhistle(
            new Item.Properties(), 1, 4));
    public static final DeferredItem<Item> BLUE_WHISTLE = ITEMS.register("blue_whistle", () -> new SimpleWhistle(
            new Item.Properties(), 2, 8));
//    public static final DeferredItem<Item> MOON_WHISTLE = ITEMS.register("moon_whistle", () -> new SimpleWhistle(
//            new Item.Properties(), 3, 12));
//    public static final DeferredItem<Item> BLACK_WHISTLE = ITEMS.register("black_whistle", () -> new SimpleWhistle(
//            new Item.Properties(), 4, 16));
//    public static final DeferredItem<Item> WHITE_WHISTLE = ITEMS.register("white_whistle", () -> new SimpleWhistle(
//            new Item.Properties(), 5, 20));

    public static final DeferredItem<Item> GRAY_ARTIFACT_BUNDLE = ITEMS.register("gray_artifact_bundle", () ->
            new ArtifactBundle(new Item.Properties(), Grade.C, 3));
    public static final DeferredItem<Item> FANCY_ARTIFACT_BUNDLE = ITEMS.register("fancy_artifact_bundle", () ->
            new ArtifactBundle(new Item.Properties(), Grade.B, 6));

    public static final DeferredItem<Item> TEST_ARTIFACT_1 = ITEMS.register("test_artifact_1", () ->
            new EArtifact(new Item.Properties(), Grade.D, 1));
    public static final DeferredItem<Item> TEST_ARTIFACT_2 = ITEMS.register("test_artifact_2", () ->
            new EArtifact(new Item.Properties(), Grade.C, 2));
    public static final DeferredItem<Item> TEST_ARTIFACT_3 = ITEMS.register("test_artifact_3", () ->
            new EArtifact(new Item.Properties(), Grade.S, 4));

    public static final DeferredItem<Item> HEALTH_JUNKIE = ITEMS.register("health_junkie", () ->
            new EArtifact(new Item.Properties(), Grade.D, 1));

    public static final DeferredItem<Item> ARTIFACT_HASTE = ITEMS.register("artifact_haste", () ->
            new HasteSkill(new Item.Properties()));

    public static final DeferredItem<Item> PRASIOLITE_SHARD = ITEMS.register("prasiolite_shard", () ->
            new Item(new Item.Properties()));
    public static final DeferredItem<Item> GROW_SWORD = ITEMS.register("grow_sword", () ->
            new GrowSwordItem(
                    MiaTiers.PRASIOLITE, new Item.Properties()
                    .attributes(SwordItem.createAttributes(MiaTiers.PRASIOLITE, 0.0F, -2.4F))
            ));
    public static final DeferredItem<Item> PRASIOLITE_PICKAXE = ITEMS.register("prasiolite_pickaxe", () ->
            new CompositeItem(
                    MiaTiers.PRASIOLITE, new Item.Properties()
                    .attributes(CompositeItem.createAttributes(MiaTiers.PRASIOLITE, 8.0F, -2.8F))
            ));
    public static final DeferredItem<Item> PRASIOLITE_HOE = ITEMS.register("prasiolite_hoe", () ->
            new PrasioliteHoeItem(
                    MiaTiers.PRASIOLITE, new Item.Properties()
                    .attributes(HoeItem.createAttributes(MiaTiers.PRASIOLITE, 0.0F, 0.0F))));

    public static final DeferredItem<Item> MISTFUZZ_PEACH = ITEMS.register("mistfuzz_peach", () ->
            new Item(new Item.Properties().food(MiaFoods.MISTFUZZ_PEACH)));

    public static final DeferredItem<Item> HOOK_ITEM = ITEMS.register(
            "hook_item",
            () -> new HookItem(new Item.Properties().component(DataComponents.CUSTOM_DATA, CustomData.EMPTY).stacksTo(1))
    );

    // Block Item
    public static final DeferredItem<Item> ROPE = ITEMS.register("rope", () ->
            new RopeItem(MiaBlocks.ROPE.get(), new Item.Properties()));

    public static final DeferredItem<Item> STAR_COMPASS = ITEMS.register("star_compass", () ->
            new StarCompassItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}