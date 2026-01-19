package com.altnoir.mia.item;

import com.altnoir.mia.entity.projectile.HookEntity;
import com.altnoir.mia.network.client.SyncHookPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class HookItem extends Item {
    public HookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        HookEntity hookEntity = new HookEntity(player, usedHand);
        hookEntity.shootFromRotation(
                player,
                player.getXRot(),
                player.getYRot(),
                0,
                2,
                1
        );
        level.addFreshEntity(hookEntity);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncHookPayload(hookEntity.getId()));
        }
        return super.use(level, player, usedHand);
    }
}
