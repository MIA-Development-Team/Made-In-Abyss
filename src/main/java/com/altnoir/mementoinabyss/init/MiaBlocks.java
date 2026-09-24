package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.abyss.andesite.AbyssAndesiteBlock;
import com.altnoir.mementoinabyss.content.abyss.column.ColumnBlock;
import com.altnoir.mementoinabyss.content.abyss.cover_grass.CoverGrassBlock;
import com.altnoir.mementoinabyss.content.abyss.farmland.HopperFarmlandBlock;
import com.altnoir.mementoinabyss.content.abyss.ore.BuddingCaeruliteBlock;
import com.altnoir.mementoinabyss.content.abyss.ore.BuddingPrasioliteBlock;
import com.altnoir.mementoinabyss.content.abyss.ore.ChlorophyteOreBlock;
import com.altnoir.mementoinabyss.content.abyss.pillar.StrippedRotatedPillarBlock;
import com.altnoir.mementoinabyss.content.abyss.plant.DreamLicheeBlock;
import com.altnoir.mementoinabyss.content.abyss.plant.FruitingSkyfogLeavesBlock;
import com.altnoir.mementoinabyss.content.abyss.plant.GloomBerryBlock;
import com.altnoir.mementoinabyss.content.abyss.plant.GreenParticleLeavesBlock;
import com.altnoir.mementoinabyss.content.abyss.plant.InvertedSaplingBlock;
import com.altnoir.mementoinabyss.content.abyss.plant.MiaFungusBlock;
import com.altnoir.mementoinabyss.content.abyss.plant.WaterTallFlowerBlock;
import com.altnoir.mementoinabyss.content.artifact.ArtifactSmithingTableBlock;
import com.altnoir.mementoinabyss.content.beacon.CaveExplorerBeaconBlock;
import com.altnoir.mementoinabyss.content.cup.EndlessCupBlock;
import com.altnoir.mementoinabyss.content.pedestal.PedestalBlock;
import com.altnoir.mementoinabyss.content.portal.AbyssPortalBlock;
import com.altnoir.mementoinabyss.content.portal.AbyssPortalCoreBlock;
import com.altnoir.mementoinabyss.content.rope.RopeConnectorBlock;
import com.altnoir.mementoinabyss.content.whistle.WhistleWorkbenchBlock;
import com.altnoir.mementoinabyss.foundation.registrate.BlockStateGen;
import com.altnoir.mementoinabyss.foundation.registrate.BuilderTransformers;
import com.altnoir.mementoinabyss.foundation.registrate.BuildingBlockFamily;
import com.altnoir.mementoinabyss.foundation.registrate.BuildingBlockVariant;
import com.altnoir.mementoinabyss.foundation.registrate.LootGen;
import com.altnoir.mementoinabyss.foundation.registrate.MiaRegistrate;
import com.altnoir.mementoinabyss.foundation.registrate.TagGen;
import com.altnoir.mementoinabyss.foundation.registrate.WoodBlockFamily;
import com.altnoir.mementoinabyss.foundation.registrate.WoodBlockFamily.PillarProperties;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.MiaTreeFeatures;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.MiaTreeGrowers;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.common.Tags;

public class MiaBlocks {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    static {
        REGISTRATE.defaultCreativeSection(MiaItemGroups.FUNCTIONAL_BLOCKS);
    }

    public static final BlockEntry<AbyssPortalBlock> ABYSS_PORTAL =
            REGISTRATE
                    .object("abyss_portal")
                    .block(AbyssPortalBlock::new)
                    .ignore()
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_BLACK)
                                            .noCollision()
                                            .noOcclusion()
                                            .lightLevel(_ -> 15)
                                            .strength(-1.0F, 3_600_000.0F)
                                            .noLootTable()
                                            .pushReaction(PushReaction.BLOCK))
                    .blockstate(BlockStateGen::abyssPortal)
                    .simpleItem()
                    .register();

    public static final BlockEntry<AbyssPortalCoreBlock> ABYSS_PORTAL_CORE =
            REGISTRATE
                    .object("abyss_portal_core")
                    .block(AbyssPortalCoreBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.STONE)
                                            .strength(-1.0F, 3_600_000.0F)
                                            .sound(SoundType.NETHERITE_BLOCK)
                                            .noLootTable()
                                            .pushReaction(PushReaction.BLOCK))
                    .blockstate(BlockStateGen::abyssPortalCore)
                    .simpleItem()
                    .register();

    public static final BlockEntry<Block> ABYSS_PORTAL_FRAME =
            REGISTRATE
                    .object("abyss_portal_frame")
                    .block(Block::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.STONE)
                                            .requiresCorrectToolForDrops()
                                            .strength(100.0F, 1200.0F)
                                            .sound(SoundType.NETHERITE_BLOCK))
                    .transform(TagGen.pickaxeOnly())
                    .simpleItem()
                    .register();

    public static final BlockEntry<PedestalBlock> PEDESTAL =
            REGISTRATE
                    .object("pedestal")
                    .block(PedestalBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.STONE)
                                            .strength(2.5F, 6.0F)
                                            .sound(SoundType.DEEPSLATE)
                                            .noOcclusion())
                    .transform(TagGen.pickaxeOnly())
                    .blockstate(BlockStateGen::pedestal)
                    .simpleItem()
                    .register();

    public static final BlockEntry<EndlessCupBlock> ENDLESS_CUP =
            REGISTRATE
                    .object("endless_cup")
                    .block(EndlessCupBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.STONE)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .strength(3.0F, 6.0F)
                                            .sound(SoundType.STONE)
                                            .noOcclusion())
                    .transform(TagGen.pickaxeOnly())
                    .blockstate(BlockStateGen::endlessCup)
                    .lang("Endless Cup")
                    .simpleItem()
                    .register();

    public static final BlockEntry<CaveExplorerBeaconBlock> CAVE_EXPLORER_BEACON =
            REGISTRATE
                    .object("cave_explorer_beacon")
                    .block(CaveExplorerBeaconBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.EMERALD)
                                            .instrument(NoteBlockInstrument.HAT)
                                            .strength(3.0F, 6.0F)
                                            .lightLevel(_ -> 15)
                                            .noOcclusion()
                                            .isRedstoneConductor((state, level, pos) -> false))
                    .blockstate(BlockStateGen::caveExplorerBeacon)
                    .lang("Cave Explorer Beacon")
                    .simpleItem()
                    .register();

    public static final BlockEntry<ArtifactSmithingTableBlock> ARTIFACT_SMITHING_TABLE =
            REGISTRATE
                    .object("artifact_smithing_table")
                    .block(ArtifactSmithingTableBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.STONE)
                                            .requiresCorrectToolForDrops()
                                            .strength(3.0F, 6.0F)
                                            .sound(SoundType.NETHERITE_BLOCK))
                    .transform(TagGen.pickaxeOnly())
                    .blockstate(BlockStateGen::artifactSmithingTable)
                    .simpleItem()
                    .register();

    public static final BlockEntry<WhistleWorkbenchBlock> WHISTLE_WORKBENCH =
            REGISTRATE
                    .object("whistle_workbench")
                    .block(WhistleWorkbenchBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.STONE)
                                            .requiresCorrectToolForDrops()
                                            .strength(3.0F, 6.0F)
                                            .sound(SoundType.WOOD))
                    .transform(TagGen.pickaxeOnly())
                    .blockstate(BlockStateGen::whistleWorkbench)
                    .simpleItem()
                    .register();

    public static final BlockEntry<RopeConnectorBlock> ROPE_CONNECTOR =
            REGISTRATE
                    .object("rope_connector")
                    .block(RopeConnectorBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.METAL)
                                            .requiresCorrectToolForDrops()
                                            .strength(3.0F, 6.0F)
                                            .sound(SoundType.CHAIN)
                                            .noOcclusion())
                    .transform(TagGen.pickaxeOnly())
                    .blockstate(BlockStateGen::ropeConnector)
                    .simpleItem()
                    .register();

    static {
        REGISTRATE.defaultCreativeSection(MiaItemGroups.BASE_BUILDING_BLOCKS);
    }

    public static final BlockEntry<AbyssAndesiteBlock> ABYSS_ANDESITE =
            REGISTRATE
                    .object("abyss_andesite")
                    .block(AbyssAndesiteBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.STONE)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .strength(3.0F, 6.0F)
                                            .sound(SoundType.DEEPSLATE))
                    .transform(TagGen.pickaxeOnly())
                    .tag(
                            MiaTags.BlockTags.BASE_STONE_ABYSS.tag,
                            MiaTags.BlockTags.ABYSS_ANDESITE_ORE_REPLACEABLE.tag)
                    .simpleItem()
                    .register();

    private static final BuildingBlockFamily ABYSS_ANDESITE_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, ABYSS_ANDESITE).register();
    public static final BlockEntry<StairBlock> ABYSS_ANDESITE_STAIRS =
            ABYSS_ANDESITE_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> ABYSS_ANDESITE_SLAB =
            ABYSS_ANDESITE_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> ABYSS_ANDESITE_WALL =
            ABYSS_ANDESITE_FAMILY.get(BuildingBlockVariant.WALL);

    public static final BlockEntry<Block> ABYSS_COBBLED_ANDESITE =
            REGISTRATE
                    .object("abyss_cobbled_andesite")
                    .block(Block::new)
                    .initialProperties(ABYSS_ANDESITE)
                    .properties(p -> p.strength(3.5F, 6.0F))
                    .transform(TagGen.pickaxeOnly())
                    .simpleItem()
                    .register();

    static {
        REGISTRATE.defaultCreativeSection(MiaItemGroups.FUNCTIONAL_BLOCKS);
    }

    public static final BlockEntry<HopperFarmlandBlock> HOPPER_FARMLAND =
            REGISTRATE
                    .object("hopper_farmland")
                    .block(HopperFarmlandBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.DIRT)
                                            .randomTicks()
                                            .strength(0.6F)
                                            .sound(SoundType.DEEPSLATE)
                                            .isViewBlocking((state, level, pos) -> true)
                                            .isSuffocating((state, level, pos) -> true))
                    .transform(TagGen.pickaxeOnly())
                    .tag(Tags.Blocks.VILLAGER_FARMLANDS)
                    .blockstate(BlockStateGen::hopperFarmland)
                    .loot(
                            (lt, b) ->
                                    lt.add(
                                            b,
                                            lt.createSingleItemTable(ABYSS_COBBLED_ANDESITE.get())))
                    .lang("Hopper Farmland")
                    .simpleItem()
                    .register();

    static {
        REGISTRATE.defaultCreativeSection(MiaItemGroups.BASE_BUILDING_BLOCKS);
    }

    private static final BuildingBlockFamily ABYSS_COBBLED_ANDESITE_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, ABYSS_COBBLED_ANDESITE).register();
    public static final BlockEntry<StairBlock> ABYSS_COBBLED_ANDESITE_STAIRS =
            ABYSS_COBBLED_ANDESITE_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> ABYSS_COBBLED_ANDESITE_SLAB =
            ABYSS_COBBLED_ANDESITE_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> ABYSS_COBBLED_ANDESITE_WALL =
            ABYSS_COBBLED_ANDESITE_FAMILY.get(BuildingBlockVariant.WALL);

    public static final BlockEntry<Block> MOSSY_ABYSS_COBBLED_ANDESITE =
            REGISTRATE
                    .object("mossy_abyss_cobbled_andesite")
                    .block(Block::new)
                    .initialProperties(ABYSS_COBBLED_ANDESITE)
                    .transform(BuilderTransformers.stone())
                    .simpleItem()
                    .register();

    private static final BuildingBlockFamily MOSSY_ABYSS_COBBLED_ANDESITE_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, MOSSY_ABYSS_COBBLED_ANDESITE).register();
    public static final BlockEntry<StairBlock> MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS =
            MOSSY_ABYSS_COBBLED_ANDESITE_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> MOSSY_ABYSS_COBBLED_ANDESITE_SLAB =
            MOSSY_ABYSS_COBBLED_ANDESITE_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> MOSSY_ABYSS_COBBLED_ANDESITE_WALL =
            MOSSY_ABYSS_COBBLED_ANDESITE_FAMILY.get(BuildingBlockVariant.WALL);

    public static final BlockEntry<Block> POLISHED_ABYSS_ANDESITE =
            REGISTRATE
                    .object("polished_abyss_andesite")
                    .block(Block::new)
                    .initialProperties(ABYSS_COBBLED_ANDESITE)
                    .properties(p -> p.sound(SoundType.POLISHED_DEEPSLATE))
                    .transform(BuilderTransformers.stone())
                    .simpleItem()
                    .register();

    private static final BuildingBlockFamily POLISHED_ABYSS_ANDESITE_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, POLISHED_ABYSS_ANDESITE).register();
    public static final BlockEntry<StairBlock> POLISHED_ABYSS_ANDESITE_STAIRS =
            POLISHED_ABYSS_ANDESITE_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> POLISHED_ABYSS_ANDESITE_SLAB =
            POLISHED_ABYSS_ANDESITE_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> POLISHED_ABYSS_ANDESITE_WALL =
            POLISHED_ABYSS_ANDESITE_FAMILY.get(BuildingBlockVariant.WALL);

    public static final BlockEntry<RotatedPillarBlock> ABYSS_ANDESITE_PILLAR =
            REGISTRATE
                    .object("abyss_andesite_pillar")
                    .block(RotatedPillarBlock::new)
                    .initialProperties(POLISHED_ABYSS_ANDESITE)
                    .transform(TagGen.pickaxeOnly())
                    .blockstate(() -> (ctx, prov) -> prov.generateLogBlock(ctx.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<Block> CHISLED_ABYSS_ANDESITE =
            REGISTRATE
                    .object("chiseled_abyss_andesite")
                    .block(Block::new)
                    .initialProperties(POLISHED_ABYSS_ANDESITE)
                    .transform(TagGen.pickaxeOnly())
                    .simpleItem()
                    .register();

    public static final BlockEntry<Block> ABYSS_ANDESITE_BRICKS =
            REGISTRATE
                    .object("abyss_andesite_bricks")
                    .block(Block::new)
                    .initialProperties(ABYSS_COBBLED_ANDESITE)
                    .properties(p -> p.sound(SoundType.DEEPSLATE_TILES))
                    .transform(TagGen.pickaxeOnly())
                    .simpleItem()
                    .register();

    public static final BlockEntry<ColumnBlock> ABYSS_ANDESITE_COLUMN =
            REGISTRATE
                    .object("abyss_andesite_column")
                    .block(ColumnBlock::new)
                    .initialProperties(POLISHED_ABYSS_ANDESITE)
                    .transform(TagGen.pickaxeOnly())
                    .blockstate(
                            () ->
                                    BlockStateGen.column(
                                            ABYSS_ANDESITE_PILLAR, ABYSS_ANDESITE_BRICKS))
                    .simpleItem()
                    .register();

    public static final BlockEntry<Block> CRACKED_ABYSS_ANDESITE_BRICKS =
            REGISTRATE
                    .object("cracked_abyss_andesite_bricks")
                    .block(Block::new)
                    .initialProperties(ABYSS_ANDESITE_BRICKS)
                    .transform(TagGen.pickaxeOnly())
                    .simpleItem()
                    .register();

    private static final BuildingBlockFamily ABYSS_ANDESITE_BRICKS_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, ABYSS_ANDESITE_BRICKS).register();
    public static final BlockEntry<StairBlock> ABYSS_ANDESITE_BRICKS_STAIRS =
            ABYSS_ANDESITE_BRICKS_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> ABYSS_ANDESITE_BRICKS_SLAB =
            ABYSS_ANDESITE_BRICKS_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> ABYSS_ANDESITE_BRICKS_WALL =
            ABYSS_ANDESITE_BRICKS_FAMILY.get(BuildingBlockVariant.WALL);

    public static final BlockEntry<Block> MOSSY_ABYSS_ANDESITE_BRICKS =
            REGISTRATE
                    .object("mossy_abyss_andesite_bricks")
                    .block(Block::new)
                    .initialProperties(ABYSS_ANDESITE_BRICKS)
                    .transform(TagGen.pickaxeOnly())
                    .simpleItem()
                    .register();

    private static final BuildingBlockFamily MOSSY_ABYSS_ANDESITE_BRICKS_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, MOSSY_ABYSS_ANDESITE_BRICKS).register();
    public static final BlockEntry<StairBlock> MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS =
            MOSSY_ABYSS_ANDESITE_BRICKS_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> MOSSY_ABYSS_ANDESITE_BRICKS_SLAB =
            MOSSY_ABYSS_ANDESITE_BRICKS_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> MOSSY_ABYSS_ANDESITE_BRICKS_WALL =
            MOSSY_ABYSS_ANDESITE_BRICKS_FAMILY.get(BuildingBlockVariant.WALL);

    public static final BlockEntry<CoverGrassBlock> COVERGRASS_ABYSS_ANDESITE =
            REGISTRATE
                    .object("covergrass_abyss_andesite")
                    .block(p -> new CoverGrassBlock(ABYSS_ANDESITE.get(), p))
                    .initialProperties(ABYSS_ANDESITE)
                    .properties(p -> p.mapColor(MapColor.GRASS).randomTicks())
                    .blockstate(BlockStateGen::coverGrass)
                    .tag(MiaTags.BlockTags.COVERGRASS.tag, BlockTags.DIRT)
                    .tag(MiaTags.BlockTags.BASE_STONE_ABYSS.tag)
                    .transform(TagGen.pickaxeOnly())
                    .loot(LootGen.silkTouchOr(ABYSS_ANDESITE))
                    .simpleItem()
                    .register();

    public static final BlockEntry<CoverGrassBlock> COVERGRASS_TUFF =
            REGISTRATE
                    .object("covergrass_tuff")
                    .block(p -> new CoverGrassBlock(Blocks.TUFF, p))
                    .initialProperties(() -> Blocks.TUFF)
                    .properties(p -> p.randomTicks())
                    .blockstate(BlockStateGen::coverGrass)
                    .tag(MiaTags.BlockTags.COVERGRASS.tag, BlockTags.DIRT)
                    .transform(TagGen.pickaxeOnly())
                    .loot(LootGen.silkTouchOr(() -> Blocks.TUFF))
                    .simpleItem()
                    .register();

    public static final BlockEntry<BrushableBlock> SUSPICIOUS_ABYSS_ANDESITE =
            REGISTRATE
                    .object("suspicious_abyss_andesite")
                    .block(
                            p ->
                                    new BrushableBlock(
                                            ABYSS_ANDESITE.get(),
                                            SoundEvents.BRUSH_SAND,
                                            SoundEvents.BRUSH_SAND_COMPLETED,
                                            p))
                    .initialProperties(ABYSS_ANDESITE)
                    .properties(
                            p ->
                                    p.instrument(NoteBlockInstrument.SNARE)
                                            .sound(SoundType.SUSPICIOUS_GRAVEL)
                                            .pushReaction(PushReaction.DESTROY)
                                            .noLootTable())
                    .transform(TagGen.pickaxeOnly())
                    .blockstate(BlockStateGen::suspiciousAbyssAndesite)
                    .item()
                    .model(() -> (ctx, prov) -> prov.generateBlockItem(ctx.get(), "_0"))
                    .build()
                    .register();

    public static final BlockEntry<AbyssAndesiteBlock> MARLITH =
            REGISTRATE
                    .object("marlith")
                    .block(AbyssAndesiteBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.TERRACOTTA_WHITE)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .strength(3.5F, 8.0F)
                                            .sound(SoundType.CALCITE))
                    .transform(BuilderTransformers.stone())
                    .simpleItem()
                    .register();

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_FOSSILIZED_LOG =
            REGISTRATE
                    .object("stripped_fossilized_log")
                    .block(RotatedPillarBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(
                                                    state ->
                                                            state.getValue(RotatedPillarBlock.AXIS)
                                                                            == Direction.Axis.Y
                                                                    ? MapColor.COLOR_BLACK
                                                                    : MapColor.PODZOL)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .strength(2.0F, 4.2F)
                                            .sound(SoundType.BASALT))
                    .transform(BuilderTransformers.stone())
                    .blockstate(() -> BlockStateGen.variantLog(12, 1, 1, 1, 1))
                    .simpleItem()
                    .register();

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_FOSSILIZED_WOOD =
            REGISTRATE
                    .object("stripped_fossilized_wood")
                    .block(RotatedPillarBlock::new)
                    .initialProperties(STRIPPED_FOSSILIZED_LOG)
                    .properties(p -> p.mapColor(MapColor.PODZOL).strength(3.0F, 4.2F))
                    .transform(BuilderTransformers.stone())
                    .blockstate(
                            () ->
                                    BlockStateGen.variantWood(
                                            STRIPPED_FOSSILIZED_LOG, 12, 1, 1, 1, 1))
                    .simpleItem()
                    .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> FOSSILIZED_LOG =
            REGISTRATE
                    .object("fossilized_log")
                    .block(p -> new StrippedRotatedPillarBlock(STRIPPED_FOSSILIZED_LOG.get(), p))
                    .initialProperties(STRIPPED_FOSSILIZED_LOG)
                    .transform(BuilderTransformers.stone())
                    .blockstate(() -> BlockStateGen.variantLog(12, 1, 1))
                    .simpleItem()
                    .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> FOSSILIZED_WOOD =
            REGISTRATE
                    .object("fossilized_wood")
                    .block(p -> new StrippedRotatedPillarBlock(STRIPPED_FOSSILIZED_WOOD.get(), p))
                    .initialProperties(STRIPPED_FOSSILIZED_WOOD)
                    .transform(BuilderTransformers.stone())
                    .blockstate(() -> BlockStateGen.variantWood(FOSSILIZED_LOG, 12, 1, 1))
                    .simpleItem()
                    .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_FOSSILIZED_LOG =
            REGISTRATE
                    .object("mossy_fossilized_log")
                    .block(p -> new StrippedRotatedPillarBlock(FOSSILIZED_LOG.get(), p))
                    .initialProperties(FOSSILIZED_LOG)
                    .transform(BuilderTransformers.stone())
                    .blockstate(() -> BlockStateGen.variantLog(12, 1, 1, 1))
                    .simpleItem()
                    .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_FOSSILIZED_WOOD =
            REGISTRATE
                    .object("mossy_fossilized_wood")
                    .block(p -> new StrippedRotatedPillarBlock(FOSSILIZED_WOOD.get(), p))
                    .initialProperties(FOSSILIZED_WOOD)
                    .transform(BuilderTransformers.stone())
                    .blockstate(() -> BlockStateGen.variantWood(MOSSY_FOSSILIZED_LOG, 12, 1, 1, 1))
                    .simpleItem()
                    .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_STRIPPED_FOSSILIZED_LOG =
            REGISTRATE
                    .object("mossy_stripped_fossilized_log")
                    .block(p -> new StrippedRotatedPillarBlock(FOSSILIZED_WOOD.get(), p))
                    .initialProperties(STRIPPED_FOSSILIZED_LOG)
                    .transform(BuilderTransformers.stone())
                    .blockstate(() -> BlockStateGen.variantLog(12, 1, 1, 1, 1))
                    .simpleItem()
                    .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD =
            REGISTRATE
                    .object("mossy_stripped_fossilized_wood")
                    .block(p -> new StrippedRotatedPillarBlock(FOSSILIZED_WOOD.get(), p))
                    .initialProperties(STRIPPED_FOSSILIZED_WOOD)
                    .transform(BuilderTransformers.stone())
                    .blockstate(
                            () ->
                                    BlockStateGen.variantWood(
                                            MOSSY_STRIPPED_FOSSILIZED_LOG, 12, 1, 1, 1, 1))
                    .simpleItem()
                    .register();

    public static final BlockEntry<Block> POLISHED_FOSSILIZED_WOOD =
            REGISTRATE
                    .object("polished_fossilized_wood")
                    .block(Block::new)
                    .initialProperties(FOSSILIZED_WOOD)
                    .transform(BuilderTransformers.stone())
                    .simpleItem()
                    .register();

    private static final BuildingBlockFamily POLISHED_FOSSILIZED_WOOD_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, POLISHED_FOSSILIZED_WOOD).register();
    public static final BlockEntry<StairBlock> POLISHED_FOSSILIZED_WOOD_STAIRS =
            POLISHED_FOSSILIZED_WOOD_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> POLISHED_FOSSILIZED_WOOD_SLAB =
            POLISHED_FOSSILIZED_WOOD_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> POLISHED_FOSSILIZED_WOOD_WALL =
            POLISHED_FOSSILIZED_WOOD_FAMILY.get(BuildingBlockVariant.WALL);

    public static final BlockEntry<Block> POLISHED_STRIPPED_FOSSILIZED_WOOD =
            REGISTRATE
                    .object("polished_stripped_fossilized_wood")
                    .block(Block::new)
                    .initialProperties(STRIPPED_FOSSILIZED_WOOD)
                    .transform(BuilderTransformers.stone())
                    .simpleItem()
                    .register();

    private static final BuildingBlockFamily POLISHED_STRIPPED_FOSSILIZED_WOOD_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, POLISHED_STRIPPED_FOSSILIZED_WOOD).register();
    public static final BlockEntry<StairBlock> POLISHED_STRIPPED_FOSSILIZED_WOOD_STAIRS =
            POLISHED_STRIPPED_FOSSILIZED_WOOD_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> POLISHED_STRIPPED_FOSSILIZED_WOOD_SLAB =
            POLISHED_STRIPPED_FOSSILIZED_WOOD_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> POLISHED_STRIPPED_FOSSILIZED_WOOD_WALL =
            POLISHED_STRIPPED_FOSSILIZED_WOOD_FAMILY.get(BuildingBlockVariant.WALL);

    public static final BlockEntry<Block> CHISLED_STRIPPED_FOSSILIZED_WOOD =
            REGISTRATE
                    .object("chiseled_stripped_fossilized_wood")
                    .block(Block::new)
                    .initialProperties(STRIPPED_FOSSILIZED_WOOD)
                    .transform(BuilderTransformers.stone())
                    .simpleItem()
                    .register();

    public static final BlockEntry<Block> FOSSILIZED_WOOD_BRICKS =
            REGISTRATE
                    .object("fossilized_wood_bricks")
                    .block(Block::new)
                    .initialProperties(FOSSILIZED_WOOD)
                    .transform(BuilderTransformers.stone())
                    .simpleItem()
                    .register();

    private static final BuildingBlockFamily FOSSILIZED_WOOD_BRICKS_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, FOSSILIZED_WOOD_BRICKS).register();
    public static final BlockEntry<StairBlock> FOSSILIZED_WOOD_BRICKS_STAIRS =
            FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> FOSSILIZED_WOOD_BRICKS_SLAB =
            FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> FOSSILIZED_WOOD_BRICKS_WALL =
            FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.WALL);

    public static final BlockEntry<Block> STRIPPED_FOSSILIZED_WOOD_BRICKS =
            REGISTRATE
                    .object("stripped_fossilized_wood_bricks")
                    .block(Block::new)
                    .initialProperties(STRIPPED_FOSSILIZED_WOOD)
                    .transform(BuilderTransformers.stone())
                    .simpleItem()
                    .register();

    private static final BuildingBlockFamily STRIPPED_FOSSILIZED_WOOD_BRICKS_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, STRIPPED_FOSSILIZED_WOOD_BRICKS).register();
    public static final BlockEntry<StairBlock> STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS =
            STRIPPED_FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB =
            STRIPPED_FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL =
            STRIPPED_FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.WALL);

    public static final BlockEntry<Block> MOSSY_FOSSILIZED_WOOD_BRICKS =
            REGISTRATE
                    .object("mossy_fossilized_wood_bricks")
                    .block(Block::new)
                    .initialProperties(FOSSILIZED_WOOD)
                    .transform(BuilderTransformers.stone())
                    .simpleItem()
                    .register();

    private static final BuildingBlockFamily MOSSY_FOSSILIZED_WOOD_BRICKS_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, MOSSY_FOSSILIZED_WOOD_BRICKS).register();
    public static final BlockEntry<StairBlock> MOSSY_FOSSILIZED_WOOD_BRICKS_STAIRS =
            MOSSY_FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> MOSSY_FOSSILIZED_WOOD_BRICKS_SLAB =
            MOSSY_FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> MOSSY_FOSSILIZED_WOOD_BRICKS_WALL =
            MOSSY_FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.WALL);

    public static final BlockEntry<Block> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS =
            REGISTRATE
                    .object("mossy_stripped_fossilized_wood_bricks")
                    .block(Block::new)
                    .initialProperties(FOSSILIZED_WOOD)
                    .transform(BuilderTransformers.stone())
                    .simpleItem()
                    .register();

    private static final BuildingBlockFamily MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_FAMILY =
            BuildingBlockFamily.stone(REGISTRATE, MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS).register();
    public static final BlockEntry<StairBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS =
            MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB =
            MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<WallBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL =
            MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_FAMILY.get(BuildingBlockVariant.WALL);

    private static final WoodBlockFamily SKYFOG_FAMILY =
            new WoodBlockFamily(
                    "skyfog",
                    "log",
                    "wood",
                    new PillarProperties(MapColor.WOOD, MapColor.PODZOL, SoundType.WOOD),
                    new PillarProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD),
                    MapColor.COLOR_GREEN,
                    WoodType.BAMBOO,
                    true,
                    MiaTags.ItemTags.SKYFOG_LOGS.tag);
    private static final WoodBlockFamily.Logs SKYFOG_LOG_FAMILY =
            SKYFOG_FAMILY.registerLogs(REGISTRATE);
    public static final BlockEntry<RotatedPillarBlock> STRIPPED_SKYFOG_LOG =
            SKYFOG_LOG_FAMILY.strippedLog();
    public static final BlockEntry<RotatedPillarBlock> STRIPPED_SKYFOG_WOOD =
            SKYFOG_LOG_FAMILY.strippedWood();
    public static final BlockEntry<StrippedRotatedPillarBlock> SKYFOG_LOG = SKYFOG_LOG_FAMILY.log();
    public static final BlockEntry<StrippedRotatedPillarBlock> SKYFOG_WOOD =
            SKYFOG_LOG_FAMILY.wood();

    private static final WoodBlockFamily VERDANT_FAMILY =
            new WoodBlockFamily(
                    "verdant",
                    "stem",
                    "hyphae",
                    new PillarProperties(MapColor.TERRACOTTA_YELLOW, MapColor.WOOD, SoundType.STEM),
                    new PillarProperties(MapColor.TERRACOTTA_RED, MapColor.WOOD, SoundType.STEM),
                    MapColor.TERRACOTTA_YELLOW,
                    WoodType.BAMBOO,
                    false,
                    MiaTags.ItemTags.VERDANT_STEMS.tag);
    private static final WoodBlockFamily.Logs VERDANT_LOG_FAMILY =
            VERDANT_FAMILY.registerLogs(REGISTRATE);
    public static final BlockEntry<RotatedPillarBlock> STRIPPED_VERDANT_STEM =
            VERDANT_LOG_FAMILY.strippedLog();
    public static final BlockEntry<RotatedPillarBlock> STRIPPED_VERDANT_HYPHAE =
            VERDANT_LOG_FAMILY.strippedWood();
    public static final BlockEntry<StrippedRotatedPillarBlock> VERDANT_STEM =
            VERDANT_LOG_FAMILY.log();
    public static final BlockEntry<StrippedRotatedPillarBlock> VERDANT_HYPHAE =
            VERDANT_LOG_FAMILY.wood();

    private static final WoodBlockFamily INVERTED_FAMILY =
            new WoodBlockFamily(
                    "inverted",
                    "log",
                    "wood",
                    new PillarProperties(MapColor.WOOD, MapColor.PODZOL, SoundType.WOOD),
                    new PillarProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD),
                    MapColor.COLOR_GREEN,
                    WoodType.CHERRY,
                    true,
                    MiaTags.ItemTags.INVERTED_LOGS.tag);
    private static final WoodBlockFamily.Logs INVERTED_LOG_FAMILY =
            INVERTED_FAMILY.registerLogs(REGISTRATE);
    public static final BlockEntry<RotatedPillarBlock> STRIPPED_INVERTED_LOG =
            INVERTED_LOG_FAMILY.strippedLog();
    public static final BlockEntry<RotatedPillarBlock> STRIPPED_INVERTED_WOOD =
            INVERTED_LOG_FAMILY.strippedWood();
    public static final BlockEntry<StrippedRotatedPillarBlock> INVERTED_LOG =
            INVERTED_LOG_FAMILY.log();
    public static final BlockEntry<StrippedRotatedPillarBlock> INVERTED_WOOD =
            INVERTED_LOG_FAMILY.wood();

    public static final BlockEntry<Block> SKYFOG_PLANKS = SKYFOG_FAMILY.registerPlanks(REGISTRATE);
    private static final BuildingBlockFamily SKYFOG_BUILDING_FAMILY =
            SKYFOG_FAMILY.buildingBlocks(REGISTRATE, SKYFOG_PLANKS).register();
    public static final BlockEntry<StairBlock> SKYFOG_STAIRS =
            SKYFOG_BUILDING_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> SKYFOG_SLAB =
            SKYFOG_BUILDING_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<FenceBlock> SKYFOG_FENCE =
            SKYFOG_BUILDING_FAMILY.get(BuildingBlockVariant.FENCE);
    public static final BlockEntry<FenceGateBlock> SKYFOG_FENCE_GATE =
            SKYFOG_BUILDING_FAMILY.get(BuildingBlockVariant.FENCE_GATE);
    public static final BlockEntry<DoorBlock> SKYFOG_DOOR =
            SKYFOG_BUILDING_FAMILY.get(BuildingBlockVariant.DOOR);
    public static final BlockEntry<TrapDoorBlock> SKYFOG_TRAPDOOR =
            SKYFOG_BUILDING_FAMILY.get(BuildingBlockVariant.TRAPDOOR);
    public static final BlockEntry<PressurePlateBlock> SKYFOG_PRESSURE_PLATE =
            SKYFOG_BUILDING_FAMILY.get(BuildingBlockVariant.PRESSURE_PLATE);
    public static final BlockEntry<ButtonBlock> SKYFOG_BUTTON =
            SKYFOG_BUILDING_FAMILY.get(BuildingBlockVariant.BUTTON);

    public static final BlockEntry<Block> VERDANT_PLANKS =
            VERDANT_FAMILY.registerPlanks(REGISTRATE);
    private static final BuildingBlockFamily VERDANT_BUILDING_FAMILY =
            VERDANT_FAMILY.buildingBlocks(REGISTRATE, VERDANT_PLANKS).register();
    public static final BlockEntry<StairBlock> VERDANT_STAIRS =
            VERDANT_BUILDING_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> VERDANT_SLAB =
            VERDANT_BUILDING_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<FenceBlock> VERDANT_FENCE =
            VERDANT_BUILDING_FAMILY.get(BuildingBlockVariant.FENCE);
    public static final BlockEntry<FenceGateBlock> VERDANT_FENCE_GATE =
            VERDANT_BUILDING_FAMILY.get(BuildingBlockVariant.FENCE_GATE);
    public static final BlockEntry<DoorBlock> VERDANT_DOOR =
            VERDANT_BUILDING_FAMILY.get(BuildingBlockVariant.DOOR);
    public static final BlockEntry<TrapDoorBlock> VERDANT_TRAPDOOR =
            VERDANT_BUILDING_FAMILY.get(BuildingBlockVariant.TRAPDOOR);
    public static final BlockEntry<PressurePlateBlock> VERDANT_PRESSURE_PLATE =
            VERDANT_BUILDING_FAMILY.get(BuildingBlockVariant.PRESSURE_PLATE);
    public static final BlockEntry<ButtonBlock> VERDANT_BUTTON =
            VERDANT_BUILDING_FAMILY.get(BuildingBlockVariant.BUTTON);

    public static final BlockEntry<Block> INVERTED_PLANKS =
            INVERTED_FAMILY.registerPlanks(REGISTRATE);
    private static final BuildingBlockFamily INVERTED_BUILDING_FAMILY =
            INVERTED_FAMILY.buildingBlocks(REGISTRATE, INVERTED_PLANKS).register();
    public static final BlockEntry<StairBlock> INVERTED_STAIRS =
            INVERTED_BUILDING_FAMILY.get(BuildingBlockVariant.STAIRS);
    public static final BlockEntry<SlabBlock> INVERTED_SLAB =
            INVERTED_BUILDING_FAMILY.get(BuildingBlockVariant.SLAB);
    public static final BlockEntry<FenceBlock> INVERTED_FENCE =
            INVERTED_BUILDING_FAMILY.get(BuildingBlockVariant.FENCE);
    public static final BlockEntry<FenceGateBlock> INVERTED_FENCE_GATE =
            INVERTED_BUILDING_FAMILY.get(BuildingBlockVariant.FENCE_GATE);
    public static final BlockEntry<DoorBlock> INVERTED_DOOR =
            INVERTED_BUILDING_FAMILY.get(BuildingBlockVariant.DOOR);
    public static final BlockEntry<TrapDoorBlock> INVERTED_TRAPDOOR =
            INVERTED_BUILDING_FAMILY.get(BuildingBlockVariant.TRAPDOOR);
    public static final BlockEntry<PressurePlateBlock> INVERTED_PRESSURE_PLATE =
            INVERTED_BUILDING_FAMILY.get(BuildingBlockVariant.PRESSURE_PLATE);
    public static final BlockEntry<ButtonBlock> INVERTED_BUTTON =
            INVERTED_BUILDING_FAMILY.get(BuildingBlockVariant.BUTTON);

    static {
        REGISTRATE.defaultCreativeSection(MiaItemGroups.BASE_NATURE_BLOCKS);
    }

    public static final BlockEntry<TallGrassBlock> MARGINAL_WEED =
            REGISTRATE
                    .object("marginal_weed")
                    .block(TallGrassBlock::new)
                    .properties(MiaBlocks::plantProperties)
                    .blockstate(BlockStateGen::crossPlant)
                    .item()
                    .model(() -> (ctx, prov) -> prov.generateFlatBlockItem(ctx.get()))
                    .build()
                    .register();

    public static final BlockEntry<TallGrassBlock> CRIMSON_VEILGRASS =
            REGISTRATE
                    .object("crimson_veilgrass")
                    .block(TallGrassBlock::new)
                    .properties(
                            p ->
                                    plantProperties(p)
                                            .mapColor(MapColor.NETHER)
                                            .sound(SoundType.ROOTS))
                    .blockstate(BlockStateGen::crossPlant)
                    .item()
                    .model(() -> (ctx, prov) -> prov.generateFlatBlockItem(ctx.get()))
                    .build()
                    .register();

    public static final BlockEntry<NetherSproutsBlock> SCORCHLEAF =
            REGISTRATE
                    .object("scorchleaf")
                    .block(NetherSproutsBlock::new)
                    .properties(
                            p ->
                                    plantProperties(p)
                                            .mapColor(MapColor.COLOR_CYAN)
                                            .sound(SoundType.NETHER_SPROUTS))
                    .blockstate(BlockStateGen::crossPlant)
                    .item()
                    .model(() -> (ctx, prov) -> prov.generateFlatBlockItem(ctx.get()))
                    .build()
                    .register();

    public static final BlockEntry<FlowerBedBlock> FORTITUDE_FLOWER =
            REGISTRATE
                    .object("fortitude_flower")
                    .block(FlowerBedBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.PLANT)
                                            .noCollision()
                                            .sound(SoundType.PINK_PETALS)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::flowerBed)
                    .simpleItem()
                    .register();

    public static final BlockEntry<WaterTallFlowerBlock> REED =
            REGISTRATE
                    .object("reed")
                    .block(WaterTallFlowerBlock::new)
                    .properties(MiaBlocks::plantProperties)
                    .blockstate(BlockStateGen::doublePlant)
                    .loot(LootGen.lowerHalf())
                    .item()
                    .model(() -> (ctx, prov) -> prov.generateFlatBlockItem(ctx.get(), "_top"))
                    .build()
                    .register();

    public static final BlockEntry<GloomBerryBlock> GLOOM_BERRY_PLANT =
            REGISTRATE
                    .object("gloom_berry_plant")
                    .block(GloomBerryBlock::new)
                    .properties(
                            p ->
                                    plantProperties(p)
                                            .randomTicks()
                                            .lightLevel(GloomBerryBlock::getLightLevel))
                    .blockstate(BlockStateGen::doubleBerry)
                    .loot(LootGen.lowerHalf(() -> MiaItems.GLOOM_BERRY.get()))
                    .register();

    public static final BlockEntry<DreamLicheeBlock> DREAM_LICHEE_PLANT =
            REGISTRATE
                    .object("dream_lichee_plant")
                    .block(DreamLicheeBlock::new)
                    .properties(p -> plantProperties(p).randomTicks())
                    .blockstate(BlockStateGen::doubleBerry)
                    .loot(LootGen.lowerHalf(() -> MiaItems.DREAM_LICHEE.get()))
                    .register();

    public static final BlockEntry<FlowerBlock> BALLOON_PLANT =
            flower("balloon_plant", MobEffects.INSTANT_HEALTH, 5.0F, SoundType.GRASS, 0);
    public static final BlockEntry<FlowerBlock> LANTERN_PLANT =
            flower("lantern_plant", MobEffects.NIGHT_VISION, 5.0F, SoundType.GRASS, 9);
    public static final BlockEntry<FlowerBlock> GREEN_PERILLA =
            flower("green_perilla", MobEffects.INSTANT_HEALTH, 5.0F, SoundType.GRASS, 0);
    public static final BlockEntry<FlowerBlock> KONJAC_ROOT =
            flower("konjac_root", MobEffects.INSTANT_HEALTH, 5.0F, SoundType.ROOTS, 0);
    public static final BlockEntry<FlowerBlock> SILVEAF_FUNGUS =
            flower("silveaf_fungus", MobEffects.INSTANT_HEALTH, 5.0F, SoundType.ROOTS, 0);

    public static final BlockEntry<GreenParticleLeavesBlock> SKYFOG_LEAVES =
            REGISTRATE
                    .object("skyfog_leaves")
                    .block(p -> new GreenParticleLeavesBlock(0.01F, p))
                    .initialProperties(() -> Blocks.AZALEA_LEAVES)
                    .tag(BlockTags.LEAVES)
                    .loot(LootGen.leaves(() -> MiaBlocks.SKYFOG_SAPLING.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<FruitingSkyfogLeavesBlock> SKYFOG_LEAVES_WITH_FRUITS =
            REGISTRATE
                    .object("skyfog_leaves_with_fruits")
                    .block(p -> new FruitingSkyfogLeavesBlock(0.01F, p))
                    .initialProperties(SKYFOG_LEAVES)
                    .tag(BlockTags.LEAVES)
                    .loot(LootGen.leaves(() -> MiaBlocks.SKYFOG_SAPLING.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<SaplingBlock> SKYFOG_SAPLING =
            REGISTRATE
                    .object("skyfog_sapling")
                    .block(p -> new SaplingBlock(MiaTreeGrowers.SKYFOG, p))
                    .properties(
                            p -> plantProperties(p).randomTicks().sound(SoundType.CHERRY_SAPLING))
                    .tag(BlockTags.SAPLINGS)
                    .blockstate(BlockStateGen::crossPlant)
                    .item()
                    .model(() -> (ctx, prov) -> prov.generateFlatBlockItem(ctx.get()))
                    .build()
                    .register();

    public static final BlockEntry<SlimeBlock> VERDANT_LEAVES =
            REGISTRATE
                    .object("verdant_leaves")
                    .block(SlimeBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_GREEN)
                                            .strength(1.0F)
                                            .sound(SoundType.WART_BLOCK)
                                            .noOcclusion()
                                            .lightLevel(_ -> 15))
                    .simpleItem()
                    .register();

    public static final BlockEntry<MiaFungusBlock> VERDANT_FUNGUS =
            REGISTRATE
                    .object("verdant_fungus")
                    .block(p -> new MiaFungusBlock(MiaTreeFeatures.VERDANT_FUNGUS, p))
                    .properties(
                            p ->
                                    plantProperties(p)
                                            .mapColor(MapColor.COLOR_CYAN)
                                            .sound(SoundType.CHERRY_SAPLING))
                    .blockstate(BlockStateGen::crossPlant)
                    .item()
                    .model(() -> (ctx, prov) -> prov.generateFlatBlockItem(ctx.get()))
                    .build()
                    .register();

    public static final BlockEntry<GreenParticleLeavesBlock> INVERTED_LEAVES =
            REGISTRATE
                    .object("inverted_leaves")
                    .block(p -> new GreenParticleLeavesBlock(0.01F, p))
                    .initialProperties(() -> Blocks.CHERRY_LEAVES)
                    .tag(BlockTags.LEAVES)
                    .loot(LootGen.leaves(() -> MiaBlocks.INVERTED_SAPLING.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<InvertedSaplingBlock> INVERTED_SAPLING =
            REGISTRATE
                    .object("inverted_sapling")
                    .block(p -> new InvertedSaplingBlock(MiaTreeGrowers.INVERTED, p))
                    .properties(
                            p -> plantProperties(p).randomTicks().sound(SoundType.CHERRY_SAPLING))
                    .tag(BlockTags.SAPLINGS)
                    .blockstate(BlockStateGen::crossPlant)
                    .item()
                    .model(() -> (ctx, prov) -> prov.generateFlatBlockItem(ctx.get()))
                    .build()
                    .register();

    public static final BlockEntry<DropExperienceBlock> ABYSS_IRON_ORE =
            ore("abyss_iron_ore", ConstantInt.of(0), Items.RAW_IRON);
    public static final BlockEntry<DropExperienceBlock> ABYSS_GOLD_ORE =
            ore("abyss_gold_ore", ConstantInt.of(0), Items.RAW_GOLD);
    public static final BlockEntry<DropExperienceBlock> ABYSS_DIAMOND_ORE =
            ore("abyss_diamond_ore", UniformInt.of(3, 7), Items.DIAMOND);
    public static final BlockEntry<DropExperienceBlock> ABYSS_EMERALD_ORE =
            ore("abyss_emerald_ore", UniformInt.of(3, 7), Items.EMERALD);
    public static final BlockEntry<DropExperienceBlock> ABYSS_QUARTZ_ORE =
            ore("abyss_quartz_ore", UniformInt.of(2, 5), Items.QUARTZ);

    public static final BlockEntry<DropExperienceBlock> ABYSS_COPPER_ORE =
            REGISTRATE
                    .object("abyss_copper_ore")
                    .block(p -> new DropExperienceBlock(ConstantInt.of(0), p))
                    .initialProperties(ABYSS_ANDESITE)
                    .properties(p -> p.strength(4.5F, 3.0F))
                    .transform(TagGen.pickaxeOnly())
                    .loot((lt, b) -> lt.add(b, lt.createCopperOreDrops(b)))
                    .simpleItem()
                    .register();
    public static final BlockEntry<DropExperienceBlock> ABYSS_LAPIS_ORE =
            REGISTRATE
                    .object("abyss_lapis_ore")
                    .block(p -> new DropExperienceBlock(UniformInt.of(2, 5), p))
                    .initialProperties(ABYSS_ANDESITE)
                    .properties(p -> p.strength(4.5F, 3.0F))
                    .transform(TagGen.pickaxeOnly())
                    .loot((lt, b) -> lt.add(b, lt.createLapisOreDrops(b)))
                    .simpleItem()
                    .register();
    public static final BlockEntry<RedStoneOreBlock> ABYSS_REDSTONE_ORE =
            REGISTRATE
                    .object("abyss_redstone_ore")
                    .block(RedStoneOreBlock::new)
                    .initialProperties(ABYSS_ANDESITE)
                    .properties(
                            p ->
                                    p.randomTicks()
                                            .lightLevel(
                                                    s -> s.getValue(RedStoneOreBlock.LIT) ? 9 : 0)
                                            .strength(4.5F, 3.0F))
                    .transform(TagGen.pickaxeOnly())
                    .loot((lt, b) -> lt.add(b, lt.createRedstoneOreDrops(b)))
                    .simpleItem()
                    .register();
    public static final BlockEntry<ChlorophyteOreBlock> ABYSS_CHLOROPHYTE_ORE =
            REGISTRATE
                    .object("abyss_chlorophyte_ore")
                    .block(p -> new ChlorophyteOreBlock(UniformInt.of(2, 5), Blocks.MUD, p))
                    .properties(
                            p ->
                                    p.mapColor(MapColor.TERRACOTTA_GREEN)
                                            .randomTicks()
                                            .strength(1.5F, 3.0F)
                                            .sound(SoundType.MUD_BRICKS))
                    .transform(TagGen.pickaxeOnly())
                    .loot((lt, b) -> lt.add(b, lt.createOreDrop(b, MiaItems.RAW_CHLOROPHYTE.get())))
                    .simpleItem()
                    .register();

    public static final BlockEntry<Block> RAW_CHLOROPHYTE_BLOCK =
            mineralBlock("raw_chlorophyte_block", MapColor.COLOR_LIGHT_GREEN, SoundType.STONE);
    public static final BlockEntry<Block> CHLOROPHYTE_BLOCK =
            mineralBlock("chlorophyte_block", MapColor.METAL, SoundType.METAL);
    public static final BlockEntry<Block> PRASIOLITE_BLOCK =
            mineralBlock("prasiolite_block", MapColor.COLOR_GREEN, SoundType.AMETHYST);
    public static final BlockEntry<BuddingPrasioliteBlock> BUDDING_PRASIOLITE =
            REGISTRATE
                    .object("budding_prasiolite")
                    .block(BuddingPrasioliteBlock::new)
                    .initialProperties(PRASIOLITE_BLOCK)
                    .properties(p -> p.randomTicks().pushReaction(PushReaction.DESTROY))
                    .transform(TagGen.pickaxeOnly())
                    .loot((lt, b) -> lt.dropWhenSilkTouch(b))
                    .simpleItem()
                    .register();
    public static final BlockEntry<AmethystClusterBlock> SMALL_PRASIOLITE_BUD =
            crystal("small_prasiolite_bud", 3, 3, 1);
    public static final BlockEntry<AmethystClusterBlock> MEDIUM_PRASIOLITE_BUD =
            crystal("medium_prasiolite_bud", 4, 3, 2);
    public static final BlockEntry<AmethystClusterBlock> LARGE_PRASIOLITE_BUD =
            crystal("large_prasiolite_bud", 5, 3, 4);
    public static final BlockEntry<AmethystClusterBlock> PRASIOLITE_CLUSTER =
            REGISTRATE
                    .object("prasiolite_cluster")
                    .block(p -> new AmethystClusterBlock(7, 3, p))
                    .properties(p -> crystalProperties(p, 5))
                    .transform(TagGen.pickaxeOnly())
                    .blockstate(() -> (ctx, prov) -> prov.createAmethystCluster(ctx.get()))
                    .loot(
                            (lt, b) ->
                                    lt.add(b, lt.createOreDrop(b, MiaItems.PRASIOLITE_SHARD.get())))
                    .simpleItem()
                    .register();
    public static final BlockEntry<Block> CAERULITE_BLOCK =
            mineralBlock("caerulite_block", MapColor.COLOR_BLUE, SoundType.AMETHYST);
    public static final BlockEntry<BuddingCaeruliteBlock> BUDDING_CAERULITE =
            REGISTRATE
                    .object("budding_caerulite")
                    .block(BuddingCaeruliteBlock::new)
                    .initialProperties(CAERULITE_BLOCK)
                    .properties(p -> p.randomTicks().pushReaction(PushReaction.DESTROY))
                    .transform(TagGen.pickaxeOnly())
                    .loot((lt, b) -> lt.dropWhenSilkTouch(b))
                    .simpleItem()
                    .register();
    public static final BlockEntry<AmethystClusterBlock> SMALL_CAERULITE_BUD =
            crystal("small_caerulite_bud", 3, 3, 1);
    public static final BlockEntry<AmethystClusterBlock> MEDIUM_CAERULITE_BUD =
            crystal("medium_caerulite_bud", 4, 3, 2);
    public static final BlockEntry<AmethystClusterBlock> LARGE_CAERULITE_BUD =
            crystal("large_caerulite_bud", 5, 3, 4);
    public static final BlockEntry<AmethystClusterBlock> CAERULITE_CLUSTER =
            REGISTRATE
                    .object("caerulite_cluster")
                    .block(p -> new AmethystClusterBlock(7, 3, p))
                    .properties(p -> crystalProperties(p, 5))
                    .transform(TagGen.pickaxeOnly())
                    .blockstate(() -> (ctx, prov) -> prov.createAmethystCluster(ctx.get()))
                    .loot((lt, b) -> lt.add(b, lt.createOreDrop(b, MiaItems.CAERULITE_SHARD.get())))
                    .simpleItem()
                    .register();
    public static final BlockEntry<Block> SUN_STONE =
            REGISTRATE
                    .object("sun_stone")
                    .block(Block::new)
                    .properties(
                            p ->
                                    p.strength(0.3F)
                                            .requiresCorrectToolForDrops()
                                            .lightLevel(_ -> 15)
                                            .sound(SoundType.FROGLIGHT))
                    .transform(TagGen.pickaxeOnly())
                    .simpleItem()
                    .register();

    private static BlockBehaviour.Properties plantProperties(BlockBehaviour.Properties properties) {
        return properties
                .mapColor(MapColor.PLANT)
                .replaceable()
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockEntry<DropExperienceBlock> ore(
            String name, IntProvider experience, Item drop) {
        return REGISTRATE
                .object(name)
                .block(p -> new DropExperienceBlock(experience, p))
                .initialProperties(ABYSS_ANDESITE)
                .properties(p -> p.strength(4.5F, 3.0F))
                .transform(TagGen.pickaxeOnly())
                .loot((lt, b) -> lt.add(b, lt.createOreDrop(b, drop)))
                .simpleItem()
                .register();
    }

    private static BlockEntry<Block> mineralBlock(String name, MapColor color, SoundType sound) {
        return REGISTRATE
                .object(name)
                .block(Block::new)
                .properties(
                        p ->
                                p.mapColor(color)
                                        .instrument(NoteBlockInstrument.BASEDRUM)
                                        .requiresCorrectToolForDrops()
                                        .strength(5.0F, 6.0F)
                                        .sound(sound))
                .transform(TagGen.pickaxeOnly())
                .simpleItem()
                .register();
    }

    private static BlockEntry<AmethystClusterBlock> crystal(
            String name, int height, int offset, int light) {
        return REGISTRATE
                .object(name)
                .block(p -> new AmethystClusterBlock(height, offset, p))
                .ignore()
                .properties(p -> crystalProperties(p, light))
                .transform(TagGen.pickaxeOnly())
                .blockstate(() -> (ctx, prov) -> prov.createAmethystCluster(ctx.get()))
                .loot((lt, b) -> lt.dropWhenSilkTouch(b))
                .simpleItem()
                .register();
    }

    private static BlockBehaviour.Properties crystalProperties(
            BlockBehaviour.Properties properties, int light) {
        return properties
                .mapColor(MapColor.COLOR_GREEN)
                .forceSolidOn()
                .noOcclusion()
                .sound(SoundType.AMETHYST_CLUSTER)
                .strength(1.5F)
                .lightLevel(_ -> light)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockEntry<FlowerBlock> flower(
            String name,
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
            float duration,
            SoundType sound,
            int light) {
        return REGISTRATE
                .object(name)
                .block(p -> new FlowerBlock(effect, duration, p))
                .properties(p -> plantProperties(p).sound(sound).lightLevel(_ -> light))
                .blockstate(BlockStateGen::crossPlant)
                .item()
                .model(() -> (ctx, prov) -> prov.generateFlatBlockItem(ctx.get()))
                .build()
                .register();
    }

    public static void register() {}
}
