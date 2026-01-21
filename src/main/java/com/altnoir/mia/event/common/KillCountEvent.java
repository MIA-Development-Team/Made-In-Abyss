package com.altnoir.mia.event.common;

import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.common.item.GrowSwordItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class KillCountEvent {
    public static void onLivingDeath(LivingEntity entity, DamageSource source) {
        // 检查死亡实体是否为敌对生物
        if (entity.getType().getCategory() != MobCategory.MONSTER) {
            return;
        }
        // 检查攻击源是否来自玩家
        if (source.getEntity() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();

            if (mainHandItem.getItem() instanceof GrowSwordItem) {
                var component = mainHandItem.get(MiaComponents.KILL_COUNT);
                int count = component != null ? component : 0;
                int newKillCount = count + 1;

                // 设置新的击杀计数
                mainHandItem.set(MiaComponents.KILL_COUNT, newKillCount);

                // 更新物品
                ((GrowSwordItem) mainHandItem.getItem()).updateAttributes(mainHandItem);
            }
        }
    }
}