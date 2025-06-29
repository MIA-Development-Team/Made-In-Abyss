package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.core.curse.ICurse;
import com.altnoir.mia.util.MiaPort;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class MiaCapabilities {
    public static EntityCapability<ICurse, Void> CURSE =
            EntityCapability.createVoid(
                    MiaPort.id(MIA.MOD_ID, "curse"),
                    ICurse.class);
}
