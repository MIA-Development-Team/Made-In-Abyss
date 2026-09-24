package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.common.item.abs.IArtifactItem;
import com.altnoir.mia.common.item.abs.IArtifactItem.Grade;
import com.altnoir.mia.common.item.abs.IEArtifact;
import com.altnoir.mia.common.recipe.ArtifactSmithingRecipeBuilder;
import com.altnoir.mia.compat.curios.CuriosTags;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.init.MiaTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MiaItemTagProvider extends ItemTagsProvider {
    public MiaItemTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTags,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tagArtifacts(provider);

        tag(CuriosTags.WHISTLE)
                .replace(false)
                .add(MiaItems.RED_WHISTLE.get())
                .add(MiaItems.BLUE_WHISTLE.get());

        tag(ItemTags.LOGS_THAT_BURN)
                .add(MiaBlocks.SKYFOG_LOG.get().asItem())
                .add(MiaBlocks.SKYFOG_WOOD.get().asItem())
                .add(MiaBlocks.STRIPPED_SKYFOG_LOG.get().asItem())
                .add(MiaBlocks.STRIPPED_SKYFOG_WOOD.get().asItem())
                .add(MiaBlocks.INVERTED_LOG.get().asItem())
                .add(MiaBlocks.INVERTED_WOOD.get().asItem())
                .add(MiaBlocks.STRIPPED_INVERTED_LOG.get().asItem())
                .add(MiaBlocks.STRIPPED_INVERTED_WOOD.get().asItem());

        // 合成标签
        tag(MiaTags.Items.UNSTRIPPED_FOSSILIZED_LOGS)
                .add(MiaBlocks.FOSSILIZED_LOG.get().asItem())
                .add(MiaBlocks.FOSSILIZED_WOOD.get().asItem());
        tag(MiaTags.Items.STRIPPED_FOSSILIZED_LOGS)
                .add(MiaBlocks.STRIPPED_FOSSILIZED_LOG.get().asItem())
                .add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get().asItem());
        tag(MiaTags.Items.FOSSILIZED_LOGS)
                .addTag(MiaTags.Items.UNSTRIPPED_FOSSILIZED_LOGS)
                .addTag(MiaTags.Items.STRIPPED_FOSSILIZED_LOGS);

        tag(MiaTags.Items.SKYFOG_LOGS)
                .add(MiaBlocks.SKYFOG_LOG.get().asItem())
                .add(MiaBlocks.SKYFOG_WOOD.get().asItem())
                .add(MiaBlocks.STRIPPED_SKYFOG_LOG.get().asItem())
                .add(MiaBlocks.STRIPPED_SKYFOG_WOOD.get().asItem());
        tag(MiaTags.Items.INVERTED_LOGS)
                .add(MiaBlocks.INVERTED_LOG.get().asItem())
                .add(MiaBlocks.INVERTED_WOOD.get().asItem())
                .add(MiaBlocks.STRIPPED_INVERTED_LOG.get().asItem())
                .add(MiaBlocks.STRIPPED_INVERTED_WOOD.get().asItem());

        tag(MiaTags.Items.PRIMO_STEMS)
                .add(MiaBlocks.PRIMO_STEM.get().asItem())
                .add(MiaBlocks.STRIPPED_PRIMO_STEM.get().asItem())
                .add(MiaBlocks.PRIMO_HYPHAE.get().asItem())
                .add(MiaBlocks.STRIPPED_PRIMO_HYPHAE.get().asItem());

        tag(ItemTags.PLANKS)
                .add(
                        MiaBlocks.SKYFOG_PLANKS.get().asItem(),
                        MiaBlocks.INVERTED_PLANKS.get().asItem(),
                        MiaBlocks.PRIMO_PLANKS.get().asItem());
        tag(ItemTags.LOGS)
                .add(MiaBlocks.PRIMO_STEM.get().asItem())
                .add(MiaBlocks.PRIMO_HYPHAE.get().asItem())
                .add(MiaBlocks.STRIPPED_PRIMO_STEM.get().asItem())
                .add(MiaBlocks.STRIPPED_PRIMO_HYPHAE.get().asItem());

        tag(ItemTags.STAIRS).add(MiaBlocks.PRIMO_STAIRS.get().asItem());
        tag(ItemTags.SLABS).add(MiaBlocks.PRIMO_SLAB.get().asItem());
        tag(ItemTags.BUTTONS).add(MiaBlocks.PRIMO_BUTTON.get().asItem());
        tag(ItemTags.FENCES).add(MiaBlocks.PRIMO_FENCE.get().asItem());
        tag(ItemTags.FENCE_GATES).add(MiaBlocks.PRIMO_FENCE_GATE.get().asItem());
        tag(ItemTags.DOORS).add(MiaBlocks.PRIMO_DOOR.get().asItem());
        tag(ItemTags.TRAPDOORS).add(MiaBlocks.PRIMO_TRAPDOOR.get().asItem());

        tag(ItemTags.WOODEN_STAIRS).add(MiaBlocks.PRIMO_STAIRS.get().asItem());
        tag(ItemTags.WOODEN_SLABS).add(MiaBlocks.PRIMO_SLAB.get().asItem());
        tag(ItemTags.WOODEN_BUTTONS).add(MiaBlocks.PRIMO_BUTTON.get().asItem());
        tag(ItemTags.WOODEN_PRESSURE_PLATES).add(MiaBlocks.PRIMO_PRESSURE_PLATE.get().asItem());
        tag(ItemTags.WOODEN_FENCES).add(MiaBlocks.PRIMO_FENCE.get().asItem());
        tag(ItemTags.WOODEN_DOORS).add(MiaBlocks.PRIMO_DOOR.get().asItem());
        tag(ItemTags.WOODEN_TRAPDOORS).add(MiaBlocks.PRIMO_TRAPDOOR.get().asItem());

        // 工具TAG
        tag(ItemTags.SWORDS).add(MiaItems.GROW_SWORD.get());
        tag(ItemTags.PICKAXES).add(MiaItems.PRASIOLITE_PICKAXE.get());
        tag(ItemTags.AXES).add(MiaItems.PRASIOLITE_PICKAXE.get());
        tag(ItemTags.SHOVELS).add(MiaItems.PRASIOLITE_PICKAXE.get());
        tag(ItemTags.HOES).add(MiaItems.PRASIOLITE_HOE.get());
    }

    private void tagArtifacts(HolderLookup.Provider provider) {
        HolderLookup<Item> items = provider.lookupOrThrow(Registries.ITEM);
        var allArtifactTag = tag(CuriosTags.ARTIFACT).replace(false);
        var enhanceableArtifactTag = tag(MiaTags.Items.SMITHING_ARTIFACT).replace(false);

        var gradeD = tag(MiaTags.Items.ARTIFACT_GRADE_D).replace(false);
        var gradeC = tag(MiaTags.Items.ARTIFACT_GRADE_C).replace(false);
        var gradeB = tag(MiaTags.Items.ARTIFACT_GRADE_B).replace(false);
        var gradeA = tag(MiaTags.Items.ARTIFACT_GRADE_A).replace(false);
        var gradeS = tag(MiaTags.Items.ARTIFACT_GRADE_S).replace(false);
        var gradeU = tag(MiaTags.Items.ARTIFACT_GRADE_U).replace(false);

        items.listElements()
                .forEach(
                        ref -> {
                            Item item = ref.value();

                            if (item instanceof IArtifactItem artifact) {
                                allArtifactTag.add(item);
                                if (item instanceof IEArtifact) {
                                    enhanceableArtifactTag.add(item);
                                }

                                // dispatch by grade
                                Grade grade = artifact.getGrade();
                                switch (grade) {
                                    case Grade.D:
                                        gradeD.add(item);
                                        break;
                                    case Grade.C:
                                        gradeC.add(item);
                                        break;
                                    case Grade.B:
                                        gradeB.add(item);
                                        break;
                                    case Grade.A:
                                        gradeA.add(item);
                                        break;
                                    case Grade.S:
                                        gradeS.add(item);
                                        break;
                                    case Grade.UNKNOWN:
                                        gradeU.add(item);
                                        break;
                                }
                            }
                        });
        tag(MiaTags.Items.ARTIFACT_MODIFIERS_MATERIAL)
                .replace(false)
                .add(ArtifactSmithingRecipeBuilder.getMaterialTags().toArray(new Item[0]));
    }
}
