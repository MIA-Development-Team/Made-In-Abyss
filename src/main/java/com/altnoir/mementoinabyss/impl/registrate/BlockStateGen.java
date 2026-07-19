package com.altnoir.mementoinabyss.impl.registrate;

import com.altnoir.mementoinabyss.content.block.cover_grass.CoverGrassBlock;
import com.altnoir.mementoinabyss.content.block.column.ColumnBlock;
import com.altnoir.mementoinabyss.content.block.column.ColumnSide;
import com.altnoir.mementoinabyss.content.block.plant.DoubleBerryBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jspecify.annotations.Nullable;

import java.util.Optional;


public class BlockStateGen {
    public static <B extends CoverGrassBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> coverGrass() {
        return (ctx, prov) -> {
            var block = ctx.getEntry();
            var model = ModelTemplates.CUBE_BOTTOM_TOP.create(
                    block,
                    sideBottomTop(
                            prov.modBlockTexture(ctx.getName() + "_side"),
                            prov.blockTexture(block.defaultBlock),
                            prov.modBlockTexture("abyss_grass_block_top")),
                    prov.modelOutput);

            var variants = BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(model));
            prov.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variants));
            prov.registerSimpleItemModel(block, model);
        };
    }

    public static <B extends RotatedPillarBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> variantAxisBlock(
            @Nullable BlockEntry<? extends Block> end, int variants, Optional<int[]> optionalWeights) {
        if (variants < 1) {
            throw new IllegalArgumentException("variants must be positive");
        }
        optionalWeights.ifPresent(weights -> {
            if (weights.length != variants) {
                throw new IllegalArgumentException("weights length must match variants");
            }
            for (int weight : weights) {
                if (weight < 1) {
                    throw new IllegalArgumentException("weights must be positive");
                }
            }
        });

        return (ctx, prov) -> {
            var weights = optionalWeights.orElse(null);
            var blockPath = ctx.getName();
            WeightedList.Builder<Variant> verticalBuilder = WeightedList.builder();
            WeightedList.Builder<Variant> horizontalBuilder = WeightedList.builder();

            for (int i = 0; i < variants; i++) {
                // Wood blocks have bark on every face and reuse the corresponding log
                // side variant. Logs use their own side variants and a shared top texture.
                var sideTexture = (end != null)
                        ? prov.blockTexture(end.get(), Integer.toString(i))
                        : prov.modBlockTexture(blockPath + i);
                var endTexture = (end != null)
                        ? sideTexture
                        : prov.modBlockTexture(blockPath + "_top");

                var verticalModel = ModelTemplates.CUBE_COLUMN.create(
                        prov.modLoc("block/" + blockPath + i),
                        TextureMapping.column(sideTexture, endTexture),
                        prov.modelOutput
                );
                var horizontalModel = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(
                        prov.modLoc("block/" + blockPath + "_horizontal" + i),
                        TextureMapping.column(sideTexture, endTexture),
                        prov.modelOutput
                );

                int weight = (weights != null) ? weights[i] : 1;
                verticalBuilder.add(BlockModelGenerators.plainModel(verticalModel), weight);
                horizontalBuilder.add(BlockModelGenerators.plainModel(horizontalModel), weight);
            }

            prov.generateAxisBlock(ctx.get(), new MultiVariant(verticalBuilder.build()), new MultiVariant(horizontalBuilder.build()));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, ColumnBlock>, RegistrateBlockModelGenerator> column(
            BlockEntry<? extends Block> pillar, BlockEntry<? extends Block> decoration) {
        return (ctx, prov) -> {
            var side = prov.blockTexture(pillar.get());
            var top = prov.blockTexture(pillar.get(), "_top");
            var dec = prov.blockTexture(decoration.get());
            var decSlot = TextureSlot.create("dec");
            var baseName = "block/" + ctx.getName();

            var single = prov.getBuilder()
                    .parent(prov.modLoc("block/template/column"))
                    .texture(TextureSlot.SIDE, side)
                    .texture(TextureSlot.TOP, top)
                    .texture(decSlot, dec)
                    .build(prov.modLoc(baseName));
            var bottom = prov.getBuilder()
                    .parent(prov.modLoc("block/template/column_bottom"))
                    .texture(TextureSlot.SIDE, side)
                    .texture(TextureSlot.TOP, top)
                    .texture(decSlot, dec)
                    .build(prov.modLoc(baseName + "_bottom"));
            var middle = prov.getBuilder()
                    .parent(prov.modLoc("block/template/column_middle"))
                    .texture(TextureSlot.SIDE, side)
                    .build(prov.modLoc(baseName + "_middle"));
            var columnTop = prov.getBuilder()
                    .parent(prov.modLoc("block/template/column_top"))
                    .texture(TextureSlot.SIDE, side)
                    .texture(TextureSlot.TOP, top)
                    .texture(decSlot, dec)
                    .build(prov.modLoc(baseName + "_top"));

            prov.blockStateOutput.accept(MultiVariantGenerator.dispatch(ctx.get()).with(
                    PropertyDispatch.initial(ColumnBlock.COLUMN)
                            .select(ColumnSide.NONE, BlockModelGenerators.plainVariant(single))
                            .select(ColumnSide.BOTTOM, BlockModelGenerators.plainVariant(bottom))
                            .select(ColumnSide.MIDDLE, BlockModelGenerators.plainVariant(middle))
                            .select(ColumnSide.TOP, BlockModelGenerators.plainVariant(columnTop))));
            prov.registerSimpleItemModel(ctx.get(), single);
        };
    }

    public static <B extends Block> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> crossPlant() {
        return (ctx, prov) -> prov.createCrossBlock(ctx.get(), BlockModelGenerators.PlantType.NOT_TINTED);
    }

    public static <B extends Block> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> doublePlant() {
        return (ctx, prov) -> prov.createDoublePlant(ctx.get(), BlockModelGenerators.PlantType.NOT_TINTED);
    }

    public static <B extends Block> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> flowerBed() {
        return (ctx, prov) -> prov.createFlowerBed(ctx.get());
    }

    public static <B extends DoubleBerryBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> doubleBerry() {
        return (ctx, prov) -> {
            var models = new Identifier[2][DoubleBerryBlock.MAX_AGE + 1];
            for (int age = 0; age <= DoubleBerryBlock.MAX_AGE; age++) {
                String bottomSuffix = "_bottom" + age;
                models[0][age] = BlockModelGenerators.PlantType.NOT_TINTED.getCross().createWithSuffix(
                        ctx.get(), bottomSuffix,
                        TextureMapping.cross(prov.modBlockTexture(ctx.getName() + bottomSuffix)), prov.modelOutput);
                if (age >= 2) {
                    String topSuffix = "_top" + age;
                    models[1][age] = BlockModelGenerators.PlantType.NOT_TINTED.getCross().createWithSuffix(
                            ctx.get(), topSuffix,
                            TextureMapping.cross(prov.modBlockTexture(ctx.getName() + topSuffix)), prov.modelOutput);
                }
            }
            prov.blockStateOutput.accept(MultiVariantGenerator.dispatch(ctx.get()).with(
                    PropertyDispatch.initial(DoubleBerryBlock.HALF, DoubleBerryBlock.AGE).generate((half, age) -> {
                        var model = half == DoubleBlockHalf.UPPER && age >= 2 ? models[1][age] : models[0][age];
                        return BlockModelGenerators.plainVariant(model);
                    })));
        };
    }

    private static TextureMapping sideBottomTop(Material side, Material bottom, Material top) {
        return new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top);
    }
}
