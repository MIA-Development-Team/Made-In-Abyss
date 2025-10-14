package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaSounds;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class MiaSoundsProvider extends SoundDefinitionsProvider {
    /**
     * Creates a new instance of this data provider.
     *
     * @param output The {@linkplain PackOutput} instance provided by the data generator.
     * @param helper The existing file helper provided by the event you are initializing this provider in.
     */
    protected MiaSoundsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, MIA.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        this.add(MiaSounds.ABYSS_PORTAL_AMBIENT.get(),
                definition()
                        .with(sound(MiaUtil.miaId("abyss_portal_ambient")))
                        .subtitle("subtitle.mia.abyss_portal_ambient")
        );
        this.add(MiaSounds.ABYSS_PORTAL_TRAVEL.get(),
                definition()
                        .with(sound(MiaUtil.miaId("abyss_portal_travel")))
                        .subtitle("subtitle.mia.abyss_portal_travel")
        );
    }
}
