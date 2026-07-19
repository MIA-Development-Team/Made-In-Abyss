package com.altnoir.mementoinabyss.content.block.plant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

public class MiaFungusBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<MiaFungusBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(block -> block.feature),
            propertiesCodec()).apply(instance, MiaFungusBlock::new));
    private static final VoxelShape SHAPE = Block.column(8.0, 0.0, 9.0);
    private final ResourceKey<ConfiguredFeature<?, ?>> feature;

    public MiaFungusBlock(ResourceKey<ConfiguredFeature<?, ?>> feature, Properties properties) {
        super(properties);
        this.feature = feature;
    }

    @Override public MapCodec<MiaFungusBlock> codec() { return CODEC; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }

    private boolean grow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        Optional<? extends Holder<ConfiguredFeature<?, ?>>> configured = level.registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE).get(feature);
        if (configured.isEmpty()) return false;
        level.removeBlock(pos, false);
        if (configured.get().value().place(level, level.getChunkSource().getGenerator(), random, pos)) return true;
        level.setBlock(pos, state, Block.UPDATE_ALL);
        return false;
    }

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return true; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return random.nextFloat() < 0.4F; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) { grow(level, pos, state, random); }
}
