package com.altnoir.mia;

import com.altnoir.abysslib.reginth.Reginth;
import com.altnoir.mia.core.curse.CurseManager;
import com.altnoir.mia.core.event.EventHandle;
import com.altnoir.mia.core.spawner.AbyssTrialSpawnerManager;
import com.altnoir.mia.datagen.MiaLangData;
import com.altnoir.mia.init.*;
import com.altnoir.mia.init.worldgen.*;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(MIA.MOD_ID)
public class MIA {
    public static final String MOD_ID = "mia";
    public static final String MOD_NAME = "Memento In Abyss";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 注册框架实例（AbyssLib 的 Reginth，fork 自上游 Registrate）。
     * <p>
     * 1.21.1 线的 {@code Reginth.create()} 会**自行**从 {@code ModList} 找到本模组的
     * event bus 并挂上监听（26.1 线不会，那边要手调 {@code registerEventListeners}），
     * 所以这里不需要额外注册。类就在 abysslib-1.21.1 的 jar 里，不要再 jarJar 上游 Registrate。
     * <p>
     * 方块/物品的分区归属由各自注册类用 {@code MIA.registrate().defaultCreativeSection(...)}
     * 声明，所以这里**不**设全局默认标签页。
     */
    private static final Reginth REGINTH = Reginth.create(MOD_ID);

    public static Reginth registrate() {
        return REGINTH;
    }

    /**
     * 本模组命名空间下的资源 id（与上游 Registrate 的 {@code modLoc} 等价）。
     */
    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static final CurseManager CURSE_MANAGER = new CurseManager();
    public static final AbyssTrialSpawnerManager SPAWNER_MANAGER = new AbyssTrialSpawnerManager();

    public MIA(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info(
                "{} {}+{} initializing...",
                MOD_NAME,
                MiaBuildInfo.VERSION,
                MiaBuildInfo.GIT_COMMIT);

        // 语言键：迁移到 Reginth 后 en_us.json 由 Reginth 独家写入，MIA 的键挂到它的 LANG provider 上。
        // 必须在这里（早于 datagen 事件）注册，否则 Reginth 的 provider 已经建好、加不进去了。
        MiaLangData.register();

        modEventBus.addListener(this::commonSetup);

        // 物品也全部走 Reginth 了，MiaItems 里没有 DeferredRegister 需要注册；
        // 但要和 MiaBlocks 一样在这里**显式加载**（同样因为注册表在类初始化时填充）。
        MiaItems.bootstrap();
        // MiaBlocks 不再需要注册：方块（含它们的 BlockItem）已经全部走 Reginth，
        // Reginth.create(MOD_ID) 自己会把监听器挂到 mod event bus 上。
        // 但必须在这里**显式加载** MiaBlocks：它的注册表是在类初始化时（REGINTH 字段 +
        // 每条 REGINTH.object(...)）填充的，晚于 RegisterEvent 加载就等于方块全丢了。
        MiaBlocks.bootstrap();
        MiaBlockEntities.register(modEventBus);
        MiaItemGroups.register(modEventBus);

        MiaAttributes.register(modEventBus);
        MiaEffects.register(modEventBus);
        MiaPotions.register(modEventBus);
        MiaComponents.register(modEventBus);
        MiaSounds.register(modEventBus);

        MiaBiomeSources.register(modEventBus);
        MiaDensityFunctionTypes.register(modEventBus);
        MiaFeatures.register(modEventBus);
        MiaPlacements.register(modEventBus);
        MiaStructurePlacementTypes.register(modEventBus);
        MiaStructureTypes.register(modEventBus);
        MiaFoliagePlacerTypes.register(modEventBus);
        MiaTrunkPlacerTypes.register(modEventBus);

        MiaRecipes.register(modEventBus);
        MiaAttachments.register(modEventBus);
        MiaParticles.register(modEventBus);
        MiaEntities.register(modEventBus);
        MiaMenus.register(modEventBus);
        MiaStats.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, MiaConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, MiaConfig.SERVER_SPEC);

        var gameEventBus = NeoForge.EVENT_BUS;
        gameEventBus.register(this);
        EventHandle.addListener(modEventBus, gameEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        MiaStats.init();
    }

    @SubscribeEvent
    private void reload(final AddReloadListenerEvent event) {
        event.addListener(CURSE_MANAGER);
        event.addListener(SPAWNER_MANAGER);
    }
}
