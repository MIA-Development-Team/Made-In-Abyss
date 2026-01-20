package com.altnoir.mia;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;


public class MiaConfig {
    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    public static int abyssRadius;

    public static boolean curse;
    public static boolean curseGod;
    public static int caveExplorerBeaconHorizontal;
    public static int caveExplorerBeaconVertical;
    public static boolean caveExplorerBeaconMaxVertical;

    private static final ModConfigSpec.IntValue ABYSS_RADIUS = COMMON_BUILDER
            .comment("The radius of the Abyss (Default: 160) | 深渊半径 (默认值: 160)")
            .defineInRange("abyss_radius", 160, 64, 10000);
    /**
     * 诅咒相关的配置
     */
    private static final ModConfigSpec.BooleanValue CURSE = SERVER_BUILDER
            .comment("Whether to enable the Curse (Default: true) | 是否启用上升诅咒 (默认值: true)")
            .define("curse", true);
    private static final ModConfigSpec.BooleanValue CURSE_GOD = SERVER_BUILDER
            .comment("Whether to Curse the Creative and Spectator(Default: false) | 是否诅咒创造模式和观察者模式 (默认值: false, 需要启用诅咒)")
            .define("curse_god", false);
    /**
     * 探窟者信标相关的配置
     */
    private static final ModConfigSpec.Builder CAVE_EXPLORER = SERVER_BUILDER.push("cave_explorer_beacon")
            .comment("Cave Explorer Beacon Configuration | 探窟者信标配置");

    private static final ModConfigSpec.IntValue CAVE_EXPLORER_BEACON_HORIZONTAL = SERVER_BUILDER
            .comment("The horizontal range of the Cave Explorer Beacon, Beacon Level x (Default: 10) + 10 | 探窟者信标水平半径,信标等级 x (默认值: 10) + 10")
            .defineInRange("cave_explorer_beacon_horizontal", 10, 1, 1000);
    private static final ModConfigSpec.IntValue CAVE_EXPLORER_BEACON_VERTICAL = SERVER_BUILDER
            .comment("The vertical range of the Cave Explorer Beacon, Beacon Level x (Default: 5) + 5 | 探窟者信标垂直半径,信标等级 x (默认值: 5) + 5")
            .defineInRange("cave_explorer_beacon_vertical", 5, 1, 1000);
    private static final ModConfigSpec.BooleanValue CAVE_EXPLORER_BEACON_MAX_VERTICAL = SERVER_BUILDER
            .comment("Whether to set the vertical range of the Cave Explorer Beacon to the world height (Default: false) | 是否把探窟者信标垂直半径设置为世界高度 (默认值: false)")
            .define("cave_explorer_beacon_max_vertical", false);


    public static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();
    public static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    private static void loadValues(ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            abyssRadius = ABYSS_RADIUS.get();
        }

        if (event.getConfig().getSpec() == SERVER_SPEC) {
            curse = CURSE.get();
            curseGod = CURSE_GOD.get();
            caveExplorerBeaconHorizontal = CAVE_EXPLORER_BEACON_HORIZONTAL.get();
            caveExplorerBeaconVertical = CAVE_EXPLORER_BEACON_VERTICAL.get();
            caveExplorerBeaconMaxVertical = CAVE_EXPLORER_BEACON_MAX_VERTICAL.get();
        }
    }

    public static void loadEvent(final ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Loading || event instanceof ModConfigEvent.Reloading) {
            loadValues(event);
        }
    }
}