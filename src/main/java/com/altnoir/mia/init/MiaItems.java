package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.component.MiaFoods;
import com.altnoir.mia.item.ArtifactBundle;
import com.altnoir.mia.item.EnhanceableArtifact;
import com.altnoir.mia.item.RopeItem;
import com.altnoir.mia.item.SimpleWhistle;
import com.altnoir.mia.item.abs.IArtifactItem.Grade;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MIA.MOD_ID);

    public static final DeferredItem<Item> RED_WHISTLE = ITEMS.register("red_whistle", () -> new SimpleWhistle(
            new Item.Properties(), 1, Attributes.MAX_HEALTH, 4, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredItem<Item> BLUE_WHISTLE = ITEMS.register("blue_whistle", () -> new SimpleWhistle(
            new Item.Properties(), 2, Attributes.MAX_HEALTH, 8, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredItem<Item> MOON_WHISTLE = ITEMS.register("moon_whistle", () -> new SimpleWhistle(
            new Item.Properties(), 3, Attributes.MAX_HEALTH, 12, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredItem<Item> BLACK_WHISTLE = ITEMS.register("black_whistle", () -> new SimpleWhistle(
            new Item.Properties(), 4, Attributes.MAX_HEALTH, 16, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredItem<Item> WHITE_WHISTLE = ITEMS.register("white_whistle", () -> new SimpleWhistle(
            new Item.Properties(), 5, Attributes.MAX_HEALTH, 20, AttributeModifier.Operation.ADD_VALUE));

    public static final DeferredItem<Item> GRAY_ARTIFACT_BUNDLE = ITEMS.register("gray_artifact_bundle",
            () -> new ArtifactBundle(new Item.Properties(), Grade.THIRD, 3));
    public static final DeferredItem<Item> FANCY_ARTIFACT_BUNDLE = ITEMS.register("fancy_artifact_bundle",
            () -> new ArtifactBundle(new Item.Properties(), Grade.SECOND, 6));

    public static final DeferredItem<Item> TEST_ARTIFACT_1 = ITEMS.register("test_artifact_1",
            () -> new EnhanceableArtifact(new Item.Properties(), Grade.FOURTH, 1));
    public static final DeferredItem<Item> TEST_ARTIFACT_2 = ITEMS.register("test_artifact_2",
            () -> new EnhanceableArtifact(new Item.Properties(), Grade.THIRD, 2));
    public static final DeferredItem<Item> TEST_ARTIFACT_3 = ITEMS.register("test_artifact_3",
            () -> new EnhanceableArtifact(new Item.Properties(), Grade.SPECIAL, 4));

    public static final DeferredItem<Item> MISTFUZZ_PEACH = ITEMS.register("mistfuzz_peach",
            () -> new Item(new Item.Properties().food(MiaFoods.MISTFUZZ_PEACH)));

    // Block Item
    public static final DeferredItem<Item> ROPE = ITEMS.register("rope",
            () -> new RopeItem(MiaBlocks.ROPE.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
