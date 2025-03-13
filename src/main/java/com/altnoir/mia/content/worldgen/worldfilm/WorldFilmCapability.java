package com.altnoir.mia.content.worldgen.worldfilm;

import com.altnoir.mia.MIA;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = MIA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldFilmCapability {
    public static final Capability<WorldFilmData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void attachToChunk(AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(
                new ResourceLocation(MIA.MOD_ID, "world_film_data"),
                new ICapabilitySerializable<CompoundTag>() {
                    private final WorldFilmData instance = new WorldFilmData();
                    private final LazyOptional<WorldFilmData> holder = LazyOptional.of(() -> instance);

                    @Override
                    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                        return CAPABILITY.orEmpty(cap, holder);
                    }

                    @Override
                    public CompoundTag serializeNBT() {
                        return instance.serializeNBT();
                    }

                    @Override
                    public void deserializeNBT(CompoundTag nbt) {
                        instance.deserializeNBT(nbt);
                    }
                }
        );
    }
}
