package com.altnoir.mementoinabyss.foundation.registrate;

import com.altnoir.abysslib.reginth.builders.BlockBuilder;
import com.altnoir.abysslib.reginth.util.nullness.NonNullUnaryOperator;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

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
