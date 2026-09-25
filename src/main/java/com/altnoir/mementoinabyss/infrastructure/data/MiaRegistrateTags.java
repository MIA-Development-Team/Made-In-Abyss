package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaTags;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Blocks;

/** Cross-entry tags and explicit block-to-item copies; per-entry tags live at registration. */
public final class MiaRegistrateTags {
    private MiaRegistrateTags() {}

    public static void addGenerators() {
        var registrate = MementoInAbyss.registrate();
        registrate.addDataGenerator(
                ProviderType.BLOCK_TAGS,
                provider -> {
                    provider.tag(MiaTags.BlockTags.ABYSS_MUD_ORE_REPLACEABLE.tag).add(Blocks.MUD);
                    provider.tag(MiaTags.BlockTags.MYCELIUM_REPLACEABLE.tag).addTag(BlockTags.DIRT);
                    provider.tag(MiaTags.BlockTags.PRIMO_STEMS.tag)
                            .add(
                                    com.altnoir.mementoinabyss.init.MiaBlocks.PRIMO_STEM.get(),
                                    com.altnoir.mementoinabyss.init.MiaBlocks.STRIPPED_PRIMO_STEM
                                            .get(),
                                    com.altnoir.mementoinabyss.init.MiaBlocks.PRIMO_HYPHAE.get(),
                                    com.altnoir.mementoinabyss.init.MiaBlocks.STRIPPED_PRIMO_HYPHAE
                                            .get());
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
                    provider.copy(
                            MiaTags.BlockTags.PRIMO_STEMS.tag, MiaTags.ItemTags.PRIMO_STEMS.tag);
                });
    }
}
