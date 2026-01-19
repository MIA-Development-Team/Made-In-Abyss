package com.altnoir.mia.item;

import com.altnoir.mia.entity.projectile.HookEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class HookItem extends Item {
    public HookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        boolean flag = customData.contains("hook");
        if (flag && level.getEntity(customData.copyTag().getInt("hook")) instanceof HookEntity entity) {
            entity.setHookState(HookEntity.HookState.POP);
            return super.use(level, player, usedHand);
        }
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
        CompoundTag tag = new CompoundTag();
        tag.putInt("hook", hookEntity.getId());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return super.use(level, player, usedHand);
    }
}
