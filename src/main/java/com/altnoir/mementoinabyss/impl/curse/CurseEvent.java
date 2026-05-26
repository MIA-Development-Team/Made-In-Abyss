package com.altnoir.mementoinabyss.impl.curse;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.curse.data.CurseRegistries;
import com.altnoir.mementoinabyss.init.MiaDataAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public class CurseEvent {
    public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        CurseRegistries.register(event);
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity))
            return;

        if (livingEntity.level().isClientSide())
            return;

        if (livingEntity instanceof Player player && player.isCreative() && !MementoInAbyss.CONFIGS.gamePlaySection.enableCurseCreative.get())
            return;

        var dimIdentifier = livingEntity.level().dimension().identifier();

        var curseOpt = CurseManager.get(dimIdentifier);
        if (curseOpt.isEmpty())
            return;

        var curseDimension = curseOpt.get();
        var data = livingEntity.getData(MiaDataAttachments.CURSE);

        int y = (int) livingEntity.getY();

        int minY = data.getMinY();

        if (y < minY) {
            data.setMinY(y);
            data.setLevel(0);
            return;
        }

        int level = y - minY;
        data.setLevel(level);

        if (level < data.getMaxLevel())
            return;

        for (var element : curseDimension.effects()) {
            var holder = BuiltInRegistries.MOB_EFFECT.get(element.effect());

            holder.ifPresent(mobEffect -> {
                livingEntity.addEffect(new MobEffectInstance(
                        mobEffect,
                        element.duration(),
                        element.amplifier(),
                        false,
                        true
                ));
            });
        }

        data.setLevel(0);
        data.setMinY(y);
    }

    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getOriginal().hasData(MiaDataAttachments.CURSE)) {
             event.getEntity().getData(MiaDataAttachments.CURSE).copyFrom(event.getOriginal().getData(MiaDataAttachments.CURSE));
        }
    }
}
