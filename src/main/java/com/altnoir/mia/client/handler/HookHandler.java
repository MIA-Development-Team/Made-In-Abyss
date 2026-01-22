package com.altnoir.mia.client.handler;

import com.altnoir.mia.MiaConfig;
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

/**
 * 钩爪客户端处理
 */
@OnlyIn(Dist.CLIENT)
public class HookHandler {
    /**
     * 处理钩爪
     */
    public static void handler(LocalPlayer player, Level level, boolean isJump) {
        ItemStack stack = findHookItem(player);
        if (stack == null) return;
        HookEntity hook = getHookEntity(stack, level);
        if (hook == null || !hook.isHooked() || player.isCrouching()) return;
        if (isJump) {
            handleJump(player, hook);
        } else {
            handlePull(player, hook);
        }
    }

    /**
     * 处理跳跃
     */
    private static void handleJump(LocalPlayer player, HookEntity hook) {
        // at开不了，等后续有缘人
        // double y = player.getJumpPower() * MiaConfig.hookJumpBoost;
        double y = (player.getAttributeValue(Attributes.JUMP_STRENGTH) + player.getJumpBoostPower()) * MiaConfig.hookJumpBoost;
        Vec3 vec3 = player.getDeltaMovement();
        player.setDeltaMovement(vec3.x, y, vec3.z);
        PacketDistributor.sendToServer(new PopHookPayload(hook.getId()));
    }

    /**
     * 处理拉取
     */
    private static void handlePull(LocalPlayer player, HookEntity hook) {
        if (player.distanceToSqr(hook) < MiaConfig.hookAutoRetractDistance * MiaConfig.hookAutoRetractDistance) {
            // 自动回收
            if (MiaClientConfig.autoHook) {
                PacketDistributor.sendToServer(new PopHookPayload(hook.getId()));
                return;
            }
            Vec3 vec3 = player.getDeltaMovement().scale(0.01);
            player.setDeltaMovement(vec3.x, 0.0, vec3.z);
            return;
        }
        double velocity = MiaConfig.hookPullVelocity;
        if (player.onGround()) {
            // 地面2倍速度
            velocity *= 2.0;
        }
        // 应用拉取
        Vec3 vec3 = hook.position().subtract(player.position()).normalize().scale(velocity);
        player.setDeltaMovement(player.getDeltaMovement().add(vec3.x, vec3.y + player.getGravity() * 0.95, vec3.z).scale(0.95));
    }

    private static ItemStack findHookItem(LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        if (mainHand.is(MiaItems.HOOK_ITEM)) {
            return mainHand;
        } else if (offhand.is(MiaItems.HOOK_ITEM)) {
            return offhand;
        }
        return null;
    }

    private static HookEntity getHookEntity(ItemStack stack, Level level) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.contains("hook")) {
            int id = data.copyTag().getInt("hook");
            if (level.getEntity(id) instanceof HookEntity hook) {
                return hook;
            }
        }
        return null;
    }
}
