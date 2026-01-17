package com.altnoir.mia.init.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.feature.trunk.InvertedStraightTrunkPlacer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaTrunkPlacerTypes {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER_TYPES = DeferredRegister.create(BuiltInRegistries.TRUNK_PLACER_TYPE, MIA.MOD_ID);

    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<InvertedStraightTrunkPlacer>> STRAIGHT_TRUNK_PLACER =
            TRUNK_PLACER_TYPES.register("inverted_straight_trunk_placer", () -> new TrunkPlacerType<>(InvertedStraightTrunkPlacer.CODEC));
//    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<ForkingTrunkPlacer>> FORKING_TRUNK_PLACER =
//            TRUNK_PLACER_TYPES.register("inverted_forking_trunk_placer", () -> new TrunkPlacerType<>(ForkingTrunkPlacer.CODEC));
//    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<GiantTrunkPlacer>> GIANT_TRUNK_PLACER =
//            TRUNK_PLACER_TYPES.register("inverted_giant_trunk_placer", () -> new TrunkPlacerType<>(GiantTrunkPlacer.CODEC));
//    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<MegaJungleTrunkPlacer>> MEGA_JUNGLE_TRUNK_PLACER =
//            TRUNK_PLACER_TYPES.register("inverted_mega_jungle_trunk_placer", () -> new TrunkPlacerType<>(MegaJungleTrunkPlacer.CODEC));
//    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<DarkOakTrunkPlacer>> DARK_OAK_TRUNK_PLACER =
//            TRUNK_PLACER_TYPES.register("inverted_dark_oak_trunk_placer", () -> new TrunkPlacerType<>(DarkOakTrunkPlacer.CODEC));
//    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<FancyTrunkPlacer>> FANCY_TRUNK_PLACER =
//            TRUNK_PLACER_TYPES.register("inverted_fancy_trunk_placer", () -> new TrunkPlacerType<>(FancyTrunkPlacer.CODEC));
//    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<BendingTrunkPlacer>> BENDING_TRUNK_PLACER =
//            TRUNK_PLACER_TYPES.register("inverted_bending_trunk_placer", () -> new TrunkPlacerType<>(BendingTrunkPlacer.CODEC));
//    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<UpwardsBranchingTrunkPlacer>> UPWARDS_BRANCHING_TRUNK_PLACER =
//            TRUNK_PLACER_TYPES.register("inverted_upwards_branching_trunk_placer", () -> new TrunkPlacerType<>(UpwardsBranchingTrunkPlacer.CODEC));
//    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<CherryTrunkPlacer>> CHERRY_TRUNK_PLACER =
//            TRUNK_PLACER_TYPES.register("inverted_cherry_trunk_placer", () -> new TrunkPlacerType<>(CherryTrunkPlacer.CODEC));

    public static void register(IEventBus eventBus) {
        TRUNK_PLACER_TYPES.register(eventBus);
    }
}