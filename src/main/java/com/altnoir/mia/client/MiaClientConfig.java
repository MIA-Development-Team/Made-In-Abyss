package com.altnoir.mia.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

@OnlyIn(Dist.CLIENT)
public class MiaClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public enum CurseIconPosition {MIDDLE, RIGHT, HIDDEN}

    public enum DisconnectButtonState {DEFAULT, DISABLED, HIDDEN}

    public static CurseIconPosition curseIconPosition;
    public static DisconnectButtonState disconnectButtonState;

    public static boolean autoHook;

    private static final ModConfigSpec.EnumValue<CurseIconPosition> CURSE_ICON_POSITION = BUILDER
            .comment("Curse icon position (Default: MIDDLE) | 诅咒图标位置 (默认值: MIDDLE)",
                    "MIDDLE: Display curse icon in the middle | 在中间显示诅咒图标",
                    "RIGHT: Display curse icon on the right | 在右侧显示诅咒图标",
                    "HIDDEN: Disable curse icon | 隐藏诅咒图标")
            .defineEnum("curse_icon_position", CurseIconPosition.MIDDLE);

    private static final ModConfigSpec.EnumValue<DisconnectButtonState> DISCONNECT_BUTTON_STATE = BUILDER
            .comment("Controls the disconnect button behavior (Default: DEFAULT) | 控制退出按钮行为 (默认值: DEFAULT)",
                    "DEFAULT: No changes to the disconnect button | 不做任何处理",
                    "DISABLED: Disable the disconnect button | 禁用退出按钮",
                    "HIDDEN: Hide the disconnect button | 隐藏退出按钮")
            .defineEnum("disconnect_button_state", DisconnectButtonState.DEFAULT);

    public static final ModConfigSpec.BooleanValue AUTO_HOOK = BUILDER
            .comment("Enable auto hook | 启用自动回收")
            .define("hook.auto", false);

    public static final ModConfigSpec CLIENT_SPEC = BUILDER.build();

    public static void onLoad(ModConfig config) {
        if (config.getSpec() == CLIENT_SPEC) {
            curseIconPosition = CURSE_ICON_POSITION.get();
            disconnectButtonState = DISCONNECT_BUTTON_STATE.get();
            autoHook = AUTO_HOOK.get();
        }
    }
}