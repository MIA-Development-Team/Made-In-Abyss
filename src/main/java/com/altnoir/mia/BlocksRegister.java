package com.altnoir.mia;

import com.altnoir.mia.content.blocks.AbyssGrassBlock;
import com.altnoir.mia.content.blocks.ChlorophyteOreBlock;
import com.altnoir.mia.content.blocks.CoverGrassBlock;

import com.altnoir.mia.content.blocks.volcano_crucible.VolcanoCrucibleBlock;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlocksRegister {
    private static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, MIA.MODID);

    public static final RegistryObject<Block> ABYSS_GRASS_BLOCK = registerBlock("abyss_grass_block",
            () -> new AbyssGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK).randomTicks().sound(SoundType.GRASS)));
    public static final RegistryObject<Block> COVERGRASS_COBBLESTONE = registerBlock("covergrass_cobblestone",
            () -> new CoverGrassBlock(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE).randomTicks().sound(SoundType.STONE)));
    public static final RegistryObject<Block> COVERGRASS_STONE = registerBlock("covergrass_stone",
            () -> new CoverGrassBlock(BlockBehaviour.Properties.copy(Blocks.STONE).randomTicks().sound(SoundType.STONE)));
    public static final RegistryObject<Block> COVERGRASS_DEEPSLATE = registerBlock("covergrass_deepslate",
            () -> new CoverGrassBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).randomTicks().sound(SoundType.DEEPSLATE)));
    public static final RegistryObject<Block> COVERGRASS_GRANITE = registerBlock("covergrass_granite",
            () -> new CoverGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRANITE).randomTicks().sound(SoundType.STONE)));
    public static final RegistryObject<Block> COVERGRASS_DIORITE = registerBlock("covergrass_diorite",
            () -> new CoverGrassBlock(BlockBehaviour.Properties.copy(Blocks.DIORITE).randomTicks().sound(SoundType.STONE)));
    public static final RegistryObject<Block> COVERGRASS_ANDESITE = registerBlock("covergrass_andesite",
            () -> new CoverGrassBlock(BlockBehaviour.Properties.copy(Blocks.ANDESITE).randomTicks().sound(SoundType.STONE)));
    public static final RegistryObject<Block> COVERGRASS_CALCITE = registerBlock("covergrass_calcite",
            () -> new CoverGrassBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE).randomTicks().sound(SoundType.CALCITE)));
    public static final RegistryObject<Block> COVERGRASS_TUFF = registerBlock("covergrass_tuff",
            () -> new CoverGrassBlock(BlockBehaviour.Properties.copy(Blocks.TUFF).randomTicks().sound(SoundType.TUFF)));
    public static final RegistryObject<Block> CHLOROPHTRE_ORE = registerBlock("chlorophyte_ore",
            () -> new ChlorophyteOreBlock(BlockBehaviour.Properties.of()
                    .strength(3f, 6f)
                    .sound(SoundType.NETHERRACK)
                    .lightLevel(state -> 3)
                    .requiresCorrectToolForDrops()
            ));
    public static final RegistryObject<Block> SUSPICOUS_ANDESITE =registerBlock("suspicious_andesite",
            () -> new BrushableBlock(Blocks.ANDESITE, BlockBehaviour.Properties.copy(Blocks.ANDESITE).sound(SoundType.DECORATED_POT),
                    SoundEvents.BRUSH_GRAVEL,SoundEvents.BRUSH_GRAVEL_COMPLETED));
    public static final RegistryObject<Block> VOLCANO_CRUCIBLE = registerBlock("volcano_crucible",
            () -> new VolcanoCrucibleBlock(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK).sound(SoundType.NETHERITE_BLOCK)));

    private static <T extends Block> RegistryObject<T> registerBlock(final String name, final Supplier<T> block) {
        var toReturn = REGISTER.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(final String name, final RegistryObject<T> block) {
        ItemsRegister.registerBlockItem(name, block);
    }

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}