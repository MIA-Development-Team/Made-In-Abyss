package com.altnoir.mementoinabyss.content.returnchest.client;

import com.altnoir.abysslib.reginth.providers.DataGenContext;
import com.altnoir.abysslib.reginth.providers.generators.ReginthBlockModelGenerator;
import com.altnoir.abysslib.reginth.providers.generators.ReginthItemModelGenerator;
import com.altnoir.abysslib.reginth.util.nullness.NonNullBiConsumer;
import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.returnchest.BalloonItem;
import com.altnoir.mementoinabyss.content.returnchest.ReturnChestBlock;
import com.altnoir.mementoinabyss.content.returnchest.ReturnChestItem;
import com.altnoir.mementoinabyss.content.returnchest.ReturnChests;
import java.util.ArrayList;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;

public final class ReturnStorageModels {
    private static final Identifier RETURN_CHEST_MODEL =
            MementoInAbyss.asResource("block/return_chest");
    private static final Identifier RETURN_CHEST_TINTED_MODEL =
            MementoInAbyss.asResource("block/return_chest_tinted");

    public static NonNullBiConsumer<
                    DataGenContext<Block, ReturnChestBlock>, ReginthBlockModelGenerator>
            chest() {
        return (context, generator) -> {
            var block = context.get();
            var shell =
                    ModelTemplates.PARTICLE_ONLY.createWithSuffix(
                            block,
                            "_shell",
                            TextureMapping.particle(
                                    new Material(
                                            MementoInAbyss.asResource(
                                                    "block/model/return_chest_particle"))),
                            generator.modelOutput);
            var variants =
                    PropertyDispatch.initial(ReturnChestBlock.PART, ReturnChestBlock.TINT)
                            .generate(
                                    (part, tint) -> {
                                        if (!part.isCore()) {
                                            return BlockModelGenerators.plainVariant(shell);
                                        }
                                        return BlockModelGenerators.plainVariant(
                                                ReturnChests.hasTintOverlay(tint)
                                                        ? RETURN_CHEST_TINTED_MODEL
                                                        : RETURN_CHEST_MODEL);
                                    });
            generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(variants));
        };
    }

    public static NonNullBiConsumer<
                    DataGenContext<Item, ReturnChestItem>, ReginthItemModelGenerator>
            chestItem() {
        return (context, generator) ->
                generator.itemModelOutput.accept(
                        context.get(), ItemModelUtils.plainModel(RETURN_CHEST_MODEL));
    }

    public static NonNullBiConsumer<DataGenContext<Item, BalloonItem>, ReginthItemModelGenerator>
            balloon() {
        return (context, generator) -> {
            var item = context.get();
            var cases = new ArrayList<SelectItemModel.SwitchCase<DyedItemColor>>();
            for (DyeColor color : DyeColor.VALUES) {
                var model = MementoInAbyss.asResource("item/balloon_" + color.getName());
                if (color == DyeColor.WHITE) {
                    ModelTemplates.FLAT_ITEM.create(
                            model,
                            TextureMapping.layer0(TextureMapping.getItemTexture(item)),
                            generator.modelOutput);
                }
                cases.add(
                        ItemModelUtils.when(
                                new DyedItemColor(BalloonItem.rgb(color)),
                                ItemModelUtils.plainModel(model)));
            }
            generator.itemModelOutput.accept(
                    item,
                    ItemModelUtils.select(
                            new ComponentContents<>(DataComponents.DYED_COLOR),
                            ItemModelUtils.plainModel(
                                    MementoInAbyss.asResource("item/balloon_white")),
                            cases));
        };
    }

    private ReturnStorageModels() {}
}
