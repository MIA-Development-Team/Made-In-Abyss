package com.altnoir.mia.client.event;

import com.altnoir.mia.client.gui.screens.inventory.tooltip.ClientArtifactBundleTooltip;
import com.altnoir.mia.component.ArtifactBundleInventoryComponent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

public class ClientTooltipComponentEvent {
    public static void onRegisterFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ArtifactBundleInventoryComponent.class, ClientArtifactBundleTooltip::new);
    }
}
