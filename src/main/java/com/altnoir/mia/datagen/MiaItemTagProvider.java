package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.compat.curios.CuriosTags;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.item.abs.IArtifactItem;
import com.altnoir.mia.item.abs.IArtifactItem.Grade;
import com.altnoir.mia.item.abs.IEArtifact;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MiaItemTagProvider extends ItemTagsProvider {
    public MiaItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tagArtifacts(provider);

        tag(MiaTags.Items.ARTIFACT_MODIFIERS_MATERIAL).replace(false)
                .add(MiaItems.MISTFUZZ_PEACH.get())
                .add(Items.IRON_INGOT)
                .add(Items.EMERALD)
                .add(Items.STONE)
                .add(Items.FIRE_CHARGE)
                .add(Items.WATER_BUCKET)
                .add(Items.BAMBOO)
                .add(Items.COBBLED_DEEPSLATE)
                .add(Items.OAK_LEAVES)
                .add(Items.OAK_PLANKS)
                .add(Items.OAK_SAPLING)
                .add(MiaItems.PRASIOLITE_SHARD.get())
                .add(Items.STICK)
                .add(Items.DIAMOND);

        tag(CuriosTags.WHISTLE).replace(false)
                .add(MiaItems.RED_WHISTLE.get())
                .add(MiaItems.BLUE_WHISTLE.get())
                .add(MiaItems.MOON_WHISTLE.get())
                .add(MiaItems.BLACK_WHISTLE.get())
                .add(MiaItems.WHITE_WHISTLE.get());

        tag(ItemTags.LOGS_THAT_BURN)
                .add(MiaBlocks.SKYFOG_LOG.get().asItem())
                .add(MiaBlocks.SKYFOG_WOOD.get().asItem())
                .add(MiaBlocks.STRIPPED_SKYFOG_LOG.get().asItem())
                .add(MiaBlocks.STRIPPED_SKYFOG_WOOD.get().asItem());

        //合成标签
        tag(ItemTags.PLANKS)
                .add(MiaBlocks.SKYFOG_PLANKS.get().asItem());

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

        // 工具TAG
        tag(ItemTags.PICKAXES)
                .add(MiaItems.PRASIOLITE_PICKAXE.get());
        tag(ItemTags.AXES)
                .add(MiaItems.PRASIOLITE_PICKAXE.get());
        tag(ItemTags.SHOVELS)
                .add(MiaItems.PRASIOLITE_PICKAXE.get());
        tag(ItemTags.HOES)
                .add(MiaItems.PRASIOLITE_HOE.get());
    }

    private void tagArtifacts(HolderLookup.Provider provider) {
        HolderLookup<Item> items = provider.lookupOrThrow(Registries.ITEM);
        var allArtifactTag = tag(CuriosTags.ARTIFACT).replace(false);
        var enhanceableArtifactTag = tag(MiaTags.Items.SMITHING_ARTIFACT).replace(false);

        var grade4ArtifactTag = tag(MiaTags.Items.ARTIFACT_GRADE_D).replace(false);
        var grade3ArtifactTag = tag(MiaTags.Items.ARTIFACT_GRADE_C).replace(false);
        var grade2ArtifactTag = tag(MiaTags.Items.ARTIFACT_GRADE_B).replace(false);
        var grade1ArtifactTag = tag(MiaTags.Items.ARTIFACT_GRADE_A).replace(false);
        var gradeSArtifactTag = tag(MiaTags.Items.ARTIFACT_GRADE_S).replace(false);
        var gradeUArtifactTag = tag(MiaTags.Items.ARTIFACT_GRADE_U).replace(false);

        items.listElements().forEach(ref -> {
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
                        grade4ArtifactTag.add(item);
                        break;
                    case Grade.C:
                        grade3ArtifactTag.add(item);
                        break;
                    case Grade.B:
                        grade2ArtifactTag.add(item);
                        break;
                    case Grade.A:
                        grade1ArtifactTag.add(item);
                        break;
                    case Grade.S:
                        gradeSArtifactTag.add(item);
                        break;
                    case Grade.UNKNOWN:
                        gradeUArtifactTag.add(item);
                        break;
                }
            }
        });
    }
}
