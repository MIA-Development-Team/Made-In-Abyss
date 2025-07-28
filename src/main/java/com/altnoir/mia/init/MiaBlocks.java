package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.*;
import com.altnoir.mia.worldgen.tree.MiaTreeGrowers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class MiaBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MIA.MOD_ID);

    public static final DeferredBlock<Block> ARTIFACT_ENHANCEMENT_TABLE = registerBlock("artifact_enhancement_table",
            () -> new ArtifactEnhancementTableBlock(BlockBehaviour.Properties.of()
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> ABYSS_ANDESITE = registerBlock("abyss_andesite",
            () -> new AbyssAndesiteBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> COVERGRASS_ABYSS_ANDESITE = registerBlock("covergrass_abyss_andesite",
            () -> new CoverGrassBlock(ABYSS_ANDESITE.get(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .randomTicks()
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE)));

    public static final DeferredBlock<Block> ABYSS_ANDESITE_STAIRS = registerBlock("abyss_andesite_stairs",
            () -> stair(ABYSS_ANDESITE.get()));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_SLAB = registerBlock("abyss_andesite_slab",
            () -> slab(ABYSS_ANDESITE.get()));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_WALL = registerBlock("abyss_andesite_wall",
            () -> wall(ABYSS_ANDESITE.get()));
    public static final DeferredBlock<Block> ABYSS_COBBLED_ANDESITE = registerBlock("abyss_cobbled_andesite",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(ABYSS_ANDESITE.get())
                    .strength(3.5F, 6.0F)));
    public static final DeferredBlock<Block> ABYSS_COBBLED_ANDESITE_STAIRS = registerBlock(
            "abyss_cobbled_andesite_stairs", () -> stair(ABYSS_COBBLED_ANDESITE.get()));
    public static final DeferredBlock<Block> ABYSS_COBBLED_ANDESITE_SLAB = registerBlock("abyss_cobbled_andesite_slab",
            () -> slab(ABYSS_COBBLED_ANDESITE.get()));
    public static final DeferredBlock<Block> ABYSS_COBBLED_ANDESITE_WALL = registerBlock("abyss_cobbled_andesite_wall",
            () -> wall(ABYSS_COBBLED_ANDESITE.get()));
    public static final DeferredBlock<Block> MOSSY_ABYSS_COBBLED_ANDESITE = registerBlock(
            "mossy_abyss_cobbled_andesite",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(ABYSS_COBBLED_ANDESITE.get())));
    public static final DeferredBlock<Block> MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS = registerBlock(
            "mossy_abyss_cobbled_andesite_stairs", () -> stair(MOSSY_ABYSS_COBBLED_ANDESITE.get()));
    public static final DeferredBlock<Block> MOSSY_ABYSS_COBBLED_ANDESITE_SLAB = registerBlock(
            "mossy_abyss_cobbled_andesite_slab", () -> slab(MOSSY_ABYSS_COBBLED_ANDESITE.get()));
    public static final DeferredBlock<Block> MOSSY_ABYSS_COBBLED_ANDESITE_WALL = registerBlock(
            "mossy_abyss_cobbled_andesite_wall", () -> wall(MOSSY_ABYSS_COBBLED_ANDESITE.get()));
    public static final DeferredBlock<Block> POLISHED_ABYSS_ANDESITE = registerBlock("polished_abyss_andesite",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(ABYSS_COBBLED_ANDESITE.get())
                    .sound(SoundType.POLISHED_DEEPSLATE)));
    public static final DeferredBlock<Block> POLISHED_ABYSS_ANDESITE_STAIRS = registerBlock(
            "polished_abyss_andesite_stairs", () -> stair(POLISHED_ABYSS_ANDESITE.get()));
    public static final DeferredBlock<Block> POLISHED_ABYSS_ANDESITE_SLAB = registerBlock(
            "polished_abyss_andesite_slab", () -> slab(POLISHED_ABYSS_ANDESITE.get()));
    public static final DeferredBlock<Block> POLISHED_ABYSS_ANDESITE_WALL = registerBlock(
            "polished_abyss_andesite_wall", () -> wall(POLISHED_ABYSS_ANDESITE.get()));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_PILLAR = registerBlock("abyss_andesite_pillar",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(POLISHED_ABYSS_ANDESITE.get())));
    public static final DeferredBlock<Block> CHISLED_ABYSS_ANDESITE = registerBlock("chiseled_abyss_andesite",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(POLISHED_ABYSS_ANDESITE.get())));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_BRICKS = registerBlock("abyss_andesite_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(ABYSS_COBBLED_ANDESITE.get())
                    .sound(SoundType.DEEPSLATE_TILES)));
    public static final DeferredBlock<Block> CRACKED_ABYSS_ANDESITE_BRICKS = registerBlock(
            "cracked_abyss_andesite_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(ABYSS_ANDESITE_BRICKS.get())));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_BRICKS_STAIRS = registerBlock(
            "abyss_andesite_bricks_stairs", () -> stair(ABYSS_ANDESITE_BRICKS.get()));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_BRICKS_SLAB = registerBlock("abyss_andesite_bricks_slab",
            () -> slab(ABYSS_ANDESITE_BRICKS.get()));
    public static final DeferredBlock<Block> ABYSS_ANDESITE_BRICKS_WALL = registerBlock("abyss_andesite_bricks_wall",
            () -> wall(ABYSS_ANDESITE_BRICKS.get()));
    public static final DeferredBlock<Block> MOSSY_ABYSS_ANDESITE_BRICKS = registerBlock("mossy_abyss_andesite_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(ABYSS_ANDESITE_BRICKS.get())));
    public static final DeferredBlock<Block> MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS = registerBlock(
            "mossy_abyss_andesite_bricks_stairs", () -> stair(MOSSY_ABYSS_ANDESITE_BRICKS.get()));
    public static final DeferredBlock<Block> MOSSY_ABYSS_ANDESITE_BRICKS_SLAB = registerBlock(
            "mossy_abyss_andesite_bricks_slab", () -> slab(MOSSY_ABYSS_ANDESITE_BRICKS.get()));
    public static final DeferredBlock<Block> MOSSY_ABYSS_ANDESITE_BRICKS_WALL = registerBlock(
            "mossy_abyss_andesite_bricks_wall", () -> wall(MOSSY_ABYSS_ANDESITE_BRICKS.get()));

    public static final DeferredBlock<Block> SKYFOG_LOG = registerBlock("skyfog_log",
            () -> log(MapColor.WOOD, MapColor.PODZOL));
    public static final DeferredBlock<Block> SKYFOG_WOOD = registerBlock("skyfog_wood", () -> wood(MapColor.WOOD));
    public static final DeferredBlock<Block> STRIPPED_SKYFOG_LOG = registerBlock("stripped_skyfog_log",
            () -> log(MapColor.WOOD, MapColor.WOOD));
    public static final DeferredBlock<Block> STRIPPED_SKYFOG_WOOD = registerBlock("stripped_skyfog_wood",
            () -> wood(MapColor.WOOD));
    public static final DeferredBlock<Block> SKYFOG_PLANKS = registerBlock("skyfog_planks",
            () -> planks(MapColor.COLOR_GREEN));
    public static final DeferredBlock<Block> SKYFOG_STARIS = registerBlock("skyfog_staris",
            () -> stair(SKYFOG_PLANKS.get()));
    public static final DeferredBlock<Block> SKYFOG_SLAB = registerBlock("skyfog_slab",
            () -> slab(SKYFOG_PLANKS.get()));
    public static final DeferredBlock<Block> SKYFOG_FENCE = registerBlock("skyfog_fence",
            () -> fence(SKYFOG_PLANKS.get()));
    public static final DeferredBlock<Block> SKYFOG_FENCE_GATE = registerBlock("skyfog_fence_gate",
            () -> fenceGate(WoodType.OAK, SKYFOG_PLANKS.get()));
    public static final DeferredBlock<Block> SKYFOG_DOOR = registerBlock("skyfog_door",
            () -> woodenDoor(BlockSetType.OAK, SKYFOG_PLANKS.get().defaultMapColor()));
    public static final DeferredBlock<Block> SKYFOG_TRAPDOOR = registerBlock("skyfog_trapdoor",
            () -> woodenTrapdoor(BlockSetType.OAK, MapColor.WOOD));
    public static final DeferredBlock<Block> SKYFOG_LEAVES = registerBlock("skyfog_leaves",
            () -> leaves(SoundType.CHERRY_LEAVES));
    public static final DeferredBlock<Block> SKYFOG_LEAVES_WITH_FRUITS = registerBlock("skyfog_leaves_with_fruits",
            () -> leaves(SoundType.CHERRY_LEAVES));
    public static final DeferredBlock<Block> SKYFOG_SAPLING = registerBlock("skyfog_sapling", () -> new SaplingBlock(
            MiaTreeGrowers.SKYFOG_TREE,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CHERRY_SAPLING)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> FORTITUDE_FLOWER = registerBlock("fortitude_flower", () -> new FlowerBlock(
            MobEffects.WEAKNESS,
            9.0F,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> SKYFOG_PRESSURE_PLATE = registerBlock("skyfog_pressure_plate",
            () -> woodenPressurePlate(BlockSetType.OAK, SKYFOG_PLANKS.get().defaultMapColor()));
    public static final DeferredBlock<Block> SKYFOG_BUTTON = registerBlock("skyfog_button",
            () -> woodenButton(BlockSetType.OAK));

    @SuppressWarnings("deprecation")
    public static final DeferredBlock<Block> LAMP_TUBE = registerBlock("lamp_tube", () -> new LampTubeBlock(
            BlockBehaviour.Properties.of()
                    .forceSolidOff()
                    .instabreak()
                    .lightLevel(litBlockEmission(BlockStateProperties.POWERED, 10))
                    .sound(SoundType.AMETHYST)
                    .noOcclusion()));

    public static final DeferredBlock<Block> ABYSS_PORTAL = registerBlock("abyss_portal", () -> new AbyssPortalBlock(
            BlockBehaviour.Properties.of()
                    .noCollission()
                    .strength(3.0F, 6.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.GLASS)
                    .pushReaction(PushReaction.BLOCK)));

    public static final DeferredBlock<Block> ABYSS_SPAWNER = registerBlock("abyss_spawner", () -> new AbyssSpawnerBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lightLevel(state -> state.getValue(AbyssSpawnerBlock.STATE).lightLevel())
                    .strength(50.0F)
                    .sound(SoundType.TRIAL_SPAWNER)
                    .isViewBlocking(MiaBlocks::never)
                    .noOcclusion()));
    public static final DeferredBlock<Block> ENDLESS_CUP = registerBlock("endless_cup", () -> new EndlessCupBlock(
            BlockBehaviour.Properties.ofFullCopy(ABYSS_ANDESITE.get())));

    @SuppressWarnings("deprecation")
    public static final DeferredBlock<Block> ROPE = BLOCKS.register("rope",
            () -> new RopeBlock(BlockBehaviour.Properties.of()
                    .forceSolidOff().strength(0.4F)
                    .sound(SoundType.WOOL).noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    private static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return false;
    }

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

    private static ToIntFunction<BlockState> litBlockEmission(BooleanProperty property, int lightValue) {
        return state -> state.getValue(property) ? lightValue : 0;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        MiaItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
