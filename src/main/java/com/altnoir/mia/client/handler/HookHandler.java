package com.altnoir.mia.client.handler;

import com.altnoir.mia.entity.projectile.HookEntity;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.network.server.DiscardHookPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class HookHandler {
    public static void handler(LocalPlayer player, Level level, boolean isJump) {
        HookEntity hookEntity = null;
        ItemStack stack;
        if (player.getMainHandItem().is(MiaItems.HOOK_ITEM)) {
            stack = player.getMainHandItem();
        } else if (player.getOffhandItem().is(MiaItems.HOOK_ITEM)) {
            stack = player.getOffhandItem();
        } else {
            return;
        }
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.contains("hook") && level.getEntity(customData.copyTag().getInt("hook")) instanceof HookEntity entity) {
            hookEntity = entity;
        }
        if (player.isCrouching() || hookEntity == null || hookEntity.getHookState() != HookEntity.HookState.HOOKED) {
            return;
        }
        // TODO 玩家乘坐实体应不应该被操作
        // TODO 摔落伤害没有减免
        if (isJump) {
            Vec3 vec3 = player.getDeltaMovement();
            // at开不了，等后续有缘人
            // double motionY =  player.getJumpPower() * 1.25;
            double motionY = player.getAttributeValue(Attributes.JUMP_STRENGTH) * 1.25;
            player.setDeltaMovement(vec3.x, motionY, vec3.z);
            if (player.isSprinting()) {
                float f = player.getYRot() * Mth.DEG_TO_RAD;
                player.setDeltaMovement(player.getDeltaMovement().add(-Mth.sin(f) * 0.2, 0.0, Mth.cos(f) * 0.2));
            }
            PacketDistributor.sendToServer(new DiscardHookPayload(hookEntity.getId()));
            return;
        }
        Vec3 subtract = hookEntity.position().subtract(player.position());
        if (subtract.lengthSqr() < 1.0) {
            Vec3 motion = player.getDeltaMovement().scale(0.05);
            player.setDeltaMovement(motion.x, 0.0, motion.z);
        } else {
            Vec3 motion = subtract.normalize().scale(/* 拉动速度 */0.2);
            player.setDeltaMovement(player.getDeltaMovement().scale(0.96).add(motion));
        }
    }
}
