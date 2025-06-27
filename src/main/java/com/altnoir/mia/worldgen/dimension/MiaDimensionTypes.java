package com.altnoir.mia.worldgen.dimension;

import com.altnoir.mia.MIA;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.OptionalLong;

public class MiaDimensionTypes {
    public static final ResourceKey<DimensionType> ABYSS_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "abyss_brink_type"));

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        context.register(ABYSS_TYPE, new DimensionType(
                        OptionalLong.of(6000L), // fixedTime
                        true,
                        false,
                        false,
                        true,
                        1.0,
                        true,
                        false,
                        0,
                        320,
                        320,
                        BlockTags.INFINIBURN_OVERWORLD,
                        BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                        0.1F,
                        new DimensionType.MonsterSettings(false, true, UniformInt.of(0, 7), 0)
                )
        );
    }
}
