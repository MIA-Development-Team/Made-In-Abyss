package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.component.MiaFoods;
import com.altnoir.mia.item.ArtifactBundle;
import com.altnoir.mia.item.HealthAbilityCard;
import com.altnoir.mia.item.RedWhistle;
import com.altnoir.mia.item.RopeItem;
import com.altnoir.mia.item.SimpleStatsArtifact;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MIA.MOD_ID);

    public static final DeferredItem<Item> RED_WHISTLE = ITEMS.register("red_whistle", () -> new RedWhistle(
            new Item.Properties(), 1));

    public static final DeferredItem<Item> HEALTH_ABILITY_CARD = ITEMS.register("health_ability_card",
            () -> new HealthAbilityCard(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> GRAY_ARTIFACT_BUNDLE = ITEMS.register("gray_artifact_bundle",
            () -> new ArtifactBundle(new Item.Properties(), 4));

    public static final DeferredItem<Item> TEST_ARTIFACT = ITEMS.register("test_artifact",
            () -> (new SimpleStatsArtifact.Builder(new Item.Properties().stacksTo(1))
                    .weight(1)
                    .addModifier(Attributes.MAX_HEALTH, 2, AttributeModifier.Operation.ADD_VALUE)).build());

    public static final DeferredItem<Item> MISTFUZZ_PEACH = ITEMS.register("mistfuzz_peach",
            () -> new Item(new Item.Properties().food(MiaFoods.MISTFUZZ_PEACH)));

    // Block Item
    public static final DeferredItem<Item> ROPE = ITEMS.register("rope",
            () -> new RopeItem(MiaBlocks.ROPE.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
