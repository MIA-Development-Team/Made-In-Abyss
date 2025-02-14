package com.altnoir.mia;

import com.altnoir.mia.client.entity.KnifeEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegister {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "mia");
    public static final RegistryObject<EntityType<KnifeEntity>> flyingSwordEntity = ENTITIES.register("flying_sword", () -> EntityType.Builder.of(KnifeEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(25).build("flying_sword"));

}