package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.block.abyss_andesite.AbyssAndesiteBlock;
import com.altnoir.mementoinabyss.content.block.cover_grass.AndesiteCoverGrassBlock;
import com.altnoir.mementoinabyss.content.block.cover_grass.TuffCoverGrassBlock;
import com.altnoir.mementoinabyss.impl.registrate.BlockStateGen;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

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
            .simpleItem()
            .register();

    public static final BlockEntry<Block> ABYSS_COBBLED_ANDESITE = REGISTRATE.object("abyss_cobbled_andesite")
            .block(Block::new)
            .initialProperties(ABYSS_ANDESITE)
            .simpleItem()
            .register();

    public static final BlockEntry<AndesiteCoverGrassBlock> COVERGRASS_ABYSS_ANDESITE = REGISTRATE.object("covergrass_abyss_andesite")
            .block(AndesiteCoverGrassBlock::new)
            .initialProperties(ABYSS_ANDESITE)
            .properties(p -> p.mapColor(MapColor.GRASS)
                    .randomTicks())
            .blockstate(BlockStateGen::coverGrass)
            .simpleItem()
            .register();

    public static final BlockEntry<TuffCoverGrassBlock> COVERGRASS_TUFF = REGISTRATE.object("covergrass_tuff")
            .block(TuffCoverGrassBlock::new)
            .initialProperties(() -> Blocks.TUFF)
            .blockstate(BlockStateGen::coverGrass)
            .simpleItem()
            .register();

    public static void register() {}
}
