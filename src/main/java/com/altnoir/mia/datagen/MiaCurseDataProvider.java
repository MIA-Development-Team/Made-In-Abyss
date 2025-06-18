package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.core.curse.CurseDataProvider;
import com.altnoir.mia.core.curse.records.CurseEffect;
import com.altnoir.mia.worldgen.dimension.MIADimensions;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffects;

import java.util.concurrent.CompletableFuture;

public class MiaCurseDataProvider extends CurseDataProvider {
    public MiaCurseDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(MIA.MOD_ID, output, registries);
    }

    @Override
    protected void addCurse() {
        add(MIADimensions.ABYSS_BRINK.location(),
                new CurseEffect[] {
                        new CurseEffect(MobEffects.HUNGER.getKey(), 0, 600),
                        new CurseEffect(MobEffects.DARKNESS.getKey(), 0, 60),
                        new CurseEffect(MobEffects.BLINDNESS.getKey(), 0, 20),
                }
        );
    }
}
