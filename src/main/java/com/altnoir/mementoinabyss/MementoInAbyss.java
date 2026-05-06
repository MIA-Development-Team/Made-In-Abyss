package com.altnoir.mementoinabyss;

import com.altnoir.mementoinabyss.data.MiaDataGen;
import com.altnoir.mementoinabyss.impl.curse.CurseManager;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaDataAttachments;
import com.altnoir.mementoinabyss.init.MiaItemGroups;
import com.altnoir.mementoinabyss.init.MiaItems;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(MementoInAbyss.ID)
public class MementoInAbyss {
    public static final String ID = "mementoinabyss";
    public static final String NAME = "Memento In Abyss";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final MiaRegistrate REGISTRATE = MiaRegistrate.create(ID);

    public MementoInAbyss(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} {}+{} initializing...", NAME, MiaBuildInfo.VERSION, MiaBuildInfo.GIT_COMMIT);

        REGISTRATE.registerEventListeners(modEventBus);

        MiaDataAttachments.register(modEventBus);

        MiaItemGroups.register();
        MiaBlocks.register();
        MiaItems.register();

        modEventBus.addListener(EventPriority.LOWEST, MiaDataGen::gatherData);
    }

    public static Identifier asResource(String string) {
        return Identifier.fromNamespaceAndPath(ID, string);
    }

    public static MiaRegistrate registrate() {
        return REGISTRATE;
    }
}
