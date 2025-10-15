package com.altnoir.mia.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@OnlyIn(Dist.CLIENT)
public class MiaClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public enum CurseIconPosition {MIDDLE, RIGHT, HIDDEN}

    public static CurseIconPosition curseIconPosition;

    private static final ModConfigSpec.EnumValue<CurseIconPosition> CURSE_ICON_POSITION = BUILDER
            .comment("Curse icon position (Default: MIDDLE) | 诅咒图标位置 (默认值: MIDDLE)",
                    "MIDDLE: Display curse icon in the middle | 在中间显示诅咒图标",
                    "RIGHT: Display curse icon on the right | 在右侧显示诅咒图标",
                    "HIDDEN: Disable curse icon | 隐藏诅咒图标")
            .defineEnum("curse_icon_position", CurseIconPosition.MIDDLE);


    public static final ModConfigSpec CLIENT_SPEC = BUILDER.build();

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == CLIENT_SPEC) {
            curseIconPosition = CURSE_ICON_POSITION.get();
        }
    }
}