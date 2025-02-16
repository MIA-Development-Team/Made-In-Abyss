package com.altnoir.mia;

import com.altnoir.mia.content.blocks.ChlorophyteOreBlock;
import com.altnoir.mia.content.blocks.ExampleBlock;
import com.altnoir.mia.content.blocks.ExamplePortalBlock;

import com.altnoir.mia.content.blocks.seventhcrucible.SeventhCrucibleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlocksRegister {
    private static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, MIA.MOD_ID);

    public static final RegistryObject<Block> EXAMPLE_BLOCK = registerBlock("example_block", ExampleBlock::new);
    public static final RegistryObject<Block> EXAMPLE_PORTAL_BLOCK = registerBlock("example_portal_block",
            ExamplePortalBlock::new);
    public static final RegistryObject<Block> ALEXANDRITE_BLOCK = registerBlock("alexandrite_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK).sound(SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> CHLOROPHTRE_ORE = registerBlock("chlorophyte_ore",
            () -> new ChlorophyteOreBlock(BlockBehaviour.Properties.copy(Blocks.STONE).sound(SoundType.NETHERRACK)));

    public static final RegistryObject<Block> SEVENTH_CRUCIBLE_BLOCK = registerBlock(
            "seventh_crucible_block",
            () -> new SeventhCrucibleBlock(BlockBehaviour.Properties.copy(Blocks.STONE))
    );

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