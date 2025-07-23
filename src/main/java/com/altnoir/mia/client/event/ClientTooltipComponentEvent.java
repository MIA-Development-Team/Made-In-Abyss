package com.altnoir.mia.client.event;

import com.altnoir.mia.client.gui.screens.inventory.tooltip.ClientWhistleTooltip;
import com.altnoir.mia.component.WhistleInventoryComponent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

public class ClientTooltipComponentEvent {
    public static void onRegisterFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(WhistleInventoryComponent.class, ClientWhistleTooltip::new);
    }
}
