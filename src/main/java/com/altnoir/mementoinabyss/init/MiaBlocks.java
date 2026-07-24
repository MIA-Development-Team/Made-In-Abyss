package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.block.abyss_andesite.AbyssAndesiteBlock;
import com.altnoir.mementoinabyss.content.block.cover_grass.CoverGrassBlock;
import com.altnoir.mementoinabyss.content.block.column.ColumnBlock;
import com.altnoir.mementoinabyss.content.block.stripped_rotated_pillar.StrippedRotatedPillarBlock;
import com.altnoir.mementoinabyss.content.block.plant.DreamLicheeBlock;
import com.altnoir.mementoinabyss.content.block.plant.GloomBerryBlock;
import com.altnoir.mementoinabyss.content.block.plant.WaterTallFlowerBlock;
import com.altnoir.mementoinabyss.content.block.plant.MiaFungusBlock;
import com.altnoir.mementoinabyss.content.block.plant.InvertedSaplingBlock;
import com.altnoir.mementoinabyss.content.block.ore.ChlorophyteOreBlock;
import com.altnoir.mementoinabyss.content.block.ore.BuddingPrasioliteBlock;
import com.altnoir.mementoinabyss.content.block.ore.BuddingCaeruliteBlock;
import com.altnoir.mementoinabyss.content.block.AbyssPortalBlock;
import com.altnoir.mementoinabyss.content.block.AbyssPortalCoreBlock;
import com.altnoir.mementoinabyss.content.block.PedestalBlock;
import com.altnoir.mementoinabyss.impl.registrate.BlockStateGen;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.altnoir.mementoinabyss.impl.registrate.TagGen;
import com.altnoir.mementoinabyss.worldgen.tree.MiaTreeGrowers;
import com.altnoir.mementoinabyss.worldgen.tree.MiaTreeFeatures;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Optional;

public class MiaBlocks {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    static {
        REGISTRATE.defaultCreativeTab(MiaItemGroups.BASE.getKey());
    }

    public static final BlockEntry<AbyssPortalBlock> ABYSS_PORTAL = REGISTRATE.object("abyss_portal")
            .block(AbyssPortalBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK).noCollision().noOcclusion()
                    .lightLevel(_ -> 15).strength(-1.0F, 3_600_000.0F)
                    .noLootTable().pushReaction(PushReaction.BLOCK))
            .blockstate(BlockStateGen::abyssPortal)
            .simpleItem()
            .register();

    public static final BlockEntry<AbyssPortalCoreBlock> ABYSS_PORTAL_CORE = REGISTRATE.object("abyss_portal_core")
            .block(AbyssPortalCoreBlock::new)
            .properties(p -> p.mapColor(MapColor.STONE).strength(-1.0F, 3_600_000.0F)
                    .sound(SoundType.NETHERITE_BLOCK).noLootTable().pushReaction(PushReaction.BLOCK))
            .blockstate(BlockStateGen::abyssPortalCore)
            .simpleItem()
            .register();

    public static final BlockEntry<Block> ABYSS_PORTAL_FRAME = REGISTRATE.object("abyss_portal_frame")
            .block(Block::new)
            .properties(p -> p.mapColor(MapColor.STONE).requiresCorrectToolForDrops()
                    .strength(100.0F, 1200.0F).sound(SoundType.NETHERITE_BLOCK))
            .transform(TagGen.pickaxeOnly())
            .blockstate(BlockStateGen::abyssPortalFrame)
            .simpleItem()
            .register();

    public static final BlockEntry<PedestalBlock> PEDESTAL = REGISTRATE.object("pedestal")
            .block(PedestalBlock::new)
            .properties(p -> p.mapColor(MapColor.STONE).strength(2.5F, 6.0F)
                    .sound(SoundType.DEEPSLATE).noOcclusion())
            .transform(TagGen.pickaxeOnly())
            .blockstate(BlockStateGen::pedestal)
            .simpleItem()
            .register();

    public static final BlockEntry<AbyssAndesiteBlock> ABYSS_ANDESITE = REGISTRATE.object("abyss_andesite")
            .block(AbyssAndesiteBlock::new)
            .properties(p -> p.mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE))
            .transform(TagGen.pickaxeOnly())
            .tag(MiaTags.BlockTags.BASE_STONE_ABYSS.tag, MiaTags.BlockTags.ABYSS_ANDESITE_ORE_REPLACEABLE.tag)
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> ABYSS_ANDESITE_STAIRS = stairs(ABYSS_ANDESITE);
    public static final BlockEntry<SlabBlock> ABYSS_ANDESITE_SLAB = slab(ABYSS_ANDESITE);
    public static final BlockEntry<WallBlock> ABYSS_ANDESITE_WALL = wall(ABYSS_ANDESITE);

    public static final BlockEntry<Block> ABYSS_COBBLED_ANDESITE = REGISTRATE.object("abyss_cobbled_andesite")
            .block(Block::new)
            .initialProperties(ABYSS_ANDESITE)
            .properties(p -> p.strength(3.5F, 6.0F))
            .transform(TagGen.pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> ABYSS_COBBLED_ANDESITE_STAIRS = stairs(ABYSS_COBBLED_ANDESITE);
    public static final BlockEntry<SlabBlock> ABYSS_COBBLED_ANDESITE_SLAB = slab(ABYSS_COBBLED_ANDESITE);
    public static final BlockEntry<WallBlock> ABYSS_COBBLED_ANDESITE_WALL = wall(ABYSS_COBBLED_ANDESITE);

    public static final BlockEntry<Block> MOSSY_ABYSS_COBBLED_ANDESITE = REGISTRATE.object("mossy_abyss_cobbled_andesite")
            .block(Block::new)
            .initialProperties(ABYSS_COBBLED_ANDESITE)
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS = stairs(MOSSY_ABYSS_COBBLED_ANDESITE);
    public static final BlockEntry<SlabBlock> MOSSY_ABYSS_COBBLED_ANDESITE_SLAB = slab(MOSSY_ABYSS_COBBLED_ANDESITE);
    public static final BlockEntry<WallBlock> MOSSY_ABYSS_COBBLED_ANDESITE_WALL = wall(MOSSY_ABYSS_COBBLED_ANDESITE);

    public static final BlockEntry<Block> POLISHED_ABYSS_ANDESITE = REGISTRATE.object("polished_abyss_andesite")
            .block(Block::new)
            .initialProperties(ABYSS_COBBLED_ANDESITE)
            .properties(p -> p.sound(SoundType.POLISHED_DEEPSLATE))
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> POLISHED_ABYSS_ANDESITE_STAIRS = stairs(POLISHED_ABYSS_ANDESITE);
    public static final BlockEntry<SlabBlock> POLISHED_ABYSS_ANDESITE_SLAB = slab(POLISHED_ABYSS_ANDESITE);
    public static final BlockEntry<WallBlock> POLISHED_ABYSS_ANDESITE_WALL = wall(POLISHED_ABYSS_ANDESITE);

    public static final BlockEntry<RotatedPillarBlock> ABYSS_ANDESITE_PILLAR = REGISTRATE.object("abyss_andesite_pillar")
            .block(RotatedPillarBlock::new)
            .initialProperties(POLISHED_ABYSS_ANDESITE)
            .transform(TagGen.pickaxeOnly())
            .blockstate(() -> (ctx, prov) -> prov.generateLogBlock(ctx.get()))
            .simpleItem()
            .register();

    public static final BlockEntry<Block> CHISLED_ABYSS_ANDESITE = REGISTRATE.object("chiseled_abyss_andesite")
            .block(Block::new)
            .initialProperties(POLISHED_ABYSS_ANDESITE)
            .transform(TagGen.pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<Block> ABYSS_ANDESITE_BRICKS = REGISTRATE.object("abyss_andesite_bricks")
            .block(Block::new)
            .initialProperties(ABYSS_COBBLED_ANDESITE)
            .properties(p -> p.sound(SoundType.DEEPSLATE_TILES))
            .transform(TagGen.pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<ColumnBlock> ABYSS_ANDESITE_COLUMN = REGISTRATE.object("abyss_andesite_column")
            .block(ColumnBlock::new)
            .initialProperties(POLISHED_ABYSS_ANDESITE)
            .transform(TagGen.pickaxeOnly())
            .blockstate(() -> BlockStateGen.column(ABYSS_ANDESITE_PILLAR, ABYSS_ANDESITE_BRICKS))
            .simpleItem()
            .register();

    public static final BlockEntry<Block> CRACKED_ABYSS_ANDESITE_BRICKS = REGISTRATE.object("cracked_abyss_andesite_bricks")
            .block(Block::new)
            .initialProperties(ABYSS_ANDESITE_BRICKS)
            .transform(TagGen.pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> ABYSS_ANDESITE_BRICKS_STAIRS = stairs(ABYSS_ANDESITE_BRICKS);
    public static final BlockEntry<SlabBlock> ABYSS_ANDESITE_BRICKS_SLAB = slab(ABYSS_ANDESITE_BRICKS);
    public static final BlockEntry<WallBlock> ABYSS_ANDESITE_BRICKS_WALL = wall(ABYSS_ANDESITE_BRICKS);

    public static final BlockEntry<Block> MOSSY_ABYSS_ANDESITE_BRICKS = REGISTRATE.object("mossy_abyss_andesite_bricks")
            .block(Block::new)
            .initialProperties(ABYSS_ANDESITE_BRICKS)
            .transform(TagGen.pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS = stairs(MOSSY_ABYSS_ANDESITE_BRICKS);
    public static final BlockEntry<SlabBlock> MOSSY_ABYSS_ANDESITE_BRICKS_SLAB = slab(MOSSY_ABYSS_ANDESITE_BRICKS);
    public static final BlockEntry<WallBlock> MOSSY_ABYSS_ANDESITE_BRICKS_WALL = wall(MOSSY_ABYSS_ANDESITE_BRICKS);

    public static final BlockEntry<CoverGrassBlock> COVERGRASS_ABYSS_ANDESITE = REGISTRATE.object("covergrass_abyss_andesite")
            .block(p -> new CoverGrassBlock(ABYSS_ANDESITE.get(), p))
            .initialProperties(ABYSS_ANDESITE)
            .properties(p -> p.mapColor(MapColor.GRASS)
                    .randomTicks())
            .blockstate(BlockStateGen::coverGrass)
            .tag(MiaTags.BlockTags.COVERGRASS.tag, BlockTags.DIRT)
            .tag(MiaTags.BlockTags.BASE_STONE_ABYSS.tag)
            .transform(TagGen.pickaxeOnly())
            .loot((lt, b) ->  {
                lt.add(b,
                        lt.createSilkTouchDispatchTable(b,
                                LootItem.lootTableItem(ABYSS_ANDESITE.get())));
            })
            .simpleItem()
            .register();

    public static final BlockEntry<CoverGrassBlock> COVERGRASS_TUFF = REGISTRATE.object("covergrass_tuff")
            .block(p -> new CoverGrassBlock(Blocks.TUFF, p))
            .initialProperties(() -> Blocks.TUFF)
            .properties(p -> p.randomTicks())
            .blockstate(BlockStateGen::coverGrass)
            .tag(MiaTags.BlockTags.COVERGRASS.tag, BlockTags.DIRT)
            .transform(TagGen.pickaxeOnly())
            .loot((lt, b) ->  {
                lt.add(b,
                        lt.createSilkTouchDispatchTable(b,
                                LootItem.lootTableItem(Blocks.TUFF)));
            })
            .simpleItem()
            .register();

    public static final BlockEntry<AbyssAndesiteBlock> MARLITH = REGISTRATE.object("marlith")
            .block(AbyssAndesiteBlock::new)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F, 8.0F)
                    .sound(SoundType.CALCITE))
            .simpleItem()
            .register();

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_FOSSILIZED_LOG = REGISTRATE.object("stripped_fossilized_log")
            .block(RotatedPillarBlock::new)
            .properties(p -> p.mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.COLOR_BLACK : MapColor.PODZOL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 4.2F)
                    .sound(SoundType.BASALT))
            .blockstate(() -> BlockStateGen.variantAxisBlock(
                    null,
                    5,
                    Optional.of(new int[]{12, 1, 1, 1, 1})))
            .item()
            .model(() -> (ctx, prov) ->
                    prov.createWithExistingModel(ctx.getEntry(), prov.modLoc("block/" + ctx.getName() + "0")))
            .build()
            .register();

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_FOSSILIZED_WOOD = REGISTRATE.object("stripped_fossilized_wood")
            .block(RotatedPillarBlock::new)
            .initialProperties(STRIPPED_FOSSILIZED_LOG)
            .properties(p -> p.mapColor(MapColor.PODZOL)
                    .strength(3.0F, 4.2F))
            .blockstate(() -> BlockStateGen.variantAxisBlock(
                    STRIPPED_FOSSILIZED_LOG,
                    5,
                    Optional.of(new int[]{12, 1, 1, 1, 1})))
            .item()
            .model(() -> (ctx, prov) ->
                    prov.createWithExistingModel(ctx.getEntry(), prov.modLoc("block/" + ctx.getName() + "0")))
            .build()
            .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> FOSSILIZED_LOG = REGISTRATE.object("fossilized_log")
            .block(p -> new StrippedRotatedPillarBlock(STRIPPED_FOSSILIZED_LOG.get(), p))
            .initialProperties(STRIPPED_FOSSILIZED_LOG)
            .blockstate(() -> BlockStateGen.variantAxisBlock(
                    null,
                    3,
                    Optional.of(new int[]{12, 1, 1})))
            .item()
            .model(() -> (ctx, prov) ->
                    prov.createWithExistingModel(ctx.getEntry(), prov.modLoc("block/" + ctx.getName() + "0")))
            .build()
            .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> FOSSILIZED_WOOD = REGISTRATE.object("fossilized_wood")
            .block(p -> new StrippedRotatedPillarBlock(STRIPPED_FOSSILIZED_WOOD.get(), p))
            .initialProperties(STRIPPED_FOSSILIZED_WOOD)
            .blockstate(() -> BlockStateGen.variantAxisBlock(
                    FOSSILIZED_LOG,
                    3,
                    Optional.of(new int[]{12, 1, 1})))
            .item()
            .model(() -> (ctx, prov) ->
                    prov.createWithExistingModel(ctx.getEntry(), prov.modLoc("block/" + ctx.getName() + "0")))
            .build()
            .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_FOSSILIZED_LOG = REGISTRATE.object("mossy_fossilized_log")
            .block(p -> new StrippedRotatedPillarBlock(FOSSILIZED_LOG.get(), p))
            .initialProperties(FOSSILIZED_LOG)
            .blockstate(() -> BlockStateGen.variantAxisBlock(
                    null,
                    4,
                    Optional.of(new int[]{12, 1, 1, 1})))
            .item()
            .model(() -> (ctx, prov) ->
                    prov.createWithExistingModel(ctx.getEntry(), prov.modLoc("block/" + ctx.getName() + "0")))
            .build()
            .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_FOSSILIZED_WOOD = REGISTRATE.object("mossy_fossilized_wood")
            .block(p -> new StrippedRotatedPillarBlock(FOSSILIZED_WOOD.get(), p))
            .initialProperties(FOSSILIZED_WOOD)
            .blockstate(() -> BlockStateGen.variantAxisBlock(
                    MOSSY_FOSSILIZED_LOG,
                    4,
                    Optional.of(new int[]{12, 1, 1, 1})))
            .item()
            .model(() -> (ctx, prov) ->
                    prov.createWithExistingModel(ctx.getEntry(), prov.modLoc("block/" + ctx.getName() + "0")))
            .build()
            .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_STRIPPED_FOSSILIZED_LOG = REGISTRATE.object("mossy_stripped_fossilized_log")
            .block(p -> new StrippedRotatedPillarBlock(FOSSILIZED_WOOD.get(), p))
            .initialProperties(STRIPPED_FOSSILIZED_LOG)
            .blockstate(() -> BlockStateGen.variantAxisBlock(
                    null,
                    5,
                    Optional.of(new int[]{12, 1, 1, 1, 1})))
            .item()
            .model(() -> (ctx, prov) ->
                    prov.createWithExistingModel(ctx.getEntry(), prov.modLoc("block/" + ctx.getName() + "0")))
            .build()
            .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD = REGISTRATE.object("mossy_stripped_fossilized_wood")
            .block(p -> new StrippedRotatedPillarBlock(FOSSILIZED_WOOD.get(), p))
            .initialProperties(STRIPPED_FOSSILIZED_WOOD)
            .blockstate(() -> BlockStateGen.variantAxisBlock(
                    MOSSY_STRIPPED_FOSSILIZED_LOG,
                    5,
                    Optional.of(new int[]{12, 1, 1, 1, 1})))
            .item()
            .model(() -> (ctx, prov) ->
                    prov.createWithExistingModel(ctx.getEntry(), prov.modLoc("block/" + ctx.getName() + "0")))
            .build()
            .register();

    public static final BlockEntry<Block> POLISHED_FOSSILIZED_WOOD = REGISTRATE.object("polished_fossilized_wood")
            .block(Block::new)
            .initialProperties(FOSSILIZED_WOOD)
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> POLISHED_FOSSILIZED_WOOD_STAIRS = stairs(POLISHED_FOSSILIZED_WOOD);
    public static final BlockEntry<SlabBlock> POLISHED_FOSSILIZED_WOOD_SLAB = slab(POLISHED_FOSSILIZED_WOOD);
    public static final BlockEntry<WallBlock> POLISHED_FOSSILIZED_WOOD_WALL = wall(POLISHED_FOSSILIZED_WOOD);

    public static final BlockEntry<Block> POLISHED_STRIPPED_FOSSILIZED_WOOD = REGISTRATE.object("polished_stripped_fossilized_wood")
            .block(Block::new)
            .initialProperties(STRIPPED_FOSSILIZED_WOOD)
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> POLISHED_STRIPPED_FOSSILIZED_WOOD_STAIRS = stairs(POLISHED_STRIPPED_FOSSILIZED_WOOD);
    public static final BlockEntry<SlabBlock> POLISHED_STRIPPED_FOSSILIZED_WOOD_SLAB = slab(POLISHED_STRIPPED_FOSSILIZED_WOOD);
    public static final BlockEntry<WallBlock> POLISHED_STRIPPED_FOSSILIZED_WOOD_WALL = wall(POLISHED_STRIPPED_FOSSILIZED_WOOD);

    public static final BlockEntry<Block> CHISLED_STRIPPED_FOSSILIZED_WOOD = REGISTRATE.object("chiseled_stripped_fossilized_wood")
            .block(Block::new)
            .initialProperties(STRIPPED_FOSSILIZED_WOOD)
            .simpleItem()
            .register();

    public static final BlockEntry<Block> FOSSILIZED_WOOD_BRICKS = REGISTRATE.object("fossilized_wood_bricks")
            .block(Block::new)
            .initialProperties(FOSSILIZED_WOOD)
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> FOSSILIZED_WOOD_BRICKS_STAIRS = stairs(FOSSILIZED_WOOD_BRICKS);
    public static final BlockEntry<SlabBlock> FOSSILIZED_WOOD_BRICKS_SLAB = slab(FOSSILIZED_WOOD_BRICKS);
    public static final BlockEntry<WallBlock> FOSSILIZED_WOOD_BRICKS_WALL = wall(FOSSILIZED_WOOD_BRICKS);

    public static final BlockEntry<Block> STRIPPED_FOSSILIZED_WOOD_BRICKS = REGISTRATE.object("stripped_fossilized_wood_bricks")
            .block(Block::new)
            .initialProperties(STRIPPED_FOSSILIZED_WOOD)
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS = stairs(STRIPPED_FOSSILIZED_WOOD_BRICKS);
    public static final BlockEntry<SlabBlock> STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB = slab(STRIPPED_FOSSILIZED_WOOD_BRICKS);
    public static final BlockEntry<WallBlock> STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL = wall(STRIPPED_FOSSILIZED_WOOD_BRICKS);

    public static final BlockEntry<Block> MOSSY_FOSSILIZED_WOOD_BRICKS = REGISTRATE.object("mossy_fossilized_wood_bricks")
            .block(Block::new)
            .initialProperties(FOSSILIZED_WOOD)
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> MOSSY_FOSSILIZED_WOOD_BRICKS_STAIRS = stairs(MOSSY_FOSSILIZED_WOOD_BRICKS);
    public static final BlockEntry<SlabBlock> MOSSY_FOSSILIZED_WOOD_BRICKS_SLAB = slab(MOSSY_FOSSILIZED_WOOD_BRICKS);
    public static final BlockEntry<WallBlock> MOSSY_FOSSILIZED_WOOD_BRICKS_WALL = wall(MOSSY_FOSSILIZED_WOOD_BRICKS);

    public static final BlockEntry<Block> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS = REGISTRATE.object("mossy_stripped_fossilized_wood_bricks")
            .block(Block::new)
            .initialProperties(FOSSILIZED_WOOD)
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS = stairs(MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS);
    public static final BlockEntry<SlabBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB = slab(MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS);
    public static final BlockEntry<WallBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL = wall(MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS);

    public static final BlockEntry<TallGrassBlock> MARGINAL_WEED = REGISTRATE.object("marginal_weed")
            .block(TallGrassBlock::new)
            .properties(MiaBlocks::plantProperties)
            .blockstate(BlockStateGen::crossPlant)
            .item().model(() -> flatPlantItem("block/marginal_weed")).build()
            .register();

    public static final BlockEntry<TallGrassBlock> CRIMSON_VEILGRASS = REGISTRATE.object("crimson_veilgrass")
            .block(TallGrassBlock::new)
            .properties(p -> plantProperties(p).mapColor(MapColor.NETHER).sound(SoundType.ROOTS))
            .blockstate(BlockStateGen::crossPlant)
            .item().model(() -> flatPlantItem("block/crimson_veilgrass")).build()
            .register();

    public static final BlockEntry<NetherSproutsBlock> SCORCHLEAF = REGISTRATE.object("scorchleaf")
            .block(NetherSproutsBlock::new)
            .properties(p -> plantProperties(p).mapColor(MapColor.COLOR_CYAN).sound(SoundType.NETHER_SPROUTS))
            .blockstate(BlockStateGen::crossPlant)
            .item().model(() -> flatPlantItem("block/scorchleaf")).build()
            .register();

    public static final BlockEntry<FlowerBedBlock> FORTITUDE_FLOWER = REGISTRATE.object("fortitude_flower")
            .block(FlowerBedBlock::new)
            .properties(p -> p.mapColor(MapColor.PLANT).noCollision().sound(SoundType.PINK_PETALS).pushReaction(PushReaction.DESTROY))
            .blockstate(BlockStateGen::flowerBed)
            .simpleItem()
            .register();

    public static final BlockEntry<WaterTallFlowerBlock> REED = REGISTRATE.object("reed")
            .block(WaterTallFlowerBlock::new)
            .properties(MiaBlocks::plantProperties)
            .blockstate(BlockStateGen::doublePlant)
            .loot((lt, b) -> lt.add(b, LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .when(ExplosionCondition.survivesExplosion())
                            .add(LootItem.lootTableItem(b)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(b)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER)))))))
            .item()
            .model(() -> flatPlantItem("block/reed_top"))
            .build()
            .register();

    public static final BlockEntry<GloomBerryBlock> GLOOM_BERRY_PLANT = REGISTRATE.object("gloom_berry_plant")
            .block(GloomBerryBlock::new)
            .properties(p -> plantProperties(p).randomTicks().lightLevel(GloomBerryBlock::getLightLevel))
            .blockstate(BlockStateGen::doubleBerry)
            .loot((lt, b) -> lt.add(b, LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .when(ExplosionCondition.survivesExplosion())
                            .add(LootItem.lootTableItem(MiaItems.GLOOM_BERRY.get())
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(b)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)))))))
            .register();

    public static final BlockEntry<DreamLicheeBlock> DREAM_LICHEE_PLANT = REGISTRATE.object("dream_lichee_plant")
            .block(DreamLicheeBlock::new)
            .properties(p -> plantProperties(p).randomTicks())
            .blockstate(BlockStateGen::doubleBerry)
            .loot((lt, b) -> lt.add(b, LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .when(ExplosionCondition.survivesExplosion())
                            .add(LootItem.lootTableItem(MiaItems.DREAM_LICHEE.get())
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(b)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)))))))
            .register();

    public static final BlockEntry<FlowerBlock> BALLOON_PLANT = flower("balloon_plant", MobEffects.INSTANT_HEALTH, 5.0F, SoundType.GRASS, 0);
    public static final BlockEntry<FlowerBlock> LANTERN_PLANT = flower("lantern_plant", MobEffects.NIGHT_VISION, 5.0F, SoundType.GRASS, 9);
    public static final BlockEntry<FlowerBlock> GREEN_PERILLA = flower("green_perilla", MobEffects.INSTANT_HEALTH, 5.0F, SoundType.GRASS, 0);
    public static final BlockEntry<FlowerBlock> KONJAC_ROOT = flower("konjac_root", MobEffects.INSTANT_HEALTH, 5.0F, SoundType.ROOTS, 0);
    public static final BlockEntry<FlowerBlock> SILVEAF_FUNGUS = flower("silveaf_fungus", MobEffects.INSTANT_HEALTH, 5.0F, SoundType.ROOTS, 0);

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_SKYFOG_LOG = REGISTRATE.object("stripped_skyfog_log")
            .block(RotatedPillarBlock::new)
            .properties(p -> treeLogProperties(p, MapColor.WOOD, MapColor.WOOD))
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN)
            .blockstate(() -> (ctx, prov) -> prov.generateLogBlock(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_SKYFOG_WOOD = REGISTRATE.object("stripped_skyfog_wood")
            .block(RotatedPillarBlock::new)
            .initialProperties(STRIPPED_SKYFOG_LOG)
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN)
            .blockstate(() -> (ctx, prov) -> prov.woodProvider(STRIPPED_SKYFOG_LOG.get()).wood(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<StrippedRotatedPillarBlock> SKYFOG_LOG = REGISTRATE.object("skyfog_log")
            .block(p -> new StrippedRotatedPillarBlock(STRIPPED_SKYFOG_LOG.get(), p))
            .properties(p -> treeLogProperties(p, MapColor.WOOD, MapColor.PODZOL))
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN)
            .blockstate(() -> (ctx, prov) -> prov.generateLogBlock(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<StrippedRotatedPillarBlock> SKYFOG_WOOD = REGISTRATE.object("skyfog_wood")
            .block(p -> new StrippedRotatedPillarBlock(STRIPPED_SKYFOG_WOOD.get(), p))
            .initialProperties(SKYFOG_LOG)
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN)
            .blockstate(() -> (ctx, prov) -> prov.woodProvider(SKYFOG_LOG.get()).wood(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<UntintedParticleLeavesBlock> SKYFOG_LEAVES = REGISTRATE.object("skyfog_leaves")
            .block(p -> new UntintedParticleLeavesBlock(0.01F, ParticleTypes.CHERRY_LEAVES, p))
            .initialProperties(() -> Blocks.AZALEA_LEAVES)
            .tag(BlockTags.LEAVES)
            .loot((lt, b) -> lt.add(b, lt.createLeavesDrops(b, MiaBlocks.SKYFOG_SAPLING.get(), 0.05F, 0.0625F, 0.083333336F, 0.1F)))
            .simpleItem().register();

    public static final BlockEntry<UntintedParticleLeavesBlock> SKYFOG_LEAVES_WITH_FRUITS = REGISTRATE.object("skyfog_leaves_with_fruits")
            .block(p -> new UntintedParticleLeavesBlock(0.01F, ParticleTypes.CHERRY_LEAVES, p))
            .initialProperties(SKYFOG_LEAVES)
            .tag(BlockTags.LEAVES)
            .loot((lt, b) -> lt.add(b, lt.createLeavesDrops(b, MiaBlocks.SKYFOG_SAPLING.get(), 0.05F, 0.0625F, 0.083333336F, 0.1F)))
            .simpleItem().register();

    public static final BlockEntry<SaplingBlock> SKYFOG_SAPLING = REGISTRATE.object("skyfog_sapling")
            .block(p -> new SaplingBlock(MiaTreeGrowers.SKYFOG, p))
            .properties(p -> plantProperties(p).randomTicks().sound(SoundType.CHERRY_SAPLING))
            .tag(BlockTags.SAPLINGS)
            .blockstate(BlockStateGen::crossPlant)
            .item().model(() -> flatPlantItem("block/skyfog_sapling")).build()
            .register();

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_VERDANT_STEM = REGISTRATE.object("stripped_verdant_stem")
            .block(RotatedPillarBlock::new)
            .properties(p -> treeLogProperties(p, MapColor.TERRACOTTA_RED, MapColor.WOOD).sound(SoundType.STEM))
            .tag(BlockTags.LOGS)
            .blockstate(() -> (ctx, prov) -> prov.generateLogBlock(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_VERDANT_HYPHAE = REGISTRATE.object("stripped_verdant_hyphae")
            .block(RotatedPillarBlock::new)
            .initialProperties(STRIPPED_VERDANT_STEM)
            .tag(BlockTags.LOGS)
            .blockstate(() -> (ctx, prov) -> prov.woodProvider(STRIPPED_VERDANT_STEM.get()).wood(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<StrippedRotatedPillarBlock> VERDANT_STEM = REGISTRATE.object("verdant_stem")
            .block(p -> new StrippedRotatedPillarBlock(STRIPPED_VERDANT_STEM.get(), p))
            .properties(p -> treeLogProperties(p, MapColor.TERRACOTTA_YELLOW, MapColor.WOOD).sound(SoundType.STEM))
            .tag(BlockTags.LOGS)
            .blockstate(() -> (ctx, prov) -> prov.generateLogBlock(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<StrippedRotatedPillarBlock> VERDANT_HYPHAE = REGISTRATE.object("verdant_hyphae")
            .block(p -> new StrippedRotatedPillarBlock(STRIPPED_VERDANT_HYPHAE.get(), p))
            .initialProperties(VERDANT_STEM)
            .tag(BlockTags.LOGS)
            .blockstate(() -> (ctx, prov) -> prov.woodProvider(VERDANT_STEM.get()).wood(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<SlimeBlock> VERDANT_LEAVES = REGISTRATE.object("verdant_leaves")
            .block(SlimeBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_GREEN).strength(1.0F).sound(SoundType.WART_BLOCK)
                    .noOcclusion().lightLevel(_ -> 15))
            .simpleItem().register();

    public static final BlockEntry<MiaFungusBlock> VERDANT_FUNGUS = REGISTRATE.object("verdant_fungus")
            .block(p -> new MiaFungusBlock(MiaTreeFeatures.VERDANT_FUNGUS, p))
            .properties(p -> plantProperties(p).mapColor(MapColor.COLOR_CYAN).sound(SoundType.CHERRY_SAPLING))
            .blockstate(BlockStateGen::crossPlant)
            .item().model(() -> flatPlantItem("block/verdant_fungus")).build()
            .register();

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_INVERTED_LOG = REGISTRATE.object("stripped_inverted_log")
            .block(RotatedPillarBlock::new)
            .properties(p -> treeLogProperties(p, MapColor.WOOD, MapColor.WOOD))
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN)
            .blockstate(() -> (ctx, prov) -> prov.generateLogBlock(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_INVERTED_WOOD = REGISTRATE.object("stripped_inverted_wood")
            .block(RotatedPillarBlock::new).initialProperties(STRIPPED_INVERTED_LOG)
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN)
            .blockstate(() -> (ctx, prov) -> prov.woodProvider(STRIPPED_INVERTED_LOG.get()).wood(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<StrippedRotatedPillarBlock> INVERTED_LOG = REGISTRATE.object("inverted_log")
            .block(p -> new StrippedRotatedPillarBlock(STRIPPED_INVERTED_LOG.get(), p))
            .properties(p -> treeLogProperties(p, MapColor.WOOD, MapColor.PODZOL))
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN)
            .blockstate(() -> (ctx, prov) -> prov.generateLogBlock(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<StrippedRotatedPillarBlock> INVERTED_WOOD = REGISTRATE.object("inverted_wood")
            .block(p -> new StrippedRotatedPillarBlock(STRIPPED_INVERTED_WOOD.get(), p)).initialProperties(INVERTED_LOG)
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN)
            .blockstate(() -> (ctx, prov) -> prov.woodProvider(INVERTED_LOG.get()).wood(ctx.get()))
            .simpleItem().register();

    public static final BlockEntry<UntintedParticleLeavesBlock> INVERTED_LEAVES = REGISTRATE.object("inverted_leaves")
            .block(p -> new UntintedParticleLeavesBlock(0.01F, ParticleTypes.CHERRY_LEAVES, p))
            .initialProperties(() -> Blocks.CHERRY_LEAVES).tag(BlockTags.LEAVES)
            .loot((lt, b) -> lt.add(b, lt.createLeavesDrops(b, MiaBlocks.INVERTED_SAPLING.get(), 0.05F, 0.0625F, 0.083333336F, 0.1F)))
            .simpleItem().register();

    public static final BlockEntry<InvertedSaplingBlock> INVERTED_SAPLING = REGISTRATE.object("inverted_sapling")
            .block(p -> new InvertedSaplingBlock(MiaTreeGrowers.INVERTED, p))
            .properties(p -> plantProperties(p).randomTicks().sound(SoundType.CHERRY_SAPLING))
            .tag(BlockTags.SAPLINGS).blockstate(BlockStateGen::crossPlant)
            .item().model(() -> flatPlantItem("block/inverted_sapling")).build().register();

    public static final BlockEntry<DropExperienceBlock> ABYSS_IRON_ORE = ore("abyss_iron_ore", ConstantInt.of(0), Items.RAW_IRON);
    public static final BlockEntry<DropExperienceBlock> ABYSS_GOLD_ORE = ore("abyss_gold_ore", ConstantInt.of(0), Items.RAW_GOLD);
    public static final BlockEntry<DropExperienceBlock> ABYSS_DIAMOND_ORE = ore("abyss_diamond_ore", UniformInt.of(3, 7), Items.DIAMOND);
    public static final BlockEntry<DropExperienceBlock> ABYSS_EMERALD_ORE = ore("abyss_emerald_ore", UniformInt.of(3, 7), Items.EMERALD);
    public static final BlockEntry<DropExperienceBlock> ABYSS_QUARTZ_ORE = ore("abyss_quartz_ore", UniformInt.of(2, 5), Items.QUARTZ);

    public static final BlockEntry<DropExperienceBlock> ABYSS_COPPER_ORE = REGISTRATE.object("abyss_copper_ore")
            .block(p -> new DropExperienceBlock(ConstantInt.of(0), p)).initialProperties(ABYSS_ANDESITE)
            .properties(p -> p.strength(4.5F, 3.0F)).transform(TagGen.pickaxeOnly())
            .loot((lt, b) -> lt.add(b, lt.createCopperOreDrops(b))).simpleItem().register();
    public static final BlockEntry<DropExperienceBlock> ABYSS_LAPIS_ORE = REGISTRATE.object("abyss_lapis_ore")
            .block(p -> new DropExperienceBlock(UniformInt.of(2, 5), p)).initialProperties(ABYSS_ANDESITE)
            .properties(p -> p.strength(4.5F, 3.0F)).transform(TagGen.pickaxeOnly())
            .loot((lt, b) -> lt.add(b, lt.createLapisOreDrops(b))).simpleItem().register();
    public static final BlockEntry<RedStoneOreBlock> ABYSS_REDSTONE_ORE = REGISTRATE.object("abyss_redstone_ore")
            .block(RedStoneOreBlock::new).initialProperties(ABYSS_ANDESITE)
            .properties(p -> p.randomTicks().lightLevel(s -> s.getValue(RedStoneOreBlock.LIT) ? 9 : 0).strength(4.5F, 3.0F))
            .transform(TagGen.pickaxeOnly()).loot((lt, b) -> lt.add(b, lt.createRedstoneOreDrops(b))).simpleItem().register();
    public static final BlockEntry<ChlorophyteOreBlock> ABYSS_CHLOROPHYTE_ORE = REGISTRATE.object("abyss_chlorophyte_ore")
            .block(p -> new ChlorophyteOreBlock(UniformInt.of(2, 5), Blocks.MUD, p))
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_GREEN).randomTicks().strength(1.5F, 3.0F).sound(SoundType.MUD_BRICKS))
            .transform(TagGen.pickaxeOnly()).loot((lt, b) -> lt.add(b, lt.createOreDrop(b, MiaItems.RAW_CHLOROPHYTE.get()))).simpleItem().register();

    public static final BlockEntry<Block> RAW_CHLOROPHYTE_BLOCK = mineralBlock("raw_chlorophyte_block", MapColor.COLOR_LIGHT_GREEN, SoundType.STONE);
    public static final BlockEntry<Block> CHLOROPHYTE_BLOCK = mineralBlock("chlorophyte_block", MapColor.METAL, SoundType.METAL);
    public static final BlockEntry<Block> PRASIOLITE_BLOCK = mineralBlock("prasiolite_block", MapColor.COLOR_GREEN, SoundType.AMETHYST);
    public static final BlockEntry<BuddingPrasioliteBlock> BUDDING_PRASIOLITE = REGISTRATE.object("budding_prasiolite")
            .block(BuddingPrasioliteBlock::new).initialProperties(PRASIOLITE_BLOCK)
            .properties(p -> p.randomTicks().pushReaction(PushReaction.DESTROY)).transform(TagGen.pickaxeOnly())
            .loot((lt, b) -> lt.dropWhenSilkTouch(b)).simpleItem().register();
    public static final BlockEntry<AmethystClusterBlock> SMALL_PRASIOLITE_BUD = crystal("small_prasiolite_bud", 3, 3, 1);
    public static final BlockEntry<AmethystClusterBlock> MEDIUM_PRASIOLITE_BUD = crystal("medium_prasiolite_bud", 4, 3, 2);
    public static final BlockEntry<AmethystClusterBlock> LARGE_PRASIOLITE_BUD = crystal("large_prasiolite_bud", 5, 3, 4);
    public static final BlockEntry<AmethystClusterBlock> PRASIOLITE_CLUSTER = REGISTRATE.object("prasiolite_cluster")
            .block(p -> new AmethystClusterBlock(7, 3, p)).properties(p -> crystalProperties(p, 5))
            .transform(TagGen.pickaxeOnly()).blockstate(() -> (ctx, prov) -> prov.createAmethystCluster(ctx.get()))
            .loot((lt, b) -> lt.add(b, lt.createOreDrop(b, MiaItems.PRASIOLITE_SHARD.get()))).simpleItem().register();
    public static final BlockEntry<Block> CAERULITE_BLOCK = mineralBlock("caerulite_block", MapColor.COLOR_BLUE, SoundType.AMETHYST);
    public static final BlockEntry<BuddingCaeruliteBlock> BUDDING_CAERULITE = REGISTRATE.object("budding_caerulite")
            .block(BuddingCaeruliteBlock::new).initialProperties(CAERULITE_BLOCK)
            .properties(p -> p.randomTicks().pushReaction(PushReaction.DESTROY)).transform(TagGen.pickaxeOnly())
            .loot((lt, b) -> lt.dropWhenSilkTouch(b)).simpleItem().register();
    public static final BlockEntry<AmethystClusterBlock> SMALL_CAERULITE_BUD = crystal("small_caerulite_bud", 3, 3, 1);
    public static final BlockEntry<AmethystClusterBlock> MEDIUM_CAERULITE_BUD = crystal("medium_caerulite_bud", 4, 3, 2);
    public static final BlockEntry<AmethystClusterBlock> LARGE_CAERULITE_BUD = crystal("large_caerulite_bud", 5, 3, 4);
    public static final BlockEntry<AmethystClusterBlock> CAERULITE_CLUSTER = REGISTRATE.object("caerulite_cluster")
            .block(p -> new AmethystClusterBlock(7, 3, p)).properties(p -> crystalProperties(p, 5))
            .transform(TagGen.pickaxeOnly()).blockstate(() -> (ctx, prov) -> prov.createAmethystCluster(ctx.get()))
            .loot((lt, b) -> lt.add(b, lt.createOreDrop(b, MiaItems.CAERULITE_SHARD.get()))).simpleItem().register();
    public static final BlockEntry<Block> SUN_STONE = REGISTRATE.object("sun_stone").block(Block::new)
            .properties(p -> p.strength(0.3F).requiresCorrectToolForDrops().lightLevel(_ -> 15).sound(SoundType.FROGLIGHT))
            .transform(TagGen.pickaxeOnly()).simpleItem().register();

    public static BlockEntry<StairBlock> stairs(BlockEntry<? extends Block> base) {
        var name = base.getId().getPath() + "_stairs";
        return REGISTRATE.object(name)
                .block(p -> new StairBlock(base.get().defaultBlockState(), p))
                .initialProperties(base)
                .tag(BlockTags.STAIRS)
                .transform(TagGen.pickaxeOnly())
                .blockstate(() -> (ctx, prov) ->
                        prov.generateStairsBlock(ctx.get(), prov.blockTexture(base.get()))
                )
                .item()
                .tag(ItemTags.STAIRS)
                .build()
                .register();
    }

    public static BlockEntry<SlabBlock> slab(BlockEntry<? extends Block> base) {
        var name = base.getId().getPath() + "_slab";
        return REGISTRATE.object(name)
                .block(SlabBlock::new)
                .initialProperties(base)
                .tag(BlockTags.SLABS)
                .transform(TagGen.pickaxeOnly())
                .blockstate(() -> (ctx, prov) ->
                        prov.generateSlabBlock(ctx.get(), BlockModelGenerators.plainVariant(prov.blockTexture(base.get()).sprite()), prov.blockTexture(base.get()))
                )
                .item()
                .tag(ItemTags.SLABS)
                .build()
                .register();
    }

    public static BlockEntry<WallBlock> wall(BlockEntry<? extends Block> base) {
        var name = base.getId().getPath() + "_wall";
        return REGISTRATE.object(name)
                .block(WallBlock::new)
                .initialProperties(base)
                .tag(BlockTags.WALLS)
                .transform(TagGen.pickaxeOnly())
                .blockstate(() -> (ctx, prov) ->
                        prov.generateWallBlock(ctx.get(), prov.blockTexture(base.get()))
                )
                .item()
                .tag(ItemTags.WALLS)
                .model(() -> (ctx, prov) -> {
                    var textures = TextureMapping.cube(prov.modBlockTexture(base.getId().getPath()));
                    var itemModel = ModelTemplates.WALL_INVENTORY.create(ctx.get(), textures, prov.modelOutput);
                    prov.createWithExistingModel(ctx.get(), itemModel);
                })
                .build()
                .register();
    }

    private static BlockBehaviour.Properties plantProperties(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.PLANT).replaceable().noCollision().instabreak()
                .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
                .ignitedByLava().pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties treeLogProperties(BlockBehaviour.Properties properties, MapColor top, MapColor side) {
        return properties.mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? top : side)
                .instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava();
    }

    private static BlockEntry<DropExperienceBlock> ore(String name, IntProvider experience, Item drop) {
        return REGISTRATE.object(name).block(p -> new DropExperienceBlock(experience, p))
                .initialProperties(ABYSS_ANDESITE).properties(p -> p.strength(4.5F, 3.0F))
                .transform(TagGen.pickaxeOnly()).loot((lt, b) -> lt.add(b, lt.createOreDrop(b, drop)))
                .simpleItem().register();
    }

    private static BlockEntry<Block> mineralBlock(String name, MapColor color, SoundType sound) {
        return REGISTRATE.object(name).block(Block::new)
                .properties(p -> p.mapColor(color).instrument(NoteBlockInstrument.BASEDRUM)
                        .requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(sound))
                .transform(TagGen.pickaxeOnly()).simpleItem().register();
    }

    private static BlockEntry<AmethystClusterBlock> crystal(String name, int height, int offset, int light) {
        return REGISTRATE.object(name).block(p -> new AmethystClusterBlock(height, offset, p)).ignore()
                .properties(p -> crystalProperties(p, light)).transform(TagGen.pickaxeOnly())
                .blockstate(() -> (ctx, prov) -> prov.createAmethystCluster(ctx.get()))
                .loot((lt, b) -> lt.dropWhenSilkTouch(b)).simpleItem().register();
    }

    private static BlockBehaviour.Properties crystalProperties(BlockBehaviour.Properties properties, int light) {
        return properties.mapColor(MapColor.COLOR_GREEN).forceSolidOn().noOcclusion()
                .sound(SoundType.AMETHYST_CLUSTER).strength(1.5F).lightLevel(_ -> light)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockEntry<FlowerBlock> flower(String name, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                                                   float duration, SoundType sound, int light) {
        return REGISTRATE.object(name)
                .block(p -> new FlowerBlock(effect, duration, p))
                .properties(p -> plantProperties(p).sound(sound).lightLevel(_ -> light))
                .blockstate(BlockStateGen::crossPlant)
                .item().model(() -> flatPlantItem("block/" + name)).build()
                .register();
    }

    private static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> flatPlantItem(String texture) {
        return (ctx, prov) -> {
            var model = ModelTemplates.FLAT_ITEM.create(ctx.get(), TextureMapping.layer0(new Material(prov.modLoc(texture))), prov.modelOutput);
            prov.createWithExistingModel(ctx.getEntry(), model);
        };
    }

    public static void register() {}
}
