package com.altnoir.mia.compat.jade;

import com.altnoir.mia.common.block.AbyssSpawnerBlock;
import com.altnoir.mia.common.block.entity.AbyssSpawnerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class MiaJadePlugin implements IWailaPlugin {
    public static final ResourceLocation SPAWNER_COOLDOWN = ResourceLocation.parse("debug:spawner_cooldown");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(AbyssSpawnerComponentProvider.INSTANCE, AbyssSpawnerBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(AbyssSpawnerComponentProvider.INSTANCE, AbyssSpawnerBlock.class);
    }
}
