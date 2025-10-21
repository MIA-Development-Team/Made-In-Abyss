package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaSounds;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class MiaSoundsProvider extends SoundDefinitionsProvider {
    public static final boolean ABYSS_MUSIC = true;
    public static final String ABYSS_PORTAL_AMBIENT = "subtitle.mia.abyss_portal_ambient";
    public static final String ABYSS_PORTAL_TRAVEL = "subtitle.mia.abyss_portal_travel";

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
                        .with(sound(MiaUtil.miaId("block/abyss_portal_ambient")))
                        .subtitle(ABYSS_PORTAL_AMBIENT)
        );
        this.add(MiaSounds.ABYSS_PORTAL_TRAVEL.get(),
                definition()
                        .with(sound(MiaUtil.miaId("block/abyss_portal_travel")))
                        .subtitle(ABYSS_PORTAL_TRAVEL)
        );

        // Biomes
        this.add(MiaSounds.MUSIC_ABYSS_BRINK_DIM.get(),
                definition()
                        .with(
                                sound(MiaUtil.miaId("music/the_first_layer")).volume(0.4F).stream(),
                                sound(MiaUtil.miaId("music/rikos_cooking")).volume(0.4F).stream()
                        )
        );
    }
}
