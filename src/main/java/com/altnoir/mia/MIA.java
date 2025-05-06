package com.altnoir.mia;

import com.altnoir.mia.content.ability.Curse;
import com.altnoir.mia.content.ability.CurseConfigManager;
import com.altnoir.mia.content.block.BlocksRegister;
import com.altnoir.mia.content.effect.EffectsRegister;
import com.altnoir.mia.content.entity.BlockEntityRegister;
import com.altnoir.mia.content.entity.EntityRegister;
import com.altnoir.mia.content.items.ItemsRegister;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
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

@Mod(MIA.MODID)
public class MIA {
    public static final String MODID = "mia";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MIA() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册 commonSetup 方法以进行模组加载
        modEventBus.addListener(this::commonSetup);

        ItemsRegister.register(modEventBus);
        BlocksRegister.register(modEventBus);
        BlockEntityRegister.register(modEventBus);
        CreativeTabsRegister.register(modEventBus);
        EffectsRegister.register(modEventBus);

        EntityRegister.ENTITIES.register(modEventBus);

        CurseConfigManager.loadConfig();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(Curse.EventHandler.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();

        // MinecraftForge.EVENT_BUS.register(new HoleGenerator());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 一些通用设置代码
        LOGGER.info("来自通用设置的问候");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 当服务器启动时执行某些操作
        LOGGER.info("来自服务器启动的问候");
    }

    // 使用 EventBusSubscriber 自动注册类中所有带有 @SubscribeEvent 注解的静态方法
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 一些客户端设置代码
            LOGGER.info("来自客户端设置的问候");
            LOGGER.info("MINECRAFT 名称 >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}