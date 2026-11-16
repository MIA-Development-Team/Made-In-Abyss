package com.altnoir.mementoinabyss.content.returnchest;

import com.altnoir.mementoinabyss.client.tooltip.TooltipPalette;
import com.altnoir.mementoinabyss.content.returnchest.client.ReturnChestAmbience;
import com.altnoir.mementoinabyss.init.MiaBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class ReturnChestBlock extends BaseEntityBlock {
    private static final MapCodec<ReturnChestBlock> CODEC = simpleCodec(ReturnChestBlock::new);
    public static final EnumProperty<ReturnChestPart> PART =
            EnumProperty.create("part", ReturnChestPart.class);
    public static final int UNBOUND = 17;
    public static final IntegerProperty TINT = IntegerProperty.create("tint", 0, UNBOUND);

    public ReturnChestBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(PART, ReturnChestPart.WN_DOWN)
                        .setValue(TINT, UNBOUND));
    }

    @Override
    protected MapCodec<ReturnChestBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, TINT);
    }

    private static @Nullable DyeColor dyeColor(ItemStack stack) {
        return stack.getItem() instanceof DyeItem ? stack.get(DataComponents.DYE) : null;
    }

    public static int tintFor(DyeColor color) {
        return color == DyeColor.LIME ? 0 : color.getId() + 1;
    }

    public static int tintForConnection(int connection) {
        return connection == 0 ? UNBOUND : tintFor(DyeColor.byId(connection - 1));
    }

    @Override
    public ReturnChestBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReturnChestBlockEntity(MiaBlockEntityTypes.RETURN_CHEST.get(), pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!ReturnChests.isCore(state)) return null;
        if (level.isClientSide()) {
            return createTickerHelper(
                    type, MiaBlockEntityTypes.RETURN_CHEST.get(), ReturnChestAmbience::tick);
        }
        return null;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ReturnChestLayout.shape(ReturnChests.part(state));
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 15;
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
        DyeColor color = dyeColor(stack);
        if (color == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level instanceof ServerLevel server
                && player.mayBuild()
                && server.mayInteract(player, pos)) {
            ReturnChestBlockEntity core = resolveController(server, pos, player);
            if (core != null && server.mayInteract(player, core.getBlockPos())) {
                int previous = core.connection();
                ReturnChests.bind(server, core, color);
                if (core.connection() != previous) {
                    stack.consume(1, player);
                }
                ReturnChests.message(player, "connected", BalloonItem.colorName(color));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server) {
            ReturnChestBlockEntity core = resolveController(server, pos, player);
            if (core == null) return InteractionResult.SUCCESS;
            int connection = core.connection();
            if (connection == 0) {
                ReturnChests.message(player, "disconnected");
            } else {
                DyeColor color = ReturnChests.color(connection);
                ReturnStorage.Channel channel = ReturnStorage.get(server).channel(color);
                player.openMenu(
                        new SimpleMenuProvider(
                                (id, inventory, viewer) ->
                                        new ReturnChestMenu(
                                                id,
                                                inventory,
                                                channel,
                                                core,
                                                pos.immutable(),
                                                connection),
                                Component.translatable("container.mementoinabyss.return_chest")
                                        .withStyle(
                                                TooltipPalette.colored(
                                                        ReturnChestColors.tint(color)))));
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static ReturnChestBlockEntity resolveController(
            ServerLevel level, BlockPos pos, Player player) {
        if (!ReturnDimensions.canUseChest(level)) {
            ReturnChests.message(player, "chest_dimension");
            return null;
        }
        if (level.getBlockEntity(pos) instanceof ReturnChestBlockEntity part) {
            ReturnChestBlockEntity core = part.controller(level);
            if (core != null) return core;
        }
        ReturnChests.message(player, "incomplete");
        return null;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY)
                        instanceof ReturnChestBlockEntity part
                && part.mayDrop()) {
            return super.getDrops(state, params);
        }
        return List.of();
    }

    @Override
    public boolean onDestroyedByPlayer(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            ItemStack tool,
            boolean willHarvest,
            FluidState fluid) {
        if (level instanceof ServerLevel server) {
            return server.setBlock(
                    pos, fluid.createLegacyBlock(), ReturnChestCleanup.removalUpdates(server, pos));
        }
        return super.onDestroyedByPlayer(state, level, pos, player, tool, willHarvest, fluid);
    }

    @Override
    public void onBlockExploded(
            BlockState state, ServerLevel level, BlockPos pos, Explosion explosion) {
        level.setBlock(
                pos, Blocks.AIR.defaultBlockState(), ReturnChestCleanup.removalUpdates(level, pos));
        wasExploded(level, pos, explosion);
    }
}
