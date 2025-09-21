package com.altnoir.mia.compat.kubejs;

import com.altnoir.mia.compat.kubejs.event.MiaSkillEvents;
import com.altnoir.mia.compat.kubejs.item.SkillBuilder;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import net.minecraft.core.registries.Registries;

public class MiaKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("SkillBuilder", SkillBuilder.class);
        bindings.add("MiaSkillEvents", MiaSkillEvents.class);
    }

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.of(Registries.ITEM, reg -> reg.add("skill", SkillBuilder.class, SkillBuilder::new));
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(MiaSkillEvents.GROUP);
    }
}