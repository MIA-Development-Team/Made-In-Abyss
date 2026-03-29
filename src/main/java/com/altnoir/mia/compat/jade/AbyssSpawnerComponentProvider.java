package com.altnoir.mia.compat.jade;

import com.altnoir.mia.common.block.entity.AbyssSpawnerBlockEntity;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum AbyssSpawnerComponentProvider implements
        IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final String tag = "spawner_cooldown";

    @Override
    public ResourceLocation getUid() {
        return MiaJadePlugin.SPAWNER_COOLDOWN;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (!blockAccessor.getServerData().contains(tag))
            return;

        var cooldown = blockAccessor.getServerData().getInt(tag);

        if (cooldown <= 0)
            return;

        tooltip.add(
                Component.translatable(
                        "tooltip.mia.abyss_spawner.cooldown",
                        MiaUtil.formatTickToTime(cooldown)
                )
        );
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        var spawner = (AbyssSpawnerBlockEntity)blockAccessor.getBlockEntity();
        compoundTag.putInt(tag, spawner.getAbyssSpawner().getRemainingCooldown());
    }
}
