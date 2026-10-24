package com.altnoir.mementoinabyss.foundation.registrate;

import com.altnoir.abysslib.reginth.Reginth;
import com.altnoir.abysslib.reginth.builders.BlockBuilder;
import com.altnoir.abysslib.reginth.util.DataIngredient;
import com.altnoir.abysslib.reginth.util.entry.BlockEntry;
import com.altnoir.mementoinabyss.content.abyss.pillar.StrippedRotatedPillarBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.Tags;

/**
 * Shared specification for logs/stems, bark blocks, planks and wooden building variants.
 * Registration is deliberately staged so callers can retain their creative-tab ordering.
 * Leaves and saplings are not building variants and keep their own behaviour and data.
 */
public record WoodBlockFamily(
        String name,
        String logSuffix,
        String woodSuffix,
        PillarProperties logProperties,
        PillarProperties strippedLogProperties,
        MapColor planksColor,
        WoodType woodType,
        boolean burns,
        TagKey<Item> logsTag) {

    public Logs registerLogs(Reginth registrate) {
        var strippedLog =
                finishLog(
                        registrate
                                .block(
                                        "stripped_" + name + "_" + logSuffix,
                                        RotatedPillarBlock::new)
                                .properties(strippedLogProperties::apply)
                                .tag(Tags.Blocks.STRIPPED_LOGS)
                                .blockstate(() -> (ctx, prov) -> prov.generateLogBlock(ctx.get())));
        var strippedWood =
                finishLog(
                        registrate
                                .block(
                                        "stripped_" + name + "_" + woodSuffix,
                                        RotatedPillarBlock::new)
                                .initialProperties(strippedLog)
                                .tag(Tags.Blocks.STRIPPED_WOODS)
                                .blockstate(
                                        () ->
                                                (ctx, prov) ->
                                                        prov.woodProvider(strippedLog.get())
                                                                .wood(ctx.get()))
                                .recipe(
                                        (ctx, prov) ->
                                                prov.woodFromLogs(ctx.get(), strippedLog.get())));
        var log =
                finishLog(
                        registrate
                                .block(
                                        name + "_" + logSuffix,
                                        p -> new StrippedRotatedPillarBlock(strippedLog.get(), p))
                                .properties(logProperties::apply)
                                .tag(Tags.Blocks.NATURAL_LOGS)
                                .blockstate(() -> (ctx, prov) -> prov.generateLogBlock(ctx.get())));
        var wood =
                finishLog(
                        registrate
                                .block(
                                        name + "_" + woodSuffix,
                                        p -> new StrippedRotatedPillarBlock(strippedWood.get(), p))
                                .initialProperties(log)
                                .tag(Tags.Blocks.NATURAL_WOODS)
                                .blockstate(
                                        () ->
                                                (ctx, prov) ->
                                                        prov.woodProvider(log.get())
                                                                .wood(ctx.get()))
                                .recipe((ctx, prov) -> prov.woodFromLogs(ctx.get(), log.get())));
        return new Logs(strippedLog, strippedWood, log, wood);
    }

    private <B extends RotatedPillarBlock> BlockEntry<B> finishLog(
            BlockBuilder<B, Reginth> builder) {
        builder.tag(BlockTags.LOGS);
        if (burns) builder.tag(BlockTags.LOGS_THAT_BURN);
        // Vanilla log item tags are copied from block tags in MiaRegistrateTags.
        var item = builder.item().tag(logsTag);
        if (!burns) item.tag(ItemTags.NON_FLAMMABLE_WOOD);
        return item.build().register();
    }

    public BlockEntry<Block> registerPlanks(Reginth registrate) {
        var item =
                registrate
                        .block(name + "_planks", Block::new)
                        .properties(
                                p ->
                                        p.mapColor(planksColor)
                                                .instrument(NoteBlockInstrument.BASS)
                                                .strength(2.0F, 3.0F)
                                                .sound(SoundType.WOOD)
                                                .ignitedByLava())
                        .transform(BuilderTransformers.wooden())
                        .recipe(
                                (ctx, prov) ->
                                        prov.planks(
                                                DataIngredient.ingredient(
                                                        prov.tag(logsTag), logsTag),
                                                RecipeCategory.BUILDING_BLOCKS,
                                                ctx::get))
                        .transform(TagGen.tagBlockAndItem(BlockTags.PLANKS, ItemTags.PLANKS));
        if (!burns) item.tag(ItemTags.NON_FLAMMABLE_WOOD);
        return item.build().register();
    }

    public BuildingBlockFamily.Builder buildingBlocks(
            Reginth registrate, BlockEntry<Block> planks) {
        return BuildingBlockFamily.wooden(registrate, name, planks, woodType, burns);
    }

    public record Logs(
            BlockEntry<RotatedPillarBlock> strippedLog,
            BlockEntry<RotatedPillarBlock> strippedWood,
            BlockEntry<StrippedRotatedPillarBlock> log,
            BlockEntry<StrippedRotatedPillarBlock> wood) {}

    public record PillarProperties(MapColor top, MapColor side, SoundType sound) {
        BlockBehaviour.Properties apply(BlockBehaviour.Properties p) {
            // Keep the existing ignition behaviour independent of the fuel/log tag.
            return p.mapColor(
                            state ->
                                    state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y
                                            ? top
                                            : side)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)
                    .sound(sound)
                    .ignitedByLava();
        }
    }
}
