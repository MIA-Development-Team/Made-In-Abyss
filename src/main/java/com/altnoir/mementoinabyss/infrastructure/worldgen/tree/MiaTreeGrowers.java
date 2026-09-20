package com.altnoir.mementoinabyss.infrastructure.worldgen.tree;

import com.altnoir.mementoinabyss.MementoInAbyss;
import java.util.Optional;
import net.minecraft.world.level.block.grower.TreeGrower;

public final class MiaTreeGrowers {
    public static final TreeGrower SKYFOG =
            new TreeGrower(
                    MementoInAbyss.ID + ":skyfog_tree",
                    Optional.of(MiaTreeFeatures.MEGA_SKYFOG_TREE),
                    Optional.of(MiaTreeFeatures.SKYFOG_TREE),
                    Optional.of(MiaTreeFeatures.SKYFOG_TREE_BEES));
    public static final TreeGrower INVERTED =
            new TreeGrower(
                    MementoInAbyss.ID + ":inverted_tree",
                    Optional.of(MiaTreeFeatures.MEGA_INVERTED_TREE),
                    Optional.of(MiaTreeFeatures.INVERTED_TREE),
                    Optional.empty());

    private MiaTreeGrowers() {}
}
