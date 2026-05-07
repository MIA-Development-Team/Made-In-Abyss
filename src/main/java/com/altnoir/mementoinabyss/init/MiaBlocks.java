package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.block.abyss_andesite.AbyssAndesiteBlock;
import com.altnoir.mementoinabyss.content.block.cover_grass.CoverGrassBlock;
import com.altnoir.mementoinabyss.impl.registrate.BlockStateGen;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.altnoir.mementoinabyss.impl.registrate.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.entries.LootItem;

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
