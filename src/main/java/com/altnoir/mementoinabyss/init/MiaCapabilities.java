package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatResource;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import org.jetbrains.annotations.Nullable;

public final class MiaCapabilities {
    public static final class Heat {
        public static final BlockCapability<ResourceHandler<HeatResource>, @Nullable Direction>
                BLOCK =
                        BlockCapability.createSided(
                                MementoInAbyss.asResource("heat_handler"),
                                ResourceHandler.asClass());

        private Heat() {}
    }

    public static void register() {
        Objects.requireNonNull(Heat.BLOCK);
    }

    private MiaCapabilities() {}
}
