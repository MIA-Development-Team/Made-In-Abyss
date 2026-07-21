package com.altnoir.mementoinabyss.init;

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
    public GuiSection guiSection = new GuiSection();

    public GamePlaySection gamePlaySection = new GamePlaySection();
    public WorldGenSection worldGenSection = new WorldGenSection();

    public static class GuiSection extends ConfigSection {
        public GuiSection() {
            super();
        }

        public ConfigGroup crossDimensionLodGroup = new ConfigGroup("cross_dimension_lod");
        public ValidatedBoolean crossDimensionLodEnabled = new ValidatedBoolean(true);
        public ValidatedInt crossDimensionLodMinimumDiameter = new ValidatedInt(1024, 8192, 256, ValidatedInt.WidgetType.TEXTBOX);
        public ValidatedInt crossDimensionLodMargin = new ValidatedInt(192, 1024, 0, ValidatedInt.WidgetType.TEXTBOX);
        @ConfigGroup.Pop
        public ValidatedInt crossDimensionLodViewDistance = new ValidatedInt(32, 128, 16, ValidatedInt.WidgetType.TEXTBOX);
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

        public ValidatedInt abyssRadius =  new ValidatedInt(160, 10240, 64, ValidatedInt.WidgetType.TEXTBOX);
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
