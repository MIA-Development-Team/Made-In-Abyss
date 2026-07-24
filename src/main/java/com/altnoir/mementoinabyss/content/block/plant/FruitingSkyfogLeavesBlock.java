package com.altnoir.mementoinabyss.content.block.plant;

import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaItems;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class FruitingSkyfogLeavesBlock extends GreenParticleLeavesBlock {
    public static final MapCodec<FruitingSkyfogLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ExtraCodecs.floatRange(0.0F, 1.0F)
                            .fieldOf("leaf_particle_chance")
                            .forGetter(block -> block.leafParticleChance),
                    propertiesCodec()
            ).apply(instance, FruitingSkyfogLeavesBlock::new)
    );

    public FruitingSkyfogLeavesBlock(float leafParticleChance, BlockBehaviour.Properties properties) {
        super(leafParticleChance, properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        return harvest(state, level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        return harvest(state, level, pos, player);
    }

    private InteractionResult harvest(BlockState state, Level level, BlockPos pos, Player player) {
        BlockState harvestedState = MiaBlocks.SKYFOG_LEAVES.get().defaultBlockState()
                .setValue(DISTANCE, state.getValue(DISTANCE))
                .setValue(PERSISTENT, state.getValue(PERSISTENT))
                .setValue(WATERLOGGED, state.getValue(WATERLOGGED));

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.setBlock(pos, harvestedState, Block.UPDATE_ALL);
            popResource(serverLevel, pos,
                    new ItemStack(MiaItems.MISTFUZZ_PEACH.get(), 1 + serverLevel.getRandom().nextInt(2)));
            serverLevel.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS, 1.0F, 0.8F + serverLevel.getRandom().nextFloat() * 0.4F);
            serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, harvestedState));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public MapCodec<? extends FruitingSkyfogLeavesBlock> codec() {
        return CODEC;
    }
}
