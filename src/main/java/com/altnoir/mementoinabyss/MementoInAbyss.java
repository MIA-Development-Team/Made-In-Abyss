package com.altnoir.mementoinabyss;

import com.altnoir.mementoinabyss.compat.MiaMods;
import com.altnoir.mementoinabyss.data.MiaDataGen;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.altnoir.mementoinabyss.init.*;
import com.altnoir.mementoinabyss.network.MiaLodNetwork;
import com.mojang.logging.LogUtils;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(MementoInAbyss.ID)
public class MementoInAbyss {
    public static final String ID = "mementoinabyss";
    public static final String NAME = "Memento In Abyss";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final MiaConfigs CONFIGS = ConfigApiJava.registerAndLoadConfig(MiaConfigs::new);

    private static final MiaRegistrate REGISTRATE = MiaRegistrate.create(ID);

    public MementoInAbyss(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} {}+{} initializing...", NAME, MiaBuildInfo.VERSION, MiaBuildInfo.GIT_COMMIT);

        REGISTRATE.registerEventListeners(modEventBus);

        MiaDataComponents.register(modEventBus);
        MiaDataAttachments.register(modEventBus);
        MiaBiomeSources.register(modEventBus);
        MiaDensityFunctionTypes.register(modEventBus);
        MiaPlacementModifiers.register(modEventBus);
        MiaWorldgenFeatures.register(modEventBus);

        MiaItemGroups.register();
        MiaSoundEvents.register();
        MiaBlocks.register();
        MiaItems.register();
        MiaArtifactItems.register();

        modEventBus.addListener(EventPriority.HIGHEST, MiaDataGen::gatherDataHighPriority);
        modEventBus.addListener(EventPriority.LOWEST, MiaDataGen::gatherData);
        modEventBus.addListener(MiaLodNetwork::register);
    }

    public static Identifier asResource(String string) {
        return Identifier.fromNamespaceAndPath(ID, string);
    }

    public static MiaRegistrate registrate() {
        return REGISTRATE;
    }
}
