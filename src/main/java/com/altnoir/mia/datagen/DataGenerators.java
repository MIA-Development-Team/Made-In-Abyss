package com.altnoir.mia.datagen;

import com.altnoir.mia.compat.curios.MiaCuriosProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class DataGenerators {
    public static void gatherData(
            DataGenerator generator,
            ExistingFileHelper existingFileHelper,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            boolean includeServer,
            boolean includeClient) {
        PackOutput packOutput = generator.getPackOutput();

        //        generators.addProvider(includeServer, new LootTableProvider(packOutput,
        // Collections.emptySet(),
        //                List.of(new
        // LootTableProvider.SubProviderEntry(MiaBlockLootTableProvider::new,
        // LootContextParamSets.BLOCK)), lookupProvider));
        generator.addProvider(
                includeServer, MiaLootTableProvider.create(packOutput, lookupProvider));
        generator.addProvider(includeServer, new MiaRecipeProvider(packOutput, lookupProvider));

        BlockTagsProvider blockTagsProvider =
                new MiaBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        ItemTagsProvider itemTagsProvider =
                new MiaItemTagProvider(
                        packOutput,
                        lookupProvider,
                        blockTagsProvider.contentsGetter(),
                        existingFileHelper);
        generator.addProvider(includeServer, blockTagsProvider);
        generator.addProvider(includeServer, itemTagsProvider);

        var worldGenProvider = new MiaWorldGenProvider(packOutput, lookupProvider);
        generator.addProvider(includeServer, worldGenProvider);

        generator.addProvider(includeServer, new MiaDataMapProvider(packOutput, lookupProvider));
        generator.addProvider(
                includeServer,
                new MiaPaintingVariantTagsProvider(
                        packOutput, worldGenProvider.getRegistryProvider(), existingFileHelper));
        generator.addProvider(
                includeServer,
                new MiaBiomeTagsProvider(
                        packOutput, worldGenProvider.getRegistryProvider(), existingFileHelper));

        generator.addProvider(
                includeServer,
                new MiaCuriosProvider(packOutput, existingFileHelper, lookupProvider));
        generator.addProvider(includeServer, new MiaCurseDataProvider(packOutput, lookupProvider));
        generator.addProvider(
                includeServer, new MiaTrialSpawnerProvider(packOutput, lookupProvider));

        // 方块的 blockstate / 模型 / 战利品表 / 语言键，以及物品的模型 / 语言键，
        // 现在全部由 Reginth 的 builder 链驱动（每个方块声明上的 .blockstate(...) /
        // .loot(...)，每个物品声明上的 .model(...)），所以这里不再注册
        // MiaBlockStateProvider / MiaBlockLootTable / MiaItemModelProvider（三个文件都已删除）。
        generator.addProvider(
                includeClient, new MiaParticleProvider(packOutput, existingFileHelper));

        generator.addProvider(includeClient, new MiaSoundsProvider(packOutput, existingFileHelper));

        // 注意：这里**不再**注册 MIA 自己的 LanguageProvider。
        // 迁移到 Reginth 后 assets/mia/lang/en_us.json 会有 Reginth 和 MIA 两个写入者，
        // 而 NeoForge 的 DataGenerator 用 HashSet 存 provider、顺序不确定（实测会翻转，
        // 一次把 MIA 的 tooltip/stat/tag 全冲掉）。现在 Reginth 是唯一写入者，
        // MIA 的键由 MiaLangData.register()（在 MIA 构造函数里调用）挂到它的 LANG provider 上。
    }
}
