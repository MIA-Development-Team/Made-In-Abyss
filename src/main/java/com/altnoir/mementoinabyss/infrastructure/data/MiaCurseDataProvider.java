package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.curse.data.CurseDataProvider;
import com.altnoir.mementoinabyss.impl.curse.record.CurseEffect;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.dimension.LevelStem;

public class MiaCurseDataProvider extends CurseDataProvider {
    protected MiaCurseDataProvider(
            PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(MementoInAbyss.ID, output, lookup);
    }

    @Override
    protected void generate(HolderLookup.Provider lookup) {
        curse(LevelStem.OVERWORLD.identifier())
                .level(1)
                .effect(new CurseEffect(MobEffects.HUNGER.getKey(), 1, 200));
    }
}
