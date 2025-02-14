package com.altnoir.mia.content.worldgen.biomemodifier;

import com.altnoir.mia.content.worldgen.MIABiomeModifierConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public record CarverAdder(HolderSet<Biome> biomes, HolderSet<ConfiguredWorldCarver<?>> carvers) implements BiomeModifier {
    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && this.biomes.contains(biome)) {
            BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
            for(var carver : this.carvers) {
                generationSettings.addCarver(GenerationStep.Carving.AIR, carver);
            }
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return MIABiomeModifierConfig.CARVER_ADDER_CODEC.get();
    }
}
