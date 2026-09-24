package com.altnoir.mementoinabyss.foundation.registrate;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.DataIngredient;
import java.util.ArrayList;
import java.util.function.BiFunction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

/** Shape-specific factories and data, independent of the family's base block and naming. */
public final class BuildingBlockVariant<B extends Block> {
    public static final BuildingBlockVariant<StairBlock> STAIRS =
            new BuildingBlockVariant<>(
                    "stairs",
                    false,
                    TagGen.STAIRS,
                    TagGen.WOODEN_STAIRS,
                    (family, name) ->
                            family.registrate()
                                    .block(
                                            name,
                                            p ->
                                                    new StairBlock(
                                                            family.base().get().defaultBlockState(),
                                                            p))
                                    .initialProperties(family.base())
                                    .blockstate(() -> BlockStateGen.stairs(family.base()))
                                    .recipe(
                                            (ctx, prov) ->
                                                    prov.stairs(
                                                            DataIngredient.items(
                                                                    family.base().get()),
                                                            RecipeCategory.BUILDING_BLOCKS,
                                                            ctx::get,
                                                            family.wooden()
                                                                    ? "wooden_stairs"
                                                                    : null,
                                                            !family.wooden())));

    public static final BuildingBlockVariant<SlabBlock> SLAB =
            new BuildingBlockVariant<>(
                    "slab",
                    false,
                    TagGen.SLABS,
                    TagGen.WOODEN_SLABS,
                    (family, name) ->
                            family.registrate()
                                    .block(name, SlabBlock::new)
                                    .initialProperties(family.base())
                                    .transform(BuilderTransformers.slab())
                                    .blockstate(() -> BlockStateGen.slab(family.base()))
                                    .recipe(
                                            (ctx, prov) ->
                                                    prov.slab(
                                                            DataIngredient.items(
                                                                    family.base().get()),
                                                            RecipeCategory.BUILDING_BLOCKS,
                                                            ctx::get,
                                                            family.wooden() ? "wooden_slab" : null,
                                                            !family.wooden())));

    public static final BuildingBlockVariant<WallBlock> WALL =
            new BuildingBlockVariant<>(
                    "wall",
                    false,
                    TagGen.WALLS,
                    null,
                    (family, name) ->
                            family.registrate()
                                    .block(name, WallBlock::new)
                                    .initialProperties(family.base())
                                    .blockstate(() -> BlockStateGen.wall(family.base()))
                                    .recipe(
                                            (ctx, prov) -> {
                                                if (family.wooden()) {
                                                    prov.wall(
                                                            RecipeCategory.DECORATIONS,
                                                            ctx.get(),
                                                            family.base().get());
                                                } else {
                                                    // Registrate's overload includes the
                                                    // stonecutting recipe.
                                                    prov.wall(
                                                            DataIngredient.items(
                                                                    family.base().get()),
                                                            RecipeCategory.DECORATIONS,
                                                            ctx::get);
                                                }
                                            }));

    public static final BuildingBlockVariant<FenceBlock> FENCE =
            new BuildingBlockVariant<>(
                    "fence",
                    false,
                    TagGen.FENCES,
                    TagGen.WOODEN_FENCES,
                    (family, name) ->
                            family.registrate()
                                    .block(name, p -> new FenceBlock(p.forceSolidOn()))
                                    .initialProperties(family.base())
                                    .blockstate(() -> BlockStateGen.fence(family.base()))
                                    .recipe(
                                            (ctx, prov) ->
                                                    prov.fence(
                                                            DataIngredient.items(
                                                                    family.base().get()),
                                                            RecipeCategory.DECORATIONS,
                                                            ctx::get,
                                                            family.wooden()
                                                                    ? "wooden_fence"
                                                                    : null)));

    public static final BuildingBlockVariant<FenceGateBlock> FENCE_GATE =
            new BuildingBlockVariant<>(
                    "fence_gate",
                    true,
                    null,
                    null,
                    (family, name) ->
                            family.registrate()
                                    .block(
                                            name,
                                            p ->
                                                    new FenceGateBlock(
                                                            family.requireWoodType(),
                                                            p.forceSolidOn()))
                                    .initialProperties(family.base())
                                    .tag(BlockTags.FENCE_GATES)
                                    .blockstate(() -> BlockStateGen.fenceGate(family.base()))
                                    .recipe(
                                            (ctx, prov) ->
                                                    prov.fenceGate(
                                                            DataIngredient.items(
                                                                    family.base().get()),
                                                            RecipeCategory.REDSTONE,
                                                            ctx::get,
                                                            "wooden_fence_gate")));

    public static final BuildingBlockVariant<DoorBlock> DOOR =
            new BuildingBlockVariant<>(
                    "door",
                    true,
                    TagGen.DOORS,
                    TagGen.WOODEN_DOORS,
                    (family, name) ->
                            family.registrate()
                                    .block(
                                            name,
                                            p ->
                                                    new DoorBlock(
                                                            family.requireWoodType().setType(), p))
                                    .properties(
                                            p ->
                                                    woodenDevice(p, family)
                                                            .strength(3.0F)
                                                            .noOcclusion()
                                                            .ignitedByLava()
                                                            .pushReaction(PushReaction.DESTROY))
                                    .blockstate(() -> (ctx, prov) -> prov.createDoor(ctx.get()))
                                    .loot(
                                            (loot, block) ->
                                                    loot.add(block, loot.createDoorTable(block)))
                                    .recipe(
                                            (ctx, prov) ->
                                                    prov.door(
                                                            DataIngredient.items(
                                                                    family.base().get()),
                                                            RecipeCategory.REDSTONE,
                                                            ctx::get,
                                                            "wooden_door")));

    public static final BuildingBlockVariant<TrapDoorBlock> TRAPDOOR =
            new BuildingBlockVariant<>(
                    "trapdoor",
                    true,
                    TagGen.TRAPDOORS,
                    TagGen.WOODEN_TRAPDOORS,
                    (family, name) ->
                            family.registrate()
                                    .block(
                                            name,
                                            p ->
                                                    new TrapDoorBlock(
                                                            family.requireWoodType().setType(), p))
                                    .properties(
                                            p ->
                                                    woodenDevice(p, family)
                                                            .strength(3.0F)
                                                            .noOcclusion()
                                                            .isValidSpawn(Blocks::never)
                                                            .ignitedByLava())
                                    .blockstate(
                                            () ->
                                                    (ctx, prov) ->
                                                            prov.createOrientableTrapdoor(
                                                                    ctx.get()))
                                    .recipe(
                                            (ctx, prov) ->
                                                    prov.trapDoor(
                                                            DataIngredient.items(
                                                                    family.base().get()),
                                                            RecipeCategory.REDSTONE,
                                                            ctx::get,
                                                            "wooden_trapdoor")));

    public static final BuildingBlockVariant<PressurePlateBlock> PRESSURE_PLATE =
            new BuildingBlockVariant<>(
                    "pressure_plate",
                    true,
                    null,
                    TagGen.WOODEN_PRESSURE_PLATES,
                    (family, name) ->
                            family.registrate()
                                    .block(
                                            name,
                                            p ->
                                                    new PressurePlateBlock(
                                                            family.requireWoodType().setType(), p))
                                    .properties(
                                            p ->
                                                    woodenDevice(p, family)
                                                            .forceSolidOn()
                                                            .noCollision()
                                                            .strength(0.5F)
                                                            .ignitedByLava()
                                                            .pushReaction(PushReaction.DESTROY))
                                    .tag(BlockTags.PRESSURE_PLATES)
                                    .blockstate(() -> BlockStateGen.pressurePlate(family.base()))
                                    .recipe(
                                            (ctx, prov) ->
                                                    prov.pressurePlate(
                                                            ctx.get(), family.base().get())));

    public static final BuildingBlockVariant<ButtonBlock> BUTTON =
            new BuildingBlockVariant<>(
                    "button",
                    true,
                    TagGen.BUTTONS,
                    TagGen.WOODEN_BUTTONS,
                    (family, name) ->
                            family.registrate()
                                    .block(
                                            name,
                                            p ->
                                                    new ButtonBlock(
                                                            family.requireWoodType().setType(),
                                                            30,
                                                            p))
                                    .properties(
                                            p ->
                                                    p.noCollision()
                                                            .strength(0.5F)
                                                            .pushReaction(PushReaction.DESTROY))
                                    .blockstate(() -> BlockStateGen.button(family.base()))
                                    .recipe(
                                            (ctx, prov) ->
                                                    prov.buttonBuilder(
                                                                    ctx.get(),
                                                                    Ingredient.of(
                                                                            family.base().get()))
                                                            .unlockedBy(
                                                                    "has_planks",
                                                                    prov.has(family.base().get()))
                                                            .save(prov)));

    private final String suffix;
    private final boolean woodOnly;
    private final @Nullable TagGen.BlockAndItemTag tag;
    private final @Nullable TagGen.BlockAndItemTag woodenTag;
    private final BiFunction<BuildingBlockFamily.Context, String, BlockBuilder<B, MiaRegistrate>>
            factory;

    private BuildingBlockVariant(
            String suffix,
            boolean woodOnly,
            @Nullable TagGen.BlockAndItemTag tag,
            @Nullable TagGen.BlockAndItemTag woodenTag,
            BiFunction<BuildingBlockFamily.Context, String, BlockBuilder<B, MiaRegistrate>>
                    factory) {
        this.suffix = suffix;
        this.woodOnly = woodOnly;
        this.tag = tag;
        this.woodenTag = woodenTag;
        this.factory = factory;
    }

    public String suffix() {
        return suffix;
    }

    void validate(BuildingBlockFamily.Context context) {
        if (woodOnly && !context.wooden()) {
            throw new IllegalArgumentException(
                    suffix + " requires a wooden family: " + context.prefix());
        }
    }

    BlockBuilder<B, MiaRegistrate> create(BuildingBlockFamily.Context context, String name) {
        var builder = factory.apply(context, name);
        return context.wooden()
                ? builder.transform(BuilderTransformers.wooden())
                : builder.transform(BuilderTransformers.stone());
    }

    BlockBuilder<B, MiaRegistrate> withItem(
            BlockBuilder<B, MiaRegistrate> builder, BuildingBlockFamily.Context context) {
        var tags = new ArrayList<TagGen.BlockAndItemTag>();
        if (tag != null) tags.add(tag);
        if (context.wooden() && woodenTag != null) tags.add(woodenTag);
        return builder.transform(
                        TagGen.tagBlockAndItem(tags.toArray(TagGen.BlockAndItemTag[]::new)))
                .build();
    }

    private static BlockBehaviour.Properties woodenDevice(
            BlockBehaviour.Properties p, BuildingBlockFamily.Context family) {
        return p.mapColor(family.base().get().defaultMapColor())
                .instrument(NoteBlockInstrument.BASS);
    }
}
