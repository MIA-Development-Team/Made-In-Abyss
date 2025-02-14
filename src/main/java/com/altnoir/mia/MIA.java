package com.altnoir.mia;

import com.altnoir.mia.client.entity.KnifeRenderer;
import com.altnoir.mia.content.worldgen.MIABiomeModifierConfig;
import com.altnoir.mia.content.worldgen.MIACarvers;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// 此处的值应与 META-INF/mods.toml 文件中的条目匹配
@Mod(MIA.MOD_ID)
public class MIA {
    // 在所有地方定义一个通用的 mod id
    public static final String MOD_ID = "mia";
    // 直接引用一个 slf4j 日志记录器
    private static final Logger LOGGER = LogUtils.getLogger();

    public MIA() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册 commonSetup 方法以进行模组加载
        modEventBus.addListener(this::commonSetup);

        ItemsRegister.register(modEventBus);
        BlocksRegister.register(modEventBus);
        CreativeTabsRegister.register(modEventBus);
        EffectsRegister.register(modEventBus);

        MIACarvers.register(modEventBus);
        MIABiomeModifierConfig.register(modEventBus);

        SoundsRegister.SOUNDS.register(modEventBus);
        EntityRegister.ENTITIES.register(modEventBus);

        CurseConfigManager.loadConfig();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(Curse.EventHandler.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
        s.scheduleAtFixedRate(() -> {
            if (!Time.get()) Time.millis++;
        }, 1, 1, TimeUnit.MILLISECONDS);

        // MinecraftForge.EVENT_BUS.register(new HoleGenerator());
    }



    private void commonSetup(final FMLCommonSetupEvent event) {
        // 一些通用设置代码
        LOGGER.info("来自通用设置的问候");

        EntityRenderers.register(EntityRegister.flyingSwordEntity.get(), KnifeRenderer::new);

//        if (Config.logDirtBlock)
//            LOGGER.info("泥土方块 >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
//
//        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
//
//        Config.items.forEach((item) -> LOGGER.info("物品 >> {}", item.toString()));
    }

    // 使用 SubscribeEvent 并让事件总线发现带有 @SubscribeEvent 注解的方法来调用
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 当服务器启动时执行某些操作
        LOGGER.info("来自服务器启动的问候");
    }

    // 使用 EventBusSubscriber 自动注册类中所有带有 @SubscribeEvent 注解的静态方法
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 一些客户端设置代码
            LOGGER.info("来自客户端设置的问候");
            LOGGER.info("MINECRAFT 名称 >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}