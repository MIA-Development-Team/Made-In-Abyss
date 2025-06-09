package com.altnoir.mia.block;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.c.AbyssAndesiteBlock;
import com.altnoir.mia.block.c.AbyssGrassBlock;
import com.altnoir.mia.block.c.CoverGrassBlock;
import com.altnoir.mia.item.MIAItems;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MIABlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MIA.MOD_ID);

    public static final DeferredBlock<Block> ABYSS_ANDESITE = registerBlock("abyss_andesite",  () ->
            new AbyssAndesiteBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE)
            )
    );
    public static final  DeferredBlock<Block> ABYSS_COBBLED_ANDESITE = registerBlock("abyss_cobbled_andesite", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(ABYSS_ANDESITE.get())
                    .strength(3.5F, 6.0F)
            )
    );
    public static final DeferredBlock<Block> ABYSS_GRASS_BLOCK = registerBlock("abyss_grass_block", () ->
            new AbyssGrassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .randomTicks()
                    .strength(0.6F)
                    .sound(SoundType.GRASS)
            )
    );
    public static final DeferredBlock<Block> COVERGRASS_ABYSS_ANDESITE = registerBlock("covergrass_abyss_andesite", () ->
            new CoverGrassBlock(ABYSS_ANDESITE.get(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .randomTicks()
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE)
            )
    );
    public static final DeferredBlock<Block> FORTITUDE_FLOWER = registerBlock("fortitude_flower", () ->
            new FlowerBlock(
                    MobEffects.WEAKNESS,
                    9.0F,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .offsetType(BlockBehaviour.OffsetType.XZ)
                            .pushReaction(PushReaction.DESTROY)
            )
    );

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        MIAItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
