package com.altnoir.mia.block.c;

import com.altnoir.mia.MIA;
import com.altnoir.mia.recipe.LampTubeRecipe;
import com.altnoir.mia.recipe.LampTubeRecipeInput;
import com.altnoir.mia.recipe.MiaRecipes;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Optional;

public class LampTubeBlock extends RodBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<LampTubeBlock> CODEC = simpleCodec(LampTubeBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;
    @Override
    public MapCodec<LampTubeBlock> codec() {
        return CODEC;
    }
    public LampTubeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(WATERLOGGED, Boolean.FALSE).setValue(LIT, false));
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos().relative(direction.getOpposite()));
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        BlockState defaultState = this.defaultBlockState().setValue(LIT, Boolean.valueOf(context.getLevel().hasNeighborSignal(context.getClickedPos()))).setValue(WATERLOGGED, Boolean.valueOf(flag));

        return blockstate.is(this) && blockstate.getValue(FACING) == direction
                ? defaultState.setValue(FACING, direction.getOpposite())
                : defaultState.setValue(FACING, direction);
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
    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        Direction direction = state.getValue(FACING);
        double d0 = (double)pos.getX() + 0.55 - (double)(random.nextFloat() * 0.1F);
        double d1 = (double)pos.getY() + 0.55 - (double)(random.nextFloat() * 0.1F);
        double d2 = (double)pos.getZ() + 0.55 - (double)(random.nextFloat() * 0.1F);
        double d3 = (double)(0.4F - (random.nextFloat() + random.nextFloat()) * 0.4F);
        if (random.nextInt(5) == 0) {
            level.addParticle(
                    ParticleTypes.END_ROD,
                    d0 + (double)direction.getStepX() * d3,
                    d1 + (double)direction.getStepY() * d3,
                    d2 + (double)direction.getStepZ() * d3,
                    random.nextGaussian() * 0.005,
                    random.nextGaussian() * 0.005,
                    random.nextGaussian() * 0.005
            );
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean flag = state.getValue(LIT);
            if (flag != level.hasNeighborSignal(pos)) {
                if (flag) {
                    level.scheduleTick(pos, this, 4);
                } else {
                    level.setBlock(pos, state.cycle(LIT), 2);
                    checkAndConvert(level, pos, state);
                    MIA.LOGGER.info("Lamp tube is {}", MiaRecipes.LAMP_TUBE_TYPE.get().toString());
                }
            }
        }
    }

    private void lampTubeChanged(BlockState state, Level level, BlockPos pos) {
        level.updateNeighborsAt(pos.above(), state.getBlock());
        boolean flag = state.getValue(LIT);
        if (flag) {
            level.scheduleTick(pos, this, 4);
        } else {
            level.setBlock(pos, state.cycle(LIT), 2);
            checkAndConvert(level, pos, state);
        }
    }

    private void checkAndConvert(Level level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(FACING);
        for (int i = 1; i <= 12; i++) {
            BlockPos targetPos = pos.relative(direction, i);
            BlockState stateInDir = level.getBlockState(targetPos);
            // 检查是否为容器
            if (level.getBlockEntity(targetPos) instanceof Container container) {
                // 遍历容器格子
                for (int c = 0; c < container.getContainerSize(); c++) {
                    ItemStack stack = container.getItem(c);
                    Optional<RecipeHolder<LampTubeRecipe>> recipe = getCurrentRecipe(level, stack);

                    if (stack.isEmpty() | recipe.isEmpty()) continue;

                    ItemStack output = recipe.get().value().result();
                    //MIA.LOGGER.info("Output: {}", output);
                    //if (!stack.isEmpty() && stack.is(ItemTags.LOGS))
                    int count = stack.getCount() / output.getCount();
                    if (count < 1 | (float) stack.getCount() % output.getCount() != 0) continue;

                    // 替换物品（还需要添加数量检测）
                    container.setItem(c, output.copyWithCount(count));
                    container.setChanged();

                    // 效果
                    playBlast(level, targetPos);
                    spawnParticles1(level, pos, targetPos, state);
                    spawnParticles2(level, targetPos);
                    return;
                }
                return;
            }else if (stateInDir.getBlock() instanceof LampTubeBlock lampTube) {
                spawnParticles1(level, pos, targetPos, state);
                lampTube.lampTubeChanged(stateInDir, level, targetPos);
                return;
            }
        }
    }

    private Optional<RecipeHolder<LampTubeRecipe>> getCurrentRecipe(Level level, ItemStack stack) {
        return level.getRecipeManager()
                .getRecipeFor(MiaRecipes.LAMP_TUBE_TYPE.get(), new LampTubeRecipeInput(stack), level);
    }

    private void spawnParticles1(Level level, BlockPos pos, BlockPos targetPos, BlockState state) {
        double startX = pos.getX() + 0.5, startY = pos.getY() + 0.5, startZ = pos.getZ() + 0.5;
        double endX = targetPos.getX() + 0.5, endY = targetPos.getY() + 0.5, endZ = targetPos.getZ() + 0.5;

        double midX = (startX + endX) / 2, midY = (startY + endY) / 2, midZ = (startZ + endZ) / 2;

        double dx = endX - startX, dy = endY - startY, dz = endZ - startZ;

        int particleCount = (int) Math.abs(dx + dy + dz) / 2 * 50;

        Direction facing = state.getValue(FACING);

        double dxFactor = (facing == Direction.WEST || facing == Direction.EAST) ? 0.16 : 0.01;
        double dyFactor = (facing == Direction.UP || facing == Direction.DOWN) ? 0.16 : 0.01;
        double dzFactor = (facing == Direction.NORTH || facing == Direction.SOUTH) ? 0.16 : 0.01;

        if (level instanceof ServerLevel serverLevel) {
            float r = 0.0F, g = 1.0F, b = 1.0F;

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

    private void playBlast(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS);
    }

    @Override
    protected void tick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(LIT) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(LIT), 2);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, WATERLOGGED);
    }
}
