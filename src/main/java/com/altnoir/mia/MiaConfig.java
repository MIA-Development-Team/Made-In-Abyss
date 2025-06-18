package com.altnoir.mia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = MIA.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class MiaConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static boolean curse;
    public static boolean banDisconnect;
    public static boolean disconnectVisible;

    private static final ModConfigSpec.BooleanValue CURSE = BUILDER
            .comment("Whether to enable the Curse (Default: true) | 是否启用上升诅咒 (默认值: true)")
            .define("curse", true);
    private static final ModConfigSpec.BooleanValue BAN_DISCONNECT = BUILDER
            .comment("Whether to ban the Disconnect Button (Default: true) | 是否禁用退出游戏按钮 (默认值: true)")
            .define("banDisconnect", true);
    private static final ModConfigSpec.BooleanValue DISCONNECT_VISIBLE = BUILDER
            .comment("Whether to Hide the Disconnect Button (Default: false, The banDisconnect needs to be enabled) | 是否隐藏退出游戏按钮 (默认值: false，需要启用禁用退出按钮)")
            .define("disconnectVisible", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        curse = CURSE.get();
        banDisconnect = BAN_DISCONNECT.get();
        disconnectVisible = DISCONNECT_VISIBLE.get();
    }
}