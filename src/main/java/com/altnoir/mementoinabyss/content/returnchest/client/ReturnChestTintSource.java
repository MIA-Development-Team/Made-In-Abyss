package com.altnoir.mementoinabyss.content.returnchest.client;

import com.altnoir.mementoinabyss.content.returnchest.ReturnChestBlock;
import com.altnoir.mementoinabyss.content.returnchest.ReturnChests;
import java.util.Set;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class ReturnChestTintSource implements BlockTintSource {
    public static final ReturnChestTintSource INSTANCE = new ReturnChestTintSource();

    private ReturnChestTintSource() {}

    @Override
    public int color(BlockState state) {
        return ReturnChests.tintColor(state);
    }

    @Override
    public Set<Property<?>> relevantProperties() {
        return Set.of(ReturnChestBlock.TINT);
    }
}
