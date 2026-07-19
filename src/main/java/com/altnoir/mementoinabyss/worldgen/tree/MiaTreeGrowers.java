package com.altnoir.mementoinabyss.worldgen.tree;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public final class MiaTreeGrowers {
    public static final TreeGrower SKYFOG = new TreeGrower(
            MementoInAbyss.ID + ":skyfog_tree",
            Optional.of(MiaTreeFeatures.MEGA_SKYFOG_TREE),
            Optional.of(MiaTreeFeatures.SKYFOG_TREE),
            Optional.of(MiaTreeFeatures.SKYFOG_TREE_BEES));

    private MiaTreeGrowers() {}
}
