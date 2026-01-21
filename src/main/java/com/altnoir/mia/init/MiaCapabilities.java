package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.common.core.curse.ICurse;
import com.altnoir.mia.util.MiaUtil;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class MiaCapabilities {
    public static EntityCapability<ICurse, Void> CURSE =
            EntityCapability.createVoid(
                    MiaUtil.id(MIA.MOD_ID, "curse"),
                    ICurse.class);
}
