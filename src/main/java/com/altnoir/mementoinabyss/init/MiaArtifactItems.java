package com.altnoir.mementoinabyss.init;

import com.altnoir.abysslib.reginth.Reginth;
import com.altnoir.abysslib.reginth.util.entry.ItemEntry;
import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.artifact.ArtifactItem;
import com.altnoir.mementoinabyss.content.equipment.StarCompassItem;
import com.altnoir.mementoinabyss.impl.artifact.ArtifactAttribute;
import com.altnoir.mementoinabyss.impl.artifact.ArtifactGrade;
import com.altnoir.mementoinabyss.impl.artifact.ArtifactProfile;
import com.altnoir.mementoinabyss.impl.artifact.component.ArtifactAttributeComponent;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class MiaArtifactItems {
    private static final Reginth REGISTRATE = MementoInAbyss.registrate();

    static {
        REGISTRATE.defaultCreativeSection(MiaItemGroups.ARTIFACTS);
    }

    public static final ItemEntry<StarCompassItem> STAR_COMPASS =
            REGISTRATE.item("star_compass", StarCompassItem::new).register();

    public static final ItemEntry<Item> ARTIFACT_SHARD_LUNA =
            shard("artifact_shard_luna", MiaTags.Items.ARTIFACT_SHARDS_TIER_1.tag);
    public static final ItemEntry<Item> ARTIFACT_SHARD_TER =
            shard("artifact_shard_ter", MiaTags.Items.ARTIFACT_SHARDS_TIER_1.tag);
    public static final ItemEntry<Item> ARTIFACT_SHARD_CORE =
            shard("artifact_shard_core", MiaTags.Items.ARTIFACT_SHARDS_TIER_1.tag);
    public static final ItemEntry<Item> ARTIFACT_SHARD_MOUNTAIN =
            shard("artifact_shard_mountain", MiaTags.Items.ARTIFACT_SHARDS_TIER_1.tag);
    public static final ItemEntry<Item> ARTIFACT_SHARD_MOTH =
            shard("artifact_shard_moth", MiaTags.Items.ARTIFACT_SHARDS_TIER_2.tag);
    public static final ItemEntry<Item> ARTIFACT_SHARD_HALBERD =
            shard("artifact_shard_halberd", MiaTags.Items.ARTIFACT_SHARDS_TIER_2.tag);
    public static final ItemEntry<Item> ARTIFACT_SHARD_ELE =
            shard("artifact_shard_ele", MiaTags.Items.ARTIFACT_SHARDS_TIER_2.tag);
    public static final ItemEntry<Item> ARTIFACT_SHARD_CUB =
            shard("artifact_shard_cub", MiaTags.Items.ARTIFACT_SHARDS_TIER_2.tag);

    public static final ItemEntry<Item> ARTIFACT_INGOT_TIER_1 =
            ingot(
                    "artifact_ingot_tier_1",
                    "Tier 1 Artifact Ingot",
                    MiaTags.Items.ARTIFACT_SHARDS_TIER_1.tag);
    public static final ItemEntry<Item> ARTIFACT_INGOT_TIER_2 =
            ingot(
                    "artifact_ingot_tier_2",
                    "Tier 2 Artifact Ingot",
                    MiaTags.Items.ARTIFACT_SHARDS_TIER_2.tag);

    public static final ItemEntry<ArtifactItem> HEALTH_JUNKIE =
            REGISTRATE
                    .item(
                            "health_junkie",
                            properties ->
                                    ArtifactItem.builder(
                                                    properties,
                                                    ArtifactProfile.of(ArtifactGrade.C, 1))
                                            .enhanceable()
                                            .component(
                                                    new ArtifactAttributeComponent(
                                                            new ArtifactAttribute(
                                                                    Attributes.KNOCKBACK_RESISTANCE,
                                                                    MementoInAbyss.asResource(
                                                                            "artifact/health_junkie/knockback_resistance"),
                                                                    0.2,
                                                                    AttributeModifier.Operation
                                                                            .ADD_MULTIPLIED_BASE),
                                                            new ArtifactAttribute(
                                                                    Attributes.MAX_HEALTH,
                                                                    MementoInAbyss.asResource(
                                                                            "artifact/health_junkie/max_health"),
                                                                    2.0,
                                                                    AttributeModifier.Operation
                                                                            .ADD_VALUE)))
                                            .build())
                    .lang("Health Junkie")
                    .register();

    public static final ItemEntry<ArtifactItem> TEST_ARTIFACT_1 =
            REGISTRATE
                    .item(
                            "test_artifact_1",
                            properties ->
                                    ArtifactItem.builder(
                                                    properties,
                                                    ArtifactProfile.of(ArtifactGrade.D, 1))
                                            .build())
                    .lang("Test Artifact I")
                    .register();

    public static final ItemEntry<ArtifactItem> TEST_ARTIFACT_2 =
            REGISTRATE
                    .item(
                            "test_artifact_2",
                            properties ->
                                    ArtifactItem.builder(
                                                    properties,
                                                    ArtifactProfile.of(ArtifactGrade.C, 2))
                                            .enhanceable()
                                            .build())
                    .lang("Test Artifact II")
                    .register();

    public static final ItemEntry<ArtifactItem> TEST_ARTIFACT_3 =
            REGISTRATE
                    .item(
                            "test_artifact_3",
                            properties ->
                                    ArtifactItem.builder(
                                                    properties,
                                                    ArtifactProfile.of(ArtifactGrade.S, 4))
                                            .enhanceable()
                                            .build())
                    .lang("Test Artifact III")
                    .register();

    private static ItemEntry<Item> shard(String name, TagKey<Item> tier) {
        return REGISTRATE
                .item(name, Item::new)
                .tag(tier)
                .properties(properties -> properties.rarity(Rarity.RARE))
                .lang("Artifact Shard")
                .register();
    }

    private static ItemEntry<Item> ingot(String name, String displayName, TagKey<Item> shards) {
        return REGISTRATE
                .item(name, Item::new)
                .lang(displayName)
                .recipe(
                        (ctx, provider) ->
                                provider.shaped(RecipeCategory.MISC, ctx.get())
                                        .define('#', shards)
                                        .pattern("##")
                                        .pattern("##")
                                        .unlockedBy("has_shard", provider.has(shards))
                                        .save(provider))
                .register();
    }

    public static void register() {}
}
