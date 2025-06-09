package com.altnoir.mia.worldgen.noisesetting;

import com.altnoir.mia.block.MIABlocks;
import com.altnoir.mia.worldgen.biome.MIABiomes;
import com.google.common.collect.ImmutableList;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class MIASurfaceRuleData extends SurfaceRuleData {
    private static final SurfaceRules.RuleSource AIR = makeStateRule(Blocks.AIR);
    private static final SurfaceRules.RuleSource BEDROCK = makeStateRule(Blocks.BEDROCK);

    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
    public static SurfaceRules.RuleSource abyssBrink(boolean aboveGround) {
        SurfaceRules.RuleSource sequence = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(MIABiomes.ABYSS_BRINK),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                        SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
                                        makeStateRule(MIABlocks.COVERGRASS_ABYSS_ANDESITE.get())
                                )
                        )
                )
        );
        ImmutableList.Builder<SurfaceRules.RuleSource> builder = ImmutableList.builder();

        builder.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)), BEDROCK));

        SurfaceRules.RuleSource ruleSource = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), sequence);
        builder.add(aboveGround ? ruleSource : sequence); // 为ture时，表面覆盖物只会在最上层生成
        //builder.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("deepslate", VerticalAnchor.absolute(0), VerticalAnchor.absolute(8)), makeStateRule(Blocks.DEEPSLATE)));
        return SurfaceRules.sequence(builder.build().toArray(SurfaceRules.RuleSource[]::new));
    }
}
