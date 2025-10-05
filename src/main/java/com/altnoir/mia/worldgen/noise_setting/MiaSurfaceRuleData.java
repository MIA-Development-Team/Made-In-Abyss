package com.altnoir.mia.worldgen.noise_setting;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import com.google.common.collect.ImmutableList;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class MiaSurfaceRuleData extends SurfaceRuleData {
    private static final SurfaceRules.RuleSource AIR = makeStateRule(Blocks.AIR);
    private static final SurfaceRules.RuleSource DEEPSLATE = makeStateRule(Blocks.DEEPSLATE);
    private static final SurfaceRules.RuleSource BEDROCK = makeStateRule(Blocks.BEDROCK);

    private static final SurfaceRules.RuleSource ABYSS_ANDESITE = makeStateRule(MiaBlocks.ABYSS_ANDESITE.get());
    private static final SurfaceRules.RuleSource COVERGRASS_ABYSS_ANDESITE = makeStateRule(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get());
    private static final SurfaceRules.RuleSource SAND = makeStateRule(Blocks.SAND);

    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    public static SurfaceRules.RuleSource abyssBrink(boolean aboveGround) {
        SurfaceRules.ConditionSource surfacerules$waterBlockCheck = SurfaceRules.waterBlockCheck(0, 0);
        SurfaceRules.RuleSource surfacerules$rulesource = SurfaceRules.ifTrue(surfacerules$waterBlockCheck, COVERGRASS_ABYSS_ANDESITE);

        SurfaceRules.RuleSource sequence = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(MiaBiomes.ABYSS_BRINK, MiaBiomes.SKYFOG_FOREST, MiaBiomes.ABYSS_PLAINS, MiaBiomes.PRASIOLITE_CAVE, Biomes.CHERRY_GROVE),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                        SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
                                        surfacerules$rulesource
                                )
                        )
                )
        );
        ImmutableList.Builder<SurfaceRules.RuleSource> builder = ImmutableList.builder();

        builder.add(SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.verticalGradient("deepslate_roof", VerticalAnchor.belowTop(5), VerticalAnchor.top())), DEEPSLATE));
        builder.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)), BEDROCK));

        SurfaceRules.RuleSource ruleSource = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), sequence);
        builder.add(aboveGround ? ruleSource : sequence); // 为ture时，表面覆盖物只会在最上层生成
        //builder.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("deepslate", VerticalAnchor.absolute(0), VerticalAnchor.absolute(8)), makeStateRule(Blocks.DEEPSLATE)));
        return SurfaceRules.sequence(builder.build().toArray(SurfaceRules.RuleSource[]::new));
    }
}
