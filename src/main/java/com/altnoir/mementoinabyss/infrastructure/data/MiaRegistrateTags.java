package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.abysslib.reginth.providers.ProviderType;
import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

/** Cross-entry tags and explicit block-to-item copies; per-entry tags live at registration. */
public final class MiaRegistrateTags {
    private MiaRegistrateTags() {}

    public static void addGenerators() {
        var registrate = MementoInAbyss.registrate();
        registrate.addDataGenerator(
                ProviderType.BLOCK_TAGS,
                provider -> {
                    provider.tag(MiaTags.BlockTags.ABYSS_MUD_ORE_REPLACEABLE.tag).add(Blocks.MUD);
                    provider.tag(MiaTags.BlockTags.MYCELIUM_REPLACEABLE.tag)
                            .addTag(BlockTags.DIRT)
                            .addTag(BlockTags.GRASS_BLOCKS);
                    provider.tag(MiaTags.BlockTags.PRIMO_STEMS.tag)
                            .add(
                                    MiaBlocks.PRIMO_STEM.get(),
                                    MiaBlocks.STRIPPED_PRIMO_STEM.get(),
                                    MiaBlocks.PRIMO_HYPHAE.get(),
                                    MiaBlocks.STRIPPED_PRIMO_HYPHAE.get());
                });
        registrate.addDataGenerator(
                ProviderType.ITEM_TAGS,
                provider -> {
                    // Only copy tags whose members all have items. Not every block tag satisfies
                    // this.
                    provider.copy(BlockTags.LOGS, ItemTags.LOGS);
                    provider.copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
                    provider.copy(BlockTags.LEAVES, ItemTags.LEAVES);
                    provider.copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
                    provider.copy(BlockTags.SMALL_FLOWERS, ItemTags.SMALL_FLOWERS);
                    provider.copy(BlockTags.FLOWERS, ItemTags.FLOWERS);
                    provider.copy(BlockTags.IRON_ORES, ItemTags.IRON_ORES);
                    provider.copy(BlockTags.GOLD_ORES, ItemTags.GOLD_ORES);
                    provider.copy(BlockTags.COPPER_ORES, ItemTags.COPPER_ORES);
                    provider.copy(BlockTags.LAPIS_ORES, ItemTags.LAPIS_ORES);
                    provider.copy(BlockTags.REDSTONE_ORES, ItemTags.REDSTONE_ORES);
                    provider.copy(BlockTags.DIAMOND_ORES, ItemTags.DIAMOND_ORES);
                    provider.copy(BlockTags.EMERALD_ORES, ItemTags.EMERALD_ORES);
                    provider.copy(Tags.Blocks.COBBLESTONES_NORMAL, Tags.Items.COBBLESTONES_NORMAL);
                    provider.copy(Tags.Blocks.COBBLESTONES_MOSSY, Tags.Items.COBBLESTONES_MOSSY);
                    provider.copy(Tags.Blocks.STONES, Tags.Items.STONES);
                    provider.copy(Tags.Blocks.ORES, Tags.Items.ORES);
                    provider.copy(Tags.Blocks.ORES_QUARTZ, Tags.Items.ORES_QUARTZ);
                    provider.copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
                    provider.copy(Tags.Blocks.BUDS, Tags.Items.BUDS);
                    provider.copy(Tags.Blocks.CLUSTERS, Tags.Items.CLUSTERS);
                    provider.copy(Tags.Blocks.BUDDING_BLOCKS, Tags.Items.BUDDING_BLOCKS);
                    provider.copy(Tags.Blocks.FENCE_GATES_WOODEN, Tags.Items.FENCE_GATES_WOODEN);
                    provider.copy(Tags.Blocks.STRIPPED_LOGS, Tags.Items.STRIPPED_LOGS);
                    provider.copy(Tags.Blocks.STRIPPED_WOODS, Tags.Items.STRIPPED_WOODS);
                    provider.copy(Tags.Blocks.NATURAL_LOGS, Tags.Items.NATURAL_LOGS);
                    provider.copy(Tags.Blocks.NATURAL_WOODS, Tags.Items.NATURAL_WOODS);
                    provider.tag(ItemTags.STONE_TOOL_MATERIALS).add(MiaBlocks.ABYSS_COBBLED_ANDESITE.get().asItem());
                    provider.tag(ItemTags.STONE_CRAFTING_MATERIALS).add(MiaBlocks.ABYSS_COBBLED_ANDESITE.get().asItem());
                    provider.copy(MiaTags.BlockTags.PRIMO_STEMS.tag, MiaTags.ItemTags.PRIMO_STEMS.tag);
                });
    }
}
