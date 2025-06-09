package com.altnoir.mia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = MIA.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static boolean curse;

    private static final ModConfigSpec.BooleanValue CURSE = BUILDER
            .comment("Whether to enable the Curse (Default: true) | 是否启用上升诅咒 (默认值: true)")
            .define("curse", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        curse = CURSE.get();
    }
}