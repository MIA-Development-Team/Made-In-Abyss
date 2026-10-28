package com.altnoir.mementoinabyss.api.worldgen;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

/** Terrain categories are chosen independently of the installed biome groups. */
public enum Layer2TerrainKind implements StringRepresentable {
    WOODLAND,
    WETLAND,
    HIGHLAND,
    PLATEAU;

    public static final Codec<Layer2TerrainKind> CODEC =
            StringRepresentable.fromEnum(Layer2TerrainKind::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
