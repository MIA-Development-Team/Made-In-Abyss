package com.altnoir.mementoinabyss.content.abyss.plant;

import com.altnoir.mementoinabyss.init.MiaItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class DuskveilIchorBlock extends RotatedPillarBlock {
    public static final MapCodec<DuskveilIchorBlock> CODEC = simpleCodec(DuskveilIchorBlock::new);
    public static final BooleanProperty READY = BlockStateProperties.LIT;
    private static final int REGROWTH_TICKS = 1200;

    public DuskveilIchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any().setValue(AXIS, Direction.Axis.Y).setValue(READY, true));
    }

    @Override
    public MapCodec<DuskveilIchorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(READY);
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
        if (!stack.is(Items.GLASS_BOTTLE) || !state.getValue(READY)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            player.setItemInHand(
                    hand,
                    ItemUtils.createFilledResult(
                            stack, player, new ItemStack(MiaItems.DUSKVEIL_ICHOR_BOTTLE.get())));
            server.setBlock(pos, state.setValue(READY, false), Block.UPDATE_ALL);
            server.scheduleTick(pos, this, REGROWTH_TICKS);
            server.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(READY))
            level.setBlock(pos, state.setValue(READY, true), Block.UPDATE_ALL);
    }
}
