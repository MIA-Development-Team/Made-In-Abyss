package com.altnoir.mia.common.block;

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
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

public class MiaFungusBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<MiaFungusBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(fungusBlock -> fungusBlock.feature),
                            propertiesCodec()
                    )
                    .apply(instance, MiaFungusBlock::new)
    );
    protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 9.0, 12.0);
    private final ResourceKey<ConfiguredFeature<?, ?>> feature;

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    public MiaFungusBlock(ResourceKey<ConfiguredFeature<?, ?>> feature, Properties properties) {
        super(properties);
        this.feature = feature;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public boolean growMushroom(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        Optional<? extends Holder<ConfiguredFeature<?, ?>>> optional = level.registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolder(this.feature);

        // Neo: Fire the BlockGrowFeatureEvent and update the result of the Optional local with the new feature.
        var event = net.neoforged.neoforge.event.EventHooks.fireBlockGrowFeature(level, random, pos, optional.orElse(null));
        if (event.isCanceled()) {
            return false;
        }
        optional = Optional.ofNullable(event.getFeature());

        if (optional.isEmpty()) {
            return false;
        } else {
            level.removeBlock(pos, false);
            if (optional.get().value().place(level, level.getChunkSource().getGenerator(), random, pos)) {
                return true;
            } else {
                level.setBlock(pos, state, 3);
                return false;
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return (double) random.nextFloat() < 0.4;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.growMushroom(level, pos, state, random);
    }
}
