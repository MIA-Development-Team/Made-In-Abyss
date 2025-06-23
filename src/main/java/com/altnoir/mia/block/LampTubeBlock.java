package com.altnoir.mia.block;

import com.altnoir.mia.block.abs.AbstractTubeBlock;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.recipe.LampTubeRecipe;
import com.altnoir.mia.recipe.LampTubeRecipeInput;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Optional;

public class LampTubeBlock extends AbstractTubeBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<LampTubeBlock> CODEC = simpleCodec(LampTubeBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 64);
    @Override
    public MapCodec<LampTubeBlock> codec() {
        return CODEC;
    }
    public LampTubeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, Boolean.valueOf(false))
                .setValue(POWERED, Boolean.valueOf(false))
                .setValue(LEVEL, 1));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LEVEL, POWERED, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var direction = context.getNearestLookingDirection().getOpposite();
        var fluidState = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        var hasNeighbor = context.getLevel().hasNeighborSignal(context.getClickedPos());
        var defaultState = this.defaultBlockState()
                .setValue(LEVEL, 1)
                .setValue(POWERED, Boolean.valueOf(hasNeighbor))
                .setValue(WATERLOGGED, Boolean.valueOf(fluidState));

        return Screen.hasShiftDown() ? defaultState.setValue(FACING, direction.getOpposite()) : defaultState.setValue(FACING, direction);
    }
    @Override
    protected @NotNull BlockState updateShape(
            BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos
    ) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void tick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        boolean flag = state.getValue(POWERED);
        if (flag && !level.hasNeighborSignal(pos)) {
            checkAndConvert(level, pos, state);
            level.setBlock(pos, state.cycle(POWERED), 2);
            //MIA.LOGGER.info("Lamp tube is {}", MiaRecipes.LAMP_TUBE_TYPE.get().toString());
        }
    }
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean flag = state.getValue(POWERED);
            if (flag != level.hasNeighborSignal(pos)) {
                if (flag) {
                    level.scheduleTick(pos, this, 4);
                } else {
                    playAmethyst(level, pos, state);
                    var newState = state;
                    newState = newState.cycle(POWERED);
                    newState = newState.setValue(LEVEL, 1);
                    level.setBlock(pos, newState, 2);
                }
            }
        }
    }
    private void checkAndConvert(Level level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(FACING);
        for (int i = 1; i <= 12; i++) {
            var targetPos = pos.relative(direction, i);
            var targetState = level.getBlockState(targetPos);

            if (level.getBlockEntity(targetPos) instanceof Container container) {
                for (int inputSlot = 0; inputSlot < container.getContainerSize(); inputSlot++) {
                    var stack = container.getItem(inputSlot);
                    var recipe = getCurrentRecipe(level, stack);

                    if (recipe.isEmpty()) continue;
                    int mul = state.getValue(LEVEL);
                    var result = recipe.get().value().result();
                    var recipeCount = Math.min(result.getCount() * mul, result.getMaxStackSize());
                    var stackCount = stack.getCount();

                    if (stackCount - recipeCount < 0) return;
                    for (int outputSlot = 0; outputSlot < container.getContainerSize(); outputSlot++) {
                        var outputStack = container.getItem(outputSlot);
                        int minMul = Math.min(mul, result.getMaxStackSize() / result.getCount());

                        if (result.getMaxStackSize() >= recipeCount) {
                            if (ItemStack.isSameItem(outputStack, result) && outputStack.getMaxStackSize() >= outputStack.getCount() + mul) {
                                outputStack.grow(minMul);
                                recipeShrinkEffect(container, level, stack, targetPos, pos, state, recipeCount);
                                break;
                            } else if (outputStack.isEmpty()) {
                                container.setItem(outputSlot, result.copyWithCount(minMul));
                                recipeShrinkEffect(container, level, stack, targetPos, pos, state, recipeCount);
                                break;
                            }
                        /*else if (recipeCount == stackCount) {
                            container.setItem(inputSlot, result.copyWithCount(minMul));
                            recipeEffect(container, level, pos, targetPos, state);
                            break;
                        }*/
                        }
                    }

                    return;
                }
                return;
            } else if (targetState.hasProperty(POWERED)) {
                playAmethyst(level, targetPos, state);
                spawnParticles1(level, pos, targetPos, state);

                var newState = targetState;
                if (targetState.getBlock() instanceof LampTubeBlock) {
                    newState = newState.setValue(LEVEL, Math.min(state.getValue(LEVEL) + 1, 64));
                }
                newState = newState.cycle(POWERED);
                level.setBlock(targetPos, newState, 2);
                level.updateNeighborsAt(pos.relative(direction, i - 1), state.getBlock());
                return;
            }
        }
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
        // 效果
        playBlast(level, targetPos, state);
        spawnParticles1(level, pos, targetPos, state);
        spawnParticles2(level, targetPos);
    }
    private void spawnParticles1(Level level, BlockPos pos, BlockPos targetPos, BlockState state) {
        double startX = pos.getX() + 0.5, startY = pos.getY() + 0.5, startZ = pos.getZ() + 0.5;
        double endX = targetPos.getX() + 0.5, endY = targetPos.getY() + 0.5, endZ = targetPos.getZ() + 0.5;

        double midX = (startX + endX) / 2, midY = (startY + endY) / 2, midZ = (startZ + endZ) / 2;

        double dx = endX - startX, dy = endY - startY, dz = endZ - startZ;

        int particleCount = (int) Math.abs(dx + dy + dz) / 2 * 50;

        Direction facing = state.getValue(FACING);

        double dxFactor = (facing == Direction.WEST || facing == Direction.EAST) ? 0.21 : 0.01;
        double dyFactor = (facing == Direction.UP || facing == Direction.DOWN) ? 0.16 : 0.01;
        double dzFactor = (facing == Direction.NORTH || facing == Direction.SOUTH) ? 0.21 : 0.01;

        if (level instanceof ServerLevel serverLevel) {
            float r = 1.0F, g = 0.5F, b = 1.F;

            serverLevel.sendParticles(
                    new DustParticleOptions(new Vector3f(r, g, b), 0.5f),
                    midX, midY, midZ,
                    particleCount,
                    dx * dxFactor,
                    dy * dyFactor,
                    dz * dzFactor,
                    0
            );
        }
    }
    private void spawnParticles2(Level level, BlockPos targetPos) {
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

    private void playAmethyst(Level level, BlockPos pos, BlockState state) {
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.5F, 1.0F);
        level.gameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Context.of(state));
    }
}
