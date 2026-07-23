package com.altnoir.mementoinabyss.worldgen.dimension;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.MiaHeight;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;

import java.util.Optional;

public final class MiaDimensionTypes {
    public static final float ABYSS_AMBIENT_LIGHT = 0.12F;
    public static final float GREAT_FAULT_AMBIENT_LIGHT = 0.025F;
    public static final ResourceKey<DimensionType> THE_ABYSS = ResourceKey.create(
            Registries.DIMENSION_TYPE, MementoInAbyss.asResource("the_abyss_type"));
    public static final ResourceKey<DimensionType> GREAT_FAULT = ResourceKey.create(
            Registries.DIMENSION_TYPE, MementoInAbyss.asResource("great_fault_type"));

    public static void bootstrap(BootstrapContext<DimensionType> context) {
        var clocks = context.lookup(Registries.WORLD_CLOCK);
        
        var attributes = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.FOG_COLOR, 0xFF708C79)
                .set(EnvironmentAttributes.SKY_COLOR, OverworldBiomes.calculateSkyColor(0.8F))
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.colorFromFloat(
                        1.0F, ABYSS_AMBIENT_LIGHT, ABYSS_AMBIENT_LIGHT, ABYSS_AMBIENT_LIGHT))
                .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.white(0.8F))
                .set(EnvironmentAttributes.CLOUD_HEIGHT, 640.0F)
                .set(EnvironmentAttributes.SUN_ANGLE, 0.0F)
                .set(EnvironmentAttributes.MOON_ANGLE, 180.0F)
                .set(EnvironmentAttributes.STAR_ANGLE, 0.0F)
                .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.0F)
                .set(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, 0)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 1.0F)
                .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 15.0F)
                .set(EnvironmentAttributes.MONSTERS_BURN, true)
                .set(NeoForgeEnvironmentAttributes.CUSTOM_SKYBOX,
                        MementoInAbyss.asResource("environment_cube"))
                .set(EnvironmentAttributes.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK)
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, true)
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .build();
        context.register(THE_ABYSS, new DimensionType(
                true, true, false, false, 1.0,
                MiaHeight.THE_ABYSS.minY(), MiaHeight.THE_ABYSS.height(), MiaHeight.THE_ABYSS.height(),
                BlockTags.INFINIBURN_OVERWORLD, ABYSS_AMBIENT_LIGHT,
                new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                DimensionType.Skybox.OVERWORLD, CardinalLighting.Type.DEFAULT, attributes,
                HolderSet.empty(),
                Optional.of(clocks.getOrThrow(MiaWorldClocks.NOON))));

        var greatFaultAttributes = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.FOG_COLOR, 0xFF8795AA)
                .set(EnvironmentAttributes.SKY_COLOR, 0xFF8795AA)
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.colorFromFloat(
                        1.0F, GREAT_FAULT_AMBIENT_LIGHT, GREAT_FAULT_AMBIENT_LIGHT, GREAT_FAULT_AMBIENT_LIGHT))
                .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.white(0.8F))
                .set(EnvironmentAttributes.CLOUD_HEIGHT, (float) MiaHeight.GREAT_FAULT.maxY())
                .set(EnvironmentAttributes.SUN_ANGLE, 0.0F)
                .set(EnvironmentAttributes.MOON_ANGLE, 180.0F)
                .set(EnvironmentAttributes.STAR_ANGLE, 0.0F)
                .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.0F)
                .set(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, 0)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 1.0F)
                .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 15.0F)
                .set(EnvironmentAttributes.MONSTERS_BURN, true)
                .set(NeoForgeEnvironmentAttributes.CUSTOM_SKYBOX,
                        MementoInAbyss.asResource("environment_cube"))
                .set(EnvironmentAttributes.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK)
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, true)
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .build();
        context.register(GREAT_FAULT, new DimensionType(
                true, true, false, false, 1.0,
                MiaHeight.GREAT_FAULT.minY(), MiaHeight.GREAT_FAULT.height(), MiaHeight.GREAT_FAULT.height(),
                BlockTags.INFINIBURN_OVERWORLD, GREAT_FAULT_AMBIENT_LIGHT,
                new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                DimensionType.Skybox.OVERWORLD, CardinalLighting.Type.DEFAULT, greatFaultAttributes,
                HolderSet.empty(),
                Optional.of(clocks.getOrThrow(MiaWorldClocks.NOON))));
    }

    private MiaDimensionTypes() {}
}
