package com.altnoir.mia.worldgen.noise_setting;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import com.google.common.collect.ImmutableList;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class MiaSurfaceRuleData extends SurfaceRuleData {
    private static final SurfaceRules.RuleSource DEEPSLATE = makeStateRule(Blocks.DEEPSLATE);
    private static final SurfaceRules.RuleSource ROOTED_DIRT = makeStateRule(Blocks.ROOTED_DIRT);
    private static final SurfaceRules.RuleSource COARSE_DIRT = makeStateRule(Blocks.COARSE_DIRT);
    private static final SurfaceRules.RuleSource BEDROCK = makeStateRule(Blocks.BEDROCK);
    // Layer 1
    private static final SurfaceRules.RuleSource ABYSS_ANDESITE = makeStateRule(MiaBlocks.ABYSS_ANDESITE.get());
    private static final SurfaceRules.RuleSource COVERGRASS_ABYSS_ANDESITE = makeStateRule(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get());
    private static final SurfaceRules.RuleSource COVERGRASS_TUFF = makeStateRule(MiaBlocks.COVERGRASS_TUFF.get());
    private static final SurfaceRules.RuleSource CALCITE = makeStateRule(Blocks.CALCITE);
    // Layer 2
    private static final SurfaceRules.RuleSource MUD = makeStateRule(Blocks.MUD);

    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    public static SurfaceRules.RuleSource theAbyss() {
        SurfaceRules.ConditionSource waterBlockCheck = SurfaceRules.waterBlockCheck(0, 0);
        // Layer 1
        SurfaceRules.RuleSource coverGrass_andesite = SurfaceRules.ifTrue(waterBlockCheck, COVERGRASS_ABYSS_ANDESITE);
        SurfaceRules.RuleSource coarse_dirt = SurfaceRules.ifTrue(waterBlockCheck, COARSE_DIRT);
        SurfaceRules.RuleSource coverGrass_tuff = SurfaceRules.ifTrue(waterBlockCheck, COVERGRASS_TUFF);
        // Layer 2

        SurfaceRules.RuleSource sequence = SurfaceRules.sequence(
                // Layer 1
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(
                                MiaBiomes.THE_ABYSS,
                                MiaBiomes.SKYFOG_FOREST,
                                MiaBiomes.FOSSILIZED_FOREST,
                                MiaBiomes.RICH_FOSSILIZED_FOREST,
                                MiaBiomes.UNDER_FOSSILIZED_FOREST,
                                MiaBiomes.ABYSS_PLAINS,
                                MiaBiomes.PRASIOLITE_CAVES,
                                MiaBiomes.ABYSS_LUSH_CAVES
                        ),
                        SurfaceRules.ifTrue(
                                SurfaceRules.stoneDepthCheck(0, true, 0, CaveSurface.FLOOR),
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
                                                coverGrass_andesite
                                        ),
                                        ABYSS_ANDESITE
                                )
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(MiaBiomes.DENSE_SKYFOG_FOREST),
                        SurfaceRules.ifTrue(
                                SurfaceRules.stoneDepthCheck(0, true, 0, CaveSurface.FLOOR),
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
                                                coarse_dirt
                                        ),
                                        makeStateRule(Blocks.DIRT)
                                )
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(MiaBiomes.ABYSS_DRIPSTONE_CAVES),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                        SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
                                        coverGrass_tuff
                                ),
                                makeStateRule(Blocks.TUFF)
                        )
                ),
                // Layer 2
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(
                                MiaBiomes.TEMPTATION_FOREST
                        ),
                        SurfaceRules.ifTrue(
                                SurfaceRules.stoneDepthCheck(0, true, 1, CaveSurface.FLOOR),
                                MUD
                        )
                ),
                SurfaceRules.sequence(
                        addCalciteRule(320, 2),
                        addCalciteRule(288, 3),
                        addCalciteRule(256, 5),
                        addCalciteRule(192),
                        addCalciteRule(128, 5),
                        addCalciteRule(64, 3),
                        addCalciteRule(48, 2)
                )
        );
        ImmutableList.Builder<SurfaceRules.RuleSource> builder = ImmutableList.builder();

        builder.add(SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.verticalGradient("deepslate_roof", VerticalAnchor.belowTop(5), VerticalAnchor.top())), DEEPSLATE));
        builder.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.aboveBottom(128), VerticalAnchor.aboveBottom(133)), ROOTED_DIRT));

        SurfaceRules.RuleSource ruleSource = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), sequence);
        builder.add(sequence); // 为ture时，表面覆盖物只会在最上层生成
        //builder.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("deepslate", VerticalAnchor.absolute(0), VerticalAnchor.absolute(8)), makeStateRule(Blocks.DEEPSLATE)));
        return SurfaceRules.sequence(builder.build().toArray(SurfaceRules.RuleSource[]::new));
    }

    private static SurfaceRules.RuleSource addCalciteRule(int y) {
        return addCalciteRule(y, 0, true);
    }

    private static SurfaceRules.RuleSource addCalciteRule(int y, int offset) {
        return addCalciteRule(y, offset, false);
    }

    private static SurfaceRules.RuleSource addCalciteRule(int y, int offset, boolean extend) {
        var conditionSource = extend
                ? SurfaceRules.yStartCheck(VerticalAnchor.absolute(y), -1)
                : SurfaceRules.yBlockCheck(VerticalAnchor.absolute(y), 0);
        return SurfaceRules.ifTrue(conditionSource,
                SurfaceRules.ifTrue(
                        SurfaceRules.not(
                                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(y + offset), 0)
                        ), CALCITE)
        );
    }

}