package com.altnoir.mementoinabyss.content.returnchest.client;

import com.altnoir.abysslib.reginth.providers.DataGenContext;
import com.altnoir.abysslib.reginth.providers.generators.ReginthBlockModelGenerator;
import com.altnoir.abysslib.reginth.providers.generators.ReginthItemModelGenerator;
import com.altnoir.abysslib.reginth.util.nullness.NonNullBiConsumer;
import com.altnoir.mementoinabyss.content.returnchest.BalloonItem;
import com.altnoir.mementoinabyss.content.returnchest.ReturnChestBlock;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
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
            Item item = context.get();
            var model =
                    ModelTemplates.THREE_LAYERED_ITEM.create(
                            item,
                            TextureMapping.layered(
                                    TextureMapping.getItemTexture(item),
                                    TextureMapping.getItemTexture(item, "_sheen"),
                                    TextureMapping.getItemTexture(item, "_string")),
                            generator.modelOutput);
            generator.itemModelOutput.accept(
                    item,
                    ItemModelUtils.tintedModel(
                            model, new Dye(DyeColor.WHITE.getTextureDiffuseColor())));
        };
    }

    private ReturnStorageModels() {}
}
