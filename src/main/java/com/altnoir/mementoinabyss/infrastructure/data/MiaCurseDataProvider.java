package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.curse.data.CurseDataProvider;
import com.altnoir.mementoinabyss.impl.curse.record.CurseEffect;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffects;

public class MiaCurseDataProvider extends CurseDataProvider {
    protected MiaCurseDataProvider(
            PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(MementoInAbyss.ID, output, lookup);
    }

    @Override
    protected void generate(HolderLookup.Provider lookup) {
        curse(MiaDimensions.THE_ABYSS_LEVEL.identifier())
                .level(1)
                .effect(new CurseEffect(MobEffects.HUNGER.getKey(), 1, 200))
                .effect(new CurseEffect(MobEffects.NAUSEA.getKey(), 1, 200));
    }
}
