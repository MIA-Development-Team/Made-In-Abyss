package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.block.abyss_andesite.AbyssAndesiteBlock;
import com.altnoir.mementoinabyss.content.block.cover_grass.CoverGrassBlock;
import com.altnoir.mementoinabyss.content.block.stripped_rotated_pillar.StrippedRotatedPillarBlock;
import com.altnoir.mementoinabyss.impl.registrate.BlockStateGen;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.altnoir.mementoinabyss.impl.registrate.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.entries.LootItem;

import java.util.Optional;

public class MiaBlocks {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    static {
        REGISTRATE.defaultCreativeTab(MiaItemGroups.BASE.getKey());
    }

    public static final BlockEntry<AbyssAndesiteBlock> ABYSS_ANDESITE = REGISTRATE.object("abyss_andesite")
            .block(AbyssAndesiteBlock::new)
            .properties(p -> p.mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE))
            .transform(TagGen.pickaxeOnly())
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

    public static final BlockEntry<CoverGrassBlock> COVERGRASS_ABYSS_ANDESITE = REGISTRATE.object("covergrass_abyss_andesite")
            .block(p -> new CoverGrassBlock(ABYSS_ANDESITE.get(), p))
            .initialProperties(ABYSS_ANDESITE)
            .properties(p -> p.mapColor(MapColor.GRASS)
                    .randomTicks())
            .blockstate(BlockStateGen::coverGrass)
            .tag(MiaTags.BlockTags.COVERGRASS.tag)
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
            .blockstate(BlockStateGen::coverGrass)
            .tag(MiaTags.BlockTags.COVERGRASS.tag)
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

    public static void register() {}
}
