package com.altnoir.mia.init;

import java.util.function.Supplier;

import com.altnoir.mia.MIA;
import com.altnoir.mia.common.inventory.ArtifactSmithingTableMenu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister
            .create(BuiltInRegistries.MENU, MIA.MOD_ID);

    public static final Supplier<MenuType<ArtifactSmithingTableMenu>> ARTIFACT_ENHANCEMENT_TABLE = MENU_TYPES
            .register(
                    "artifact_enhancement_table",
                    () -> new MenuType(ArtifactSmithingTableMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
