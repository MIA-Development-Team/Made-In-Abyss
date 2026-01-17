package com.altnoir.mia.block.grower;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public class InvertedTreeGrower {
    private final String name;
    private final float secondaryChance;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> megaTree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryMegaTree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> tree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryTree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> flowers;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryFlowers;

    private static final Map<String, InvertedTreeGrower> GROWERS = new Object2ObjectArrayMap<>();
    public static final Codec<InvertedTreeGrower> CODEC = Codec.stringResolver(treeGrower -> treeGrower.name, GROWERS::get);


    public InvertedTreeGrower(
            String name,
            Optional<ResourceKey<ConfiguredFeature<?, ?>>> megaTree,
            Optional<ResourceKey<ConfiguredFeature<?, ?>>> tree,
            Optional<ResourceKey<ConfiguredFeature<?, ?>>> flowers
    ) {
        this(name, 0.0F, megaTree, Optional.empty(), tree, Optional.empty(), flowers, Optional.empty());
    }

    public InvertedTreeGrower(
            String name,
            float secondaryChance,
            Optional<ResourceKey<ConfiguredFeature<?, ?>>> megaTree,
            Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryMegaTree,
            Optional<ResourceKey<ConfiguredFeature<?, ?>>> tree,
            Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryTree,
            Optional<ResourceKey<ConfiguredFeature<?, ?>>> flowers,
            Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryFlowers
    ) {
        this.name = name;
        this.secondaryChance = secondaryChance;
        this.megaTree = megaTree;
        this.secondaryMegaTree = secondaryMegaTree;
        this.tree = tree;
        this.secondaryTree = secondaryTree;
        this.flowers = flowers;
        this.secondaryFlowers = secondaryFlowers;
    }

    @Nullable
    private ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean flowers) {
        if (random.nextFloat() < this.secondaryChance) {
            if (flowers && this.secondaryFlowers.isPresent()) {
                return this.secondaryFlowers.get();
            }

            if (this.secondaryTree.isPresent()) {
                return this.secondaryTree.get();
            }
        }

        return flowers && this.flowers.isPresent() ? this.flowers.get() : this.tree.orElse(null);
    }

    @Nullable
    private ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource random) {
        return this.secondaryMegaTree.isPresent() && random.nextFloat() < this.secondaryChance ? this.secondaryMegaTree.get() : this.megaTree.orElse(null);
    }

    public boolean growTree(ServerLevel level, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, RandomSource random) {
        ResourceKey<ConfiguredFeature<?, ?>> resourcekey = this.getConfiguredMegaFeature(random);
        if (resourcekey != null) {
            Holder<ConfiguredFeature<?, ?>> holder = level.registryAccess()
                    .registryOrThrow(Registries.CONFIGURED_FEATURE)
                    .getHolder(resourcekey)
                    .orElse(null);
            var event = EventHooks.fireBlockGrowFeature(level, random, pos, holder);
            holder = event.getFeature();
            if (event.isCanceled()) return false;
            if (holder != null) {
                for (int i = 0; i >= -1; i--) {
                    for (int j = 0; j >= -1; j--) {
                        if (isTwoByTwoSapling(state, level, pos, i, j)) {
                            ConfiguredFeature<?, ?> configuredfeature = holder.value();
                            BlockState blockstate = Blocks.AIR.defaultBlockState();
                            level.setBlock(pos.offset(i, 0, j), blockstate, 4);
                            level.setBlock(pos.offset(i + 1, 0, j), blockstate, 4);
                            level.setBlock(pos.offset(i, 0, j + 1), blockstate, 4);
                            level.setBlock(pos.offset(i + 1, 0, j + 1), blockstate, 4);
                            if (configuredfeature.place(level, chunkGenerator, random, pos.offset(i, 0, j))) {
                                return true;
                            }

                            level.setBlock(pos.offset(i, 0, j), state, 4);
                            level.setBlock(pos.offset(i + 1, 0, j), state, 4);
                            level.setBlock(pos.offset(i, 0, j + 1), state, 4);
                            level.setBlock(pos.offset(i + 1, 0, j + 1), state, 4);
                            return false;
                        }
                    }
                }
            }
        }

        ResourceKey<ConfiguredFeature<?, ?>> resourcekey1 = this.getConfiguredFeature(random, this.hasFlowers(level, pos));
        if (resourcekey1 == null) {
            return false;
        } else {
            Holder<ConfiguredFeature<?, ?>> holder1 = level.registryAccess()
                    .registryOrThrow(Registries.CONFIGURED_FEATURE)
                    .getHolder(resourcekey1)
                    .orElse(null);
            var event = EventHooks.fireBlockGrowFeature(level, random, pos, holder1);
            holder1 = event.getFeature();
            if (event.isCanceled()) return false;
            if (holder1 == null) {
                return false;
            } else {
                ConfiguredFeature<?, ?> configuredfeature1 = holder1.value();
                BlockState blockstate1 = level.getFluidState(pos).createLegacyBlock();
                level.setBlock(pos, blockstate1, 4);
                if (configuredfeature1.place(level, chunkGenerator, random, pos)) {
                    if (level.getBlockState(pos) == blockstate1) {
                        level.sendBlockUpdated(pos, state, blockstate1, 2);
                    }

                    return true;
                } else {
                    level.setBlock(pos, state, 4);
                    return false;
                }
            }
        }
    }

    private static boolean isTwoByTwoSapling(BlockState state, BlockGetter level, BlockPos pos, int xOffset, int yOffset) {
        Block block = state.getBlock();
        return level.getBlockState(pos.offset(xOffset, 0, yOffset)).is(block)
                && level.getBlockState(pos.offset(xOffset + 1, 0, yOffset)).is(block)
                && level.getBlockState(pos.offset(xOffset, 0, yOffset + 1)).is(block)
                && level.getBlockState(pos.offset(xOffset + 1, 0, yOffset + 1)).is(block);
    }

    private boolean hasFlowers(LevelAccessor level, BlockPos pos) {
        for (BlockPos blockpos : BlockPos.MutableBlockPos.betweenClosed(pos.below().north(2).west(2), pos.above().south(2).east(2))) {
            if (level.getBlockState(blockpos).is(BlockTags.FLOWERS)) {
                return true;
            }
        }

        return false;
    }
}