package com.altnoir.mementoinabyss.content.returnchest;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class ReturnChestBlock extends EnderChestBlock {
    private static final MapCodec<EnderChestBlock> CODEC = simpleCodec(ReturnChestBlock::new);
    static final IntegerProperty CONNECTION = IntegerProperty.create("connection", 0, 16);

    public ReturnChestBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CONNECTION, 0));
    }

    @Override
    public MapCodec<EnderChestBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECTION);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (!(stack.getItem() instanceof BalloonItem)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level instanceof ServerLevel server) {
            if (!ReturnDimensions.canUseChest(server)) {
                BalloonItem.message(player, "chest_dimension");
            } else if (player.mayBuild()
                    && server.mayInteract(player, pos)
                    && server.getBlockEntity(pos) instanceof EnderChestBlockEntity chest) {
                DyeColor color = BalloonItem.color(stack);
                int connection = color.getId() + 1;
                if (state.getValue(CONNECTION) != connection
                        && server.setBlock(
                                pos,
                                state.setValue(CONNECTION, connection),
                                Block.UPDATE_CLIENTS)) {
                    for (ServerPlayer viewer : server.players()) {
                        if (viewer.containerMenu instanceof ReturnChestMenu menu
                                && menu.isFor(chest)) {
                            viewer.closeContainer();
                        }
                    }
                }
                BalloonItem.message(player, "connected", BalloonItem.colorName(color));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server) {
            int connection = state.getValue(CONNECTION);
            if (!ReturnDimensions.canUseChest(server)) {
                BalloonItem.message(player, "chest_dimension");
            } else if (connection == 0) {
                BalloonItem.message(player, "disconnected");
            } else if (server.getBlockEntity(pos) instanceof EnderChestBlockEntity chest) {
                DyeColor color = DyeColor.byId(connection - 1);
                ReturnStorage.Channel channel = ReturnStorage.get(server).channel(color);
                player.openMenu(
                        new SimpleMenuProvider(
                                (id, inventory, viewer) ->
                                        new ReturnChestMenu(
                                                id, inventory, channel, chest, connection),
                                Component.translatable(
                                        "container.mementoinabyss.return_chest",
                                        BalloonItem.colorName(color))));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
