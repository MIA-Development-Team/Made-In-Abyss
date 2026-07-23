package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.artifact.ArtifactAttribute;
import com.altnoir.mementoinabyss.content.artifact.ArtifactGrade;
import com.altnoir.mementoinabyss.content.artifact.ArtifactItem;
import com.altnoir.mementoinabyss.content.artifact.ArtifactProfile;
import com.altnoir.mementoinabyss.content.artifact.component.ArtifactAttributeComponent;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class MiaArtifactItems {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    static {
        REGISTRATE.defaultCreativeTab(MiaItemGroups.ARTIFACT.getKey());
    }

    public static final ItemEntry<ArtifactItem> TEST_ARTIFACT_1 = REGISTRATE
            .item("test_artifact_1", properties -> ArtifactItem
                    .builder(properties, ArtifactProfile.of(ArtifactGrade.D, 1))
                    .build())
            .lang("Test Artifact I")
            .register();

    public static final ItemEntry<ArtifactItem> TEST_ARTIFACT_2 = REGISTRATE
            .item("test_artifact_2", properties -> ArtifactItem
                    .builder(properties, ArtifactProfile.of(ArtifactGrade.C, 2))
                    .enhanceable()
                    .build())
            .lang("Test Artifact II")
            .register();

    public static final ItemEntry<ArtifactItem> TEST_ARTIFACT_3 = REGISTRATE
            .item("test_artifact_3", properties -> ArtifactItem
                    .builder(properties, ArtifactProfile.of(ArtifactGrade.S, 4))
                    .enhanceable()
                    .build())
            .lang("Test Artifact III")
            .register();

    public static final ItemEntry<ArtifactItem> HEALTH_JUNKIE = REGISTRATE
            .item("health_junkie", properties -> ArtifactItem
                    .builder(properties, ArtifactProfile.of(ArtifactGrade.C, 1))
                    .enhanceable()
                    .component(new ArtifactAttributeComponent(
                            new ArtifactAttribute(
                                    Attributes.KNOCKBACK_RESISTANCE,
                                    MementoInAbyss.asResource("artifact/health_junkie/knockback_resistance"),
                                    0.2,
                                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            ),
                            new ArtifactAttribute(
                                    Attributes.MAX_HEALTH,
                                    MementoInAbyss.asResource("artifact/health_junkie/max_health"),
                                    2.0,
                                    AttributeModifier.Operation.ADD_VALUE
                            )
                    ))
                    .build())
            .lang("Health Junkie")
            .register();

    public static void register() {}
}
