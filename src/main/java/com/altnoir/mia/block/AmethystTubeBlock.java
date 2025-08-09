package com.altnoir.mia.block;

import com.altnoir.mia.block.abs.ACrystalTubeBlock;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.recipe.LampTubeRecipe;
import com.altnoir.mia.recipe.LampTubeRecipeInput;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AmethystTubeBlock extends ACrystalTubeBlock {
    public static final MapCodec<AmethystTubeBlock> CODEC = simpleCodec(AmethystTubeBlock::new);
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 8);

    @Override
    public MapCodec<AmethystTubeBlock> codec() {
        return CODEC;
    }

    public AmethystTubeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LEVEL, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(LEVEL, 1);
    }

    @Override
    protected void tick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        boolean flag = state.getValue(POWERED);
        if (flag && !level.hasNeighborSignal(pos)) {
            finalProcessing(level, pos, state);
            level.setBlock(pos, state.cycle(POWERED), 2);
            //MIA.LOGGER.info("Lamp tube is {}", MiaRecipes.LAMP_TUBE_TYPE.get().toString());
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide) {
            boolean flag = state.getValue(POWERED);
            if (flag != level.hasNeighborSignal(pos)) {
                if (flag) {
                    level.scheduleTick(pos, this, 2);
                } else {
                    playAmethyst(level, pos, state);
                    var newState = state;
                    newState = newState.cycle(POWERED);
                    newState = newState.setValue(LEVEL, 1);
                    level.setBlock(pos, newState, 2);
                }
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    protected boolean crystalProcessing(Level level, BlockPos pos, BlockState state, BlockPos targetPos, BlockState targetState, int i) {
        if (level.getBlockEntity(targetPos) instanceof Container container) {
            return processRecipe(level, pos, state, targetPos, container);
        } else return propagateSignal(level, pos, state, targetPos, targetState, i);
    }

    private boolean processRecipe(Level level, BlockPos pos, BlockState state, BlockPos targetPos, Container container) {
        for (int inputSlot = 0; inputSlot < container.getContainerSize(); inputSlot++) {
            var stack = container.getItem(inputSlot);
            var recipe = getCurrentRecipe(level, stack);

            if (recipe.isEmpty()) continue;
            int mul = state.getValue(LEVEL); // 倍率
            var result = recipe.get().value().result(); // 输出物品
            var recipeCount = Math.min(result.getCount() * mul, result.getMaxStackSize()); // 输出物品数量
            var stackCount = stack.getCount(); // 输入物品数量

            if (stackCount - recipeCount < 0) return false;

            for (int outputSlot = 0; outputSlot < container.getContainerSize(); outputSlot++) {
                var outputStack = container.getItem(outputSlot);
                int minMul = Math.min(mul, result.getMaxStackSize() / result.getCount());

                if (result.getMaxStackSize() >= recipeCount) {
                    if (ItemStack.isSameItem(outputStack, result) && outputStack.getMaxStackSize() >= outputStack.getCount() + mul) {
                        outputStack.grow(minMul);
                        recipeShrinkEffect(container, level, stack, targetPos, pos, state, recipeCount);
                        return true;
                    } else if (outputStack.isEmpty()) {
                        container.setItem(outputSlot, result.copyWithCount(minMul));
                        recipeShrinkEffect(container, level, stack, targetPos, pos, state, recipeCount);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean propagateSignal(Level level, BlockPos pos, BlockState state, BlockPos targetPos, BlockState targetState, int i) {
        if (!targetState.hasProperty(POWERED)) return false;

        playAmethyst(level, targetPos, state);
        signalParticles(1.0F, 0.5F, 1.0F, level, pos, targetPos, state);

        var newState = targetState;
        if (targetState.getBlock() instanceof AmethystTubeBlock) {
            newState = newState.setValue(LEVEL, Math.min(state.getValue(LEVEL) + 1, 8));
        }
        Direction direction = state.getValue(FACING);
        newState = newState.cycle(POWERED);
        level.setBlock(targetPos, newState, 2);
        level.updateNeighborsAt(pos.relative(direction, i - 1), state.getBlock());
        return true;
    }

    private Optional<RecipeHolder<LampTubeRecipe>> getCurrentRecipe(Level level, ItemStack stack) {
        return level.getRecipeManager()
                .getRecipeFor(MiaRecipes.LAMP_TUBE_TYPE.get(), new LampTubeRecipeInput(stack), level);
    }

    private void recipeShrinkEffect(Container container, Level level, ItemStack stack, BlockPos targetPos, BlockPos pos, BlockState state, int count) {
        stack.shrink(count);
        recipeEffect(container, level, pos, targetPos, state);
    }

    private void recipeEffect(Container container, Level level, BlockPos pos, BlockPos targetPos, BlockState state) {
        container.setChanged();

        playBlast(level, targetPos, state);
        signalParticles(1.0F, 0.5F, 1.0F, level, pos, targetPos, state);
        targetBlockParticles(level, targetPos);
    }


    private void targetBlockParticles(Level level, BlockPos targetPos) {
        double endX = targetPos.getX() + 0.5;
        double endY = targetPos.getY() + 0.5;
        double endZ = targetPos.getZ() + 0.5;
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, endX, endY, endZ, 10, 0.2, 0.2, 0.2, 0.03);
        }
    }

    private void playBlast(Level level, BlockPos pos, BlockState state) {
        level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS);
        level.gameEvent(GameEvent.EXPLODE, pos, GameEvent.Context.of(state));
    }
}
