package com.altnoir.mementoinabyss.content.item;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.block.entity.RopeConnectorBlockEntity;
import com.altnoir.mementoinabyss.impl.rope.RopeEndpointSelection;
import com.altnoir.mementoinabyss.init.MiaDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class RopeItem extends Item {
    private static final int USE_DURATION = 72_000;
    private static final double THROW_SPEED = 14.0;

    public RopeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        RopeEndpointSelection selection = stack.get(MiaDataComponents.ROPE_ENDPOINT.get());
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(clickedPos) instanceof RopeConnectorBlockEntity clickedConnector)) {
            if (selection == null) {
                return InteractionResult.PASS;
            }
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            return placeFreeEnd(level, player, stack, selection, context.getClickLocation());
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (selection == null) {
            if (clickedConnector.isConnected()) {
                clickedConnector.disconnect();
            }
            int initialLength = initialLength(player, stack, clickedPos);
            stack.set(MiaDataComponents.ROPE_ENDPOINT.get(), new RopeEndpointSelection(
                    level.dimension().identifier(),
                    clickedPos,
                    initialLength
            ));
            reserveInitialLength(player, stack, initialLength);
            notify(player, "rope.endpoint_picked");
            level.playSound(null, clickedPos, SoundEvents.LEAD_UNTIED,
                    SoundSource.BLOCKS, 0.7F, 1.0F);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (selection.connector().equals(clickedPos)
                && selection.dimension().equals(level.dimension().identifier())) {
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!selection.dimension().equals(level.dimension().identifier())) {
            stack.set(MiaDataComponents.ROPE_ENDPOINT.get(), new RopeEndpointSelection(
                    level.dimension().identifier(),
                    clickedPos,
                    initialLength(player, stack, clickedPos)
            ));
            notify(player, "rope.wrong_dimension");
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!(level.getBlockEntity(selection.connector()) instanceof RopeConnectorBlockEntity)) {
            stack.set(MiaDataComponents.ROPE_ENDPOINT.get(), new RopeEndpointSelection(
                    level.dimension().identifier(),
                    clickedPos,
                    initialLength(player, stack, clickedPos)
            ));
            notify(player, "rope.missing_endpoint");
            return InteractionResult.SUCCESS_SERVER;
        }

        double distance = Math.sqrt(selection.connector().distSqr(clickedPos));
        double maximumDistance = MementoInAbyss.CONFIGS.gamePlaySection.hookMaxDistance.get();
        if (distance > maximumDistance) {
            notify(player, "rope.too_far", maximumDistance);
            return InteractionResult.FAIL;
        }
        int targetLength = Math.max(1, (int) Math.ceil(distance));
        int additionalItems = Math.max(0, targetLength - selection.length());
        if (!player.hasInfiniteMaterials() && additionalItems > stack.getCount() - 1) {
            notify(player, "rope.not_enough_length", targetLength);
            return InteractionResult.FAIL;
        }

        if (!RopeConnectorBlockEntity.connect(level, selection.connector(), clickedPos, targetLength)) {
            return InteractionResult.FAIL;
        }

        resizeReservedLength(player, stack, selection.length(), targetLength);
        stack.remove(MiaDataComponents.ROPE_ENDPOINT.get());
        stack.consume(1, player);
        notify(player, "rope.connected");
        level.playSound(null, clickedPos, SoundEvents.LEAD_TIED,
                SoundSource.BLOCKS, 0.8F, 1.0F);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        RopeEndpointSelection selection = stack.get(MiaDataComponents.ROPE_ENDPOINT.get());
        if (selection == null || !selection.dimension().equals(level.dimension().identifier())) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                stack.remove(MiaDataComponents.ROPE_ENDPOINT.get());
                refundReservedLength(player, stack, selection);
                notify(player, "rope.endpoint_released");
                level.playSound(null, player.blockPosition(), SoundEvents.LEAD_UNTIED,
                        SoundSource.PLAYERS, 0.7F, 0.9F);
            }
            return level.isClientSide()
                    ? InteractionResult.SUCCESS
                    : InteractionResult.SUCCESS_SERVER;
        }
        if (!(level.getBlockEntity(selection.connector()) instanceof RopeConnectorBlockEntity)) {
            if (!level.isClientSide()) {
                stack.remove(MiaDataComponents.ROPE_ENDPOINT.get());
                refundReservedLength(player, stack, selection);
                notify(player, "rope.missing_endpoint");
            }
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player) || level.isClientSide()) {
            return false;
        }
        RopeEndpointSelection selection = stack.get(MiaDataComponents.ROPE_ENDPOINT.get());
        if (selection == null || !selection.dimension().equals(level.dimension().identifier())) {
            return false;
        }
        return placeFreeEnd(level, player, stack, selection, heldPosition(player)).consumesAction();
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.get(MiaDataComponents.ROPE_ENDPOINT.get()) != null || super.isFoil(stack);
    }

    public static boolean adjustSelectedLength(Player player, ItemStack stack, int direction) {
        RopeEndpointSelection selection = stack.get(MiaDataComponents.ROPE_ENDPOINT.get());
        if (!(stack.getItem() instanceof RopeItem) || selection == null || direction == 0) {
            return false;
        }
        int configuredMaximum = Math.max(1,
                (int) Math.floor(MementoInAbyss.CONFIGS.gamePlaySection.hookMaxDistance.get()));
        int adjusted = Math.clamp(selection.length() + Integer.signum(direction), 1, configuredMaximum);
        if (!player.hasInfiniteMaterials() && adjusted > selection.length() && stack.getCount() <= 1) {
            return false;
        }
        if (adjusted == selection.length()) {
            return false;
        }
        if (!player.hasInfiniteMaterials()) {
            if (adjusted > selection.length()) {
                stack.shrink(1);
            } else {
                stack.grow(1);
            }
        }
        stack.set(MiaDataComponents.ROPE_ENDPOINT.get(), selection.withLength(adjusted));
        notify(player, "rope.length", adjusted, configuredMaximum);
        return true;
    }

    private static InteractionResult placeFreeEnd(
            Level level,
            Player player,
            ItemStack stack,
            RopeEndpointSelection selection,
            net.minecraft.world.phys.Vec3 end
    ) {
        if (!(level.getBlockEntity(selection.connector()) instanceof RopeConnectorBlockEntity)) {
            stack.remove(MiaDataComponents.ROPE_ENDPOINT.get());
            refundReservedLength(player, stack, selection);
            notify(player, "rope.missing_endpoint");
            return InteractionResult.FAIL;
        }
        double distance = net.minecraft.world.phys.Vec3.atCenterOf(selection.connector()).distanceTo(end);
        double maximumDistance = MementoInAbyss.CONFIGS.gamePlaySection.hookMaxDistance.get();
        if (distance > selection.length() + 1.0E-6
                || selection.length() > maximumDistance) {
            notify(player, "rope.not_enough_length", selection.length());
            return InteractionResult.FAIL;
        }
        net.minecraft.world.phys.Vec3 throwVelocity = player.getLookAngle()
                .scale(THROW_SPEED)
                .add(player.getDeltaMovement().scale(20.0));
        if (!RopeConnectorBlockEntity.connectFree(
                level, selection.connector(), end, selection.length(), throwVelocity)) {
            return InteractionResult.FAIL;
        }
        stack.remove(MiaDataComponents.ROPE_ENDPOINT.get());
        stack.consume(1, player);
        notify(player, "rope.free_end_placed");
        level.playSound(null, BlockPos.containing(end), SoundEvents.LEAD_UNTIED,
                SoundSource.BLOCKS, 0.7F, 1.0F);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static int initialLength(Player player, ItemStack stack, BlockPos connector) {
        int needed = (int) Math.ceil(net.minecraft.world.phys.Vec3.atCenterOf(connector)
                .distanceTo(heldPosition(player)));
        int maximum = Math.max(1, player.hasInfiniteMaterials()
                ? (int) Math.floor(MementoInAbyss.CONFIGS.gamePlaySection.hookMaxDistance.get())
                : Math.min(stack.getCount(),
                        (int) Math.floor(MementoInAbyss.CONFIGS.gamePlaySection.hookMaxDistance.get())));
        return Math.clamp(needed, 1, maximum);
    }

    private static void reserveInitialLength(Player player, ItemStack stack, int length) {
        if (!player.hasInfiniteMaterials() && length > 1) {
            stack.shrink(length - 1);
        }
    }

    private static void resizeReservedLength(
            Player player,
            ItemStack stack,
            int oldLength,
            int newLength
    ) {
        if (player.hasInfiniteMaterials()) {
            return;
        }
        if (newLength > oldLength) {
            stack.shrink(newLength - oldLength);
        } else if (newLength < oldLength) {
            stack.grow(oldLength - newLength);
        }
    }

    private static void refundReservedLength(
            Player player,
            ItemStack stack,
            RopeEndpointSelection selection
    ) {
        if (!player.hasInfiniteMaterials() && selection.length() > 1) {
            stack.grow(selection.length() - 1);
        }
    }

    private static net.minecraft.world.phys.Vec3 heldPosition(Player player) {
        return player.getEyePosition()
                .add(player.getLookAngle().scale(0.55))
                .add(0.0, -0.35, 0.0);
    }

    private static void notify(Player player, String key, Object... arguments) {
        if (player != null) {
            player.sendOverlayMessage(Component.translatable("mementoinabyss." + key, arguments));
        }
    }
}
