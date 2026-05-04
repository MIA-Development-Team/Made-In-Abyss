package com.altnoir.mementoinabyss.impl.event;

import com.altnoir.mementoinabyss.impl.curse.CurseManager;
import com.altnoir.mementoinabyss.impl.tillable.TillEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class CommonEvents {
    @SubscribeEvent
    public static void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        var server = event.getServer();
        CurseManager.init(server.registryAccess());
    }

    @SubscribeEvent
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        TillEvent.onRightClickBlock(event);
    }

    @EventBusSubscriber
    public static class ModBusEvents {
    }
}
