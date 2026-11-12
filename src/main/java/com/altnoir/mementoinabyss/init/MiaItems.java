package com.altnoir.mementoinabyss.init;

import com.altnoir.abysslib.reginth.Reginth;
import com.altnoir.abysslib.reginth.util.entry.ItemEntry;
import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.returnchest.BalloonItem;
import com.altnoir.mementoinabyss.content.returnchest.client.ReturnStorageModels;
import com.altnoir.mementoinabyss.content.rope.RopeItem;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.RegisterEvent;

public class MiaItems {
    private static final Reginth REGISTRATE = MementoInAbyss.registrate();

    static {
        REGISTRATE.defaultCreativeSection(MiaItemGroups.BASE_ITEMS);
    }

    public static final ItemEntry<BlockItem> GLOOM_BERRY =
            REGISTRATE
                    .item("gloom_berry", p -> new BlockItem(MiaBlocks.GLOOM_BERRY_PLANT.get(), p))
                    .tag(Tags.Items.FOODS_BERRY)
                    .compostable(0.3F)
                    .properties(
                            p ->
                                    p.food(
                                            new FoodProperties.Builder()
                                                    .nutrition(4)
                                                    .saturationModifier(0.3F)
                                                    .build()))
                    .register();

    public static final ItemEntry<BlockItem> DREAM_LICHEE =
            REGISTRATE
                    .item("dream_lichee", p -> new BlockItem(MiaBlocks.DREAM_LICHEE_PLANT.get(), p))
                    .tag(Tags.Items.FOODS_FRUIT)
                    .compostable(0.65F)
                    .properties(
                            p ->
                                    p.food(
                                            new FoodProperties.Builder()
                                                    .nutrition(2)
                                                    .saturationModifier(0.4F)
                                                    .build()))
                    .register();

    public static final ItemEntry<Item> MISTFUZZ_PEACH =
            REGISTRATE
                    .item("mistfuzz_peach", Item::new)
                    .tag(Tags.Items.FOODS_FRUIT)
                    .compostable(0.65F)
                    .properties(
                            p ->
                                    p.food(
                                            new FoodProperties.Builder()
                                                    .nutrition(3)
                                                    .saturationModifier(0.4F)
                                                    .build()))
                    .register();
    public static final ItemEntry<BlockItem> DUSKVEIL_BERRIES =
            REGISTRATE
                    .item("duskveil_berries", p -> new BlockItem(MiaBlocks.DUSKVEIL_VINES.get(), p))
                    .tag(Tags.Items.FOODS_BERRY)
                    .compostable(0.3F)
                    .properties(p -> p.food(Foods.GLOW_BERRIES))
                    .register();
    public static final ItemEntry<Item> DUSKVEIL_ICHOR_BOTTLE =
            REGISTRATE
                    .item("duskveil_ichor_bottle", Item::new)
                    .properties(p -> p.stacksTo(16))
                    .model(
                            () ->
                                    (ctx, prov) -> {
                                        var model =
                                                ModelTemplates.FLAT_ITEM.create(
                                                        ctx.get(),
                                                        TextureMapping.layer0(
                                                                new Material(
                                                                        prov.modLoc(
                                                                                "item/ichor_bottle"))),
                                                        prov.modelOutput);
                                        prov.createWithExistingModel(ctx.getEntry(), model);
                                    })
                    .register();

    public static final ItemEntry<Item> RAW_CHLOROPHYTE =
            REGISTRATE.item("raw_chlorophyte", Item::new).tag(Tags.Items.RAW_MATERIALS).register();
    public static final ItemEntry<Item> PRASIOLITE_SHARD =
            REGISTRATE.item("prasiolite_shard", Item::new).tag(Tags.Items.GEMS).register();
    public static final ItemEntry<Item> CAERULITE_SHARD =
            REGISTRATE.item("caerulite_shard", Item::new).tag(Tags.Items.GEMS).register();

    private static final Map<DyeColor, ItemEntry<BalloonItem>> BALLOONS =
            new EnumMap<>(DyeColor.class);

    public static final ItemEntry<BalloonItem> WHITE_BALLOON =
            REGISTRATE
                    .item("balloon_white", BalloonItem::new)
                    .lang("White Balloon")
                    .tag(MiaTags.Items.BALLOONS.tag)
                    .model(() -> ReturnStorageModels.balloon())
                    .register();

    static {
        BALLOONS.put(DyeColor.WHITE, WHITE_BALLOON);
        // Match the dye order in the vanilla Ingredients tab, after the white balloon above.
        for (DyeColor color :
                List.of(
                        DyeColor.LIGHT_GRAY,
                        DyeColor.GRAY,
                        DyeColor.BLACK,
                        DyeColor.BROWN,
                        DyeColor.RED,
                        DyeColor.ORANGE,
                        DyeColor.YELLOW,
                        DyeColor.LIME,
                        DyeColor.GREEN,
                        DyeColor.CYAN,
                        DyeColor.LIGHT_BLUE,
                        DyeColor.BLUE,
                        DyeColor.PURPLE,
                        DyeColor.MAGENTA,
                        DyeColor.PINK)) {
            BALLOONS.put(
                    color,
                    REGISTRATE
                            .item(
                                    "balloon_" + color.getName(),
                                    properties -> new BalloonItem(properties, color))
                            .tag(MiaTags.Items.BALLOONS.tag)
                            .register());
        }
    }

    public static final ItemEntry<RopeItem> ROPE =
            REGISTRATE
                    .item("rope", RopeItem::new)
                    .model(
                            () ->
                                    (context, provider) -> {
                                        var model =
                                                ModelTemplates.FLAT_ITEM.create(
                                                        context.get(),
                                                        TextureMapping.layer0(
                                                                new Material(
                                                                        provider.modLoc(
                                                                                "item/rope"))),
                                                        provider.modelOutput);
                                        provider.createWithExistingModel(context.getEntry(), model);
                                    })
                    .register();

    public static ItemEntry<BalloonItem> balloon(DyeColor color) {
        return BALLOONS.get(color);
    }

    public static void register(IEventBus bus) {
        bus.addListener(MiaItems::registerAliases);
    }

    private static void registerAliases(RegisterEvent event) {
        var items = event.getRegistry(Registries.ITEM);
        if (items != null) {
            items.addAlias(MementoInAbyss.asResource("balloon"), WHITE_BALLOON.getId());
        }
    }
}
