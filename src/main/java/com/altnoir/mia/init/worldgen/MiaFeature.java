package com.altnoir.mia.init.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.worldgen.feature.BigClusterFeature;
import com.altnoir.mia.worldgen.feature.ClusterFeature;
import com.altnoir.mia.worldgen.feature.LakeFeature;
import com.altnoir.mia.worldgen.feature.configurations.ClusterConfiguration;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaFeature {
    public static final DeferredRegister<Feature<?>> FEATURE = DeferredRegister.create(Registries.FEATURE, MIA.MOD_ID);

    public static final DeferredHolder<Feature<?>, BigClusterFeature> BIG_CLUSTER = FEATURE.register(
            "big_cluster", () -> new BigClusterFeature(ClusterConfiguration.CODEC)

    );
    public static final DeferredHolder<Feature<?>, ClusterFeature> CLUSTER = FEATURE.register(
            "cluster", () -> new ClusterFeature(ClusterConfiguration.CODEC)
    );
    public static final DeferredHolder<Feature<?>, LakeFeature> LAKE = FEATURE.register(
            "lake", () -> new LakeFeature(LakeFeature.Configuration.CODEC)
    );

    public static void register(IEventBus eventBus) {
        FEATURE.register(eventBus);
    }

    public static boolean isStone(BlockState state) {
        return state.is(MiaTags.Blocks.BASE_STONE_ABYSS);
    }
}
