package com.altnoir.mia.worldgen.processor_list;

import com.altnoir.mia.util.MiaUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

import java.util.List;

public class MiaProcessorLists {
    public static final ResourceKey<StructureProcessorList> ABYSS_STRONGHOLD_GENERIC_DEGRADATION = createKey("abyss_stronghold_generic_degradation");

    public static void bootstrap(BootstrapContext<StructureProcessorList> context) {
        HolderGetter<Block> holdergetter = context.lookup(Registries.BLOCK);

        register(
                context,
                ABYSS_STRONGHOLD_GENERIC_DEGRADATION,
                ImmutableList.of(
                        new RuleProcessor(
                                ImmutableList.of(
                                        new ProcessorRule(
                                                new RandomBlockMatchTest(Blocks.POLISHED_TUFF, 0.25F),
                                                AlwaysTrueTest.INSTANCE, Blocks.TUFF.defaultBlockState()),
                                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHITE_CANDLE, 0.05F),
                                                AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
                                )
                        ),
                        new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
                )
        );
    }

    private static void register(
            BootstrapContext<StructureProcessorList> context, ResourceKey<StructureProcessorList> key, List<StructureProcessor> processors
    ) {
        context.register(key, new StructureProcessorList(processors));
    }

    private static ResourceKey<StructureProcessorList> createKey(String name) {
        return ResourceKey.create(Registries.PROCESSOR_LIST, MiaUtil.miaId(name));
    }
}
