package com.altnoir.mementoinabyss.content.lamptube;

import com.altnoir.mementoinabyss.content.pedestal.PedestalBlockEntity;
import com.altnoir.mementoinabyss.init.MiaBlockEntityTypes;
import com.altnoir.mementoinabyss.init.MiaRecipes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public class AmethystTubeBlock extends CrystalTubeBlock implements EntityBlock {
    public static final MapCodec<AmethystTubeBlock> CODEC = simpleCodec(AmethystTubeBlock::new);
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 8);

    public AmethystTubeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<AmethystTubeBlock> codec() {
        return CODEC;
    }

    @Override
    protected IntegerProperty levelProperty() {
        return LEVEL;
    }

    @Override
    protected int maxLevel() {
        return 8;
    }

    @Override
    protected int beamColor() {
        return 0xFF80FF;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return MiaBlockEntityTypes.AMETHYST_LAMPTUBE.create(pos, state);
    }

    @Override
    protected boolean affect(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockPos targetPos,
            BlockState targetState,
            int distance) {
        if (level.getBlockEntity(targetPos) instanceof PedestalBlockEntity pedestal) {
            return processRecipe(pedestal, level, pos, state, targetPos);
        }
        if (!targetState.isSolidRender()) {
            return relay(level, pos, state, targetPos, targetState, distance);
        }
        return false;
    }

    private boolean processRecipe(
            PedestalBlockEntity pedestal,
            Level level,
            BlockPos pos,
            BlockState state,
            BlockPos targetPos) {
        if (!(level.getBlockEntity(pos) instanceof AmethystTubeBlockEntity tube)) {
            return false;
        }
        ItemStack inputStack = pedestal.extractInput(Item.ABSOLUTE_MAX_STACK_SIZE, true);
        if (inputStack.isEmpty()) {
            return false;
        }
        var server = level.getServer();
        if (server == null) {
            return false;
        }
        var storedHeat = tube.storedHeat();
        var recipe =
                server.getRecipeManager()
                        .getRecipeFor(
                                MiaRecipes.LAMP_TUBE_TYPE.get(),
                                new LampTubeRecipeInput(inputStack, storedHeat),
                                level)
                        .map(RecipeHolder::value);
        if (recipe.isEmpty()) {
            return false;
        }

        LampTubeRecipe matched = recipe.get();
        for (int multiplier = state.getValue(LEVEL); multiplier >= 1; multiplier--) {
            int inputCount = matched.itemCount(multiplier);
            int heatCount = matched.heatAmount(multiplier);
            if (inputStack.getCount() < inputCount || storedHeat.amount() < heatCount) {
                continue;
            }
            ItemStack output = matched.resultStack(multiplier);
            if (!pedestal.insertOutput(output, true)) {
                continue;
            }
            if (heatCount > 0) {
                var heat = matched.heatInput();
                try (Transaction transaction = Transaction.openRoot()) {
                    int extracted =
                            tube.heatHandler.extract(heat.heat(), heatCount, transaction);
                    if (extracted != heatCount) {
                        continue;
                    }
                    pedestal.extractInput(inputCount, false);
                    pedestal.insertOutput(output, false);
                    transaction.commit();
                }
            } else {
                pedestal.extractInput(inputCount, false);
                pedestal.insertOutput(output, false);
            }
            level.playSound(null, targetPos, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS);
            level.gameEvent(GameEvent.EXPLODE, targetPos, GameEvent.Context.of(state));
            beam(level, pos, targetPos, state);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        targetPos.getX() + 0.5,
                        targetPos.getY() + 0.5,
                        targetPos.getZ() + 0.5,
                        5,
                        0.1,
                        0.2,
                        0.1,
                        0.03);
            }
            return true;
        }
        return false;
    }
}
