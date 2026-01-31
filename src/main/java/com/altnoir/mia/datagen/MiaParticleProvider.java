package com.altnoir.mia.datagen;

import com.altnoir.mia.init.MiaParticles;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

public class MiaParticleProvider extends ParticleDescriptionProvider {
    /**
     * Creates an instance of the data provider.
     *
     * @param output     the expected root directory the data generator outputs to
     * @param fileHelper the helper used to validate a texture's existence
     */
    protected MiaParticleProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, fileHelper);
    }

    @Override
    protected void addDescriptions() {
        this.spriteSet(
                MiaParticles.SKYFOG_LEAVES.get(),
                MiaUtil.miaId("skyfog"),
                12,
                false
        );
    }
}
