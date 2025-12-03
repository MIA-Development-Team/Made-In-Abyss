package com.altnoir.mia.core.spawner.records;

import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.EntityType;

public class EntityTableInstance extends WeightedEntry.IntrusiveBase {
    private final EntityType<?> entityType;

    public EntityTableInstance(EntityType<?> entityType, int weight) {
        super(weight);
        this.entityType = entityType;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }
}
