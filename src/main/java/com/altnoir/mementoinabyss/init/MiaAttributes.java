package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaAttributes {
    private static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, MementoInAbyss.ID);

    public static final Holder<Attribute> CRITICAL_HIT = ATTRIBUTES.register(
            "critical_hit",
            () -> new RangedAttribute(
                    "attribute.name.mementoinabyss.critical_hit",
                    0.0,
                    0.0,
                    1024.0
            ).setSyncable(true)
    );
    public static final Holder<Attribute> CRITICAL_HIT_DAMAGE = ATTRIBUTES.register(
            "critical_hit_damage",
            () -> new RangedAttribute(
                    "attribute.name.mementoinabyss.critical_hit_damage",
                    1.5,
                    0.0,
                    1024.0
            ).setSyncable(true)
    );

    public static void register(IEventBus bus) {
        ATTRIBUTES.register(bus);
    }

    public static void addEntityAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, CRITICAL_HIT);
        event.add(EntityType.PLAYER, CRITICAL_HIT_DAMAGE);
    }

    private MiaAttributes() {}
}
