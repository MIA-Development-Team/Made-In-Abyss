package com.altnoir.mementoinabyss.compat.jade;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.lamptube.AmethystTubeBlock;
import com.altnoir.mementoinabyss.content.lamptube.AmethystTubeBlockEntity;
import net.minecraft.resources.Identifier;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class MiaJadePlugin implements IWailaPlugin {
    public static final Identifier LAMP_TUBE_HEAT = MementoInAbyss.asResource("lamp_tube_heat");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(
                LampTubeHeatProvider.INSTANCE, AmethystTubeBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(
                LampTubeHeatComponent.INSTANCE, AmethystTubeBlock.class);
    }
}
