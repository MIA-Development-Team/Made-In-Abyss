package com.altnoir.mia.worldgen;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.init.worldgen.MiaFeatures;
import com.altnoir.mia.worldgen.feature.configurations.PrimoHugeFungusConfiguration;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

import java.util.Optional;

/**
 * 太初菌相关的地形特征（移植自 PoopSky 的 {@code PoConfigureFeatures} 里对应的 4 个条目）。
 * <p>
 * 这里只有"骨粉 / 生长"这一条路径会用到，所以没有配套的 {@code PlacedFeature}
 * （PoopSky 的 {@code PoPlacedFeatures} 同样是空的）：
 * <ul>
 *   <li>{@link #PRIMO_FUNGUS} / {@link #GLOW_PRIMO_FUNGUS}：由 {@code primo_fungus} /
 *       {@code glow_primo_fungus} 方块骨粉时直接放置；</li>
 *   <li>{@link #MYCELIUM_PATCH_BONEMEAL}：由 {@link MiaBlocks#MYCELIUM_BLOCK} 骨粉时放置，
 *       它内部再放置 {@link #MYCELIUM_VEGETATION}。</li>
 * </ul>
 */
public class PrimoFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> PRIMO_FUNGUS =
            MiaFeatureUtils.resourceKey("primo_fungus");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GLOW_PRIMO_FUNGUS =
            MiaFeatureUtils.resourceKey("glow_primo_fungus");

    public static final ResourceKey<ConfiguredFeature<?, ?>> MYCELIUM_VEGETATION =
            MiaFeatureUtils.resourceKey("mycelium_vegetation");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MYCELIUM_PATCH_BONEMEAL =
            MiaFeatureUtils.resourceKey("mycelium_patch_bonemeal");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);

        // 巨型菌生长时可以顶掉的方块：原版那套"可替换植物"清单，外加本模组自己的三种。
        BlockPredicate replaceablePlants = BlockPredicate.matchesBlocks(
                Blocks.OAK_SAPLING,
                Blocks.SPRUCE_SAPLING,
                Blocks.BIRCH_SAPLING,
                Blocks.JUNGLE_SAPLING,
                Blocks.ACACIA_SAPLING,
                Blocks.CHERRY_SAPLING,
                Blocks.DARK_OAK_SAPLING,
                Blocks.MANGROVE_PROPAGULE,
                Blocks.DANDELION,
                Blocks.TORCHFLOWER,
                Blocks.POPPY,
                Blocks.BLUE_ORCHID,
                Blocks.ALLIUM,
                Blocks.AZURE_BLUET,
                Blocks.RED_TULIP,
                Blocks.ORANGE_TULIP,
                Blocks.WHITE_TULIP,
                Blocks.PINK_TULIP,
                Blocks.OXEYE_DAISY,
                Blocks.CORNFLOWER,
                Blocks.WITHER_ROSE,
                Blocks.LILY_OF_THE_VALLEY,
                Blocks.BROWN_MUSHROOM,
                Blocks.RED_MUSHROOM,
                Blocks.WHEAT,
                Blocks.SUGAR_CANE,
                Blocks.ATTACHED_PUMPKIN_STEM,
                Blocks.ATTACHED_MELON_STEM,
                Blocks.PUMPKIN_STEM,
                Blocks.MELON_STEM,
                Blocks.LILY_PAD,
                Blocks.NETHER_WART,
                Blocks.COCOA,
                Blocks.CARROTS,
                Blocks.POTATOES,
                Blocks.CHORUS_PLANT,
                Blocks.CHORUS_FLOWER,
                Blocks.TORCHFLOWER_CROP,
                Blocks.PITCHER_CROP,
                Blocks.BEETROOTS,
                Blocks.SWEET_BERRY_BUSH,
                Blocks.WARPED_FUNGUS,
                Blocks.CRIMSON_FUNGUS,
                Blocks.WEEPING_VINES,
                Blocks.WEEPING_VINES_PLANT,
                Blocks.TWISTING_VINES,
                Blocks.TWISTING_VINES_PLANT,
                Blocks.CAVE_VINES,
                Blocks.CAVE_VINES_PLANT,
                Blocks.SPORE_BLOSSOM,
                Blocks.AZALEA,
                Blocks.FLOWERING_AZALEA,
                Blocks.MOSS_CARPET,
                Blocks.PINK_PETALS,
                Blocks.BIG_DRIPLEAF,
                Blocks.BIG_DRIPLEAF_STEM,
                Blocks.SMALL_DRIPLEAF,
                MiaBlocks.PRIMO_FUNGUS.get(),
                MiaBlocks.GLOW_PRIMO_FUNGUS.get(),
                MiaBlocks.MUSHROOM_BED.get()
        );

        // 普通的太初菌：菌柄 + 太初菌伞，伞上偶发紫水晶芽（与原模组一致）。
        MiaFeatureUtils.register(context, PRIMO_FUNGUS, MiaFeatures.HUGE_PRIMO_FUNGUS.get(),
                new PrimoHugeFungusConfiguration(
                        BlockTags.DIRT,
                        MiaBlocks.PRIMO_STEM.get().defaultBlockState(),
                        MiaBlocks.PRIMO_CAP.get().defaultBlockState(),
                        Optional.of(Blocks.BUDDING_AMETHYST.defaultBlockState()),
                        replaceablePlants
                ));

        // 发光太初菌：菌柄相同，菌伞换成发光的。
        MiaFeatureUtils.register(context, GLOW_PRIMO_FUNGUS, MiaFeatures.HUGE_PRIMO_FUNGUS.get(),
                new PrimoHugeFungusConfiguration(
                        BlockTags.DIRT,
                        MiaBlocks.PRIMO_STEM.get().defaultBlockState(),
                        MiaBlocks.GLOW_PRIMO_CAP.get().defaultBlockState(),
                        Optional.empty(),
                        replaceablePlants
                ));

        // 菌丝块骨粉后蔓延出来的植被。
        // 注意前半段：mushroom_bed 的 4 档 AMOUNT × 4 个朝向共 16 条，各权重 2 —— 也就是说
        // 菌丝蔓延时会顺带长出蘑菇簇。这段在原模组里是先在循环里塞进 builder、再 add 后面 5 条的，
        // 顺序即生成顺序，不能调换。
        SimpleWeightedRandomList.Builder<BlockState> builder = SimpleWeightedRandomList.builder();
        for (int i = 1; i <= 4; i++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                builder.add(MiaBlocks.MUSHROOM_BED.get().defaultBlockState()
                        .setValue(PinkPetalsBlock.AMOUNT, i)
                        .setValue(PinkPetalsBlock.FACING, direction), 2);
            }
        }
        MiaFeatureUtils.register(context, MYCELIUM_VEGETATION, Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(
                        new WeightedStateProvider(builder
                                .add(MiaBlocks.GLOW_PRIMO_FUNGUS.get().defaultBlockState(), 8)
                                .add(MiaBlocks.PRIMO_FUNGUS.get().defaultBlockState(), 12)
                                .add(MiaBlocks.MYCELIUM_MAT.get().defaultBlockState()
                                        .setValue(BlockStateProperties.DOWN, true), 25)
                                .add(Blocks.BROWN_MUSHROOM.defaultBlockState(), 4)
                                .add(Blocks.RED_MUSHROOM.defaultBlockState(), 4)
                        )
                ));

        MiaFeatureUtils.register(context, MYCELIUM_PATCH_BONEMEAL, Feature.VEGETATION_PATCH,
                vegetationPatch(MiaTags.Blocks.MYCELIUM_REPLACEABLE, MiaBlocks.MYCELIUM_BLOCK.get(),
                        holdergetter.getOrThrow(MYCELIUM_VEGETATION), 0.6F, 0.75F));
    }

    private static VegetationPatchConfiguration vegetationPatch(
            TagKey<Block> replaceable, Block ground, Holder<ConfiguredFeature<?, ?>> feature,
            float grow, float infection
    ) {
        return new VegetationPatchConfiguration(
                replaceable,
                BlockStateProvider.simple(ground),
                PlacementUtils.inlinePlaced(feature),
                CaveSurface.FLOOR,
                ConstantInt.of(1), 0.0F, 5,
                grow, UniformInt.of(1, 2), infection
        );
    }
}
