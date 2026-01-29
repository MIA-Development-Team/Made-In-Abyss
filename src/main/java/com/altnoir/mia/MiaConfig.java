package com.altnoir.mia;

import net.neoforged.fml.config.ModConfig;
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

    // ==================== 钩爪配置 ====================
    public static double hookMaxDistance;
    public static double hookShootVelocity;
    public static double hookPullVelocity;
    public static double hookStopPullDistance;
    public static double hookRetractVelocity;
    public static double hookRetractDistance;
    public static double hookJumpBoost;

    private static final ModConfigSpec.IntValue ABYSS_RADIUS = COMMON_BUILDER
            .comment("The radius of the Abyss (Default: 160) | 深渊半径 (默认值: 160)")
            .defineInRange("abyss_radius", 160, 64, 10000);
    /**
     * 无尽锤的配置
     */
    public static final ModConfigSpec.IntValue BLAZE_LEAP_EXPLOSION_COUNT = COMMON_BUILDER
            .comment("The Boom counts of the Blaze Leap (Default: 4)| Blaze Leap武器攻击时的爆炸次数 (默认值: 4)")
            .defineInRange("blaze_leap.explosion_count", 3, 1, 64);

    public static final ModConfigSpec.DoubleValue BLAZE_LEAP_EXPLOSION_RADIUS = COMMON_BUILDER
            .comment("The Boom radius of the Blaze Leap (Default: 2) | Blaze Leap武器爆炸范围 (默认值: 2)")
            .defineInRange("blaze_leap.explosion_radius", 2.0, 0.1, 20.0);
    /**
     * 钩爪相关的配置
     */
    private static final ModConfigSpec.Builder HOOK = COMMON_BUILDER.push("hook")
            .comment("Hook Configuration | 钩爪配置");

    private static final ModConfigSpec.DoubleValue HOOK_MAX_DISTANCE = HOOK
            .comment("Maximum hook distance in blocks (Default: 32.0) | 最大钩爪距离 (默认值: 32.0)")
            .defineInRange("hook_max_distance", 32.0, 1.0, 256.0);

    private static final ModConfigSpec.DoubleValue HOOK_SHOOT_VELOCITY = HOOK
            .comment("Hook shoot velocity (Default: 4.0) | 钩爪射击速度 (默认值: 4.0)")
            .defineInRange("hook_shoot_velocity", 4.0, 0.5, 4.0);

    private static final ModConfigSpec.DoubleValue HOOK_PULL_VELOCITY = HOOK
            .comment("Base pull velocity (Default: 0.3) | 基础拉取速度 (默认值: 0.2)")
            .defineInRange("hook_pull_velocity", 0.2, 0.01, 2.0);

    private static final ModConfigSpec.DoubleValue HOOK_STOP_DISTANCE = HOOK
            .comment("Distance at which the hook stops pulling the player (Default: 1.0) | 钩爪停止拉取距离 (默认值: 1.0)")
            .defineInRange("hook_stop_distance", 1.41421, 1.0, 10.0);

    private static final ModConfigSpec.DoubleValue HOOK_RETRACT_VELOCITY = HOOK
            .comment("Hook retract velocity (Default: 0.75) | 钩爪收回速度 (默认值: 0.75)")
            .defineInRange("hook_retract_velocity", 0.75, 0.1, 5.0);

    private static final ModConfigSpec.DoubleValue HOOK_RETRACT_DISTANCE = HOOK
            .comment("Retract distance in blocks (Default: 4.0) | 回收距离 (默认值: 4.0)")
            .defineInRange("hook_retract_distance", 4.0, 1.0, 16.0);

    private static final ModConfigSpec.DoubleValue HOOK_JUMP_BOOST = HOOK
            .comment("Jump boost multiplier (Default: 1.25) | 跳跃增强倍数 (默认值: 1.25)")
            .defineInRange("hook_jump_boost", 1.25, 1.0, 3.0);

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
     * 无尽锤的配置
     */
    public static final ModConfigSpec.IntValue BLAZE_REAP_EXPLOSION_COUNT = 
        COMMON_BUILDER
        .comment("The Boom counts of the Blaze REAP (Default: 4)| Blaze REAP武器攻击时的爆炸次数 (默认值: 4)")
        .defineInRange("blaze_reap.explosion_count", 3, 1, 64); 
    
    public static final ModConfigSpec.DoubleValue BLAZE_REAP_EXPLOSION_RADIUS = 
        COMMON_BUILDER
        .comment("The Boom radius of the Blaze REAP (Default: 2) | Blaze REAP武器爆炸范围 (默认值: 2)")
        .defineInRange("blaze_reap.explosion_radius", 2.0, 0.1, 20.0);
    /**
     * 探窟者信标相关的配置
     */
    private static final ModConfigSpec.Builder CAVE_EXPLORER = SERVER_BUILDER.push("cave_explorer_beacon")
            .comment("Cave Explorer Beacon Configuration | 探窟者信标配置");

    private static final ModConfigSpec.IntValue CAVE_EXPLORER_BEACON_HORIZONTAL = CAVE_EXPLORER
            .comment("The horizontal range of the Cave Explorer Beacon, Beacon Level x (Default: 10) + 10 | 探窟者信标水平半径,信标等级 x (默认值: 10) + 10")
            .defineInRange("cave_explorer_beacon_horizontal", 10, 1, 1000);
    private static final ModConfigSpec.IntValue CAVE_EXPLORER_BEACON_VERTICAL = CAVE_EXPLORER
            .comment("The vertical range of the Cave Explorer Beacon, Beacon Level x (Default: 5) + 5 | 探窟者信标垂直半径,信标等级 x (默认值: 5) + 5")
            .defineInRange("cave_explorer_beacon_vertical", 5, 1, 1000);
    private static final ModConfigSpec.BooleanValue CAVE_EXPLORER_BEACON_MAX_VERTICAL = CAVE_EXPLORER
            .comment("Whether to set the vertical range of the Cave Explorer Beacon to the world height (Default: false) | 是否把探窟者信标垂直半径设置为世界高度 (默认值: false)")
            .define("cave_explorer_beacon_max_vertical", false);

    public static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();
    public static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    public static void onLoad(ModConfig config) {
        if (config.getSpec() == COMMON_SPEC) {
            abyssRadius = ABYSS_RADIUS.get();

            hookMaxDistance = HOOK_MAX_DISTANCE.get();
            hookShootVelocity = HOOK_SHOOT_VELOCITY.get();
            hookPullVelocity = HOOK_PULL_VELOCITY.get();
            hookStopPullDistance = HOOK_STOP_DISTANCE.get();
            hookRetractVelocity = HOOK_RETRACT_VELOCITY.get();
            hookRetractDistance = HOOK_RETRACT_DISTANCE.get();
            hookJumpBoost = HOOK_JUMP_BOOST.get();
        }

        if (config.getSpec() == SERVER_SPEC) {
            curse = CURSE.get();
            curseGod = CURSE_GOD.get();
            caveExplorerBeaconHorizontal = CAVE_EXPLORER_BEACON_HORIZONTAL.get();
            caveExplorerBeaconVertical = CAVE_EXPLORER_BEACON_VERTICAL.get();
            caveExplorerBeaconMaxVertical = CAVE_EXPLORER_BEACON_MAX_VERTICAL.get();
        }
    }
}
