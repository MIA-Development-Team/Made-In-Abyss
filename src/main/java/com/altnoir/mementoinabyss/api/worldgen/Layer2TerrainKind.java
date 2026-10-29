package com.altnoir.mementoinabyss.api.worldgen;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

/** Terrain categories are chosen independently of the installed biome groups. */
public enum Layer2TerrainKind implements StringRepresentable {
    WOODLAND,
    WETLAND,
    HIGHLAND,
    PLATEAU,
    WETLAND_WOODLAND,
    WETLAND_HIGHLAND,
    OCEAN,
    OCEAN_WOODLAND,
    WOODLAND_OCEAN;

    public Layer2TerrainKind primaryTerrain() {
        return switch (this) {
            case WETLAND_WOODLAND, WETLAND_HIGHLAND -> WETLAND;
            case OCEAN_WOODLAND -> OCEAN;
            case WOODLAND_OCEAN -> WOODLAND;
            default -> this;
        };
    }

    public Layer2TerrainKind companionTerrain() {
        return switch (this) {
            case WETLAND_WOODLAND, OCEAN_WOODLAND -> WOODLAND;
            case WETLAND_HIGHLAND -> HIGHLAND;
            case WOODLAND_OCEAN -> OCEAN;
            default -> this;
        };
    }

    public static final Codec<Layer2TerrainKind> CODEC =
            StringRepresentable.fromEnum(Layer2TerrainKind::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
