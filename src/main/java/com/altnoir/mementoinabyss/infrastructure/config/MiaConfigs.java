package com.altnoir.mementoinabyss.infrastructure.config;

import com.altnoir.mementoinabyss.MementoInAbyss;
import me.fzzyhmstrs.fzzy_config.annotations.NonSync;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.api.SaveType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

public class MiaConfigs extends Config {
    public MiaConfigs() {
        super(MementoInAbyss.asResource("config"));
    }

    @NonSync
    public GraphsSection graphsSection = new GraphsSection();

    public GamePlaySection gamePlaySection = new GamePlaySection();
    public WorldGenSection worldGenSection = new WorldGenSection();

    public static class GraphsSection extends ConfigSection {
        public GraphsSection() {
            super();
        }

        public ConfigGroup performanceGroup = new ConfigGroup("performance");

        @ConfigGroup.Pop
        public ValidatedInt miaCpuThreadLimit =
                new ValidatedInt(
                        Math.max(1, Runtime.getRuntime().availableProcessors() - 6),
                        Math.max(1, Runtime.getRuntime().availableProcessors()),
                        1,
                        ValidatedInt.WidgetType.TEXTBOX);

        public ConfigGroup crossDimensionLodGroup = new ConfigGroup("cross_dimension_lod");
        public ValidatedBoolean crossDimensionLodEnabled = new ValidatedBoolean(true);
        public ValidatedInt crossDimensionLodCaptureQueueLimit =
                new ValidatedInt(1024, 16384, 64, ValidatedInt.WidgetType.TEXTBOX);

        @ConfigGroup.Pop
        public ValidatedInt crossDimensionLodViewDistance =
                new ValidatedInt(32, 128, 16, ValidatedInt.WidgetType.TEXTBOX);
    }

    public static class GamePlaySection extends ConfigSection {
        public GamePlaySection() {
            super();
        }

        public ConfigGroup curseGroup = new ConfigGroup("curse");
        public ValidatedBoolean enableCurse = new ValidatedBoolean(true);
        @ConfigGroup.Pop
        public ValidatedBoolean enableCurseCreative = new ValidatedBoolean(false);

        public ConfigGroup blazeReapGroup = new ConfigGroup("blaze_reap");
        public ValidatedInt blazeReapExplosionCount = new ValidatedInt(4, 64, 1);

        @ConfigGroup.Pop
        public ValidatedDouble blazeReapExplosionRadius = new ValidatedDouble(2, 20, 0.1);

        public ConfigGroup hookGroup = new ConfigGroup("hook");
        public ValidatedDouble hookMaxDistance = new ValidatedDouble(32, 256, 1);
        public ValidatedDouble hookShootVelocity = new ValidatedDouble(4, 4, 0.5);
        public ValidatedDouble hookPullVelocity = new ValidatedDouble(0.2, 2, 0.01);
        public ValidatedDouble hookStopPullDistance = new ValidatedDouble(1.41421, 10, 1);
        public ValidatedDouble hookRetractVelocity = new ValidatedDouble(0.75, 5, 0.1);
        public ValidatedDouble hookRetractDistance = new ValidatedDouble(4, 16, 1);
        @ConfigGroup.Pop
        public ValidatedDouble hookJumpBoost = new ValidatedDouble(1.25, 3, 1);

        public ConfigGroup caveExplorerBeacon = new ConfigGroup("cave_explorer_beacon");
        public ValidatedInt caveExplorerBeaconHorizontal = new ValidatedInt(10, 1024, 1);
        public ValidatedInt caveExplorerBeaconVertical = new ValidatedInt(5, 1024, 1);

        @ConfigGroup.Pop
        public ValidatedBoolean caveExplorerBeaconMaxVertical = new ValidatedBoolean(false);
    }

    public static class WorldGenSection extends ConfigSection {
        public WorldGenSection() {
            super();
        }

        public ValidatedInt abyssRadius =
                new ValidatedInt(160, 10240, 64, ValidatedInt.WidgetType.TEXTBOX);
        public ValidatedBoolean overworldAbyssEnabled = new ValidatedBoolean(true);

        /**
         * Shape exponent of the outer slope, clamped to 0.5-3.0 by the geometry itself. The slope always
         * spans the same band and lands on the surrounding ground at the influence radius; this only
         * picks its profile. 1.0 is the plain fall; below it the ring holds its height further out and
         * then drops faster, which reads as the steeper flank; above it flattens the middle of the band
         * and spends the height in a bank at the far end.
         */
        public ValidatedDouble abyssOuterSlope =
                new ValidatedDouble(1.0, 3.0, 0.5, ValidatedDouble.WidgetType.TEXTBOX);

        /**
         * Width of the outer slope's descent, in units of the effective overworld radius
         * ({@code 2 * abyssRadius}), measured outwards from
         * the ring. This is the knob that sets how steep the junction is: the whole ring-to-ground drop
         * is spent across this band, so the average gradient is the drop divided by
         * {@code abyssSlopeBand * 2 * abyssRadius}. Lower is steeper, never below 1.0 - the band must be at
         * least as wide as the ground the blend has to cover, or the carve stops before it has landed
         * and leaves a cliff. Raising it past 2.0 only spends more pristine terrain on a shallower
         * approach.
         */
        public ValidatedDouble abyssSlopeBand =
                new ValidatedDouble(1.25, 2.0, 1.0, ValidatedDouble.WidgetType.TEXTBOX);
    }

    @Override
    public int defaultPermLevel() {
        return 4;
    }

    @Override
    public FileType fileType() {
        return FileType.TOML;
    }

    @Override
    public SaveType saveType() {
        return SaveType.SEPARATE;
    }
}
