package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.item.whistle.WhistleItem;
import com.altnoir.mementoinabyss.impl.whistle.grid.WhistleGrid;
import com.altnoir.mementoinabyss.impl.whistle.grid.SkillShape;
import com.altnoir.mementoinabyss.impl.whistle.fragment.amplifier.WhistleAmplifierDefinition;
import com.altnoir.mementoinabyss.content.item.whistle.fragment.amplifier.WhistleAmplifierItem;
import com.altnoir.mementoinabyss.content.item.whistle.skill.EchoReedItem;
import com.altnoir.mementoinabyss.impl.whistle.skill.WhistleSkillCategory;
import com.altnoir.mementoinabyss.impl.whistle.skill.WhistleSkillDefinition;
import com.altnoir.mementoinabyss.impl.whistle.skill.WhistleNote;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;

public final class MiaWhistleItems {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    static {
        REGISTRATE.defaultCreativeSection(MiaItemGroups.ARTIFACTS);
    }

    public static final ItemEntry<WhistleItem> RED_WHISTLE = REGISTRATE
            .item("red_whistle", properties -> new WhistleItem(properties, WhistleGrid.RED_WHISTLE))
            .register();

    public static final ItemEntry<EchoReedItem> ECHO_REED = REGISTRATE
            .item("echo_reed", properties -> new EchoReedItem(
                    properties,
                    new WhistleSkillDefinition(
                            SkillShape.of(
                                    0, 0,
                                    0, 1,
                                    0, 2,
                                    1, 2
                            ),
                            WhistleSkillCategory.ECHO,
                            true,
                            java.util.List.of(
                                    WhistleNote.UP,
                                    WhistleNote.RIGHT,
                                    WhistleNote.DOWN,
                                    WhistleNote.LEFT
                            ),
                            200
                    )
            ))
            .model(() -> (context, provider) -> {
                var model = ModelTemplates.FLAT_ITEM.create(
                        context.get(),
                        TextureMapping.layer0(new Material(
                                provider.modLoc("item/skill/artifact_haste")
                        )),
                        provider.modelOutput
                );
                provider.createWithExistingModel(context.getEntry(), model);
            })
            .register();

    public static final ItemEntry<WhistleAmplifierItem> RESONANCE_PRESSURE_FRAGMENT = REGISTRATE
            .item("resonance_pressure_fragment", properties -> new WhistleAmplifierItem(
                    properties,
                    new WhistleAmplifierDefinition(
                            SkillShape.of(0, 0, 0, 1),
                            false,
                            1.35F,
                            1.25F
                    )
            ))
            .model(() -> (context, provider) -> {
                var model = ModelTemplates.FLAT_ITEM.create(
                        context.get(),
                        TextureMapping.layer0(new Material(
                                provider.modLoc("item/skill/null")
                        )),
                        provider.modelOutput
                );
                provider.createWithExistingModel(context.getEntry(), model);
            })
            .register();

    public static void register() {}

    private MiaWhistleItems() {}
}
