package com.altnoir.mia.common.core.spawner;

import com.altnoir.mia.common.core.spawner.records.AbyssTrialSpawnerPattern;
import com.altnoir.mia.common.core.spawner.records.EntityTableInstance;
import com.altnoir.mia.common.core.spawner.records.LootTableInstance;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbyssTrialSpawnerDataProvider implements DataProvider {
    private final String modId;
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final List<SpawnerDefinition> definitions = new ArrayList<>();

    public AbyssTrialSpawnerDataProvider(String modId, PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.modId = modId;
        this.output = output;
        this.registries = registries;
    }

    protected abstract void addSpawners();

    protected void add(ResourceLocation id, AbyssTrialSpawnerPattern pattern) {
        definitions.add(new SpawnerDefinition(id, pattern));
    }

    protected AbyssTrialSpawnerPattern createPattern(
            List<EntityTableInstance> entities,
            List<LootTableInstance> loots,
            int baseMobs,
            int mobsPerPlayer,
            int spawnPerTick,
            int spawnRange) {
        var entityTables = WeightedRandomList.create(entities);
        var lootTables = WeightedRandomList.create(loots);
        return new AbyssTrialSpawnerPattern(lootTables, entityTables, baseMobs, mobsPerPlayer, spawnPerTick, spawnRange);
    }

    protected EntityTableInstance entity(EntityType<?> entityType, int weight) {
        return new EntityTableInstance(entityType, weight);
    }

    protected EntityTableBuilder entityBuilder(EntityType<?> entityType, int weight) {
        return new EntityTableBuilder(entityType, weight);
    }

    public static class EntityTableBuilder {
        private final EntityType<?> entityType;
        private final int weight;
        private final Map<EquipmentSlot, ItemStack> equipment = new HashMap<>();
        private final List<MobEffectInstance> effects = new ArrayList<>();
        private final Map<Holder<Attribute>, AttributeModifier> attributeModifiers = new HashMap<>();

        public EntityTableBuilder(EntityType<?> entityType, int weight) {
            this.entityType = entityType;
            this.weight = weight;
        }

        public EntityTableBuilder equipment(EquipmentSlot slot, Item item) {
            this.equipment.put(slot, new ItemStack(item));
            return this;
        }

        public EntityTableBuilder equipment(EquipmentSlot slot, ItemStack stack) {
            this.equipment.put(slot, stack);
            return this;
        }

        public EntityTableBuilder mainHand(Item item) {
            return equipment(EquipmentSlot.MAINHAND, item);
        }

        public EntityTableBuilder offHand(Item item) {
            return equipment(EquipmentSlot.OFFHAND, item);
        }

        public EntityTableBuilder head(Item item) {
            return equipment(EquipmentSlot.HEAD, item);
        }

        public EntityTableBuilder chest(Item item) {
            return equipment(EquipmentSlot.CHEST, item);
        }

        public EntityTableBuilder legs(Item item) {
            return equipment(EquipmentSlot.LEGS, item);
        }

        public EntityTableBuilder feet(Item item) {
            return equipment(EquipmentSlot.FEET, item);
        }

        public EntityTableBuilder effect(Holder<MobEffect> effect, int duration, int amplifier) {
            this.effects.add(new MobEffectInstance(effect, duration, amplifier));
            return this;
        }

        public EntityTableBuilder effect(Holder<MobEffect> effect, int duration) {
            return effect(effect, duration, 0);
        }

        public EntityTableBuilder permanentEffect(Holder<MobEffect> effect, int amplifier) {
            return effect(effect, -1, amplifier);
        }

        public EntityTableBuilder permanentEffect(Holder<MobEffect> effect) {
            return permanentEffect(effect, 0);
        }

        public EntityTableBuilder attribute(Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
            this.attributeModifiers.put(attribute, new AttributeModifier(id, amount, operation));
            return this;
        }

        public EntityTableBuilder addHealth(String modId, double amount) {
            return attribute(Attributes.MAX_HEALTH, ResourceLocation.fromNamespaceAndPath(modId, "trial_spawner_health"), amount, AttributeModifier.Operation.ADD_VALUE);
        }

        public EntityTableBuilder multiplyHealth(String modId, double multiplier) {
            return attribute(Attributes.MAX_HEALTH, ResourceLocation.fromNamespaceAndPath(modId, "trial_spawner_health"), multiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }

        public EntityTableBuilder addDamage(String modId, double amount) {
            return attribute(Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath(modId, "trial_spawner_damage"), amount, AttributeModifier.Operation.ADD_VALUE);
        }

        public EntityTableBuilder multiplyDamage(String modId, double multiplier) {
            return attribute(Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath(modId, "trial_spawner_damage"), multiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }

        public EntityTableBuilder addSpeed(String modId, double amount) {
            return attribute(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(modId, "trial_spawner_speed"), amount, AttributeModifier.Operation.ADD_VALUE);
        }

        public EntityTableBuilder multiplySpeed(String modId, double multiplier) {
            return attribute(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(modId, "trial_spawner_speed"), multiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }

        public EntityTableInstance build() {
            return new EntityTableInstance(entityType, weight, equipment, effects, attributeModifiers);
        }
    }

    protected LootTableInstance loot(ResourceKey<LootTable> lootTableKey, int weight) {
        return new LootTableInstance(lootTableKey, weight);
    }

    protected LootTableInstance loot(ResourceLocation lootTableId, int weight) {
        return new LootTableInstance(ResourceKey.create(Registries.LOOT_TABLE, lootTableId), weight);
    }

    protected LootTableInstance loot(String namespace, String path, int weight) {
        return new LootTableInstance(
            ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(namespace, path)), 
            weight
        );
    }

    private void validate(HolderLookup.Provider registries, ResourceLocation id, AbyssTrialSpawnerPattern pattern) {
        for (var entity : pattern.entityTables().unwrap()) {
            var entityType = entity.getEntityType();
            var entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        }

        if (pattern.baseMobs() < 0) {
            throw new IllegalArgumentException("baseMobs must be >= 0 in spawner " + id);
        }
        if (pattern.mobsPerPlayer() < 0) {
            throw new IllegalArgumentException("mobsPerPlayer must be >= 0 in spawner " + id);
        }
        if (pattern.spawnPerTick() <= 0) {
            throw new IllegalArgumentException("spawnPerTick must be > 0 in spawner " + id);
        }
        if (pattern.spawnRange() <= 0) {
            throw new IllegalArgumentException("spawnRange must be > 0 in spawner " + id);
        }
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        return registries.thenCompose(lookup -> {
            addSpawners();

            var basePath = output.getOutputFolder().resolve("data/" + modId + "/mia/trial_spawner");
            var futures = new ArrayList<CompletableFuture<?>>();

            for (var def : definitions) {
                validate(lookup, def.id, def.pattern);

                var root = new JsonObject();

                if (!def.pattern.entityTables().unwrap().isEmpty()) {
                    var entityTablesArray = new JsonArray();
                    for (var entityEntry : def.pattern.entityTables().unwrap()) {
                        var entityJson = new JsonObject();
                        var entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityEntry.getEntityType());
                        entityJson.addProperty("entity", entityId.toString());
                        entityJson.addProperty("weight", entityEntry.getWeight().asInt());
                        
                        if (entityEntry.hasEquipment()) {
                            var equipmentJson = new JsonObject();
                            for (var equipEntry : entityEntry.getEquipment().entrySet()) {
                                var itemId = BuiltInRegistries.ITEM.getKey(equipEntry.getValue().getItem());
                                equipmentJson.addProperty(equipEntry.getKey().getName(), itemId.toString());
                            }
                            entityJson.add("equipment", equipmentJson);
                        }
                        
                        if (entityEntry.hasEffects()) {
                            var effectsArray = new JsonArray();
                            for (var effect : entityEntry.getEffects()) {
                                var effectJson = new JsonObject();
                                var effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
                                effectJson.addProperty("effect", effectId.toString());
                                effectJson.addProperty("duration", effect.getDuration());
                                effectJson.addProperty("amplifier", effect.getAmplifier());
                                if (effect.isAmbient()) effectJson.addProperty("ambient", true);
                                if (!effect.isVisible()) effectJson.addProperty("visible", false);
                                if (!effect.showIcon()) effectJson.addProperty("show_icon", false);
                                effectsArray.add(effectJson);
                            }
                            entityJson.add("effects", effectsArray);
                        }
                        
                        if (entityEntry.hasAttributeModifiers()) {
                            var modifiersArray = new JsonArray();
                            for (var modEntry : entityEntry.getAttributeModifiers().entrySet()) {
                                var modJson = new JsonObject();
                                var attrId = BuiltInRegistries.ATTRIBUTE.getKey(modEntry.getKey().value());
                                modJson.addProperty("attribute", attrId.toString());
                                modJson.addProperty("id", modEntry.getValue().id().toString());
                                modJson.addProperty("amount", modEntry.getValue().amount());
                                modJson.addProperty("operation", modEntry.getValue().operation().name().toLowerCase());
                                modifiersArray.add(modJson);
                            }
                            entityJson.add("attribute_modifiers", modifiersArray);
                        }
                        
                        entityTablesArray.add(entityJson);
                    }
                    root.add("entity_tables", entityTablesArray);
                }

                if (!def.pattern.lootTables().unwrap().isEmpty()) {
                    var lootTablesArray = new JsonArray();
                    for (var lootEntry : def.pattern.lootTables().unwrap()) {
                        var lootJson = new JsonObject();
                        lootJson.addProperty("loot_table", lootEntry.getLootTableKey().location().toString());
                        lootJson.addProperty("weight", lootEntry.getWeight().asInt());
                        lootTablesArray.add(lootJson);
                    }
                    root.add("loot_tables", lootTablesArray);
                }

                root.addProperty("base_mobs", def.pattern.baseMobs());
                root.addProperty("mobs_per_player", def.pattern.mobsPerPlayer());
                root.addProperty("spawn_per_tick", def.pattern.spawnPerTick());
                root.addProperty("spawn_range", def.pattern.spawnRange());

                var fileName = def.id.getPath() + ".json";
                var path = basePath.resolve(fileName);

                var future = DataProvider.saveStable(cachedOutput, root, path);
                futures.add(future);
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }

    @Override
    public @NotNull String getName() {
        return "Abyss Trial Spawner data for " + this.modId;
    }

    private record SpawnerDefinition(ResourceLocation id, AbyssTrialSpawnerPattern pattern) {
    }
}
