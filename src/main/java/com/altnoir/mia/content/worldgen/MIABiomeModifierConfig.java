package com.altnoir.mia.content.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.content.worldgen.biomemodifier.CarverAdder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MIABiomeModifierConfig {
    public static DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MIA.MOD_ID);

    public static RegistryObject<Codec<CarverAdder>> CARVER_ADDER_CODEC = BIOME_MODIFIER_SERIALIZERS.register("carver_adder", () ->
            RecordCodecBuilder.create(builder -> builder.group(
                    // declare fields
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(CarverAdder::biomes),
                    ConfiguredWorldCarver.LIST_CODEC.fieldOf("carvers").forGetter(CarverAdder::carvers)
                    // declare constructor
            ).apply(builder, CarverAdder::new)));

    public static void register(IEventBus modEventBus) {
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
    }
}
