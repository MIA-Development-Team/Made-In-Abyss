package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.common.block.*;
import com.altnoir.mia.worldgen.feature.tree.MiaTreeFeatures;
import com.altnoir.mia.worldgen.feature.tree.MiaTreeGrowers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class MiaBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MIA.MOD_ID);

    public static final DeferredBlock<Block> ABYSS_ANDESITE = registerBlock("abyss_andesite", () ->
            new AbyssAndesiteBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.DEEPSLATE)));
    // 草方块
    public static final DeferredBlock<Block> COVERGRASS_ABYSS_ANDESITE = registerBlock("covergrass_abyss_andesite", () ->
            new CoverGrassBlock(
                    ABYSS_ANDESITE.get(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .randomTicks()
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> COVERGRASS_TUFF = registerBlock("covergrass_tuff", () ->
            new CoverGrassBlock(
                    Blocks.TUFF, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .randomTicks()
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.TUFF)));

    // 深界安山岩
    public static final DeferredBlock<Block> ABYSS_ANDESITE_STAIRS = registerBlock("abyss_andesite_stairs", () -> stair(ABYSS_ANDESITE.get()));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_SLAB = registerBlock("abyss_andesite_slab", () -> slab(ABYSS_ANDESITE.get()));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_WALL = registerBlock("abyss_andesite_wall", () -> wall(ABYSS_ANDESITE.get()));
    // 圆石
    public static final DeferredBlock<Block> ABYSS_COBBLED_ANDESITE = registerBlock("abyss_cobbled_andesite", () ->
            new Block(
                    BlockBehaviour.Properties.ofFullCopy(ABYSS_ANDESITE.get())
                            .strength(3.5F, 6.0F)));
    public static final DeferredBlock<Block> ABYSS_COBBLED_ANDESITE_STAIRS = registerBlock("abyss_cobbled_andesite_stairs", () -> stair(ABYSS_COBBLED_ANDESITE.get()));
    public static final DeferredBlock<Block> ABYSS_COBBLED_ANDESITE_SLAB = registerBlock("abyss_cobbled_andesite_slab", () -> slab(ABYSS_COBBLED_ANDESITE.get()));
    public static final DeferredBlock<Block> ABYSS_COBBLED_ANDESITE_WALL = registerBlock("abyss_cobbled_andesite_wall", () -> wall(ABYSS_COBBLED_ANDESITE.get()));
    // 苔石
    public static final DeferredBlock<Block> MOSSY_ABYSS_COBBLED_ANDESITE = registerBlock("mossy_abyss_cobbled_andesite", () ->
            new Block(
                    BlockBehaviour.Properties.ofFullCopy(ABYSS_COBBLED_ANDESITE.get())));
    public static final DeferredBlock<Block> MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS = registerBlock("mossy_abyss_cobbled_andesite_stairs", () -> stair(MOSSY_ABYSS_COBBLED_ANDESITE.get()));
    public static final DeferredBlock<Block> MOSSY_ABYSS_COBBLED_ANDESITE_SLAB = registerBlock("mossy_abyss_cobbled_andesite_slab", () -> slab(MOSSY_ABYSS_COBBLED_ANDESITE.get()));
    public static final DeferredBlock<Block> MOSSY_ABYSS_COBBLED_ANDESITE_WALL = registerBlock("mossy_abyss_cobbled_andesite_wall", () -> wall(MOSSY_ABYSS_COBBLED_ANDESITE.get()));
    // 磨制
    public static final DeferredBlock<Block> POLISHED_ABYSS_ANDESITE = registerBlock("polished_abyss_andesite", () ->
            new Block(
                    BlockBehaviour.Properties.ofFullCopy(ABYSS_COBBLED_ANDESITE.get())
                            .sound(SoundType.POLISHED_DEEPSLATE)));
    public static final DeferredBlock<Block> POLISHED_ABYSS_ANDESITE_STAIRS = registerBlock("polished_abyss_andesite_stairs", () -> stair(POLISHED_ABYSS_ANDESITE.get()));
    public static final DeferredBlock<Block> POLISHED_ABYSS_ANDESITE_SLAB = registerBlock("polished_abyss_andesite_slab", () -> slab(POLISHED_ABYSS_ANDESITE.get()));
    public static final DeferredBlock<Block> POLISHED_ABYSS_ANDESITE_WALL = registerBlock("polished_abyss_andesite_wall", () -> wall(POLISHED_ABYSS_ANDESITE.get()));
    // 柱
    public static final DeferredBlock<Block> ABYSS_ANDESITE_PILLAR = registerBlock("abyss_andesite_pillar", () ->
            new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(POLISHED_ABYSS_ANDESITE.get())));
    // 切
    public static final DeferredBlock<Block> CHISLED_ABYSS_ANDESITE = registerBlock("chiseled_abyss_andesite", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(POLISHED_ABYSS_ANDESITE.get())));
    // 砖
    public static final DeferredBlock<Block> ABYSS_ANDESITE_BRICKS = registerBlock("abyss_andesite_bricks", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(ABYSS_COBBLED_ANDESITE.get()).sound(SoundType.DEEPSLATE_TILES)));
    public static final DeferredBlock<Block> CRACKED_ABYSS_ANDESITE_BRICKS = registerBlock("cracked_abyss_andesite_bricks", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(ABYSS_ANDESITE_BRICKS.get())));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_BRICKS_STAIRS = registerBlock("abyss_andesite_bricks_stairs", () -> stair(ABYSS_ANDESITE_BRICKS.get()));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_BRICKS_SLAB = registerBlock("abyss_andesite_bricks_slab", () -> slab(ABYSS_ANDESITE_BRICKS.get()));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_BRICKS_WALL = registerBlock("abyss_andesite_bricks_wall", () -> wall(ABYSS_ANDESITE_BRICKS.get()));

    public static final DeferredBlock<Block> MOSSY_ABYSS_ANDESITE_BRICKS = registerBlock("mossy_abyss_andesite_bricks", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(ABYSS_ANDESITE_BRICKS.get())));
    public static final DeferredBlock<Block> MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS = registerBlock("mossy_abyss_andesite_bricks_stairs", () -> stair(MOSSY_ABYSS_ANDESITE_BRICKS.get()));
    public static final DeferredBlock<Block> MOSSY_ABYSS_ANDESITE_BRICKS_SLAB = registerBlock("mossy_abyss_andesite_bricks_slab", () -> slab(MOSSY_ABYSS_ANDESITE_BRICKS.get()));
    public static final DeferredBlock<Block> MOSSY_ABYSS_ANDESITE_BRICKS_WALL = registerBlock("mossy_abyss_andesite_bricks_wall", () -> wall(MOSSY_ABYSS_ANDESITE_BRICKS.get()));

    // 化石树
    public static final DeferredBlock<Block> FOSSILIZED_LOG = registerBlock("fossilized_log", () ->
            new StrippedRotatedPillarBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.COLOR_BLACK : MapColor.PODZOL)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .requiresCorrectToolForDrops()
                            .strength(2.0F, 4.2F)
                            .sound(SoundType.BASALT))
    );
    public static final DeferredBlock<Block> FOSSILIZED_WOOD = registerBlock("fossilized_wood", () ->
            new StrippedRotatedPillarBlock(
                    BlockBehaviour.Properties.ofFullCopy(FOSSILIZED_LOG.get())
                            .mapColor(MapColor.PODZOL)
                            .strength(3.0F, 4.2F))
    );
    public static final DeferredBlock<Block> STRIPPED_FOSSILIZED_LOG = registerBlock("stripped_fossilized_log", () ->
            new StrippedRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(FOSSILIZED_LOG.get()).mapColor(MapColor.COLOR_BLACK)));
    public static final DeferredBlock<Block> STRIPPED_FOSSILIZED_WOOD = registerBlock("stripped_fossilized_wood", () ->
            new StrippedRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(FOSSILIZED_WOOD.get()).mapColor(MapColor.COLOR_BLACK)));

    public static final DeferredBlock<Block> MOSSY_FOSSILIZED_LOG = registerBlock("mossy_fossilized_log", () ->
            new StrippedRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(FOSSILIZED_LOG.get())));
    public static final DeferredBlock<Block> MOSSY_FOSSILIZED_WOOD = registerBlock("mossy_fossilized_wood", () ->
            new StrippedRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(FOSSILIZED_WOOD.get())));
    public static final DeferredBlock<Block> MOSSY_STRIPPED_FOSSILIZED_LOG = registerBlock("mossy_stripped_fossilized_log", () ->
            new StrippedRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(STRIPPED_FOSSILIZED_LOG.get())));
    public static final DeferredBlock<Block> MOSSY_STRIPPED_FOSSILIZED_WOOD = registerBlock("mossy_stripped_fossilized_wood", () ->
            new StrippedRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(STRIPPED_FOSSILIZED_WOOD.get())));
    // 磨制
    public static final DeferredBlock<Block> POLISHED_FOSSILIZED_WOOD = registerBlock("polished_fossilized_wood", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(FOSSILIZED_WOOD.get())));
    public static final DeferredBlock<Block> POLISHED_FOSSILIZED_WOOD_STAIRS = registerBlock("polished_fossilized_wood_stairs", () -> stair(POLISHED_FOSSILIZED_WOOD.get()));
    public static final DeferredBlock<Block> POLISHED_FOSSILIZED_WOOD_SLAB = registerBlock("polished_fossilized_wood_slab", () -> slab(POLISHED_FOSSILIZED_WOOD.get()));
    public static final DeferredBlock<Block> POLISHED_FOSSILIZED_WOOD_WALL = registerBlock("polished_fossilized_wood_wall", () -> wall(POLISHED_FOSSILIZED_WOOD.get()));

    public static final DeferredBlock<Block> POLISHED_STRIPPED_FOSSILIZED_WOOD = registerBlock("polished_stripped_fossilized_wood", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(STRIPPED_FOSSILIZED_WOOD.get())));
    public static final DeferredBlock<Block> POLISHED_STRIPPED_FOSSILIZED_WOOD_STAIRS = registerBlock("polished_stripped_fossilized_wood_stairs", () -> stair(POLISHED_STRIPPED_FOSSILIZED_WOOD.get()));
    public static final DeferredBlock<Block> POLISHED_STRIPPED_FOSSILIZED_WOOD_SLAB = registerBlock("polished_stripped_fossilized_wood_slab", () -> slab(POLISHED_STRIPPED_FOSSILIZED_WOOD.get()));
    public static final DeferredBlock<Block> POLISHED_STRIPPED_FOSSILIZED_WOOD_WALL = registerBlock("polished_stripped_fossilized_wood_wall", () -> wall(POLISHED_STRIPPED_FOSSILIZED_WOOD.get()));
    public static final DeferredBlock<Block> CHISLED_STRIPPED_FOSSILIZED_WOOD = registerBlock("chiseled_stripped_fossilized_wood", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(STRIPPED_FOSSILIZED_WOOD.get())));
    // 砖
    public static final DeferredBlock<Block> FOSSILIZED_WOOD_BRICKS = registerBlock("fossilized_wood_bricks", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(FOSSILIZED_WOOD.get())));
    public static final DeferredBlock<Block> FOSSILIZED_WOOD_BRICKS_STAIRS = registerBlock("fossilized_wood_bricks_stairs", () -> stair(FOSSILIZED_WOOD_BRICKS.get()));
    public static final DeferredBlock<Block> FOSSILIZED_WOOD_BRICKS_SLAB = registerBlock("fossilized_wood_bricks_slab", () -> slab(FOSSILIZED_WOOD_BRICKS.get()));
    public static final DeferredBlock<Block> FOSSILIZED_WOOD_BRICKS_WALL = registerBlock("fossilized_wood_bricks_wall", () -> wall(FOSSILIZED_WOOD_BRICKS.get()));

    public static final DeferredBlock<Block> STRIPPED_FOSSILIZED_WOOD_BRICKS = registerBlock("stripped_fossilized_wood_bricks", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(STRIPPED_FOSSILIZED_WOOD.get())));
    public static final DeferredBlock<Block> STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS = registerBlock("stripped_fossilized_wood_bricks_stairs", () -> stair(STRIPPED_FOSSILIZED_WOOD_BRICKS.get()));
    public static final DeferredBlock<Block> STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB = registerBlock("stripped_fossilized_wood_bricks_slab", () -> slab(STRIPPED_FOSSILIZED_WOOD_BRICKS.get()));
    public static final DeferredBlock<Block> STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL = registerBlock("stripped_fossilized_wood_bricks_wall", () -> wall(STRIPPED_FOSSILIZED_WOOD_BRICKS.get()));

    // 天雾树
    public static final DeferredBlock<Block> SKYFOG_LOG = registerBlock("skyfog_log", () -> log(MapColor.WOOD, MapColor.PODZOL));
    public static final DeferredBlock<Block> SKYFOG_WOOD = registerBlock("skyfog_wood", () -> wood(MapColor.WOOD));
    public static final DeferredBlock<Block> STRIPPED_SKYFOG_LOG = registerBlock("stripped_skyfog_log", () -> log(MapColor.WOOD, MapColor.WOOD));
    public static final DeferredBlock<Block> STRIPPED_SKYFOG_WOOD = registerBlock("stripped_skyfog_wood", () -> wood(MapColor.WOOD));
    public static final DeferredBlock<Block> SKYFOG_PLANKS = registerBlock("skyfog_planks", () -> planks(MapColor.COLOR_GREEN));
    public static final DeferredBlock<Block> SKYFOG_STAIRS = registerBlock("skyfog_stairs", () -> stair(SKYFOG_PLANKS.get()));
    public static final DeferredBlock<Block> SKYFOG_SLAB = registerBlock("skyfog_slab", () -> slab(SKYFOG_PLANKS.get()));
    public static final DeferredBlock<Block> SKYFOG_FENCE = registerBlock("skyfog_fence", () -> fence(SKYFOG_PLANKS.get()));
    public static final DeferredBlock<Block> SKYFOG_FENCE_GATE = registerBlock("skyfog_fence_gate", () -> fenceGate(WoodType.BAMBOO, SKYFOG_PLANKS.get()));
    public static final DeferredBlock<Block> SKYFOG_DOOR = registerBlock("skyfog_door", () -> woodenDoor(BlockSetType.BAMBOO, SKYFOG_PLANKS.get().defaultMapColor()));
    public static final DeferredBlock<Block> SKYFOG_TRAPDOOR = registerBlock("skyfog_trapdoor", () -> woodenTrapdoor(BlockSetType.BAMBOO, MapColor.WOOD));
    public static final DeferredBlock<Block> SKYFOG_LEAVES = registerBlock("skyfog_leaves", () -> leaves(SoundType.AZALEA_LEAVES));
    public static final DeferredBlock<Block> SKYFOG_LEAVES_WITH_FRUITS = registerBlock("skyfog_leaves_with_fruits", () -> leaves(SoundType.AZALEA_LEAVES));
    public static final DeferredBlock<Block> SKYFOG_SAPLING = registerBlock("skyfog_sapling", () ->
            new SaplingBlock(MiaTreeGrowers.SKYFOG_TREE, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CHERRY_SAPLING)
                    .pushReaction(PushReaction.DESTROY)));
    //翠寂菌
    public static final DeferredBlock<Block> VERDANT_STEM = registerBlock("verdant_stem", () -> log(MapColor.WOOD, MapColor.TERRACOTTA_YELLOW));
    public static final DeferredBlock<Block> VERDANT_HYPHAE = registerBlock("verdant_hyphae", () -> wood(MapColor.WOOD));
    public static final DeferredBlock<Block> STRIPPED_VERDANT_STEM = registerBlock("stripped_verdant_stem", () -> log(MapColor.WOOD, MapColor.TERRACOTTA_RED));
    public static final DeferredBlock<Block> STRIPPED_VERDANT_HYPHAE = registerBlock("stripped_verdant_hyphae", () -> wood(MapColor.TERRACOTTA_RED));
    public static final DeferredBlock<Block> VERDANT_PLANKS = registerBlock("verdant_planks", () -> planks(MapColor.TERRACOTTA_YELLOW));
    public static final DeferredBlock<Block> VERDANT_STAIRS = registerBlock("verdant_stairs", () -> stair(VERDANT_PLANKS.get()));
    public static final DeferredBlock<Block> VERDANT_SLAB = registerBlock("verdant_slab", () -> slab(VERDANT_PLANKS.get()));
    public static final DeferredBlock<Block> VERDANT_FENCE = registerBlock("verdant_fence", () -> fence(VERDANT_PLANKS.get()));
    public static final DeferredBlock<Block> VERDANT_FENCE_GATE = registerBlock("verdant_fence_gate", () -> fenceGate(WoodType.BAMBOO, VERDANT_PLANKS.get()));
    public static final DeferredBlock<Block> VERDANT_DOOR = registerBlock("verdant_door", () -> woodenDoor(BlockSetType.BAMBOO, VERDANT_PLANKS.get().defaultMapColor()));
    public static final DeferredBlock<Block> VERDANT_TRAPDOOR = registerBlock("verdant_trapdoor", () -> woodenTrapdoor(BlockSetType.BAMBOO, MapColor.TERRACOTTA_YELLOW));
    public static final DeferredBlock<Block> VERDANT_LEAVES = registerBlock("verdant_leaves", () ->
            new SlimeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(1.0F).sound(SoundType.WART_BLOCK).noOcclusion()
                    .lightLevel(state -> 15)
                    .isValidSpawn(Blocks::never)
                    .isRedstoneConductor(MiaBlocks::never)
                    .isSuffocating(MiaBlocks::never)
                    .isViewBlocking(MiaBlocks::never)
            ));
    public static final DeferredBlock<Block> VERDANT_FUNGUS = registerBlock("verdant_fungus", () ->
            new MiaFungusBlock(
                    MiaTreeFeatures.VERDANT_FUNGUS,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_CYAN)
                            .instabreak()
                            .noCollission()
                            .sound(SoundType.CHERRY_SAPLING)
                            .pushReaction(PushReaction.DESTROY)
            ));
    //倒悬树
    public static final DeferredBlock<Block> INVERTED_LOG = registerBlock("inverted_log", () -> log(MapColor.WOOD, MapColor.PODZOL));
    public static final DeferredBlock<Block> INVERTED_WOOD = registerBlock("inverted_wood", () -> wood(MapColor.WOOD));
    public static final DeferredBlock<Block> STRIPPED_INVERTED_LOG = registerBlock("stripped_inverted_log", () -> log(MapColor.WOOD, MapColor.WOOD));
    public static final DeferredBlock<Block> STRIPPED_INVERTED_WOOD = registerBlock("stripped_inverted_wood", () -> wood(MapColor.WOOD));
    public static final DeferredBlock<Block> INVERTED_PLANKS = registerBlock("inverted_planks", () -> planks(MapColor.COLOR_GREEN));
    public static final DeferredBlock<Block> INVERTED_STAIRS = registerBlock("inverted_stairs", () -> stair(INVERTED_PLANKS.get()));
    public static final DeferredBlock<Block> INVERTED_SLAB = registerBlock("inverted_slab", () -> slab(INVERTED_PLANKS.get()));
    public static final DeferredBlock<Block> INVERTED_FENCE = registerBlock("inverted_fence", () -> fence(INVERTED_PLANKS.get()));
    public static final DeferredBlock<Block> INVERTED_FENCE_GATE = registerBlock("inverted_fence_gate", () -> fenceGate(WoodType.CHERRY, INVERTED_PLANKS.get()));
    public static final DeferredBlock<Block> INVERTED_DOOR = registerBlock("inverted_door", () -> woodenDoor(BlockSetType.CHERRY, INVERTED_PLANKS.get().defaultMapColor()));
    public static final DeferredBlock<Block> INVERTED_TRAPDOOR = registerBlock("inverted_trapdoor", () -> woodenTrapdoor(BlockSetType.CHERRY, MapColor.WOOD));
    public static final DeferredBlock<Block> INVERTED_LEAVES = registerBlock("inverted_leaves", () -> leaves(SoundType.CHERRY_LEAVES));
    public static final DeferredBlock<Block> INVERTED_SAPLING = registerBlock("inverted_sapling", () ->
            new InvertedSaplingBlock(MiaTreeGrowers.INVERTED_TREE, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CHERRY_SAPLING)
                    .pushReaction(PushReaction.DESTROY)));

    // 开关
    public static final DeferredBlock<Block> SKYFOG_PRESSURE_PLATE = registerBlock("skyfog_pressure_plate", () ->
            woodenPressurePlate(BlockSetType.BAMBOO, SKYFOG_PLANKS.get().defaultMapColor()));
    public static final DeferredBlock<Block> SKYFOG_BUTTON = registerBlock("skyfog_button", () ->
            woodenButton(BlockSetType.BAMBOO));
    public static final DeferredBlock<Block> VERDANT_PRESSURE_PLATE = registerBlock("verdant_pressure_plate", () ->
            woodenPressurePlate(BlockSetType.BAMBOO, VERDANT_PLANKS.get().defaultMapColor()));
    public static final DeferredBlock<Block> VERDANT_BUTTON = registerBlock("verdant_button", () ->
            woodenButton(BlockSetType.BAMBOO));
    public static final DeferredBlock<Block> INVERTED_PRESSURE_PLATE = registerBlock("inverted_pressure_plate", () ->
            woodenPressurePlate(BlockSetType.CHERRY, INVERTED_PLANKS.get().defaultMapColor()));
    public static final DeferredBlock<Block> INVERTED_BUTTON = registerBlock("inverted_button", () ->
            woodenButton(BlockSetType.CHERRY));
    // 植物
    public static final DeferredBlock<Block> MARGINAL_WEED = registerBlock("marginal_weed", () ->
            tallGrass(MapColor.PLANT, SoundType.GRASS)
    );
    public static final DeferredBlock<Block> CRIMSON_VEILGRASS = registerBlock("crimson_veilgrass", () ->
            tallGrass(MapColor.NETHER, SoundType.ROOTS)
    );
    public static final DeferredBlock<Block> SCORCHLEAF = registerBlock("scorchleaf", () ->
            new NetherSproutsBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_CYAN)
                            .replaceable()
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.NETHER_SPROUTS)
                            .offsetType(BlockBehaviour.OffsetType.XZ)
                            .pushReaction(PushReaction.DESTROY)
            )
    );
    public static final DeferredBlock<Block> FORTITUDE_FLOWER = registerBlock("fortitude_flower", () ->
            new PinkPetalsBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .sound(SoundType.PINK_PETALS)
                    .pushReaction(PushReaction.DESTROY)
            )
    );
    public static final DeferredBlock<Block> REED = registerBlock("reed", () ->
            new WaterTallFlowerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)
            )
    );
    public static final DeferredBlock<Block> BALLOON_PLANT = registerBlock("balloon_plant", () ->
            flower(MobEffects.NIGHT_VISION, 5.0F, MapColor.PLANT, SoundType.GRASS)
    );
    public static final DeferredBlock<Block> GREEN_PERILLA = registerBlock("green_perilla", () ->
            flower(MobEffects.DIG_SPEED, 5.0F, MapColor.PLANT, SoundType.GRASS)
    );
    public static final DeferredBlock<Block> KONJAC_ROOT = registerBlock("konjac_root", () ->
            flower(MobEffects.HEAL, 5.0F, MapColor.PLANT, SoundType.ROOTS)
    );
    public static final DeferredBlock<Block> SILVEAF_FUNGUS = registerBlock("silveaf_fungus", () ->
            flower(MobEffects.HEAL, 5.0F, MapColor.PLANT, SoundType.ROOTS)
    );
    // 晶石
    public static final DeferredBlock<Block> PRASIOLITE_BLOCK = registerBlock("prasiolite_block", () ->
            new PrasioliteBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .strength(1.5F)
                            .sound(SoundType.AMETHYST)
                            .requiresCorrectToolForDrops()
            ));
    public static final DeferredBlock<Block> BUDDING_PRASIOLITE = registerBlock("budding_prasiolite", () ->
            new BuddingPrasioliteBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .randomTicks()
                            .strength(1.5F)
                            .sound(SoundType.AMETHYST)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.DESTROY)
            ));
    public static final DeferredBlock<Block> PRASIOLITE_CLUSTER = registerBlock("prasiolite_cluster", () ->
            cluster(7, 3, SoundType.AMETHYST_CLUSTER, 5)
    );
    public static final DeferredBlock<Block> LARGE_PRASIOLITE_BUD = registerBlock("large_prasiolite_bud", () ->
            cluster(5, 3, SoundType.AMETHYST_CLUSTER, 4)
    );
    public static final DeferredBlock<Block> MEDIUM_PRASIOLITE_BUD = registerBlock("medium_prasiolite_bud", () ->
            cluster(4, 3, SoundType.AMETHYST_CLUSTER, 2)
    );
    public static final DeferredBlock<Block> SMALL_PRASIOLITE_BUD = registerBlock("small_prasiolite_bud", () ->
            cluster(3, 3, SoundType.AMETHYST_CLUSTER, 1)
    );

    // 设备
    public static final DeferredBlock<Block> HOPPER_FARMLAND = registerBlock("hopper_farmland", () ->
            new HopperFarmBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DIRT)
                            .randomTicks()
                            .strength(0.6F)
                            .sound(SoundType.DEEPSLATE)
                            .isViewBlocking(MiaBlocks::always)
                            .isSuffocating(MiaBlocks::always)
            ));
    public static final DeferredBlock<Block> SUN_STONE = registerBlock("sun_stone", () ->
            new SunStoneBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.3F)
                            .requiresCorrectToolForDrops()
                            .lightLevel(state -> 15)
                            .sound(SoundType.FROGLIGHT)
            )
    );

    public static final DeferredBlock<Block> PRASIOLITE_LAMPTUBE = registerBlock("prasiolite_lamptube", () ->
            new PrasioliteTubeBlock(
                    BlockBehaviour.Properties.of()
                            .forceSolidOff()
                            .strength(1.5F)
                            .lightLevel(state -> 5)
                            .sound(SoundType.AMETHYST)
                            .noOcclusion())
    );
    @SuppressWarnings("deprecation")
    public static final DeferredBlock<Block> AMETHYST_LAMPTUBE = registerBlock("amethyst_lamptube", () ->
            new AmethystTubeBlock(
                    BlockBehaviour.Properties.of()
                            .forceSolidOff()
                            .strength(1.5F)
                            .lightLevel(state -> 5)
                            .sound(SoundType.AMETHYST)
                            .noOcclusion())
    );
    public static final DeferredBlock<Block> PEDESTAL = registerBlock("pedestal", () ->
            new PedestalBlock(
                    BlockBehaviour.Properties.of()
                            .forceSolidOff()
                            .strength(1.5F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .noOcclusion())
    );
    public static final DeferredBlock<Block> ABYSS_PORTAL = registerBlock("abyss_portal", () ->
            new AbyssPortalBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_GREEN)
                            .noCollission()
                            .lightLevel(state -> 15)
                            .strength(-1.0F, 3600000.0F)
                            .noLootTable()
                            .pushReaction(PushReaction.BLOCK))
    );
    public static final DeferredBlock<Block> ABYSS_PORTAL_FRAME = registerBlock("abyss_portal_frame", () ->
            new AbyssPortalFrameBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .lightLevel(state -> 1)
                            .strength(-1.0F, 3600000.0F)
                            .noLootTable())
    );

    public static final DeferredBlock<Block> ABYSS_SPAWNER = registerBlock("abyss_spawner", () ->
            new AbyssSpawnerBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .lightLevel(state -> state.getValue(AbyssSpawnerBlock.STATE).lightLevel())
                            .strength(50.0F)
                            .sound(SoundType.TRIAL_SPAWNER)
                            .isViewBlocking(MiaBlocks::never)
                            .noOcclusion())
    );
    public static final DeferredBlock<Block> CAVE_EXPLORER_BEACON = registerBlock("cave_explorer_beacon", () ->
            new CaveExplorerBeaconBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.EMERALD)
                            .instrument(NoteBlockInstrument.HAT)
                            .strength(3.0F, 6.0F)
                            .lightLevel(state -> 15)
                            .noOcclusion()
                            .isRedstoneConductor(MiaBlocks::never)
            )
    );

    @SuppressWarnings("deprecation")
    public static final DeferredBlock<Block> ROPE = BLOCKS.register("rope", () ->
            new RopeBlock(BlockBehaviour.Properties.of()
                    .forceSolidOff().strength(0.4F)
                    .sound(SoundType.WOOL).noOcclusion()
                    .pushReaction(PushReaction.DESTROY))
    );
    // 工作台
    public static final DeferredBlock<Block> ARTIFACT_SMITHING_TABLE = registerBlock("artifact_smithing_table", () ->
            new ArtifactSmithingTableBlock(BlockBehaviour.Properties.of()
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.NETHERITE_BLOCK))
    );
    public static final DeferredBlock<Block> ENDLESS_CUP = registerBlock("endless_cup", 1, () ->
            new EndlessCupBlock(
                    BlockBehaviour.Properties.ofFullCopy(ABYSS_ANDESITE.get()))
    );

    private static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return false;
    }

    private static boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return true;
    }

    // 工具
    private static Block log(MapColor topMapColor, MapColor sideMapColor) {
        return new MiaWoodBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? topMapColor
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
        return new StairBlock(block.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(block));
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
        return new PressurePlateBlock(type, BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .forceSolidOn()
                .instrument(NoteBlockInstrument.BASS)
                .noCollission()
                .strength(0.5F)
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY));
    }

    private static Block woodenButton(BlockSetType type) {
        return new ButtonBlock(type, 30,
                BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY));
    }

    private static Block woodenDoor(BlockSetType type, MapColor mapColor) {
        return new DoorBlock(type,
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F)
                        .noOcclusion()
                        .ignitedByLava()
                        .pushReaction(PushReaction.DESTROY));
    }

    private static Block woodenTrapdoor(BlockSetType type, MapColor mapColor) {
        return new TrapDoorBlock(type,
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F)
                        .noOcclusion()
                        .isValidSpawn(Blocks::never)
                        .ignitedByLava());
    }

    private static Block cluster(float height, float aabbOffset, SoundType soundType, int lightValue) {
        return new PrasioliteClusterBlock(height, aabbOffset,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_GREEN)
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
                        .pushReaction(PushReaction.DESTROY)
        );
    }

    private static Block flower(Holder<MobEffect> effects, float duration, MapColor mapColor, SoundType soundType) {
        return new FlowerBlock(effects, duration,
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .noCollission()
                        .instabreak()
                        .sound(soundType)
                        .offsetType(BlockBehaviour.OffsetType.XZ)
                        .pushReaction(PushReaction.DESTROY)
        );
    }

    private static ToIntFunction<BlockState> litBlockEmission(BooleanProperty property, int lightValue) {
        return state -> state.getValue(property) ? lightValue : 0;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, int stackSize, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, stackSize);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        MiaItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block, int stackSize) {
        MiaItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().stacksTo(stackSize)));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
