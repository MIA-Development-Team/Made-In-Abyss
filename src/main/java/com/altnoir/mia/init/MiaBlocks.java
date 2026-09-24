package com.altnoir.mia.init;

import com.altnoir.abysslib.reginth.Reginth;
import com.altnoir.abysslib.reginth.providers.loot.ReginthBlockLootTables;
import com.altnoir.abysslib.reginth.util.entry.BlockEntry;
import com.altnoir.mia.MIA;
import com.altnoir.mia.common.block.*;
import com.altnoir.mia.common.block.MyceliumBlock;
import com.altnoir.mia.common.item.RopeItem;
import com.altnoir.mia.datagen.BlockStateGen;
import com.altnoir.mia.datagen.MiaLootGen;
import com.altnoir.mia.worldgen.feature.tree.MiaTreeFeatures;
import com.altnoir.mia.worldgen.feature.tree.MiaTreeGrowers;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;

public class MiaBlocks {

    /**
     * Reginth 实例（AbyssLib 的注册框架）。**所有**方块/方块物品都从这里注册；
     * 旧的 {@code DeferredRegister BLOCKS} 已经随迁移完成删掉。
     * <p>
     * {@code Reginth.create()} 在 MIA 类初始化时就已挂好 mod event bus 与 GatherDataEvent，
     * 所以这里的 datagen（blockstate / 模型 / 战利品表 / 语言键）会自动生效。
     */
    private static final Reginth REGINTH = MIA.registrate();

    /**
     * 强制初始化本类。
     * <p>
     * <b>为什么需要这个空方法</b>：Reginth 的注册表是在**类初始化时**被填充的 ——
     * {@link #REGINTH} 字段建好、然后每一条 {@code REGINTH.object(...)} 把条目交给它。
     * 如果本类直到 {@code RegisterEvent} 之后才被加载，方块就会一个都注册不上（或者更糟：
     * 别的类在 ITEM 事件里 {@code MiaBlocks.X.get()} 拿到 unbound value）。
     * 以前是靠 {@code MIA} 构造函数里的 {@code MiaBlocks.register(modEventBus)} 顺带触发的，
     * 那套 DeferredRegister 删掉之后必须显式碰一下，所以在 {@code MIA} 构造函数里调用它。
     */
    public static void bootstrap() {}

    // 深层安山岩
    // converted (closure): old provider helper = mirroredBlock
    public static final BlockEntry<AbyssAndesiteBlock> ABYSS_ANDESITE =
            REGINTH.object("abyss_andesite")
                    .block(AbyssAndesiteBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.STONE)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .strength(3.0F, 6.0F)
                                            .sound(SoundType.DEEPSLATE))
                    .blockstate(BlockStateGen::mirrored)
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            tables.createSingleItemTableWithSilkTouch(
                                                    block, MiaBlocks.ABYSS_COBBLED_ANDESITE.get())))
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();
    // 古理石
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<AbyssAndesiteBlock> MARLITH =
            REGINTH.object("marlith")
                    .block(AbyssAndesiteBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.TERRACOTTA_WHITE)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .strength(3.5F, 8.0F)
                                            .sound(SoundType.CALCITE))
                    .simpleItem()
                    .register();
    // 草方块
    // converted (closure): old provider helper = coverGrassBlock
    public static final BlockEntry<CoverGrassBlock> COVERGRASS_ABYSS_ANDESITE =
            REGINTH.object("covergrass_abyss_andesite")
                    .block(p -> new CoverGrassBlock(MiaBlocks.ABYSS_ANDESITE.get(), p))
                    .properties(
                            p ->
                                    p.mapColor(MapColor.GRASS)
                                            .randomTicks()
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .strength(3.0F, 6.0F)
                                            .sound(SoundType.DEEPSLATE))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.coverGrass(
                                            ctx, prov, MiaBlocks.ABYSS_ANDESITE.get()))
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            tables.createSingleItemTableWithSilkTouch(
                                                    block, MiaBlocks.ABYSS_COBBLED_ANDESITE.get())))
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();
    // converted (closure): old provider helper = coverGrassBlock
    public static final BlockEntry<CoverGrassBlock> COVERGRASS_TUFF =
            REGINTH.object("covergrass_tuff")
                    .block(p -> new CoverGrassBlock(Blocks.TUFF, p))
                    .properties(
                            p ->
                                    p.mapColor(MapColor.GRASS)
                                            .randomTicks()
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .strength(3.0F, 6.0F)
                                            .sound(SoundType.TUFF))
                    .blockstate(
                            (ctx, prov) -> BlockStateGen.coverGrassVanilla(ctx, prov, Blocks.TUFF))
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            tables.createSingleItemTableWithSilkTouch(
                                                    block, Blocks.TUFF)))
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();

    // 深界安山岩
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> ABYSS_ANDESITE_STAIRS =
            REGINTH.object("abyss_andesite_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.ABYSS_ANDESITE.get().defaultBlockState(), p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.ABYSS_ANDESITE.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(ctx, prov, MiaBlocks.ABYSS_ANDESITE.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> ABYSS_ANDESITE_SLAB =
            REGINTH.object("abyss_andesite_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.ABYSS_ANDESITE.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(ctx, prov, MiaBlocks.ABYSS_ANDESITE.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> ABYSS_ANDESITE_WALL =
            REGINTH.object("abyss_andesite_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(ctx, prov, MiaBlocks.ABYSS_ANDESITE.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx, prov, MiaBlocks.ABYSS_ANDESITE.get()))
                    .build()
                    .register();
    // 圆石
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> ABYSS_COBBLED_ANDESITE =
            REGINTH.object("abyss_cobbled_andesite")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE.get())
                                            .strength(3.5F, 6.0F))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> ABYSS_COBBLED_ANDESITE_STAIRS =
            REGINTH.object("abyss_cobbled_andesite_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.ABYSS_COBBLED_ANDESITE
                                                    .get()
                                                    .defaultBlockState(),
                                            p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.ABYSS_COBBLED_ANDESITE.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx, prov, MiaBlocks.ABYSS_COBBLED_ANDESITE.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> ABYSS_COBBLED_ANDESITE_SLAB =
            REGINTH.object("abyss_cobbled_andesite_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.ABYSS_COBBLED_ANDESITE.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(
                                            ctx, prov, MiaBlocks.ABYSS_COBBLED_ANDESITE.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> ABYSS_COBBLED_ANDESITE_WALL =
            REGINTH.object("abyss_cobbled_andesite_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_COBBLED_ANDESITE.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(
                                            ctx, prov, MiaBlocks.ABYSS_COBBLED_ANDESITE.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx, prov, MiaBlocks.ABYSS_COBBLED_ANDESITE.get()))
                    .build()
                    .register();
    // 苔石
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> MOSSY_ABYSS_COBBLED_ANDESITE =
            REGINTH.object("mossy_abyss_cobbled_andesite")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.ABYSS_COBBLED_ANDESITE.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS =
            REGINTH.object("mossy_abyss_cobbled_andesite_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE
                                                    .get()
                                                    .defaultBlockState(),
                                            p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> MOSSY_ABYSS_COBBLED_ANDESITE_SLAB =
            REGINTH.object("mossy_abyss_cobbled_andesite_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> MOSSY_ABYSS_COBBLED_ANDESITE_WALL =
            REGINTH.object("mossy_abyss_cobbled_andesite_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get()))
                    .build()
                    .register();
    // 磨制
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> POLISHED_ABYSS_ANDESITE =
            REGINTH.object("polished_abyss_andesite")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_COBBLED_ANDESITE.get())
                                            .sound(SoundType.POLISHED_DEEPSLATE))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> POLISHED_ABYSS_ANDESITE_STAIRS =
            REGINTH.object("polished_abyss_andesite_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.POLISHED_ABYSS_ANDESITE
                                                    .get()
                                                    .defaultBlockState(),
                                            p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.POLISHED_ABYSS_ANDESITE.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx, prov, MiaBlocks.POLISHED_ABYSS_ANDESITE.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> POLISHED_ABYSS_ANDESITE_SLAB =
            REGINTH.object("polished_abyss_andesite_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.POLISHED_ABYSS_ANDESITE.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(
                                            ctx, prov, MiaBlocks.POLISHED_ABYSS_ANDESITE.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> POLISHED_ABYSS_ANDESITE_WALL =
            REGINTH.object("polished_abyss_andesite_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.POLISHED_ABYSS_ANDESITE.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(
                                            ctx, prov, MiaBlocks.POLISHED_ABYSS_ANDESITE.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx, prov, MiaBlocks.POLISHED_ABYSS_ANDESITE.get()))
                    .build()
                    .register();
    // 柱
    // converted (closure): old provider helper = pillarBlockWithItem
    public static final BlockEntry<RotatedPillarBlock> ABYSS_ANDESITE_PILLAR =
            REGINTH.object("abyss_andesite_pillar")
                    .block(RotatedPillarBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.POLISHED_ABYSS_ANDESITE.get()))
                    .blockstate(BlockStateGen::pillar)
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();
    // converted (closure): old provider helper = columnBlockWithItem
    public static final BlockEntry<ColumnBlock> ABYSS_ANDESITE_COLUMN =
            REGINTH.object("abyss_andesite_column")
                    .block(ColumnBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.POLISHED_ABYSS_ANDESITE.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.column(
                                            ctx,
                                            prov,
                                            MiaBlocks.ABYSS_ANDESITE_PILLAR.get(),
                                            MiaBlocks.ABYSS_ANDESITE_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();

    // 切
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> CHISLED_ABYSS_ANDESITE =
            REGINTH.object("chiseled_abyss_andesite")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.POLISHED_ABYSS_ANDESITE.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // 砖
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> ABYSS_ANDESITE_BRICKS =
            REGINTH.object("abyss_andesite_bricks")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_COBBLED_ANDESITE.get())
                                            .sound(SoundType.DEEPSLATE_TILES))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> CRACKED_ABYSS_ANDESITE_BRICKS =
            REGINTH.object("cracked_abyss_andesite_bricks")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.ABYSS_ANDESITE_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> ABYSS_ANDESITE_BRICKS_STAIRS =
            REGINTH.object("abyss_andesite_bricks_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.ABYSS_ANDESITE_BRICKS
                                                    .get()
                                                    .defaultBlockState(),
                                            p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.ABYSS_ANDESITE_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx, prov, MiaBlocks.ABYSS_ANDESITE_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> ABYSS_ANDESITE_BRICKS_SLAB =
            REGINTH.object("abyss_andesite_bricks_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.ABYSS_ANDESITE_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(
                                            ctx, prov, MiaBlocks.ABYSS_ANDESITE_BRICKS.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> ABYSS_ANDESITE_BRICKS_WALL =
            REGINTH.object("abyss_andesite_bricks_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE_BRICKS.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(
                                            ctx, prov, MiaBlocks.ABYSS_ANDESITE_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx, prov, MiaBlocks.ABYSS_ANDESITE_BRICKS.get()))
                    .build()
                    .register();

    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> MOSSY_ABYSS_ANDESITE_BRICKS =
            REGINTH.object("mossy_abyss_andesite_bricks")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.ABYSS_ANDESITE_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS =
            REGINTH.object("mossy_abyss_andesite_bricks_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS
                                                    .get()
                                                    .defaultBlockState(),
                                            p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx, prov, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> MOSSY_ABYSS_ANDESITE_BRICKS_SLAB =
            REGINTH.object("mossy_abyss_andesite_bricks_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(
                                            ctx, prov, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> MOSSY_ABYSS_ANDESITE_BRICKS_WALL =
            REGINTH.object("mossy_abyss_andesite_bricks_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(
                                            ctx, prov, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx, prov, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get()))
                    .build()
                    .register();

    // 化石树
    // converted (closure): old provider helper = variantPillarBlockWithItem
    public static final BlockEntry<StrippedRotatedPillarBlock> FOSSILIZED_LOG =
            REGINTH.object("fossilized_log")
                    .block(StrippedRotatedPillarBlock::new)
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
                    .blockstate((ctx, prov) -> BlockStateGen.variantPillar(ctx, prov, 3))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model((ctx, prov) -> BlockStateGen.itemParent(ctx, prov, "0"))
                    .build()
                    .register();
    // converted (closure): old provider helper = variantAxisBlockWithItem
    public static final BlockEntry<StrippedRotatedPillarBlock> FOSSILIZED_WOOD =
            REGINTH.object("fossilized_wood")
                    .block(StrippedRotatedPillarBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.FOSSILIZED_LOG.get())
                                            .mapColor(MapColor.PODZOL)
                                            .strength(3.0F, 4.2F))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.variantAxis(
                                            ctx, prov, MiaBlocks.FOSSILIZED_LOG.get(), 3))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model((ctx, prov) -> BlockStateGen.itemParent(ctx, prov, "0"))
                    .build()
                    .register();
    // converted (closure): old provider helper = variantPillarBlockWithItem
    public static final BlockEntry<StrippedRotatedPillarBlock> STRIPPED_FOSSILIZED_LOG =
            REGINTH.object("stripped_fossilized_log")
                    .block(StrippedRotatedPillarBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.FOSSILIZED_LOG.get())
                                            .mapColor(MapColor.COLOR_BLACK))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.variantPillar(
                                            ctx, prov, 5, new int[] {12, 1, 1, 1, 1}))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model((ctx, prov) -> BlockStateGen.itemParent(ctx, prov, "0"))
                    .build()
                    .register();
    // converted (closure): old provider helper = variantAxisBlockWithItem
    public static final BlockEntry<StrippedRotatedPillarBlock> STRIPPED_FOSSILIZED_WOOD =
            REGINTH.object("stripped_fossilized_wood")
                    .block(StrippedRotatedPillarBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.FOSSILIZED_WOOD.get())
                                            .mapColor(MapColor.COLOR_BLACK))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.variantAxis(
                                            ctx,
                                            prov,
                                            MiaBlocks.STRIPPED_FOSSILIZED_LOG.get(),
                                            5,
                                            new int[] {12, 1, 1, 1, 1}))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model((ctx, prov) -> BlockStateGen.itemParent(ctx, prov, "0"))
                    .build()
                    .register();

    // converted (closure): old provider helper = variantPillarBlockWithItem
    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_FOSSILIZED_LOG =
            REGINTH.object("mossy_fossilized_log")
                    .block(StrippedRotatedPillarBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.FOSSILIZED_LOG.get()))
                    .blockstate((ctx, prov) -> BlockStateGen.variantPillar(ctx, prov, 4))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model((ctx, prov) -> BlockStateGen.itemParent(ctx, prov, "0"))
                    .build()
                    .register();
    // converted (closure): old provider helper = variantAxisBlockWithItem
    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_FOSSILIZED_WOOD =
            REGINTH.object("mossy_fossilized_wood")
                    .block(StrippedRotatedPillarBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.FOSSILIZED_WOOD.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.variantAxis(
                                            ctx, prov, MiaBlocks.MOSSY_FOSSILIZED_LOG.get(), 4))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model((ctx, prov) -> BlockStateGen.itemParent(ctx, prov, "0"))
                    .build()
                    .register();
    // converted (closure): old provider helper = variantPillarBlockWithItem
    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_STRIPPED_FOSSILIZED_LOG =
            REGINTH.object("mossy_stripped_fossilized_log")
                    .block(StrippedRotatedPillarBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.STRIPPED_FOSSILIZED_LOG.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.variantPillar(
                                            ctx, prov, 5, new int[] {12, 1, 1, 1, 1}))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model((ctx, prov) -> BlockStateGen.itemParent(ctx, prov, "0"))
                    .build()
                    .register();
    // converted (closure): old provider helper = variantAxisBlockWithItem
    public static final BlockEntry<StrippedRotatedPillarBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD =
            REGINTH.object("mossy_stripped_fossilized_wood")
                    .block(StrippedRotatedPillarBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.variantAxis(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_LOG.get(),
                                            5,
                                            new int[] {12, 1, 1, 1, 1}))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model((ctx, prov) -> BlockStateGen.itemParent(ctx, prov, "0"))
                    .build()
                    .register();
    // 磨制
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> POLISHED_FOSSILIZED_WOOD =
            REGINTH.object("polished_fossilized_wood")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.FOSSILIZED_WOOD.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> POLISHED_FOSSILIZED_WOOD_STAIRS =
            REGINTH.object("polished_fossilized_wood_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.POLISHED_FOSSILIZED_WOOD
                                                    .get()
                                                    .defaultBlockState(),
                                            p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.POLISHED_FOSSILIZED_WOOD.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx, prov, MiaBlocks.POLISHED_FOSSILIZED_WOOD.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> POLISHED_FOSSILIZED_WOOD_SLAB =
            REGINTH.object("polished_fossilized_wood_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.POLISHED_FOSSILIZED_WOOD.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(
                                            ctx, prov, MiaBlocks.POLISHED_FOSSILIZED_WOOD.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> POLISHED_FOSSILIZED_WOOD_WALL =
            REGINTH.object("polished_fossilized_wood_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.POLISHED_FOSSILIZED_WOOD.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(
                                            ctx, prov, MiaBlocks.POLISHED_FOSSILIZED_WOOD.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx, prov, MiaBlocks.POLISHED_FOSSILIZED_WOOD.get()))
                    .build()
                    .register();

    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> POLISHED_STRIPPED_FOSSILIZED_WOOD =
            REGINTH.object("polished_stripped_fossilized_wood")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> POLISHED_STRIPPED_FOSSILIZED_WOOD_STAIRS =
            REGINTH.object("polished_stripped_fossilized_wood_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD
                                                    .get()
                                                    .defaultBlockState(),
                                            p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx,
                                            prov,
                                            MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> POLISHED_STRIPPED_FOSSILIZED_WOOD_SLAB =
            REGINTH.object("polished_stripped_fossilized_wood_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(
                                            ctx,
                                            prov,
                                            MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> POLISHED_STRIPPED_FOSSILIZED_WOOD_WALL =
            REGINTH.object("polished_stripped_fossilized_wood_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD
                                                            .get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(
                                            ctx,
                                            prov,
                                            MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx,
                                            prov,
                                            MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get()))
                    .build()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> CHISLED_STRIPPED_FOSSILIZED_WOOD =
            REGINTH.object("chiseled_stripped_fossilized_wood")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // 砖
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> FOSSILIZED_WOOD_BRICKS =
            REGINTH.object("fossilized_wood_bricks")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.FOSSILIZED_WOOD.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> FOSSILIZED_WOOD_BRICKS_STAIRS =
            REGINTH.object("fossilized_wood_bricks_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.FOSSILIZED_WOOD_BRICKS
                                                    .get()
                                                    .defaultBlockState(),
                                            p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.FOSSILIZED_WOOD_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx, prov, MiaBlocks.FOSSILIZED_WOOD_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> FOSSILIZED_WOOD_BRICKS_SLAB =
            REGINTH.object("fossilized_wood_bricks_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.FOSSILIZED_WOOD_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(
                                            ctx, prov, MiaBlocks.FOSSILIZED_WOOD_BRICKS.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> FOSSILIZED_WOOD_BRICKS_WALL =
            REGINTH.object("fossilized_wood_bricks_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.FOSSILIZED_WOOD_BRICKS.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(
                                            ctx, prov, MiaBlocks.FOSSILIZED_WOOD_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx, prov, MiaBlocks.FOSSILIZED_WOOD_BRICKS.get()))
                    .build()
                    .register();

    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> STRIPPED_FOSSILIZED_WOOD_BRICKS =
            REGINTH.object("stripped_fossilized_wood_bricks")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS =
            REGINTH.object("stripped_fossilized_wood_bricks_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS
                                                    .get()
                                                    .defaultBlockState(),
                                            p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx,
                                            prov,
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB =
            REGINTH.object("stripped_fossilized_wood_bricks_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(
                                            ctx,
                                            prov,
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL =
            REGINTH.object("stripped_fossilized_wood_bricks_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(
                                            ctx,
                                            prov,
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx,
                                            prov,
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .build()
                    .register();
    // 苔藓砖
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> MOSSY_FOSSILIZED_WOOD_BRICKS =
            REGINTH.object("mossy_fossilized_wood_bricks")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.FOSSILIZED_WOOD.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> MOSSY_FOSSILIZED_WOOD_BRICKS_STAIRS =
            REGINTH.object("mossy_fossilized_wood_bricks_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS
                                                    .get()
                                                    .defaultBlockState(),
                                            p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> MOSSY_FOSSILIZED_WOOD_BRICKS_SLAB =
            REGINTH.object("mossy_fossilized_wood_bricks_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> MOSSY_FOSSILIZED_WOOD_BRICKS_WALL =
            REGINTH.object("mossy_fossilized_wood_bricks_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS.get()))
                    .build()
                    .register();

    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS =
            REGINTH.object("mossy_stripped_fossilized_wood_bricks")
                    .block(Block::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS =
            REGINTH.object("mossy_stripped_fossilized_wood_bricks_stairs")
                    .block(
                            p ->
                                    new StairBlock(
                                            MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS
                                                    .get()
                                                    .defaultBlockState(),
                                            p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB =
            REGINTH.object("mossy_stripped_fossilized_wood_bricks_slab")
                    .block(SlabBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = wallBlockWithItem
    public static final BlockEntry<WallBlock> MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL =
            REGINTH.object("mossy_stripped_fossilized_wood_bricks_wall")
                    .block(WallBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS
                                                            .get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wall(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.wallItem(
                                            ctx,
                                            prov,
                                            MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS.get()))
                    .build()
                    .register();

    // 天雾树
    // 大面积迁移样板：方块状态/模型/战利品表/语言键全部由 Reginth 的 builder 链驱动，
    // 对应关系与旧的 MiaBlockStateProvider / MiaBlockLootTable 逐项对齐（见 BlockStateGen 的类注释）。
    public static final BlockEntry<MiaWoodBlock> SKYFOG_LOG =
            REGINTH.object("skyfog_log")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(
                                                    state ->
                                                            state.getValue(RotatedPillarBlock.AXIS)
                                                                            == Direction.Axis.Y
                                                                    ? MapColor.WOOD
                                                                    : MapColor.PODZOL)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate(BlockStateGen::log)
                    .simpleItem()
                    .register();

    public static final BlockEntry<MiaWoodBlock> SKYFOG_WOOD =
            REGINTH.object("skyfog_wood")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.WOOD)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate((ctx, prov) -> BlockStateGen.wood(ctx, prov, SKYFOG_LOG.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<MiaWoodBlock> STRIPPED_SKYFOG_LOG =
            REGINTH.object("stripped_skyfog_log")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(state -> MapColor.WOOD)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate(BlockStateGen::log)
                    .simpleItem()
                    .register();

    public static final BlockEntry<MiaWoodBlock> STRIPPED_SKYFOG_WOOD =
            REGINTH.object("stripped_skyfog_wood")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.WOOD)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate(
                            (ctx, prov) -> BlockStateGen.wood(ctx, prov, STRIPPED_SKYFOG_LOG.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<MiaPlankBlock> SKYFOG_PLANKS =
            REGINTH.object("skyfog_planks")
                    .block(MiaPlankBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_GREEN)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F, 3.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .simpleItem()
                    .register();

    public static final BlockEntry<StairBlock> SKYFOG_STAIRS =
            REGINTH.object("skyfog_stairs")
                    .block(p -> new StairBlock(SKYFOG_PLANKS.get().defaultBlockState(), p))
                    .properties(p -> BlockBehaviour.Properties.ofFullCopy(SKYFOG_PLANKS.get()))
                    .blockstate((ctx, prov) -> BlockStateGen.stairs(ctx, prov, SKYFOG_PLANKS.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<SlabBlock> SKYFOG_SLAB =
            REGINTH.object("skyfog_slab")
                    .block(SlabBlock::new)
                    .properties(p -> BlockBehaviour.Properties.ofFullCopy(SKYFOG_PLANKS.get()))
                    .blockstate((ctx, prov) -> BlockStateGen.slab(ctx, prov, SKYFOG_PLANKS.get()))
                    // 台阶不能用默认的 dropSelf：type=double 时要掉 2 个
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();

    public static final BlockEntry<FenceBlock> SKYFOG_FENCE =
            REGINTH.object("skyfog_fence")
                    .block(FenceBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(SKYFOG_PLANKS.get())
                                            .forceSolidOn())
                    .blockstate((ctx, prov) -> BlockStateGen.fence(ctx, prov, SKYFOG_PLANKS.get()))
                    .item()
                    .model((ctx, prov) -> BlockStateGen.fenceItem(ctx, prov, SKYFOG_PLANKS.get()))
                    .build()
                    .register();

    public static final BlockEntry<FenceGateBlock> SKYFOG_FENCE_GATE =
            REGINTH.object("skyfog_fence_gate")
                    .block(p -> new FenceGateBlock(WoodType.BAMBOO, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(SKYFOG_PLANKS.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) -> BlockStateGen.fenceGate(ctx, prov, SKYFOG_PLANKS.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<DoorBlock> SKYFOG_DOOR =
            REGINTH.object("skyfog_door")
                    .block(p -> new DoorBlock(BlockSetType.BAMBOO, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(SKYFOG_PLANKS.get().defaultMapColor())
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(3.0F)
                                            .noOcclusion()
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::door)
                    // 门不能用默认的 dropSelf：只有下半部分（half=lower）掉一个
                    .loot((tables, block) -> tables.add(block, tables.createDoorTable(block)))
                    .item()
                    .model(BlockStateGen::doorItem)
                    .build()
                    .register();

    public static final BlockEntry<TrapDoorBlock> SKYFOG_TRAPDOOR =
            REGINTH.object("skyfog_trapdoor")
                    .block(p -> new TrapDoorBlock(BlockSetType.BAMBOO, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.WOOD)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(3.0F)
                                            .noOcclusion()
                                            // 注意：isValidSpawn 要的是 4 参谓词（BlockState, BlockGetter,
                                            // BlockPos, EntityType），
                                            // 所以用原版的 Blocks::never，不是本类那个 3 参的 MiaBlocks::never。
                                            .isValidSpawn(Blocks::never)
                                            .ignitedByLava())
                    .blockstate(BlockStateGen::trapdoor)
                    // Reginth 的自动物品模型在活板门上会落到不存在的 mia:block/<name>，要显式指定 _bottom
                    .item()
                    .model(BlockStateGen::trapdoorItem)
                    .build()
                    .register();

    public static final BlockEntry<SaplingBlock> SKYFOG_SAPLING =
            REGINTH.object("skyfog_sapling")
                    .block(p -> new SaplingBlock(MiaTreeGrowers.SKYFOG_TREE, p))
                    .properties(
                            p ->
                                    p.mapColor(MapColor.PLANT)
                                            .noCollission()
                                            .randomTicks()
                                            .instabreak()
                                            .sound(SoundType.CHERRY_SAPLING)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::bush)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();

    public static final BlockEntry<MiaLeavesBlock> SKYFOG_LEAVES =
            REGINTH.object("skyfog_leaves")
                    .block(MiaLeavesBlock::new)
                    .properties(p -> leavesProps(SoundType.AZALEA_LEAVES))
                    .blockstate(BlockStateGen::leaves)
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            MiaLootGen.skyfogLeaves(
                                                    tables, block, SKYFOG_SAPLING.get())))
                    .simpleItem()
                    .register();

    public static final BlockEntry<MiaLeavesBlock> SKYFOG_LEAVES_WITH_FRUITS =
            REGINTH.object("skyfog_leaves_with_fruits")
                    .block(MiaLeavesBlock::new)
                    .properties(p -> leavesProps(SoundType.AZALEA_LEAVES))
                    .blockstate(BlockStateGen::leaves)
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            MiaLootGen.skyfogLeavesWithFruits(
                                                    tables,
                                                    block,
                                                    SKYFOG_SAPLING.get(),
                                                    MiaItems.MISTFUZZ_PEACH.get())))
                    .simpleItem()
                    .register();

    // 翠寂菌
    // converted (closure): old provider helper = logBlockWithItem
    public static final BlockEntry<MiaWoodBlock> VERDANT_STEM =
            REGINTH.object("verdant_stem")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(
                                                    state ->
                                                            state.getValue(RotatedPillarBlock.AXIS)
                                                                            == Direction.Axis.Y
                                                                    ? MapColor.WOOD
                                                                    : MapColor.TERRACOTTA_YELLOW)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate(BlockStateGen::log)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = woodBlockWithItem
    public static final BlockEntry<MiaWoodBlock> VERDANT_HYPHAE =
            REGINTH.object("verdant_hyphae")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.WOOD)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wood(ctx, prov, MiaBlocks.VERDANT_STEM.get()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = logBlockWithItem
    public static final BlockEntry<MiaWoodBlock> STRIPPED_VERDANT_STEM =
            REGINTH.object("stripped_verdant_stem")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(
                                                    state ->
                                                            state.getValue(RotatedPillarBlock.AXIS)
                                                                            == Direction.Axis.Y
                                                                    ? MapColor.WOOD
                                                                    : MapColor.TERRACOTTA_RED)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate(BlockStateGen::log)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = woodBlockWithItem
    public static final BlockEntry<MiaWoodBlock> STRIPPED_VERDANT_HYPHAE =
            REGINTH.object("stripped_verdant_hyphae")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.TERRACOTTA_RED)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wood(
                                            ctx, prov, MiaBlocks.STRIPPED_VERDANT_STEM.get()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<MiaPlankBlock> VERDANT_PLANKS =
            REGINTH.object("verdant_planks")
                    .block(MiaPlankBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.TERRACOTTA_YELLOW)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F, 3.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> VERDANT_STAIRS =
            REGINTH.object("verdant_stairs")
                    .block(p -> new StairBlock(VERDANT_PLANKS.get().defaultBlockState(), p))
                    .properties(p -> BlockBehaviour.Properties.ofFullCopy(VERDANT_PLANKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(ctx, prov, MiaBlocks.VERDANT_PLANKS.get()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> VERDANT_SLAB =
            REGINTH.object("verdant_slab")
                    .block(SlabBlock::new)
                    .properties(p -> BlockBehaviour.Properties.ofFullCopy(VERDANT_PLANKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(ctx, prov, MiaBlocks.VERDANT_PLANKS.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = fenceBlockWithItem
    public static final BlockEntry<FenceBlock> VERDANT_FENCE =
            REGINTH.object("verdant_fence")
                    .block(FenceBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(VERDANT_PLANKS.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.fence(ctx, prov, MiaBlocks.VERDANT_PLANKS.get()))
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.fenceItem(
                                            ctx, prov, MiaBlocks.VERDANT_PLANKS.get()))
                    .build()
                    .register();
    // converted (closure): old provider helper = fenceGateBlockWithItem
    public static final BlockEntry<FenceGateBlock> VERDANT_FENCE_GATE =
            REGINTH.object("verdant_fence_gate")
                    .block(p -> new FenceGateBlock(WoodType.BAMBOO, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(VERDANT_PLANKS.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.fenceGate(
                                            ctx, prov, MiaBlocks.VERDANT_PLANKS.get()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = doorBlockWithItem
    public static final BlockEntry<DoorBlock> VERDANT_DOOR =
            REGINTH.object("verdant_door")
                    .block(p -> new DoorBlock(BlockSetType.BAMBOO, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(VERDANT_PLANKS.get().defaultMapColor())
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(3.0F)
                                            .noOcclusion()
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::door)
                    .loot((tables, block) -> tables.add(block, tables.createDoorTable(block)))
                    .item()
                    .model(BlockStateGen::doorItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = trapdoorBlockWithItem
    public static final BlockEntry<TrapDoorBlock> VERDANT_TRAPDOOR =
            REGINTH.object("verdant_trapdoor")
                    .block(p -> new TrapDoorBlock(BlockSetType.BAMBOO, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.TERRACOTTA_YELLOW)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(3.0F)
                                            .noOcclusion()
                                            .isValidSpawn(Blocks::never)
                                            .ignitedByLava())
                    .blockstate(BlockStateGen::trapdoor)
                    .item()
                    .model(BlockStateGen::trapdoorItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = leavesBlock
    public static final BlockEntry<SlimeBlock> VERDANT_LEAVES =
            REGINTH.object("verdant_leaves")
                    .block(SlimeBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_GREEN)
                                            .strength(1.0F)
                                            .sound(SoundType.WART_BLOCK)
                                            .noOcclusion()
                                            .lightLevel(state -> 15)
                                            .isValidSpawn(Blocks::never)
                                            .isRedstoneConductor(MiaBlocks::never)
                                            .isSuffocating(MiaBlocks::never)
                                            .isViewBlocking(MiaBlocks::never))
                    .blockstate((ctx, prov) -> BlockStateGen.leaves(ctx, prov, "translucent"))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = bushBlock
    public static final BlockEntry<MiaFungusBlock> VERDANT_FUNGUS =
            REGINTH.object("verdant_fungus")
                    .block(p -> new MiaFungusBlock(MiaTreeFeatures.VERDANT_FUNGUS, p))
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_CYAN)
                                            .instabreak()
                                            .noCollission()
                                            .sound(SoundType.CHERRY_SAPLING)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::bush)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // ==================== 太初菌 / 菌丝（移植自 PoopSky） ====================
    // 命名与原模组一一对应；贴图、模型、掉落、特性均按原实现还原。
    // 注意 primo_stem / primo_hyphae 用的是原版 RotatedPillarBlock 而不是 MiaWoodBlock：
    // 前者是"下界木"性质（SoundType.STEM / NETHER_WOOD、不可燃），MiaWoodBlock 会带可燃性。

    public static final BlockEntry<MyceliumBlock> MYCELIUM_BLOCK =
            REGINTH.object("mycelium_block")
                    .block(MyceliumBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_PURPLE)
                                            .strength(0.1F)
                                            .sound(SoundType.MOSS)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::rotationYCubeAll)
                    .simpleItem()
                    .register();

    public static final BlockEntry<MyceliumMatBlock> MYCELIUM_MAT =
            REGINTH.object("mycelium_mat")
                    .block(MyceliumMatBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_PURPLE)
                                            .replaceable()
                                            .noCollission()
                                            .strength(0.2F)
                                            .sound(SoundType.GLOW_LICHEN)
                                            .lightLevel(GlowLichenBlock.emission(3))
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::multifaceMat)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();

    public static final BlockEntry<PinkPetalsBlock> MUSHROOM_BED =
            REGINTH.object("mushroom_bed")
                    .block(PinkPetalsBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.TERRACOTTA_RED)
                                            .noCollission()
                                            .sound(SoundType.PINK_PETALS)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::flowerBed)
                    .loot((tables, block) -> tables.add(block, tables.createPetalsDrops(block)))
                    .item()
                    .model(BlockStateGen::aloneItem)
                    .build()
                    .register();

    // 菌柄/菌核用 StrippedRotatedPillarBlock：它只加"斧头去皮"（映射见该类的 getStrippables），
    // 不像 MiaWoodBlock 那样额外加可燃性 —— 原模组的太初木是"下界木"性质，不可燃。
    public static final BlockEntry<StrippedRotatedPillarBlock> PRIMO_STEM =
            REGINTH.object("primo_stem")
                    .block(StrippedRotatedPillarBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_ORANGE)
                                            .strength(2.0F)
                                            .sound(SoundType.STEM)
                                            .instrument(NoteBlockInstrument.BASS))
                    .blockstate(BlockStateGen::log)
                    .simpleItem()
                    .register();

    public static final BlockEntry<StrippedRotatedPillarBlock> PRIMO_HYPHAE =
            REGINTH.object("primo_hyphae")
                    .block(StrippedRotatedPillarBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_ORANGE)
                                            .strength(2.0F)
                                            .sound(SoundType.STEM)
                                            .instrument(NoteBlockInstrument.BASS))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wood(ctx, prov, MiaBlocks.PRIMO_STEM.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_PRIMO_STEM =
            REGINTH.object("stripped_primo_stem")
                    .block(RotatedPillarBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_ORANGE)
                                            .strength(2.0F)
                                            .sound(SoundType.STEM)
                                            .instrument(NoteBlockInstrument.BASS))
                    .blockstate(BlockStateGen::log)
                    .simpleItem()
                    .register();

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_PRIMO_HYPHAE =
            REGINTH.object("stripped_primo_hyphae")
                    .block(RotatedPillarBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_ORANGE)
                                            .strength(2.0F)
                                            .sound(SoundType.STEM)
                                            .instrument(NoteBlockInstrument.BASS))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wood(
                                            ctx, prov, MiaBlocks.STRIPPED_PRIMO_STEM.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<Block> PRIMO_PLANKS =
            REGINTH.object("primo_planks")
                    .block(Block::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_ORANGE)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F, 3.0F)
                                            .sound(SoundType.NETHER_WOOD))
                    .simpleItem()
                    .register();

    public static final BlockEntry<StairBlock> PRIMO_STAIRS =
            REGINTH.object("primo_stairs")
                    .block(p -> new StairBlock(PRIMO_PLANKS.get().defaultBlockState(), p))
                    .properties(p -> BlockBehaviour.Properties.ofFullCopy(PRIMO_PLANKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(ctx, prov, MiaBlocks.PRIMO_PLANKS.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<SlabBlock> PRIMO_SLAB =
            REGINTH.object("primo_slab")
                    .block(SlabBlock::new)
                    .properties(p -> BlockBehaviour.Properties.ofFullCopy(PRIMO_PLANKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(ctx, prov, MiaBlocks.PRIMO_PLANKS.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();

    public static final BlockEntry<FenceBlock> PRIMO_FENCE =
            REGINTH.object("primo_fence")
                    .block(FenceBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_FENCE)
                                            .mapColor(MapColor.COLOR_ORANGE))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.fence(ctx, prov, MiaBlocks.PRIMO_PLANKS.get()))
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.fenceItem(
                                            ctx, prov, MiaBlocks.PRIMO_PLANKS.get()))
                    .build()
                    .register();

    public static final BlockEntry<FenceGateBlock> PRIMO_FENCE_GATE =
            REGINTH.object("primo_fence_gate")
                    .block(p -> new FenceGateBlock(WoodType.CRIMSON, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_FENCE_GATE)
                                            .mapColor(MapColor.COLOR_ORANGE))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.fenceGate(
                                            ctx, prov, MiaBlocks.PRIMO_PLANKS.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<DoorBlock> PRIMO_DOOR =
            REGINTH.object("primo_door")
                    .block(p -> new DoorBlock(BlockSetType.CRIMSON, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_DOOR)
                                            .mapColor(MapColor.COLOR_YELLOW))
                    .blockstate(BlockStateGen::door)
                    .loot((tables, block) -> tables.add(block, tables.createDoorTable(block)))
                    .item()
                    .model(BlockStateGen::doorItem)
                    .build()
                    .register();

    public static final BlockEntry<TrapDoorBlock> PRIMO_TRAPDOOR =
            REGINTH.object("primo_trapdoor")
                    .block(p -> new TrapDoorBlock(BlockSetType.CRIMSON, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_TRAPDOOR)
                                            .mapColor(MapColor.COLOR_YELLOW))
                    .blockstate(BlockStateGen::trapdoor)
                    .item()
                    .model(BlockStateGen::trapdoorItem)
                    .build()
                    .register();

    public static final BlockEntry<PressurePlateBlock> PRIMO_PRESSURE_PLATE =
            REGINTH.object("primo_pressure_plate")
                    .block(p -> new PressurePlateBlock(BlockSetType.CRIMSON, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                            Blocks.CRIMSON_PRESSURE_PLATE))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.pressurePlate(
                                            ctx, prov, MiaBlocks.PRIMO_PLANKS.get()))
                    .simpleItem()
                    .register();

    public static final BlockEntry<ButtonBlock> PRIMO_BUTTON =
            REGINTH.object("primo_button")
                    .block(p -> new ButtonBlock(BlockSetType.CRIMSON, 30, p))
                    .properties(p -> BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_BUTTON))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.button(ctx, prov, MiaBlocks.PRIMO_PLANKS.get()))
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.buttonItem(
                                            ctx, prov, MiaBlocks.PRIMO_PLANKS.get()))
                    .build()
                    .register();

    public static final BlockEntry<PrimoCapBlock> PRIMO_CAP =
            REGINTH.object("primo_cap")
                    .block(PrimoCapBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_YELLOW)
                                            .strength(1.0F)
                                            .sound(SoundType.WART_BLOCK))
                    .simpleItem()
                    .register();

    public static final BlockEntry<GlowPrimoCapBlock> GLOW_PRIMO_CAP =
            REGINTH.object("glow_primo_cap")
                    .block(GlowPrimoCapBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_BLUE)
                                            .strength(1.0F)
                                            .sound(SoundType.WART_BLOCK)
                                            .noOcclusion()
                                            .lightLevel(state -> 12))
                    .blockstate(BlockStateGen::translucentCubeAll)
                    .simpleItem()
                    .register();

    // 注意：这两个真菌**故意不加 noCollission**（原实现如此）——菌盖可以站上去并被弹起。
    public static final BlockEntry<PrimoFungusBlock> PRIMO_FUNGUS =
            REGINTH.object("primo_fungus")
                    .block(p -> new PrimoFungusBlock(MiaTreeFeatures.PRIMO_FUNGUS, p))
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_ORANGE)
                                            .instabreak()
                                            .sound(SoundType.FUNGUS)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.mushroomFungus(
                                            ctx, prov, MiaBlocks.PRIMO_PLANKS.get()))
                    .item()
                    .model(BlockStateGen::aloneItem)
                    .build()
                    .register();

    public static final BlockEntry<PrimoFungusBlock> GLOW_PRIMO_FUNGUS =
            REGINTH.object("glow_primo_fungus")
                    .block(p -> new PrimoFungusBlock(MiaTreeFeatures.GLOW_PRIMO_FUNGUS, p))
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_ORANGE)
                                            .instabreak()
                                            .lightLevel(state -> 7)
                                            .sound(SoundType.FUNGUS)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.glowMushroomFungus(
                                            ctx, prov, MiaBlocks.GLOW_PRIMO_CAP.get()))
                    .item()
                    .model(BlockStateGen::aloneItem)
                    .build()
                    .register();

    // 倒悬树
    // converted (closure): old provider helper = logBlockWithItem
    public static final BlockEntry<MiaWoodBlock> INVERTED_LOG =
            REGINTH.object("inverted_log")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(
                                                    state ->
                                                            state.getValue(RotatedPillarBlock.AXIS)
                                                                            == Direction.Axis.Y
                                                                    ? MapColor.WOOD
                                                                    : MapColor.PODZOL)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate(BlockStateGen::log)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = woodBlockWithItem
    public static final BlockEntry<MiaWoodBlock> INVERTED_WOOD =
            REGINTH.object("inverted_wood")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.WOOD)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wood(ctx, prov, MiaBlocks.INVERTED_LOG.get()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = logBlockWithItem
    public static final BlockEntry<MiaWoodBlock> STRIPPED_INVERTED_LOG =
            REGINTH.object("stripped_inverted_log")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(state -> MapColor.WOOD)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate(BlockStateGen::log)
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = woodBlockWithItem
    public static final BlockEntry<MiaWoodBlock> STRIPPED_INVERTED_WOOD =
            REGINTH.object("stripped_inverted_wood")
                    .block(MiaWoodBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.WOOD)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.wood(
                                            ctx, prov, MiaBlocks.STRIPPED_INVERTED_LOG.get()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<MiaPlankBlock> INVERTED_PLANKS =
            REGINTH.object("inverted_planks")
                    .block(MiaPlankBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_GREEN)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F, 3.0F)
                                            .sound(SoundType.WOOD)
                                            .ignitedByLava())
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = stairsBlockWithItem
    public static final BlockEntry<StairBlock> INVERTED_STAIRS =
            REGINTH.object("inverted_stairs")
                    .block(p -> new StairBlock(INVERTED_PLANKS.get().defaultBlockState(), p))
                    .properties(p -> BlockBehaviour.Properties.ofFullCopy(INVERTED_PLANKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.stairs(
                                            ctx, prov, MiaBlocks.INVERTED_PLANKS.get()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = slabBlockWithItem
    public static final BlockEntry<SlabBlock> INVERTED_SLAB =
            REGINTH.object("inverted_slab")
                    .block(SlabBlock::new)
                    .properties(p -> BlockBehaviour.Properties.ofFullCopy(INVERTED_PLANKS.get()))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.slab(ctx, prov, MiaBlocks.INVERTED_PLANKS.get()))
                    .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = fenceBlockWithItem
    public static final BlockEntry<FenceBlock> INVERTED_FENCE =
            REGINTH.object("inverted_fence")
                    .block(FenceBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(INVERTED_PLANKS.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.fence(ctx, prov, MiaBlocks.INVERTED_PLANKS.get()))
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.fenceItem(
                                            ctx, prov, MiaBlocks.INVERTED_PLANKS.get()))
                    .build()
                    .register();
    // converted (closure): old provider helper = fenceGateBlockWithItem
    public static final BlockEntry<FenceGateBlock> INVERTED_FENCE_GATE =
            REGINTH.object("inverted_fence_gate")
                    .block(p -> new FenceGateBlock(WoodType.CHERRY, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(INVERTED_PLANKS.get())
                                            .forceSolidOn())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.fenceGate(
                                            ctx, prov, MiaBlocks.INVERTED_PLANKS.get()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = doorBlockWithItem
    public static final BlockEntry<DoorBlock> INVERTED_DOOR =
            REGINTH.object("inverted_door")
                    .block(p -> new DoorBlock(BlockSetType.CHERRY, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(INVERTED_PLANKS.get().defaultMapColor())
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(3.0F)
                                            .noOcclusion()
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::door)
                    .loot((tables, block) -> tables.add(block, tables.createDoorTable(block)))
                    .item()
                    .model(BlockStateGen::doorItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = trapdoorBlockWithItem
    public static final BlockEntry<TrapDoorBlock> INVERTED_TRAPDOOR =
            REGINTH.object("inverted_trapdoor")
                    .block(p -> new TrapDoorBlock(BlockSetType.CHERRY, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.WOOD)
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(3.0F)
                                            .noOcclusion()
                                            .isValidSpawn(Blocks::never)
                                            .ignitedByLava())
                    .blockstate(BlockStateGen::trapdoor)
                    .item()
                    .model(BlockStateGen::trapdoorItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = leavesBlock
    public static final BlockEntry<MiaLeavesBlock> INVERTED_LEAVES =
            REGINTH.object("inverted_leaves")
                    .block(MiaLeavesBlock::new)
                    .properties(p -> leavesProps(SoundType.CHERRY_LEAVES))
                    .blockstate(BlockStateGen::leaves)
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            MiaLootGen.plainLeaves(
                                                    tables,
                                                    block,
                                                    MiaBlocks.INVERTED_SAPLING.get())))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = bushBlock
    public static final BlockEntry<InvertedSaplingBlock> INVERTED_SAPLING =
            REGINTH.object("inverted_sapling")
                    .block(p -> new InvertedSaplingBlock(MiaTreeGrowers.INVERTED_TREE, p))
                    .properties(
                            p ->
                                    p.mapColor(MapColor.PLANT)
                                            .noCollission()
                                            .randomTicks()
                                            .instabreak()
                                            .sound(SoundType.CHERRY_SAPLING)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::bush)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();

    // 开关
    // converted (closure): old provider helper = pressurePlateBlockWithItem
    public static final BlockEntry<PressurePlateBlock> SKYFOG_PRESSURE_PLATE =
            REGINTH.object("skyfog_pressure_plate")
                    .block(p -> new PressurePlateBlock(BlockSetType.BAMBOO, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(SKYFOG_PLANKS.get().defaultMapColor())
                                            .forceSolidOn()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .noCollission()
                                            .strength(0.5F)
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.pressurePlate(
                                            ctx, prov, MiaBlocks.SKYFOG_PLANKS.get()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = buttonBlockWithItem
    public static final BlockEntry<ButtonBlock> SKYFOG_BUTTON =
            REGINTH.object("skyfog_button")
                    .block(p -> new ButtonBlock(BlockSetType.BAMBOO, 30, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .noCollission()
                                            .strength(0.5F)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.button(ctx, prov, MiaBlocks.SKYFOG_PLANKS.get()))
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.buttonItem(
                                            ctx, prov, MiaBlocks.SKYFOG_PLANKS.get()))
                    .build()
                    .register();
    // converted (closure): old provider helper = pressurePlateBlockWithItem
    public static final BlockEntry<PressurePlateBlock> VERDANT_PRESSURE_PLATE =
            REGINTH.object("verdant_pressure_plate")
                    .block(p -> new PressurePlateBlock(BlockSetType.BAMBOO, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(VERDANT_PLANKS.get().defaultMapColor())
                                            .forceSolidOn()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .noCollission()
                                            .strength(0.5F)
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.pressurePlate(
                                            ctx, prov, MiaBlocks.VERDANT_PLANKS.get()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = buttonBlockWithItem
    public static final BlockEntry<ButtonBlock> VERDANT_BUTTON =
            REGINTH.object("verdant_button")
                    .block(p -> new ButtonBlock(BlockSetType.BAMBOO, 30, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .noCollission()
                                            .strength(0.5F)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.button(ctx, prov, MiaBlocks.VERDANT_PLANKS.get()))
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.buttonItem(
                                            ctx, prov, MiaBlocks.VERDANT_PLANKS.get()))
                    .build()
                    .register();
    // converted (closure): old provider helper = pressurePlateBlockWithItem
    public static final BlockEntry<PressurePlateBlock> INVERTED_PRESSURE_PLATE =
            REGINTH.object("inverted_pressure_plate")
                    .block(p -> new PressurePlateBlock(BlockSetType.CHERRY, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(INVERTED_PLANKS.get().defaultMapColor())
                                            .forceSolidOn()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .noCollission()
                                            .strength(0.5F)
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.pressurePlate(
                                            ctx, prov, MiaBlocks.INVERTED_PLANKS.get()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = buttonBlockWithItem
    public static final BlockEntry<ButtonBlock> INVERTED_BUTTON =
            REGINTH.object("inverted_button")
                    .block(p -> new ButtonBlock(BlockSetType.CHERRY, 30, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.of()
                                            .noCollission()
                                            .strength(0.5F)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.button(
                                            ctx, prov, MiaBlocks.INVERTED_PLANKS.get()))
                    .item()
                    .model(
                            (ctx, prov) ->
                                    BlockStateGen.buttonItem(
                                            ctx, prov, MiaBlocks.INVERTED_PLANKS.get()))
                    .build()
                    .register();
    // 植物
    // converted (closure): old provider helper = bushBlock
    public static final BlockEntry<Block> MARGINAL_WEED =
            REGINTH.object("marginal_weed")
                    .block(p -> tallGrass(MapColor.PLANT, SoundType.GRASS))
                    .blockstate(BlockStateGen::bush)
                    .loot(
                            (tables, block) ->
                                    tables.add(block, MiaLootGen.abyssGrassDrops(tables, block)))
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = bushBlock
    public static final BlockEntry<Block> CRIMSON_VEILGRASS =
            REGINTH.object("crimson_veilgrass")
                    .block(p -> tallGrass(MapColor.NETHER, SoundType.ROOTS))
                    .blockstate(BlockStateGen::bush)
                    .loot(
                            (tables, block) ->
                                    tables.add(block, MiaLootGen.abyssGrassDrops(tables, block)))
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = bushBlock
    public static final BlockEntry<NetherSproutsBlock> SCORCHLEAF =
            REGINTH.object("scorchleaf")
                    .block(NetherSproutsBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_CYAN)
                                            .replaceable()
                                            .noCollission()
                                            .instabreak()
                                            .sound(SoundType.NETHER_SPROUTS)
                                            .offsetType(BlockBehaviour.OffsetType.XZ)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::bush)
                    .loot(
                            (tables, block) ->
                                    tables.add(block, MiaLootGen.abyssGrassDrops(tables, block)))
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = createFlowerBed
    public static final BlockEntry<PinkPetalsBlock> FORTITUDE_FLOWER =
            REGINTH.object("fortitude_flower")
                    .block(PinkPetalsBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.PLANT)
                                            .noCollission()
                                            .sound(SoundType.PINK_PETALS)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::flowerBed)
                    .loot((tables, block) -> tables.add(block, tables.createPetalsDrops(block)))
                    .item()
                    .model(BlockStateGen::aloneItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = createDoublePlant
    public static final BlockEntry<WaterTallFlowerBlock> REED =
            REGINTH.object("reed")
                    .block(WaterTallFlowerBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.PLANT)
                                            .noCollission()
                                            .instabreak()
                                            .sound(SoundType.GRASS)
                                            .offsetType(BlockBehaviour.OffsetType.XZ)
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::doublePlant)
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            MiaLootGen.singlePropConditionTable(
                                                    tables,
                                                    block,
                                                    DoublePlantBlock.HALF,
                                                    DoubleBlockHalf.LOWER)))
                    .item()
                    .model((ctx, prov) -> BlockStateGen.bushItem(ctx, prov, "_top"))
                    .build()
                    .register();
    // converted (closure): old provider helper = createDoubleBerry
    public static final BlockEntry<GloomBerryBlock> GLOOM_BERRY_PLANT =
            REGINTH.object("gloom_berry_plant")
                    .block(GloomBerryBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.PLANT)
                                            .noCollission()
                                            .instabreak()
                                            .randomTicks()
                                            .lightLevel(GloomBerryBlock::getLightLevel)
                                            .sound(SoundType.GRASS)
                                            .offsetType(BlockBehaviour.OffsetType.XZ)
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.doubleBerry(
                                            ctx,
                                            prov,
                                            com.altnoir.mia.common.block.DoubleBerryblock.MAX_AGE))
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            MiaLootGen.singleCropConditionTable(
                                                    tables,
                                                    block,
                                                    MiaItems.GLOOM_BERRY.get(),
                                                    DoublePlantBlock.HALF,
                                                    DoubleBlockHalf.LOWER)))
                    .item()
                    .model((ctx, prov) -> BlockStateGen.bushItem(ctx, prov, "_top3"))
                    .build()
                    .register();
    // converted (closure): old provider helper = createDoubleBerry
    public static final BlockEntry<DreamLicheeBlock> DREAM_LICHEE_PLANT =
            REGINTH.object("dream_lichee_plant")
                    .block(DreamLicheeBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.PLANT)
                                            .noCollission()
                                            .instabreak()
                                            .randomTicks()
                                            .sound(SoundType.GRASS)
                                            .offsetType(BlockBehaviour.OffsetType.XZ)
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.doubleBerry(
                                            ctx,
                                            prov,
                                            com.altnoir.mia.common.block.DoubleBerryblock.MAX_AGE))
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            MiaLootGen.singleCropConditionTable(
                                                    tables,
                                                    block,
                                                    MiaItems.DREAM_LICHEE.get(),
                                                    DoublePlantBlock.HALF,
                                                    DoubleBlockHalf.LOWER)))
                    .item()
                    .model((ctx, prov) -> BlockStateGen.bushItem(ctx, prov, "_top3"))
                    .build()
                    .register();
    // converted (closure): old provider helper = bushBlock
    public static final BlockEntry<Block> BALLOON_PLANT =
            REGINTH.object("balloon_plant")
                    .block(p -> flower(MobEffects.HEAL, 5.0F, MapColor.PLANT, SoundType.GRASS))
                    .blockstate(BlockStateGen::bush)
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = bushBlock
    public static final BlockEntry<Block> LANTERN_PLANT =
            REGINTH.object("lantern_plant")
                    .block(
                            p ->
                                    flower(
                                            MobEffects.NIGHT_VISION,
                                            5.0F,
                                            MapColor.PLANT,
                                            SoundType.GRASS,
                                            9))
                    .blockstate(BlockStateGen::bush)
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = bushBlock
    public static final BlockEntry<Block> GREEN_PERILLA =
            REGINTH.object("green_perilla")
                    .block(p -> flower(MobEffects.HEAL, 5.0F, MapColor.PLANT, SoundType.GRASS))
                    .blockstate(BlockStateGen::bush)
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = bushBlock
    public static final BlockEntry<Block> KONJAC_ROOT =
            REGINTH.object("konjac_root")
                    .block(p -> flower(MobEffects.HEAL, 5.0F, MapColor.PLANT, SoundType.ROOTS))
                    .blockstate(BlockStateGen::bush)
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = bushBlock
    public static final BlockEntry<Block> SILVEAF_FUNGUS =
            REGINTH.object("silveaf_fungus")
                    .block(p -> flower(MobEffects.HEAL, 5.0F, MapColor.PLANT, SoundType.ROOTS))
                    .blockstate(BlockStateGen::bush)
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // 矿物
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<DropExperienceBlock> ABYSS_IRON_ORE =
            REGINTH.object("abyss_iron_ore")
                    .block(p -> new DropExperienceBlock(ConstantInt.of(0), p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE.get())
                                            .strength(4.5F, 3.0F))
                    .loot(
                            (tables, block) ->
                                    tables.add(block, tables.createOreDrop(block, Items.RAW_IRON)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<DropExperienceBlock> ABYSS_COPPER_ORE =
            REGINTH.object("abyss_copper_ore")
                    .block(p -> new DropExperienceBlock(ConstantInt.of(0), p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE.get())
                                            .strength(4.5F, 3.0F))
                    .loot((tables, block) -> tables.add(block, tables.createCopperOreDrops(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<DropExperienceBlock> ABYSS_GOLD_ORE =
            REGINTH.object("abyss_gold_ore")
                    .block(p -> new DropExperienceBlock(ConstantInt.of(0), p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE.get())
                                            .strength(4.5F, 3.0F))
                    .loot(
                            (tables, block) ->
                                    tables.add(block, tables.createOreDrop(block, Items.RAW_GOLD)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<DropExperienceBlock> ABYSS_LAPIS_ORE =
            REGINTH.object("abyss_lapis_ore")
                    .block(p -> new DropExperienceBlock(UniformInt.of(2, 5), p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE.get())
                                            .strength(4.5F, 3.0F))
                    .loot((tables, block) -> tables.add(block, tables.createLapisOreDrops(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<RedStoneOreBlock> ABYSS_REDSTONE_ORE =
            REGINTH.object("abyss_redstone_ore")
                    .block(RedStoneOreBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE.get())
                                            .randomTicks()
                                            .lightLevel(
                                                    litBlockEmission(BlockStateProperties.LIT, 9))
                                            .strength(4.5F, 3.0F))
                    .loot(
                            (tables, block) ->
                                    tables.add(block, tables.createRedstoneOreDrops(block)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<DropExperienceBlock> ABYSS_DIAMOND_ORE =
            REGINTH.object("abyss_diamond_ore")
                    .block(p -> new DropExperienceBlock(UniformInt.of(3, 7), p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE.get())
                                            .strength(4.5F, 3.0F))
                    .loot(
                            (tables, block) ->
                                    tables.add(block, tables.createOreDrop(block, Items.DIAMOND)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<DropExperienceBlock> ABYSS_EMERALD_ORE =
            REGINTH.object("abyss_emerald_ore")
                    .block(p -> new DropExperienceBlock(UniformInt.of(3, 7), p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE.get())
                                            .strength(4.5F, 3.0F))
                    .loot(
                            (tables, block) ->
                                    tables.add(block, tables.createOreDrop(block, Items.EMERALD)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<DropExperienceBlock> ABYSS_QUARTZ_ORE =
            REGINTH.object("abyss_quartz_ore")
                    .block(p -> new DropExperienceBlock(UniformInt.of(2, 5), p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE.get())
                                            .strength(4.5F, 3.0F))
                    .loot(
                            (tables, block) ->
                                    tables.add(block, tables.createOreDrop(block, Items.QUARTZ)))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<ChlorophyteOreBlock> ABYSS_CHLOROPHYTE_ORE =
            REGINTH.object("abyss_chlorophyte_ore")
                    .block(p -> new ChlorophyteOreBlock(UniformInt.of(2, 5), Blocks.MUD, p))
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.MUD_BRICKS)
                                            .mapColor(MapColor.TERRACOTTA_GREEN)
                                            .randomTicks()
                                            .strength(1.5F, 3.0F))
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            tables.createOreDrop(
                                                    block, MiaItems.RAW_CHLOROPHYTE.get())))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = createBrushableBlock
    public static final BlockEntry<MiaBrushableBlock> SUSPICIOUS_ABYSS_ANDESITE =
            REGINTH.object("suspicious_abyss_andesite")
                    .block(
                            p ->
                                    new MiaBrushableBlock(
                                            MiaBlocks.ABYSS_ANDESITE.get(),
                                            SoundEvents.BRUSH_SAND,
                                            SoundEvents.BRUSH_SAND_COMPLETED,
                                            p))
                    .properties(
                            p ->
                                    p.mapColor(MapColor.STONE)
                                            .instrument(NoteBlockInstrument.SNARE)
                                            .strength(3.0F, 6.0F)
                                            .sound(SoundType.SUSPICIOUS_GRAVEL)
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::brushable)
                    .loot((tables, block) -> tables.add(block, LootTable.lootTable()))
                    .item()
                    .model((ctx, prov) -> BlockStateGen.itemParent(ctx, prov, "_0"))
                    .build()
                    .register();
    // 矿块
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> RAW_CHLOROPHYTE_BLOCK =
            REGINTH.object("raw_chlorophyte_block")
                    .block(Block::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_LIGHT_GREEN)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .strength(5.0F, 6.0F))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> CHLOROPHYTE_BLOCK =
            REGINTH.object("chlorophyte_block")
                    .block(Block::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.METAL)
                                            .instrument(NoteBlockInstrument.BELL)
                                            .requiresCorrectToolForDrops()
                                            .strength(5.0F, 6.0F)
                                            .sound(SoundType.METAL))
                    .simpleItem()
                    .register();
    // 翠水晶（翡翠）
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<CrystalBlock> PRASIOLITE_BLOCK =
            REGINTH.object("prasiolite_block")
                    .block(CrystalBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_GREEN)
                                            .strength(1.5F)
                                            .sound(SoundType.AMETHYST)
                                            .requiresCorrectToolForDrops())
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<BuddingPrasioliteBlock> BUDDING_PRASIOLITE =
            REGINTH.object("budding_prasiolite")
                    .block(BuddingPrasioliteBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.PRASIOLITE_BLOCK.get())
                                            .randomTicks()
                                            .pushReaction(PushReaction.DESTROY))
                    .loot((tables, block) -> tables.add(block, LootTable.lootTable()))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = clusterBlock
    public static final BlockEntry<Block> PRASIOLITE_CLUSTER =
            REGINTH.object("prasiolite_cluster")
                    .block(p -> cluster(7, 3, MapColor.COLOR_GREEN, SoundType.AMETHYST_CLUSTER, 5))
                    .blockstate(BlockStateGen::cluster)
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            MiaLootGen.clusterDrops(
                                                    tables,
                                                    block,
                                                    MiaItems.PRASIOLITE_SHARD.get(),
                                                    4.0F)))
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = clusterBlock
    public static final BlockEntry<Block> LARGE_PRASIOLITE_BUD =
            REGINTH.object("large_prasiolite_bud")
                    .block(p -> cluster(5, 3, MapColor.COLOR_GREEN, SoundType.AMETHYST_CLUSTER, 4))
                    .blockstate(BlockStateGen::cluster)
                    .loot(ReginthBlockLootTables::dropWhenSilkTouch)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = clusterBlock
    public static final BlockEntry<Block> MEDIUM_PRASIOLITE_BUD =
            REGINTH.object("medium_prasiolite_bud")
                    .block(p -> cluster(4, 3, MapColor.COLOR_GREEN, SoundType.AMETHYST_CLUSTER, 2))
                    .blockstate(BlockStateGen::cluster)
                    .loot(ReginthBlockLootTables::dropWhenSilkTouch)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = clusterBlock
    public static final BlockEntry<Block> SMALL_PRASIOLITE_BUD =
            REGINTH.object("small_prasiolite_bud")
                    .block(p -> cluster(3, 3, MapColor.COLOR_GREEN, SoundType.AMETHYST_CLUSTER, 1))
                    .blockstate(BlockStateGen::cluster)
                    .loot(ReginthBlockLootTables::dropWhenSilkTouch)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // 苍水晶（苍璃）
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<CrystalBlock> CAERULITE_BLOCK =
            REGINTH.object("caerulite_block")
                    .block(CrystalBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_BLUE)
                                            .strength(1.5F)
                                            .sound(SoundType.AMETHYST)
                                            .requiresCorrectToolForDrops())
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<BuddingCaeruliteBlock> BUDDING_CAERULITE =
            REGINTH.object("budding_caerulite")
                    .block(BuddingCaeruliteBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(CAERULITE_BLOCK.get())
                                            .randomTicks()
                                            .pushReaction(PushReaction.DESTROY))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = clusterBlock
    public static final BlockEntry<Block> CAERULITE_CLUSTER =
            REGINTH.object("caerulite_cluster")
                    .block(p -> cluster(7, 3, MapColor.COLOR_BLUE, SoundType.AMETHYST_CLUSTER, 5))
                    .blockstate(BlockStateGen::cluster)
                    .loot(
                            (tables, block) ->
                                    tables.add(
                                            block,
                                            MiaLootGen.clusterDrops(
                                                    tables,
                                                    block,
                                                    MiaItems.CAERULITE_SHARD.get(),
                                                    4.0F)))
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = clusterBlock
    public static final BlockEntry<Block> LARGE_CAERULITE_BUD =
            REGINTH.object("large_caerulite_bud")
                    .block(p -> cluster(5, 3, MapColor.COLOR_BLUE, SoundType.AMETHYST_CLUSTER, 4))
                    .blockstate(BlockStateGen::cluster)
                    .loot(ReginthBlockLootTables::dropWhenSilkTouch)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = clusterBlock
    public static final BlockEntry<Block> MEDIUM_CAERULITE_BUD =
            REGINTH.object("medium_caerulite_bud")
                    .block(p -> cluster(4, 3, MapColor.COLOR_BLUE, SoundType.AMETHYST_CLUSTER, 2))
                    .blockstate(BlockStateGen::cluster)
                    .loot(ReginthBlockLootTables::dropWhenSilkTouch)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = clusterBlock
    public static final BlockEntry<Block> SMALL_CAERULITE_BUD =
            REGINTH.object("small_caerulite_bud")
                    .block(p -> cluster(3, 3, MapColor.COLOR_BLUE, SoundType.AMETHYST_CLUSTER, 1))
                    .blockstate(BlockStateGen::cluster)
                    .loot(ReginthBlockLootTables::dropWhenSilkTouch)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    /* // 梦霓
    public static final DeferredBlock<Block> SOMNITE_BLOCK = registerBlock("somnite_block", () ->
            new PrasioliteBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PINK)
                            .strength(1.5F)
                            .sound(SoundType.AMETHYST)
                            .requiresCorrectToolForDrops()
            )
    );
    // 炽水晶（炽琰）
    public static final DeferredBlock<Block> IGNILITE_BLOCK = registerBlock("ignilite_block", () ->
            new PrasioliteBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .strength(1.5F)
                            .sound(SoundType.AMETHYST)
                            .requiresCorrectToolForDrops()
            )
    );
    // 阳髓
    public static final DeferredBlock<Block> SOLSTICEITE_BLOCK = registerBlock("orichalcite_block", () ->
            new PrasioliteBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(1.5F)
                            .sound(SoundType.AMETHYST)
                            .requiresCorrectToolForDrops()
            )
    );*/
    // 设备
    // converted (closure): old provider helper = moistureBlockState
    public static final BlockEntry<HopperFarmBlock> HOPPER_FARMLAND =
            REGINTH.object("hopper_farmland")
                    .block(HopperFarmBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.DIRT)
                                            .randomTicks()
                                            .strength(0.6F)
                                            .sound(SoundType.DEEPSLATE)
                                            .isViewBlocking(MiaBlocks::always)
                                            .isSuffocating(MiaBlocks::always))
                    .blockstate(BlockStateGen::moisture)
                    .loot(
                            (tables, block) ->
                                    tables.dropOther(block, MiaBlocks.ABYSS_COBBLED_ANDESITE.get()))
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<SunStoneBlock> SUN_STONE =
            REGINTH.object("sun_stone")
                    .block(SunStoneBlock::new)
                    .properties(
                            p ->
                                    p.strength(0.3F)
                                            .requiresCorrectToolForDrops()
                                            .lightLevel(state -> 15)
                                            .sound(SoundType.FROGLIGHT))
                    .simpleItem()
                    .register();

    // converted (closure): old provider helper = lampTubeBlock
    public static final BlockEntry<PrasioliteTubeBlock> PRASIOLITE_LAMPTUBE =
            REGINTH.object("prasiolite_lamptube")
                    .block(PrasioliteTubeBlock::new)
                    .properties(
                            p ->
                                    p.forceSolidOff()
                                            .strength(1.5F)
                                            .lightLevel(state -> 5)
                                            .sound(SoundType.AMETHYST)
                                            .noOcclusion())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.lampTube(
                                            ctx, prov, MiaBlocks.PRASIOLITE_BLOCK.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();

    @SuppressWarnings("deprecation")
    // converted (closure): old provider helper = lampTubeBlock
    public static final BlockEntry<AmethystTubeBlock> AMETHYST_LAMPTUBE =
            REGINTH.object("amethyst_lamptube")
                    .block(AmethystTubeBlock::new)
                    .properties(
                            p ->
                                    p.forceSolidOff()
                                            .strength(1.5F)
                                            .lightLevel(state -> 5)
                                            .sound(SoundType.AMETHYST)
                                            .noOcclusion())
                    .blockstate(
                            (ctx, prov) -> BlockStateGen.lampTube(ctx, prov, Blocks.AMETHYST_BLOCK))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();

    // converted (closure): old provider helper = baseBlockState
    public static final BlockEntry<PedestalBlock> PEDESTAL =
            REGINTH.object("pedestal")
                    .block(PedestalBlock::new)
                    .properties(
                            p ->
                                    p.forceSolidOff()
                                            .strength(1.5F)
                                            .sound(SoundType.NETHERITE_BLOCK)
                                            .noOcclusion())
                    .blockstate(BlockStateGen::pedestal)
                    .loot((tables, block) -> tables.dropSelf(block))
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();
    // converted (closure): old provider helper = baseBlockState
    public static final BlockEntry<AbyssPortalBlock> ABYSS_PORTAL =
            REGINTH.object("abyss_portal")
                    .block(AbyssPortalBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_LIGHT_GREEN)
                                            .noCollission()
                                            .lightLevel(state -> 15)
                                            .strength(-1.0F, 3600000.0F)
                                            .noLootTable()
                                            .pushReaction(PushReaction.BLOCK))
                    .blockstate(BlockStateGen::abyssPortal)
                    .item()
                    .model(BlockStateGen::bushItem)
                    .build()
                    .register();
    // converted (closure): old provider helper = baseBlockState
    public static final BlockEntry<AbyssPortalCoreBlock> ABYSS_PORTAL_CORE =
            REGINTH.object("abyss_portal_core")
                    .block(AbyssPortalCoreBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_YELLOW)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .sound(SoundType.NETHERITE_BLOCK)
                                            .lightLevel(state -> 1)
                                            .strength(-1.0F, 3600000.0F)
                                            .noLootTable())
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.abyssPortalCore(
                                            ctx, prov, MiaBlocks.ABYSS_PORTAL_FRAME.get()))
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();
    // converted (closure): old provider helper = blockWithItem
    public static final BlockEntry<Block> ABYSS_PORTAL_FRAME =
            REGINTH.object("abyss_portal_frame")
                    .block(Block::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.COLOR_BLACK)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .sound(SoundType.NETHERITE_BLOCK)
                                            .requiresCorrectToolForDrops()
                                            .strength(100.0F, 1200.0F))
                    .simpleItem()
                    .register();
    // converted (closure): old provider helper = abyssSpawnerBlockState
    public static final BlockEntry<AbyssSpawnerBlock> ABYSS_SPAWNER =
            REGINTH.object("abyss_spawner")
                    .block(AbyssSpawnerBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.STONE)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .lightLevel(
                                                    state ->
                                                            state.getValue(AbyssSpawnerBlock.STATE)
                                                                    .lightLevel())
                                            .strength(50.0F)
                                            .sound(SoundType.TRIAL_SPAWNER)
                                            .isViewBlocking(MiaBlocks::never)
                                            .noOcclusion())
                    .blockstate(BlockStateGen::abyssSpawner)
                    .loot(ReginthBlockLootTables::dropWhenSilkTouch)
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();
    // converted (closure): old provider helper = templateAllBlock
    public static final BlockEntry<CaveExplorerBeaconBlock> CAVE_EXPLORER_BEACON =
            REGINTH.object("cave_explorer_beacon")
                    .block(CaveExplorerBeaconBlock::new)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.EMERALD)
                                            .instrument(NoteBlockInstrument.HAT)
                                            .strength(3.0F, 6.0F)
                                            .lightLevel(
                                                    litBlockEmission(BlockStateProperties.LIT, 15))
                                            .noOcclusion()
                                            .isRedstoneConductor(MiaBlocks::never))
                    .blockstate((ctx, prov) -> BlockStateGen.templateAll(ctx, prov, "abyss_beacon"))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();
    // converted (closure): old provider helper = templateAllBlock（物品 stackSize 1）
    public static final BlockEntry<EndlessCupBlock> ENDLESS_CUP =
            REGINTH.object("endless_cup")
                    .block(EndlessCupBlock::new)
                    .properties(
                            p ->
                                    BlockBehaviour.Properties.ofFullCopy(
                                                    MiaBlocks.ABYSS_ANDESITE.get())
                                            .sound(SoundType.STONE))
                    .blockstate(BlockStateGen::templateAll)
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .properties(p -> p.stacksTo(1))
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();

    @SuppressWarnings("deprecation")
    // converted (closure): old provider helper = ropeBlock
    // 物品是 RopeItem（不是普通 BlockItem），用 BlockBuilder.item(factory) 注册：
    // 它会显式把 LANG 设成 noop（HEAD 里就没有 item.mia.rope 这个键），
    // 模型再用 aloneItem 覆盖掉"从方块状态推导"的默认值。
    public static final BlockEntry<RopeBlock> ROPE =
            REGINTH.object("rope")
                    .block(RopeBlock::new)
                    .properties(
                            p ->
                                    p.forceSolidOff()
                                            .strength(0.4F)
                                            .sound(SoundType.WOOL)
                                            .noOcclusion()
                                            .pushReaction(PushReaction.DESTROY))
                    .blockstate(BlockStateGen::rope)
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item(RopeItem::new)
                    .model(BlockStateGen::aloneItem)
                    .build()
                    .register();

    // 工作台
    // converted (closure): old provider helper = baseBlockState
    public static final BlockEntry<ArtifactSmithingTableBlock> ARTIFACT_SMITHING_TABLE =
            REGINTH.object("artifact_smithing_table")
                    .block(ArtifactSmithingTableBlock::new)
                    .properties(
                            p ->
                                    p.requiresCorrectToolForDrops()
                                            .strength(3.0F, 6.0F)
                                            .sound(SoundType.NETHERITE_BLOCK))
                    .blockstate(
                            (ctx, prov) ->
                                    BlockStateGen.artifactSmithingTable(
                                            ctx, prov, MiaBlocks.CHISLED_ABYSS_ANDESITE.get()))
                    .loot(ReginthBlockLootTables::dropSelf)
                    .item()
                    .model(BlockStateGen::itemParent)
                    .build()
                    .register();

    private static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return false;
    }

    private static boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return true;
    }

    /**
     * 树叶的通用属性（每种树叶只有 SoundType 不同），与旧 {@code leaves(SoundType)} helper 等价。
     */
    private static BlockBehaviour.Properties leavesProps(SoundType soundType) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.2F)
                .randomTicks()
                .sound(soundType)
                .noOcclusion()
                .isValidSpawn(Blocks::ocelotOrParrot)
                .isSuffocating(MiaBlocks::never)
                .isViewBlocking(MiaBlocks::never)
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY)
                .isRedstoneConductor(MiaBlocks::never);
    }

    // 工具
    private static Block log(MapColor topMapColor, MapColor sideMapColor) {
        return new MiaWoodBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(
                                state ->
                                        state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y
                                                ? topMapColor
                                                : sideMapColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F)
                        .sound(SoundType.WOOD)
                        .ignitedByLava());
    }

    private static Block wood(MapColor mapColor) {
        return new MiaWoodBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F)
                        .sound(SoundType.WOOD)
                        .ignitedByLava());
    }

    private static Block planks(MapColor mapColor) {
        return new MiaPlankBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F)
                        .sound(SoundType.WOOD)
                        .ignitedByLava());
    }

    private static Block stair(Block block) {
        return new StairBlock(
                block.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(block));
    }

    private static Block slab(Block block) {
        return new SlabBlock(BlockBehaviour.Properties.ofFullCopy(block));
    }

    private static Block fence(Block block) {
        return new FenceBlock(BlockBehaviour.Properties.ofFullCopy(block).forceSolidOn());
    }

    private static Block fenceGate(WoodType type, Block block) {
        return new FenceGateBlock(type, BlockBehaviour.Properties.ofFullCopy(block).forceSolidOn());
    }

    private static Block wall(Block block) {
        return new WallBlock(BlockBehaviour.Properties.ofFullCopy(block).forceSolidOn());
    }

    private static Block leaves(SoundType soundType) {
        return new MiaLeavesBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .strength(0.2F)
                        .randomTicks()
                        .sound(soundType)
                        .noOcclusion()
                        .isValidSpawn(Blocks::ocelotOrParrot)
                        .isSuffocating(MiaBlocks::never)
                        .isViewBlocking(MiaBlocks::never)
                        .ignitedByLava()
                        .pushReaction(PushReaction.DESTROY)
                        .isRedstoneConductor(MiaBlocks::never));
    }

    private static Block woodenPressurePlate(BlockSetType type, MapColor mapColor) {
        return new PressurePlateBlock(
                type,
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .forceSolidOn()
                        .instrument(NoteBlockInstrument.BASS)
                        .noCollission()
                        .strength(0.5F)
                        .ignitedByLava()
                        .pushReaction(PushReaction.DESTROY));
    }

    private static Block woodenButton(BlockSetType type) {
        return new ButtonBlock(
                type,
                30,
                BlockBehaviour.Properties.of()
                        .noCollission()
                        .strength(0.5F)
                        .pushReaction(PushReaction.DESTROY));
    }

    private static Block woodenDoor(BlockSetType type, MapColor mapColor) {
        return new DoorBlock(
                type,
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F)
                        .noOcclusion()
                        .ignitedByLava()
                        .pushReaction(PushReaction.DESTROY));
    }

    private static Block woodenTrapdoor(BlockSetType type, MapColor mapColor) {
        return new TrapDoorBlock(
                type,
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F)
                        .noOcclusion()
                        .isValidSpawn(Blocks::never)
                        .ignitedByLava());
    }

    private static Block cluster(
            float height,
            float aabbOffset,
            MapColor mapColor,
            SoundType soundType,
            int lightValue) {
        return new AbyssCrystalBlock(
                height,
                aabbOffset,
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .forceSolidOn()
                        .noOcclusion()
                        .sound(soundType)
                        .strength(1.5F)
                        .lightLevel(state -> lightValue)
                        .pushReaction(PushReaction.DESTROY));
    }

    private static Block tallGrass(MapColor mapColor, SoundType soundType) {
        return new TallGrassBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .replaceable()
                        .noCollission()
                        .instabreak()
                        .sound(soundType)
                        .offsetType(BlockBehaviour.OffsetType.XYZ)
                        .ignitedByLava()
                        .pushReaction(PushReaction.DESTROY));
    }

    private static Block flower(
            Holder<MobEffect> effects, float duration, MapColor mapColor, SoundType soundType) {
        return new FlowerBlock(
                effects,
                duration,
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .noCollission()
                        .instabreak()
                        .sound(soundType)
                        .offsetType(BlockBehaviour.OffsetType.XZ)
                        .pushReaction(PushReaction.DESTROY));
    }

    private static Block flower(
            Holder<MobEffect> effects,
            float duration,
            MapColor mapColor,
            SoundType soundType,
            int lightValue) {
        return new FlowerBlock(
                effects,
                duration,
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .noCollission()
                        .instabreak()
                        .sound(soundType)
                        .lightLevel(state -> lightValue)
                        .offsetType(BlockBehaviour.OffsetType.XZ)
                        .pushReaction(PushReaction.DESTROY));
    }

    private static ToIntFunction<BlockState> litBlockEmission(
            BooleanProperty property, int lightValue) {
        return state -> state.getValue(property) ? lightValue : 0;
    }
}
