package com.altnoir.mia;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;


public class MiaConfig {
    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    public enum DisconnectButtonState {
        DEFAULT, DISABLED, HIDDEN
    }

    public static int abyssRadius;

    public static boolean curse;
    public static boolean curseGod;
    public static DisconnectButtonState disconnectButtonState;

    private static final ModConfigSpec.IntValue ABYSS_RADIUS = COMMON_BUILDER
            .comment("The radius of the Abyss (Default: 160) | 深渊半径 (默认值: 160)")
            .defineInRange("abyss_radius", 160, 64, 10000);

    private static final ModConfigSpec.BooleanValue CURSE = SERVER_BUILDER
            .comment("Whether to enable the Curse (Default: true) | 是否启用上升诅咒 (默认值: true)")
            .define("curse", true);
    private static final ModConfigSpec.BooleanValue CURSE_GOD = SERVER_BUILDER
            .comment("Whether to Curse the Creative and Spectator(Default: false) | 是否诅咒创造模式和观察者模式 (默认值: false, 需要启用诅咒)")
            .define("curse_god", false);
    private static final ModConfigSpec.EnumValue<DisconnectButtonState> DISCONNECT_BUTTON_STATE = COMMON_BUILDER
            .comment("Controls the disconnect button behavior (Default: DEFAULT) | 控制退出按钮行为 (默认值: DEFAULT)",
                    "DEFAULT: No changes to the disconnect button | 不做任何处理",
                    "DISABLED: Disable the disconnect button | 禁用退出按钮",
                    "HIDDEN: Hide the disconnect button | 隐藏退出按钮")
            .defineEnum("disconnect_button_state", DisconnectButtonState.DEFAULT);


    public static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();
    public static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    private static void loadValues(ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            abyssRadius = ABYSS_RADIUS.get();
            disconnectButtonState = DISCONNECT_BUTTON_STATE.get();
        }

        if (event.getConfig().getSpec() == SERVER_SPEC) {
            curse = CURSE.get();
            curseGod = CURSE_GOD.get();
        }
    }

    public static void loadEvent(final ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Loading || event instanceof ModConfigEvent.Reloading) {
            loadValues(event);
        }
    }
}