package com.altnoir.mementoinabyss.infrastructure.worldgen.lighting;

import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.BiomeAmbientLightSettings;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.Vec3;

public final class BiomeAmbientLightSampler {
    private static final int GRID_SIZE = 8;
    private final Map<Biome, Float> brightness = new IdentityHashMap<>();
    private final BiomeManager biomeManager;
    private final float[] corners = new float[8];
    private int cellX = Integer.MIN_VALUE;
    private int cellY;
    private int cellZ;
    private float weight;
    private float weightedBrightness;

    public BiomeAmbientLightSampler(
            BiomeAmbientLightSettings settings,
            HolderLookup<Biome> biomes,
            BiomeManager biomeManager) {
        this.biomeManager = biomeManager;
        biomes.listElements()
                .forEach(
                        holder -> {
                            float value = settings.brightness(holder);
                            if (value >= 0.0F) brightness.put(holder.value(), value);
                        });
    }

    public boolean isEmpty() {
        return brightness.isEmpty();
    }

    public void invalidate() {
        cellX = Integer.MIN_VALUE;
    }

    public void sample(Vec3 position) {
        double x = (position.x - 2.0) / GRID_SIZE;
        double y = (position.y - 2.0) / GRID_SIZE;
        double z = (position.z - 2.0) / GRID_SIZE;
        int ix = Mth.floor(x), iy = Mth.floor(y), iz = Mth.floor(z);
        if (ix != cellX || iy != cellY || iz != cellZ) {
            cellX = ix;
            cellY = iy;
            cellZ = iz;
            for (int corner = 0; corner < 8; corner++) {
                var biome =
                        biomeManager.getNoiseBiomeAtQuart(
                                (ix + (corner & 1)) * 2,
                                (iy + ((corner >> 1) & 1)) * 2,
                                (iz + ((corner >> 2) & 1)) * 2);
                corners[corner] = brightness.getOrDefault(biome.value(), -1.0F);
            }
        }
        float tx = (float) Mth.smoothstep(x - ix);
        float ty = (float) Mth.smoothstep(y - iy);
        float tz = (float) Mth.smoothstep(z - iz);
        weight = 0;
        weightedBrightness = 0;
        for (int corner = 0; corner < 8; corner++) {
            if (corners[corner] < 0) continue;
            float w =
                    ((corner & 1) == 0 ? 1 - tx : tx)
                            * ((corner & 2) == 0 ? 1 - ty : ty)
                            * ((corner & 4) == 0 ? 1 - tz : tz);
            weight += w;
            weightedBrightness += w * corners[corner];
        }
        weight = Mth.clamp(weight, 0.0F, 1.0F);
    }

    public float weight() {
        return weight;
    }

    public float weightedBrightness() {
        return weightedBrightness;
    }
}
