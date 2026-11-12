package com.altnoir.mementoinabyss.content.returnchest;

import com.altnoir.mementoinabyss.content.parcel.ParcelBlockEntity;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jspecify.annotations.Nullable;

public final class BalloonItem extends Item {
    private final @Nullable DyeColor fixedColor;

    public BalloonItem(Properties properties) {
        this(properties, null);
    }

    public BalloonItem(Properties properties, @Nullable DyeColor fixedColor) {
        super(properties);
        this.fixedColor = fixedColor;
    }

    public static DyeColor color(ItemStack stack) {
        if (stack.getItem() instanceof BalloonItem balloon && balloon.fixedColor != null) {
            return balloon.fixedColor;
        }
        // Existing balloon stacks can still carry a resonance in the old dye component.
        DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
        if (dyed != null) {
            for (DyeColor color : DyeColor.VALUES) {
                if (rgb(color) == dyed.rgb()) return color;
            }
        }
        return DyeColor.WHITE;
    }

    public static int rgb(DyeColor color) {
        return color.getTextureDiffuseColor() & 0xFFFFFF;
    }

    public static Component colorName(DyeColor color) {
        return Component.translatable("color.minecraft." + color.getName());
    }

    static void message(Player player, String key, Object... arguments) {
        player.sendOverlayMessage(
                Component.translatable("message.mementoinabyss.return_chest." + key, arguments));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack balloon, UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);
        if (player == null || !state.is(MiaBlocks.PARCEL.get())) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (!ReturnDimensions.canLaunch(level)) {
            message(player, "launch_dimension");
            return InteractionResult.FAIL;
        }
        if (!player.mayBuild() || !level.mayInteract(player, pos)) return InteractionResult.FAIL;
        for (int dy = 1; dy <= BalloonCargo.PARCEL_FLIGHT_HEIGHT; dy++) {
            BlockPos above = pos.above(dy);
            if (!level.isInWorldBounds(above) || !level.getBlockState(above).isAir()) {
                message(player, "blocked");
                return InteractionResult.FAIL;
            }
        }
        if (NeoForge.EVENT_BUS.post(new BreakBlockEvent(level, pos, state, player)).isCanceled()
                || level.getBlockState(pos) != state
                || !(level.getBlockEntity(pos) instanceof ParcelBlockEntity parcel)) {
            return InteractionResult.FAIL;
        }

        DyeColor color = color(balloon);
        ReturnStorage storage = ReturnStorage.get(level);
        ReturnStorage.Channel channel = storage.channel(color);
        if (channel.inTransit()) {
            message(player, "busy", colorName(color));
            return InteractionResult.FAIL;
        }

        List<ItemStack> items = new ArrayList<>();
        for (int slot = 0; slot < parcel.getContainerSize(); slot++) {
            ItemStack item = parcel.getItem(slot);
            if (!item.isEmpty()) items.add(item.copy());
        }
        if (items.isEmpty()) {
            message(player, "empty");
            return InteractionResult.FAIL;
        }
        if (channel.pack(items) == null) {
            message(player, "full", colorName(color));
            return InteractionResult.FAIL;
        }

        BalloonCargo visual = BalloonCargo.PARCEL_BALLOON.get().create(level, EntitySpawnReason.TRIGGERED);
        if (visual == null) return InteractionResult.FAIL;
        visual.prepare(pos, state.getValue(BlockStateProperties.HORIZONTAL_FACING), color);

        // Transfer the inventory ourselves, bypassing the parcel's ordinary removal drops.
        if (!level.setBlock(
                pos,
                state.getFluidState().createLegacyBlock(),
                Block.UPDATE_ALL | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS)) {
            return InteractionResult.FAIL;
        }
        level.updateNeighbourForOutputSignal(pos, state.getBlock());

        long arrival = level.getServer().overworld().getGameTime() + visual.flightDuration();
        storage.depart(
                channel, new ReturnStorage.Flight(visual.getUUID(), arrival, List.copyOf(items)));
        level.addFreshEntity(visual);
        balloon.consume(1, player);
        message(player, "launched", colorName(color));
        return InteractionResult.SUCCESS;
    }
}
