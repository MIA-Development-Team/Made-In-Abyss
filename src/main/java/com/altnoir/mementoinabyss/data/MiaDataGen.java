package com.altnoir.mementoinabyss.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class MiaDataGen {
    public static void gatherData(GatherDataEvent.Client event) {
        if (!event.getModContainer().getModId().equals(MementoInAbyss.ID))
            return;

        event.createProvider(MiaCurseDataProvider::new);
    }
}
