package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.abysslib.reginth.providers.ProviderType;
import com.altnoir.abysslib.reginth.providers.ReginthLangProvider;
import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.foundation.utility.FilesHelper;
import com.altnoir.mementoinabyss.infrastructure.worldgen.MiaPaintingVariants;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensionTypes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaWorldClocks;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.GreatFaultFeatures;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.GreatFaultPlacements;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.MiaAbyssFeatures;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.MiaAbyssPlacements;
import com.altnoir.mementoinabyss.infrastructure.worldgen.noise.MiaDensityFunctions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.noise.MiaNoiseData;
import com.altnoir.mementoinabyss.infrastructure.worldgen.noise.MiaNoiseGeneratorSettings;
import com.altnoir.mementoinabyss.infrastructure.worldgen.structure.MiaProcessorLists;
import com.altnoir.mementoinabyss.infrastructure.worldgen.structure.MiaStructurePools;
import com.altnoir.mementoinabyss.infrastructure.worldgen.structure.MiaStructureSets;
import com.altnoir.mementoinabyss.infrastructure.worldgen.structure.MiaStructures;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.MiaTreeFeatures;
import com.altnoir.mementoinabyss.init.MiaItemGroups;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class MiaDataGen {
    public static void gatherDataHighPriority(GatherDataEvent.Client event) {
        if (event.getModContainer().getModId().equals(MementoInAbyss.ID)) addExtraRegistrateData();
    }

    public static void gatherData(GatherDataEvent.Client event) {
        if (!event.getModContainer().getModId().equals(MementoInAbyss.ID)) return;

        event.createProvider(
                output ->
                        new MiaGuideDataProvider(
                                output, event.getResourceManager(PackType.SERVER_DATA)));
        event.createProvider(MiaCurseDataProvider::new);
        event.createProvider(MiaCuriosDataProvider::new);
        event.createProvider(MiaArtifactEnhancementRecipeProvider.Runner::new);
        event.createProvider(MiaLampTubeRecipeProvider.Runner::new);
        event.createProvider(MiaLootTableProvider::create);
        event.createDatapackRegistryObjects(
                new RegistrySetBuilder()
                        .add(Registries.WORLD_CLOCK, MiaWorldClocks::bootstrap)
                        .add(Registries.DIMENSION_TYPE, MiaDimensionTypes::bootstrap)
                        .add(Registries.NOISE, MiaNoiseData::bootstrap)
                        .add(Registries.DENSITY_FUNCTION, MiaDensityFunctions::bootstrap)
                        .add(Registries.NOISE_SETTINGS, MiaNoiseGeneratorSettings::bootstrap)
                        .add(
                                Registries.CONFIGURED_FEATURE,
                                context -> {
                                    MiaAbyssFeatures.bootstrap(context);
                                    MiaTreeFeatures.bootstrap(context);
                                    GreatFaultFeatures.bootstrap(context);
                                })
                        .add(
                                Registries.PLACED_FEATURE,
                                context -> {
                                    MiaAbyssPlacements.bootstrap(context);
                                    GreatFaultPlacements.bootstrap(context);
                                })
                        .add(Registries.BIOME, MiaBiomes::bootstrap)
                        .add(Registries.PROCESSOR_LIST, MiaProcessorLists::bootstrap)
                        .add(Registries.TEMPLATE_POOL, MiaStructurePools::bootstrap)
                        .add(Registries.STRUCTURE, MiaStructures::bootstrap)
                        .add(Registries.STRUCTURE_SET, MiaStructureSets::bootstrap)
                        .add(Registries.PAINTING_VARIANT, MiaPaintingVariants::bootstrap)
                        .add(Registries.LEVEL_STEM, MiaDimensions::bootstrap));
    }

    private static void addExtraRegistrateData() {
        MementoInAbyss.registrate()
                .addDataGenerator(
                        ProviderType.LANG,
                        provider -> {
                            BiConsumer<String, String> langConsumer = provider::add;

                            // 创造栏标题：Reginth 的 creativeTab() 只会按 toEnglishName(注册名) 兜底生成
                            // （mia -> "Mia"、mia_artifact -> "Mia Artifact"），这里覆盖成正式展示名。
                            //
                            // 注意：不能用 provider.add(tab, value) —— Reginth 自己那个 extraLang 生成器
                            // 已经把同一个键写进去了（Reginth.java:186 的 addLang("itemGroup", ...)），
                            // 而 LanguageProvider.add 对重复键是抛异常语义（旧 RegistrateLangProvider 才是覆盖语义），
                            // 所以用 add 会得到 "Duplicate translation key itemGroup.mementoinabyss.mia"。
                            // 这里改用 put 语义直接写进 LanguageProvider 的 data map，与生成器执行顺序无关。
                            overwriteLang(provider, MiaItemGroups.BASE.get(), "Memento In Abyss");
                            overwriteLang(
                                    provider,
                                    MiaItemGroups.ARTIFACT.get(),
                                    "Memento In Abyss: Artifact");

                            provideDefaultLang("interface", langConsumer);
                            provideDefaultLang("tooltips", langConsumer);
                        });
        MiaRegistrateTags.addGenerators();
    }

    /**
     * 以「后写覆盖」语义写入一个创造栏标题键，等价于 {@link ReginthLangProvider#add(CreativeModeTab, String)}
     * 但不做重复键检查。
     *
     * <p>Reginth 会同时产出 en_us 与 en_ud 两个文件（en_ud 是内部 AccessibleLanguageProvider 用
     * {@code toUpsideDown} 生成的），所以这里也要一起覆盖，否则 en_ud 里仍会残留按注册名自动生成的标题。
     */
    private static void overwriteLang(
            ReginthLangProvider provider, CreativeModeTab tab, String name) {
        var contents = tab.getDisplayName().getContents();
        if (!(contents instanceof TranslatableContents lang)) {
            throw new IllegalArgumentException(
                    "Creative tab does not have a translatable name: " + tab.getDisplayName());
        }
        langData(provider).put(lang.getKey(), Component.literal(name));
        try {
            Object upsideDown = upsideDownField().get(provider);
            langData(upsideDown)
                    .put(lang.getKey(), Component.literal(toUpsideDown(provider, name)));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not access ReginthLangProvider#upsideDown", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Component> langData(Object provider) {
        try {
            Field field = LanguageProvider.class.getDeclaredField("data");
            field.setAccessible(true);
            return (Map<String, Component>) field.get(provider);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Could not access LanguageProvider#data; creative tab titles cannot be"
                            + " overridden",
                    e);
        }
    }

    private static Field upsideDownField() throws NoSuchFieldException {
        Field field = ReginthLangProvider.class.getDeclaredField("upsideDown");
        field.setAccessible(true);
        return field;
    }

    private static String toUpsideDown(ReginthLangProvider provider, String value)
            throws ReflectiveOperationException {
        Method method = ReginthLangProvider.class.getDeclaredMethod("toUpsideDown", String.class);
        method.setAccessible(true);
        return (String) method.invoke(provider, value);
    }

    private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
        var path = "assets/mementoinabyss/lang/default/" + fileName + ".json";
        var jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) {
            throw new IllegalStateException(
                    String.format("Could not find default lang file: %s", path));
        }
        var jsonObject = jsonElement.getAsJsonObject();
        for (var entry : jsonObject.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue().getAsString();
            consumer.accept(key, value);
        }
    }
}
