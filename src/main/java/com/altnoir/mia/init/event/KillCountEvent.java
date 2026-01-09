package com.altnoir.mia.init.event;

import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.item.GrowSwordItem;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class KillCountEvent {
    public static void onLivingDeath(LivingDeathEvent event) {
        // 检查死亡实体是否为敌对生物
        if (event.getEntity().getType().getCategory() != MobCategory.MONSTER) {
            return;
        }
        // 检查攻击源是否来自玩家
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();

            if (mainHandItem.getItem() instanceof GrowSwordItem) {
                // 增加击杀计数
                var component = mainHandItem.get(MiaComponents.KILL_COUNT);
                int count = component != null ? component : 0;
                int newKillCount = count + 1;

                // 设置新的击杀计数
                mainHandItem.set(MiaComponents.KILL_COUNT, newKillCount);

                // 更新物品属性
                ((GrowSwordItem) mainHandItem.getItem()).updateAttributes(mainHandItem);
            }
        }
    }
}