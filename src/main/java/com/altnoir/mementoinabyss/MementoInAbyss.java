package com.altnoir.mementoinabyss;

import com.altnoir.mementoinabyss.impl.curse.CurseManager;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaItemGroups;
import com.altnoir.mementoinabyss.init.MiaItems;
import net.minecraft.resources.Identifier;
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

    public static final CurseManager CURSE_MANAGER = new CurseManager();

    public MementoInAbyss(IEventBus modEventBus, ModContainer modContainer) {
        REGISTRATE.registerEventListeners(modEventBus);

        MiaItemGroups.register();
        MiaBlocks.register();
        MiaItems.register();
    }

    public static Identifier asResource(String string) {
        return Identifier.fromNamespaceAndPath(ID, string);
    }

    public static MiaRegistrate registrate() {
        return REGISTRATE;
    }
}
