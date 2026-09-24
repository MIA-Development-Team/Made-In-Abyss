package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MiaBlockTagProvider extends BlockTagsProvider {
    public MiaBlockTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 深渊标签
        tag(MiaTags.Blocks.ABYSS_ANDESITE_ORE_REPLACEABLES).add(MiaBlocks.ABYSS_ANDESITE.get());
        tag(MiaTags.Blocks.ABYSS_MUD_ORE_REPLACEABLES).add(Blocks.MUD);

        tag(MiaTags.Blocks.BASE_STONE_ABYSS)
                .add(MiaBlocks.ABYSS_ANDESITE.get())
                .add(MiaBlocks.MARLITH.get())
                .addTag(BlockTags.BASE_STONE_OVERWORLD);

        tag(MiaTags.Blocks.COVERGRASS)
                .add(Blocks.GRASS_BLOCK)
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get())
                .add(MiaBlocks.COVERGRASS_TUFF.get());

        tag(MiaTags.Blocks.ABYSS_DRIPSTONE_UNREPLACEABLE).add(Blocks.BEDROCK);

        // 深渊工具标签
        tag(MiaTags.Blocks.NEED_PRASIOLITE_TOOL).addTag(BlockTags.NEEDS_IRON_TOOL);

        tag(MiaTags.Blocks.INCORRECT_FOR_PRASIOLITE_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_IRON_TOOL)
                .remove(MiaTags.Blocks.NEED_PRASIOLITE_TOOL);

        tag(MiaTags.Blocks.MINEABLE_WITH_COMPOSITE)
                .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addTag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(BlockTags.MINEABLE_WITH_SHOVEL);

        // 基础标签
        tag(BlockTags.DIRT)
                .add(
                        MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get(),
                        MiaBlocks.COVERGRASS_TUFF.get(),
                        MiaBlocks.MYCELIUM_BLOCK.get());
        tag(BlockTags.ANIMALS_SPAWNABLE_ON)
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get())
                .add(MiaBlocks.COVERGRASS_TUFF.get());

        tag(BlockTags.MOSS_REPLACEABLE)
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get())
                .add(MiaBlocks.COVERGRASS_TUFF.get())
                .add(MiaBlocks.ABYSS_ANDESITE.get())
                .add(MiaBlocks.MARLITH.get());

        tag(BlockTags.LOGS_THAT_BURN) // 自动添加LOGS标签
                .add(MiaBlocks.SKYFOG_LOG.get())
                .add(MiaBlocks.SKYFOG_WOOD.get())
                .add(MiaBlocks.STRIPPED_SKYFOG_LOG.get())
                .add(MiaBlocks.STRIPPED_SKYFOG_WOOD.get())
                .add(MiaBlocks.VERDANT_STEM.get())
                .add(MiaBlocks.VERDANT_HYPHAE.get())
                .add(MiaBlocks.STRIPPED_VERDANT_STEM.get())
                .add(MiaBlocks.STRIPPED_VERDANT_HYPHAE.get())
                .add(MiaBlocks.INVERTED_LOG.get())
                .add(MiaBlocks.INVERTED_WOOD.get())
                .add(MiaBlocks.STRIPPED_INVERTED_LOG.get())
                .add(MiaBlocks.STRIPPED_INVERTED_WOOD.get());
        tag(BlockTags.LEAVES)
                .add(MiaBlocks.SKYFOG_LEAVES.get())
                .add(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS.get())
                .add(MiaBlocks.INVERTED_LEAVES.get());
        tag(BlockTags.PLANKS)
                .add(MiaBlocks.SKYFOG_PLANKS.get())
                .add(MiaBlocks.VERDANT_PLANKS.get())
                .add(MiaBlocks.INVERTED_PLANKS.get());
        tag(BlockTags.WOODEN_STAIRS)
                .add(MiaBlocks.SKYFOG_STAIRS.get())
                .add(MiaBlocks.VERDANT_STAIRS.get())
                .add(MiaBlocks.INVERTED_STAIRS.get());

        tag(BlockTags.STAIRS)
                .add(MiaBlocks.ABYSS_ANDESITE_STAIRS.get())
                .add(MiaBlocks.ABYSS_COBBLED_ANDESITE_STAIRS.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_STAIRS.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_STAIRS.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS.get());
        tag(BlockTags.SLABS)
                .add(MiaBlocks.ABYSS_ANDESITE_SLAB.get())
                .add(MiaBlocks.ABYSS_COBBLED_ANDESITE_SLAB.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_SLAB.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB.get())
                .add(MiaBlocks.SKYFOG_SLAB.get())
                .add(MiaBlocks.VERDANT_SLAB.get())
                .add(MiaBlocks.INVERTED_SLAB.get());
        tag(BlockTags.WALLS)
                .add(MiaBlocks.ABYSS_ANDESITE_WALL.get())
                .add(MiaBlocks.ABYSS_COBBLED_ANDESITE_WALL.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_WALL.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_WALL.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_WALL.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_WALL.get())
                .add(MiaBlocks.POLISHED_FOSSILIZED_WOOD_WALL.get())
                .add(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_WALL.get())
                .add(MiaBlocks.FOSSILIZED_WOOD_BRICKS_WALL.get())
                .add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL.get())
                .add(MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS_WALL.get())
                .add(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL.get());

        tag(BlockTags.FENCES)
                .add(MiaBlocks.SKYFOG_FENCE.get())
                .add(MiaBlocks.VERDANT_FENCE.get())
                .add(MiaBlocks.INVERTED_FENCE.get());
        tag(BlockTags.FENCE_GATES)
                .add(MiaBlocks.SKYFOG_FENCE_GATE.get())
                .add(MiaBlocks.VERDANT_FENCE_GATE.get())
                .add(MiaBlocks.INVERTED_FENCE_GATE.get());
        tag(BlockTags.SAPLINGS)
                .add(MiaBlocks.SKYFOG_SAPLING.get())
                .add(MiaBlocks.INVERTED_SAPLING.get());

        tag(BlockTags.FLOWERS).add(MiaBlocks.FORTITUDE_FLOWER.get());

        tag(BlockTags.CLIMBABLE).add(MiaBlocks.ROPE.get());

        // 矿物标签
        tag(Tags.Blocks.ORES_IRON).add(MiaBlocks.ABYSS_IRON_ORE.get());
        tag(Tags.Blocks.ORES_COPPER).add(MiaBlocks.ABYSS_COPPER_ORE.get());
        tag(Tags.Blocks.ORES_GOLD).add(MiaBlocks.ABYSS_GOLD_ORE.get());
        tag(Tags.Blocks.ORES_LAPIS).add(MiaBlocks.ABYSS_LAPIS_ORE.get());
        tag(Tags.Blocks.ORES_REDSTONE).add(MiaBlocks.ABYSS_REDSTONE_ORE.get());
        tag(Tags.Blocks.ORES_DIAMOND).add(MiaBlocks.ABYSS_DIAMOND_ORE.get());
        tag(Tags.Blocks.ORES_EMERALD).add(MiaBlocks.ABYSS_EMERALD_ORE.get());
        tag(Tags.Blocks.ORES_QUARTZ).add(MiaBlocks.ABYSS_QUARTZ_ORE.get());
        tag(Tags.Blocks.ORES).add(MiaBlocks.ABYSS_CHLOROPHYTE_ORE.get());

        // NeoForge标签
        tag(Tags.Blocks.VILLAGER_FARMLANDS).add(MiaBlocks.HOPPER_FARMLAND.get());

        // 原版工具标签
        tag(BlockTags.MINEABLE_WITH_HOE).add(MiaBlocks.VERDANT_LEAVES.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get())
                .add(MiaBlocks.COVERGRASS_TUFF.get())
                .add(MiaBlocks.ABYSS_ANDESITE.get())
                .add(MiaBlocks.ABYSS_ANDESITE_STAIRS.get())
                .add(MiaBlocks.ABYSS_ANDESITE_SLAB.get())
                .add(MiaBlocks.ABYSS_ANDESITE_WALL.get())
                .add(MiaBlocks.ABYSS_COBBLED_ANDESITE.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_SLAB.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_WALL.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_STAIRS.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_WALL.get())
                .add(MiaBlocks.ABYSS_ANDESITE_PILLAR.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS.get())
                .add(MiaBlocks.CHISLED_ABYSS_ANDESITE.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_STAIRS.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_WALL.get())
                .add(MiaBlocks.CRACKED_ABYSS_ANDESITE_BRICKS.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_WALL.get())
                .add(MiaBlocks.MARLITH.get())
                .add(MiaBlocks.FOSSILIZED_LOG.get())
                .add(MiaBlocks.FOSSILIZED_WOOD.get())
                .add(MiaBlocks.STRIPPED_FOSSILIZED_LOG.get())
                .add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get())
                .add(MiaBlocks.MOSSY_FOSSILIZED_LOG.get())
                .add(MiaBlocks.MOSSY_FOSSILIZED_WOOD.get())
                .add(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_LOG.get())
                .add(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD.get())
                .add(MiaBlocks.POLISHED_FOSSILIZED_WOOD.get())
                .add(MiaBlocks.POLISHED_FOSSILIZED_WOOD_STAIRS.get())
                .add(MiaBlocks.POLISHED_FOSSILIZED_WOOD_SLAB.get())
                .add(MiaBlocks.POLISHED_FOSSILIZED_WOOD_WALL.get())
                .add(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get())
                .add(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_STAIRS.get())
                .add(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_SLAB.get())
                .add(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_WALL.get())
                .add(MiaBlocks.CHISLED_STRIPPED_FOSSILIZED_WOOD.get())
                .add(MiaBlocks.FOSSILIZED_WOOD_BRICKS.get())
                .add(MiaBlocks.FOSSILIZED_WOOD_BRICKS_STAIRS.get())
                .add(MiaBlocks.FOSSILIZED_WOOD_BRICKS_SLAB.get())
                .add(MiaBlocks.FOSSILIZED_WOOD_BRICKS_WALL.get())
                .add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get())
                .add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS.get())
                .add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB.get())
                .add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL.get())
                .add(MiaBlocks.ABYSS_IRON_ORE.get())
                .add(MiaBlocks.ABYSS_COPPER_ORE.get())
                .add(MiaBlocks.ABYSS_GOLD_ORE.get())
                .add(MiaBlocks.ABYSS_LAPIS_ORE.get())
                .add(MiaBlocks.ABYSS_REDSTONE_ORE.get())
                .add(MiaBlocks.ABYSS_DIAMOND_ORE.get())
                .add(MiaBlocks.ABYSS_EMERALD_ORE.get())
                .add(MiaBlocks.ABYSS_QUARTZ_ORE.get())
                .add(MiaBlocks.ABYSS_CHLOROPHYTE_ORE.get())
                .add(MiaBlocks.SUSPICIOUS_ABYSS_ANDESITE.get())
                .add(MiaBlocks.PRASIOLITE_BLOCK.get())
                .add(MiaBlocks.BUDDING_PRASIOLITE.get())
                .add(MiaBlocks.PRASIOLITE_CLUSTER.get())
                .add(MiaBlocks.LARGE_PRASIOLITE_BUD.get())
                .add(MiaBlocks.MEDIUM_PRASIOLITE_BUD.get())
                .add(MiaBlocks.SMALL_PRASIOLITE_BUD.get())
                .add(MiaBlocks.CAERULITE_BLOCK.get())
                .add(MiaBlocks.BUDDING_CAERULITE.get())
                .add(MiaBlocks.CAERULITE_CLUSTER.get())
                .add(MiaBlocks.LARGE_CAERULITE_BUD.get())
                .add(MiaBlocks.MEDIUM_CAERULITE_BUD.get())
                .add(MiaBlocks.SMALL_CAERULITE_BUD.get())
                .add(MiaBlocks.HOPPER_FARMLAND.get())
                .add(MiaBlocks.AMETHYST_LAMPTUBE.get())
                .add(MiaBlocks.PRASIOLITE_LAMPTUBE.get())
                .add(MiaBlocks.PEDESTAL.get())
                .add(MiaBlocks.ARTIFACT_SMITHING_TABLE.get())
                .add(MiaBlocks.ENDLESS_CUP.get())
                .add(MiaBlocks.SUN_STONE.get());

        //        tag(BlockTags.MINEABLE_WITH_SHOVEL)
        //                .add(MiaBlocks.HOPPER_FARMLAND.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(MiaBlocks.ABYSS_GOLD_ORE.get())
                .add(MiaBlocks.ABYSS_REDSTONE_ORE.get())
                .add(MiaBlocks.ABYSS_DIAMOND_ORE.get())
                .add(MiaBlocks.ABYSS_EMERALD_ORE.get());
        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(MiaBlocks.ABYSS_IRON_ORE.get())
                .add(MiaBlocks.ABYSS_COPPER_ORE.get())
                .add(MiaBlocks.ABYSS_LAPIS_ORE.get())
                .add(MiaBlocks.ABYSS_QUARTZ_ORE.get())
                .add(MiaBlocks.ABYSS_CHLOROPHYTE_ORE.get());

        tag(MiaTags.Blocks.MYCELIUM_REPLACEABLE).addTag(BlockTags.DIRT);

        tag(BlockTags.REPLACEABLE_BY_TREES).add(MiaBlocks.MYCELIUM_BLOCK.get());
        tag(BlockTags.MUSHROOM_GROW_BLOCK).add(MiaBlocks.MYCELIUM_BLOCK.get());
        tag(BlockTags.INSIDE_STEP_SOUND_BLOCKS)
                .add(MiaBlocks.MYCELIUM_MAT.get())
                .add(MiaBlocks.MUSHROOM_BED.get());

        tag(MiaTags.Blocks.PRIMO_STEMS)
                .add(MiaBlocks.PRIMO_STEM.get())
                .add(MiaBlocks.STRIPPED_PRIMO_STEM.get())
                .add(MiaBlocks.PRIMO_HYPHAE.get())
                .add(MiaBlocks.STRIPPED_PRIMO_HYPHAE.get());
        // 注意：prime 系列是"下界木"性质，只进 LOGS、**不进** LOGS_THAT_BURN（不可燃）。
        tag(BlockTags.LOGS)
                .add(MiaBlocks.PRIMO_STEM.get())
                .add(MiaBlocks.PRIMO_HYPHAE.get())
                .add(MiaBlocks.STRIPPED_PRIMO_STEM.get())
                .add(MiaBlocks.STRIPPED_PRIMO_HYPHAE.get());

        tag(BlockTags.WOODEN_STAIRS).add(MiaBlocks.PRIMO_STAIRS.get());
        tag(BlockTags.WOODEN_SLABS).add(MiaBlocks.PRIMO_SLAB.get());
        tag(BlockTags.WOODEN_BUTTONS).add(MiaBlocks.PRIMO_BUTTON.get());
        tag(BlockTags.WOODEN_PRESSURE_PLATES).add(MiaBlocks.PRIMO_PRESSURE_PLATE.get());
        tag(BlockTags.WOODEN_FENCES).add(MiaBlocks.PRIMO_FENCE.get());
        tag(BlockTags.FENCE_GATES).add(MiaBlocks.PRIMO_FENCE_GATE.get());
        tag(BlockTags.WOODEN_DOORS).add(MiaBlocks.PRIMO_DOOR.get());
        tag(BlockTags.WOODEN_TRAPDOORS).add(MiaBlocks.PRIMO_TRAPDOOR.get());

        // 木质件走斧头、菌类走锄头。下面这两个清单与原模组的生成结果逐条一致 ——
        // 包括两处原模组本身的遗漏（prime_planks 未进 #minecraft:planks、primo_button 未进
        // mineable/axe），为保持"一模一样"此处照搬，未擅自补上。
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(MiaBlocks.PRIMO_PLANKS.get())
                .add(MiaBlocks.PRIMO_STAIRS.get())
                .add(MiaBlocks.PRIMO_SLAB.get())
                .add(MiaBlocks.PRIMO_FENCE.get())
                .add(MiaBlocks.PRIMO_FENCE_GATE.get())
                .add(MiaBlocks.PRIMO_DOOR.get())
                .add(MiaBlocks.PRIMO_TRAPDOOR.get())
                .add(MiaBlocks.PRIMO_PRESSURE_PLATE.get());
        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(MiaBlocks.MYCELIUM_BLOCK.get())
                .add(MiaBlocks.MYCELIUM_MAT.get())
                .add(MiaBlocks.MUSHROOM_BED.get())
                .add(MiaBlocks.PRIMO_CAP.get())
                .add(MiaBlocks.GLOW_PRIMO_CAP.get());

        // 标签嵌套
        tag(BlockTags.SCULK_REPLACEABLE).addTag(MiaTags.Blocks.ABYSS_ANDESITE_ORE_REPLACEABLES);
    }
}
