package com.altnoir.mementoinabyss.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public final class LampTubeHeatComponent implements IBlockComponentProvider {
    public static final LampTubeHeatComponent INSTANCE = new LampTubeHeatComponent();

    private LampTubeHeatComponent() {}

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        LampTubeHeatProvider.INSTANCE
                .decodeFromData(accessor)
                .ifPresent(
                        heat ->
                                tooltip.add(
                                        Component.translatable(
                                                "jade.mementoinabyss.lamp_tube.heat",
                                                heat.amount(),
                                                heat.capacity())));
    }

    @Override
    public Identifier getUid() {
        return MiaJadePlugin.LAMP_TUBE_HEAT;
    }
}
