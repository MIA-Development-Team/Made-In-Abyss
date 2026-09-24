package com.altnoir.mementoinabyss.foundation.registrate;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Composable registration policies; unlike initialProperties, these also configure data. */
public final class BuilderTransformers {
    private BuilderTransformers() {}

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> stone() {
        return builder ->
                builder.properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                        .transform(TagGen.pickaxeOnly());
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> wooden() {
        return builder -> builder.transform(TagGen.axeOnly());
    }

    public static <B extends SlabBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> slab() {
        return builder ->
                builder.tag(BlockTags.SLABS)
                        .loot((loot, block) -> loot.add(block, loot.createSlabItemTable(block)));
    }
}
