package com.altnoir.mementoinabyss.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaArtifactItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

public final class MiaCuriosDataProvider extends CuriosDataProvider {
    public MiaCuriosDataProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(MementoInAbyss.ID, output, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries) {
        var artifactSlot = createSlot("artifact")
                .size(1)
                .order(-998)
                .icon(MementoInAbyss.asResource("slot/empty_artifact_slot"))
                .addCosmetic(false);

        createEntities("artifacts")
                .addEntities(EntityType.PLAYER, EntityType.ARMOR_STAND)
                .addSlots(artifactSlot.getId());

        tag(artifactSlot).add(
                MiaArtifactItems.TEST_ARTIFACT_1.get(),
                MiaArtifactItems.TEST_ARTIFACT_2.get(),
                MiaArtifactItems.TEST_ARTIFACT_3.get(),
                MiaArtifactItems.HEALTH_JUNKIE.get()
        );
    }
}
