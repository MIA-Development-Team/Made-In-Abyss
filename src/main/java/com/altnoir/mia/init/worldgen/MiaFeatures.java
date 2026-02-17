package com.altnoir.mia.init.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.worldgen.feature.*;
import com.altnoir.mia.worldgen.feature.configurations.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaFeatures {
    public static final DeferredRegister<Feature<?>> FEATURE = DeferredRegister.create(Registries.FEATURE, MIA.MOD_ID);

    public static final DeferredHolder<Feature<?>, BigClusterFeature> BIG_CLUSTER = FEATURE.register(
            "big_cluster", () -> new BigClusterFeature(ClusterConfiguration.CODEC)
    );
    public static final DeferredHolder<Feature<?>, ClusterFeature> CLUSTER = FEATURE.register(
            "cluster", () -> new ClusterFeature(ClusterConfiguration.CODEC)
    );
    public static final DeferredHolder<Feature<?>, InvertedTreeFeature> INVERTED_TREE = FEATURE.register(
            "inverted_tree", () -> new InvertedTreeFeature(TreeConfiguration.CODEC)
    );
    public static final DeferredHolder<Feature<?>, MiaCavePillarFeature> ABYSS_CAVE_PILLAR = FEATURE.register(
            "abyss_cave_pillar", () -> new MiaCavePillarFeature(MiaCavePillarConfiguration.CODEC)
    );
    public static final DeferredHolder<Feature<?>, LakeFeature> LAKE = FEATURE.register(
            "lake", () -> new LakeFeature(LakeFeature.Configuration.CODEC)
    );
    public static final DeferredHolder<Feature<?>, MonsterCheatFeature> MONSTER_CHEAT = FEATURE.register(
            "monster_cheat", () -> new MonsterCheatFeature(MonsterCheatConfiguration.CODEC)
    );
    public static final DeferredHolder<Feature<?>, SlabRuinsFeature> SLAB_RUINS = FEATURE.register(
            "slab_ruins", () -> new SlabRuinsFeature(SlabRuinsConfiguration.CODEC)
    );
    public static final DeferredHolder<Feature<?>, BlockTrunkFeature> BLOCK_TRUNK = FEATURE.register(
            "block_trunk", () -> new BlockTrunkFeature(BlockTrunkConfiguration.CODEC)
    );

    public static final DeferredHolder<Feature<?>, AbyssPortalFeature> ABYSS_PORTAL = FEATURE.register(
            "abyss_portal", () -> new AbyssPortalFeature(NoneFeatureConfiguration.CODEC)
    );

    public static void register(IEventBus eventBus) {
        FEATURE.register(eventBus);
    }

    public static boolean isStone(BlockState state) {
        return state.is(MiaTags.Blocks.BASE_STONE_ABYSS);
    }
}
