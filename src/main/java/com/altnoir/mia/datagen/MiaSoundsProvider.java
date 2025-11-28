package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaSounds;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
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
        if (ABYSS_MUSIC) {
            this.add(MiaSounds.MUSIC_ABYSS_BRINK_DIM.get(),
                    definition().replace(true)
                            .with(
                                    sound(MiaUtil.miaId("music/the_first_layer")).volume(0.4F).stream(),
                                    sound(MiaUtil.miaId("music/rikos_cooking")).volume(0.4F).stream()
                            )
            );
        } else {
            this.add(MiaSounds.MUSIC_ABYSS_BRINK_DIM.get(), definition().with(defaultMusicSounds()));
        }
    }

    /**
     * 创建默认的Minecraft音乐音效数组
     *
     * @return SoundDefinition.Sound[] 包含默认音乐的数组
     */
    private SoundDefinition.Sound[] defaultMusicSounds() {
        return new SoundDefinition.Sound[]{
                sound("music/game/an_ordinary_day").volume(0.8F).stream(),
                sound("music/game/clark").stream(),
                sound("music/game/echo_in_the_wind").volume(0.4F).stream(),
                sound("music/game/featherfall").volume(0.4F).stream(),
                sound("music/game/floating_dream").volume(0.5F).stream(),
                sound("music/game/left_to_bloom").volume(0.4F).stream(),
                sound("music/game/mice_on_venus").stream(),
                sound("music/game/minecraft").stream(),
                sound("music/game/one_more_day").volume(0.4F).stream(),
                //sound("music/game/os_piano").volume(0.4F).weight(2).stream(),
                sound("music/game/swamp/aerie").volume(0.4F).stream(),
                sound("music/game/swamp/firebugs").volume(0.4F).stream(),
                sound("music/game/swamp/labyrinthine").volume(0.4F).stream(),
                sound("music/game/sweden").stream()
        };
    }
}
