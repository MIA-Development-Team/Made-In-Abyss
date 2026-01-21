package com.altnoir.mia.client.handler;

import com.altnoir.mia.client.MiaClientConfig;
import com.altnoir.mia.common.entity.projectile.HookEntity;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.network.server.PopHookPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

// TODO HookHandler需要重构
@OnlyIn(Dist.CLIENT)
public class HookHandler {
    public static void handler(LocalPlayer player, Level level, boolean isJump) {
        ItemStack stack;
        if (player.getMainHandItem().is(MiaItems.HOOK_ITEM)) {
            stack = player.getMainHandItem();
        } else if (player.getOffhandItem().is(MiaItems.HOOK_ITEM)) {
            stack = player.getOffhandItem();
        } else {
            return;
        }
        HookEntity hookEntity = null;
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.contains("hook") && level.getEntity(customData.copyTag().getInt("hook")) instanceof HookEntity entity) {
            hookEntity = entity;
        }
        if (hookEntity == null || hookEntity.getHookState() != HookEntity.HookState.HOOKED || player.isCrouching()) {
            return;
        }
        // TODO 玩家乘坐实体不知道应不应该被操作
        if (isJump) {
            Vec3 vec3 = player.getDeltaMovement();
            // at开不了，等后续有缘人
            // double y =  player.getJumpPower() * 1.25;
            // 二编，再次尝试了一下at，失败，把getJumpBoostPower加上去了
            double y = (player.getAttributeValue(Attributes.JUMP_STRENGTH) + player.getJumpBoostPower()) * 1.25;
            player.setDeltaMovement(vec3.x, y, vec3.z);
            PacketDistributor.sendToServer(new PopHookPayload(hookEntity.getId()));
            return;
        }
        Vec3 subtract = hookEntity.position().subtract(player.position());
        if (subtract.lengthSqr() < 1.0) {
            if (MiaClientConfig.autoHook) {
                PacketDistributor.sendToServer(new PopHookPayload(hookEntity.getId()));
                return;
            }
            Vec3 vec3 = player.getDeltaMovement().scale(0.05);
            player.setDeltaMovement(vec3.x, 0.0, vec3.z);
        } else {
            // 拉动速度
            double velocity = 0.2;
            Vec3 currentMotion = player.getDeltaMovement();
            // 玩家在地面给予速度补偿，但手动的很鬼畜
            if (player.onGround()) velocity *= 2.0;

            Vec3 vec3 = subtract.normalize().scale(velocity);

            Vec3 gravity = new Vec3(0, player.getGravity() * 0.975, 0);
            Vec3 totalForce = !player.onGround() ? vec3.add(gravity) : vec3;

            player.setDeltaMovement(currentMotion.add(totalForce).scale(0.96));
        }
    }
}
