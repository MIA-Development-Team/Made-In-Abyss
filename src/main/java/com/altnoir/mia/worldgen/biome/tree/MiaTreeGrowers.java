package com.altnoir.mia.worldgen.biome.tree;

import com.altnoir.mia.MIA;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class MiaTreeGrowers {
    public static final TreeGrower SKYFOG_TREE = new TreeGrower(MIA.MOD_ID + ":skyfog_tree",
            Optional.of(MiaTreeFeatures.MEGA_SKYFOG_TREE),
            Optional.of(MiaTreeFeatures.SKYFOG_TREE),
            Optional.of(MiaTreeFeatures.SKYFOG_TREE_BEES)
    );
}
