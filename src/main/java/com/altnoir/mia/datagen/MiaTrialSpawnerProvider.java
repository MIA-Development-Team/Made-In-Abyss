package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.common.core.spawner.AbyssTrialSpawnerDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MiaTrialSpawnerProvider extends AbyssTrialSpawnerDataProvider {
    public MiaTrialSpawnerProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(MIA.MOD_ID, output, registries);
    }

    @Override
    protected void addSpawners() {
        add(
            ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "example_zombie"),
            createPattern(
                List.of(
                    entity(EntityType.ZOMBIE, 10),
                    entity(EntityType.HUSK, 5),
                    entity(EntityType.DROWNED, 3)
                ),
                List.of(
                    loot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_COMMON, 10),
                    loot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_RARE, 3),
                    loot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_UNIQUE, 1)
                ),
                3,
                2,
                1,
                4
            )
        );

        add(
            ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "example_skeleton"),
            createPattern(
                List.of(
                    entity(EntityType.SKELETON, 10),
                    entity(EntityType.STRAY, 5),
                    entity(EntityType.BOGGED, 2)
                ),
                List.of(
                    loot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_COMMON, 10),
                    loot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_RARE, 5)
                ),
                2,
                1,
                1,
                5
            )
        );

        add(
            ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "example_spider"),
            createPattern(
                List.of(
                    entity(EntityType.SPIDER, 10),
                    entity(EntityType.CAVE_SPIDER, 5)
                ),
                List.of(
                    loot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_COMMON, 10)
                ),
                4,
                2,
                2,
                6
            )
        );

        add(
            ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "example_boss"),
            createPattern(
                List.of(
                    entity(EntityType.WARDEN, 1)
                ),
                List.of(
                    loot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_UNIQUE, 10),
                    loot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE, 5)
                ),
                1,
                0,
                1,
                8
            )
        );
        
        add(
            ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "example_equipped_zombie"),
            createPattern(
                List.of(
                    entityBuilder(EntityType.ZOMBIE, 10)
                        .mainHand(Items.DIAMOND_SWORD)
                        .head(Items.IRON_HELMET)
                        .chest(Items.IRON_CHESTPLATE)
                        .permanentEffect(MobEffects.FIRE_RESISTANCE)
                        .multiplyHealth(MIA.MOD_ID, 1.5)
                        .build(),
                    entityBuilder(EntityType.SKELETON, 5)
                        .mainHand(Items.BOW)
                        .head(Items.CHAINMAIL_HELMET)
                        .effect(MobEffects.MOVEMENT_SPEED, -1, 1)
                        .addDamage(MIA.MOD_ID, 2.0)
                        .build()
                ),
                List.of(
                    loot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_RARE, 10),
                    loot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_UNIQUE, 3)
                ),
                2,
                1,
                1,
                5
            )
        );
    }
}
