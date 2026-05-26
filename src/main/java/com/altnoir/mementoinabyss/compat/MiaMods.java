package com.altnoir.mementoinabyss.compat;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public enum MiaMods {
    PONDER;

    @Getter
    private final String id;
    @Getter
    private final boolean isLoaded;

    MiaMods() {
        id = name().toLowerCase(Locale.ROOT);
        isLoaded = FMLLoader.getCurrent().getLoadingModList().getModFileById(id) != null;
    }

    public Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(id, path);
    }

    public @Nullable Block getBlock(String id) {
        return BuiltInRegistries.BLOCK.get(asResource(id))
                .map(Holder.Reference::value)
                .orElse(null);
    }

    public @Nullable Item getItem(String id) {
        return BuiltInRegistries.ITEM.get(asResource(id))
                .map(Holder.Reference::value)
                .orElse(null);
    }

    public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
        if (isLoaded())
            return Optional.of(toRun.get().get());
        return Optional.empty();
    }

    public void executeIfInstalled(Supplier<Runnable> toExecute) {
        if (isLoaded()) {
            toExecute.get().run();
        }
    }
}
