package com.altnoir.mia.event.common;

import com.altnoir.mia.util.MiaUtil;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public class PackFindersEvent {
    public static void packSetup(AddPackFindersEvent event) {
        event.addPackFinders(
                MiaUtil.miaId("resourcepacks/mia_music_pack"),
                PackType.CLIENT_RESOURCES,
                MiaUtil.translatable("pack.mia.music_pack.name"),
                PackSource.BUILT_IN,
                false,
                Pack.Position.TOP
        );
    }
}
