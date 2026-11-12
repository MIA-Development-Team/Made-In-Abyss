package com.altnoir.mementoinabyss.content.returnchest.client;

import com.altnoir.abysslib.reginth.providers.DataGenContext;
import com.altnoir.abysslib.reginth.providers.generators.ReginthBlockModelGenerator;
import com.altnoir.abysslib.reginth.providers.generators.ReginthItemModelGenerator;
import com.altnoir.abysslib.reginth.util.nullness.NonNullBiConsumer;
import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.returnchest.BalloonItem;
import com.altnoir.mementoinabyss.content.returnchest.ReturnChestBlock;
import java.util.ArrayList;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class ReturnStorageModels {
    public static NonNullBiConsumer<
                    DataGenContext<Block, ReturnChestBlock>, ReginthBlockModelGenerator>
            chest() {
        return (context, generator) ->
                generator.createParticleOnlyBlock(context.get(), Blocks.OBSIDIAN);
    }

    public static NonNullBiConsumer<DataGenContext<Item, BlockItem>, ReginthItemModelGenerator>
            chestItem() {
        return (context, generator) -> {
            var item = context.get();
            var model =
                    ModelTemplates.CHEST_INVENTORY.create(
                            item, TextureMapping.particle(Blocks.OBSIDIAN), generator.modelOutput);
            generator.itemModelOutput.accept(
                    item,
                    ItemModelUtils.specialModel(
                            model,
                            new ChestSpecialRenderer.Unbaked(ChestSpecialRenderer.ENDER_CHEST)));
        };
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
                cases.add(ItemModelUtils.when(
                        new DyedItemColor(BalloonItem.rgb(color)), ItemModelUtils.plainModel(model)));
            }
            generator.itemModelOutput.accept(
                    item,
                    ItemModelUtils.select(
                            new ComponentContents<>(DataComponents.DYED_COLOR),
                            ItemModelUtils.plainModel(MementoInAbyss.asResource("item/balloon_white")),
                            cases));
        };
    }

    private ReturnStorageModels() {}
}
