package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, MIA.MOD_ID);

    public static final Holder<Attribute> CRITICAL_HIT = ATTRIBUTES.register("generic.critical_hit", () ->
            new RangedAttribute("attribute.name.generic.critical_hit", 0.0, 0.0, 1024.0));
    public static final Holder<Attribute> CRITICAL_HIT_DAMAGE = ATTRIBUTES.register("generic.critical_hit_damage", () ->
            new RangedAttribute("attribute.name.generic.critical_hit_damage", 1.5, 0.0, 1024.0));

    public static void register(IEventBus bus) {
        ATTRIBUTES.register(bus);
    }
}
