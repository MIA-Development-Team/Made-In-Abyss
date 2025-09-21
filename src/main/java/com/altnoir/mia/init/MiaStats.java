package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaStats {
    public static final DeferredRegister<ResourceLocation> CUSTOM_STATS = DeferredRegister.create(Registries.CUSTOM_STAT, MIA.MOD_ID);

    public static final DeferredHolder<ResourceLocation, ResourceLocation> INTERACT_WITH_ARTIFACT_SMITHING_TABLE = CUSTOM_STATS.register("artifact_smithing_table", () ->
            MiaUtil.miaId("interact_with_artifact_smithing_table"));

    public static void register(IEventBus modEventBus) {
        CUSTOM_STATS.register(modEventBus);
    }

    public static void init() {
        Stats.CUSTOM.get(INTERACT_WITH_ARTIFACT_SMITHING_TABLE.get(), StatFormatter.DEFAULT);
    }
}
