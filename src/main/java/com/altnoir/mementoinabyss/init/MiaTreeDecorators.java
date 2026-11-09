package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.DuskveilIchorDecorator;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.DuskveilVinesDecorator;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.GroundPatchTreeDecorator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaTreeDecorators {
    private static final DeferredRegister<TreeDecoratorType<?>> TYPES =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, MementoInAbyss.ID);
    public static final DeferredHolder<
                    TreeDecoratorType<?>, TreeDecoratorType<DuskveilIchorDecorator>>
            DUSKVEIL_ICHOR =
                    TYPES.register(
                            "duskveil_ichor",
                            () -> new TreeDecoratorType<>(DuskveilIchorDecorator.CODEC));
    public static final DeferredHolder<
                    TreeDecoratorType<?>, TreeDecoratorType<DuskveilVinesDecorator>>
            DUSKVEIL_VINES =
                    TYPES.register(
                            "duskveil_vines",
                            () -> new TreeDecoratorType<>(DuskveilVinesDecorator.CODEC));
    public static final DeferredHolder<
                    TreeDecoratorType<?>, TreeDecoratorType<GroundPatchTreeDecorator>>
            GROUND_PATCH =
                    TYPES.register(
                            "ground_patch",
                            () -> new TreeDecoratorType<>(GroundPatchTreeDecorator.CODEC));

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }

    private MiaTreeDecorators() {}
}
