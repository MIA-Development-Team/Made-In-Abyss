package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.artifact.enhancement.ArtifactEnhancementMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MementoInAbyss.ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ArtifactEnhancementMenu>>
            ARTIFACT_ENHANCEMENT = MENUS.register(
                    "artifact_enhancement",
                    () -> new MenuType<>(ArtifactEnhancementMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }

    private MiaMenus() {}
}
