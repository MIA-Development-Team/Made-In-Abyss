package com.altnoir.mia.block.abs;

import net.minecraft.world.level.block.Block;

public abstract class AbstractCoverGrassBlock extends Block{
    public final Block defaultBlock;

    public AbstractCoverGrassBlock(Block defaultBlock, Properties properties) {
        super(properties);
        this.defaultBlock = defaultBlock;
    }
}