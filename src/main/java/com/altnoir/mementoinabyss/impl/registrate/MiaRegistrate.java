package com.altnoir.mementoinabyss.impl.registrate;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class MiaRegistrate extends AbstractRegistrate<MiaRegistrate> {
    protected MiaRegistrate(String modId) {
        super(modId);
    }

    public static MiaRegistrate create(String modId) {
        return new MiaRegistrate(modId);
    }

    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, MiaRegistrate> creativeTab(Consumer<CreativeModeTab.Builder> config) {
        return creativeTab(self(), config);
    }

    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, MiaRegistrate> creativeTab(String name) {
        return creativeTab(self(), name);
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> creativeTab(P parent, Consumer<CreativeModeTab.Builder> config) {
        return creativeTab(parent, currentName(), config);
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> creativeTab(P parent, String name) {
        return creativeTab(parent, name, tab -> {
        });
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> creativeTab(P parent, String name, Consumer<CreativeModeTab.Builder> config) {
        return this.generic(parent, name, Registries.CREATIVE_MODE_TAB, () -> {
            var builder = CreativeModeTab.builder()
                    .icon(() -> getAll(Registries.ITEM).stream().findFirst().map(ItemEntry::cast).map(ItemEntry::asStack).orElse(new ItemStack(Items.AIR)))
                    .title(this.addLang("itemGroup", MementoInAbyss.asResource(name), MementoInAbyss.NAME + " : " + RegistrateLangProvider.toEnglishName(name)));
            config.accept(builder);
            return builder.build();
        });
    }
}
