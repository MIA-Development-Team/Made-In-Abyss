package com.altnoir.mia.datagen;

import com.altnoir.abysslib.reginth.providers.ProviderType;
import com.altnoir.abysslib.reginth.providers.ReginthLangProvider;
import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaPaintingVariants;
import com.altnoir.mia.init.MiaPotions;
import com.altnoir.mia.init.MiaStats;
import com.altnoir.mia.util.FilesHelper;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * MIA 的语言键数据（迁移到 Reginth 之后）。
 * <p>
 * <b>为什么不再是 provider</b>：{@code assets/mia/lang/en_us.json} 迁移后有两个写入者 ——
 * MIA 自己的 {@code LanguageProvider} 和 Reginth 的 {@code ReginthLangProvider}。而 NeoForge 的
 * {@code DataGenerator} 用 <b>HashSet</b> 存 provider，<b>运行顺序不确定</b>：实测同一套代码，
 * 起初是 MIA 的赢（18.5 KB），多迁几个 Reginth 方块后就翻转成 Reginth 的赢（只剩 690 B，
 * MIA 的 tooltip / stat / tag / biome 键全被冲掉）。
 * <p>
 * 现在 <b>Reginth 是唯一写入者</b>：方块名/物品名由 Reginth 按 {@code toEnglishName} 自动写，
 * 其余（tooltip / stat / tag / biome / painting / potion / interface / ponder）由这里补齐。
 * <p>
 * 接线方式：{@link #register()} 在 {@code MIA} 构造函数里调用（必须早于 datagen），
 * 往 Reginth 的 LANG provider 上挂一个回调；provider 真正生成时才执行 {@link #fillInto}，
 * 那时注册表已经填好。取名规则与 {@code ReginthLangProvider.toEnglishName} 一致
 * （下划线分词 + 首字母大写），所以方块/物品的键值与迁移前逐一相同。
 */
public final class MiaLangData {

    private MiaLangData() {}

    /**
     * 在 {@code MIA} 构造函数里调用一次。
     */
    public static void register() {
        MIA.registrate().addDataGenerator(ProviderType.LANG, MiaLangData::fillInto);
    }

    private static void fillInto(ReginthLangProvider out) {
        addDefault(out, "interface");
        addDefault(out, "tooltip");
        addDefault(out, "ponder");

        MiaBiomes.BIOMES.forEach(
                biome -> out.add("biome.mia." + biome.location().getPath(), formatName(biome)));

        MiaPaintingVariants.PAINTING_VARIANTS.forEach(
                paintingVariant -> {
                    out.add(
                            "painting.mia." + paintingVariant.location().getPath() + ".title",
                            formatName(paintingVariant.location().getPath()));
                    out.add(
                            "painting.mia." + paintingVariant.location().getPath() + ".author",
                            "Memento In Abyss");
                });

        // 方块**和物品**现在都全部迁到 Reginth 了，所以这里既不再遍历 MiaBlocks.BLOCKS，
        // 也不再遍历 MiaItems.ITEMS（两个 DeferredRegister 都已经删掉）。
        // Reginth 自己会写 block.mia.* / item.mia.*；两边都写会直接抛
        // "Duplicate translation key"（NeoForge 的 LanguageProvider.add 拒绝重复键）——踩过。

        MiaPotions.POTIONS
                .getEntries()
                .forEach(
                        holder -> {
                            String key = holder.getId().getPath();
                            out.add(
                                    "item.minecraft.potion.effect." + key,
                                    "Potion of " + formatName(key));
                            out.add(
                                    "item.minecraft.splash_potion.effect." + key,
                                    "Splash Potion of " + formatName(key));
                            out.add(
                                    "item.minecraft.lingering_potion.effect." + key,
                                    "Lingering Potion of " + formatName(key));
                        });

        MiaStats.CUSTOM_STATS
                .getEntries()
                .forEach(
                        holder ->
                                out.add(
                                        "stat.mia.interact_with_" + holder.getId().getPath(),
                                        "Interactions with "
                                                + formatName(holder.getId().getPath())));

        // Tags start
        out.add("tag.item.curios.whistle", "Whistle");
        out.add("tag.item.mia.fossilized_logs", "Fossilized Logs");
        out.add("tag.item.mia.stripped_fossilized_logs", "Stripped Fossilized Logs");
        out.add("tag.item.mia.skyfog_logs", "Skyfog Logs");
        // Tags end
    }

    private static void addDefault(ReginthLangProvider out, String fileName) {
        var path = "assets/mia/lang/default/" + fileName + ".json";
        var jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) {
            throw new IllegalStateException(
                    String.format("Could not find default lang file: %s", path));
        }
        for (var entry : jsonElement.getAsJsonObject().entrySet()) {
            out.add(entry.getKey(), entry.getValue().getAsString());
        }
    }

    private static String formatName(ResourceKey<Biome> key) {
        var path = key.location().getPath();
        int index = path.lastIndexOf('/');
        if (index != -1) {
            path = path.substring(index + 1);
        }
        return formatName(path);
    }

    private static String formatName(String name) {
        return Arrays.stream(name.split("_"))
                .filter(word -> !word.isEmpty())
                .map(
                        word ->
                                Character.toUpperCase(word.charAt(0))
                                        + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
