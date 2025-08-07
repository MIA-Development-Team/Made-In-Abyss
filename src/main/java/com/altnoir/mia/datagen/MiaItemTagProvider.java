package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.compat.curios.CuriosItemTags;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItemTags;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.item.EnhanceableArtifact;
import com.altnoir.mia.item.abs.IArtifactItem;
import com.altnoir.mia.item.abs.IArtifactItem.Grade;

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

        tag(MiaItemTags.ARTIFACT_ENHANCE_MATERIAL).replace(false)
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
                .add(Items.STONE_PICKAXE)
                .add(Items.STICK)
                .add(Items.DIAMOND);

        tag(CuriosItemTags.WHISTLE).replace(false)
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

        tag(ItemTags.PLANKS)
                .add(MiaBlocks.SKYFOG_PLANKS.get().asItem());
    }

    private void tagArtifacts(HolderLookup.Provider provider) {
        HolderLookup<Item> items = provider.lookupOrThrow(Registries.ITEM);
        var allArtifactTag = tag(CuriosItemTags.ARTIFACT).replace(false);
        var enhanceableArtifactTag = tag(MiaItemTags.ENHANCEABLE_ARTIFACT).replace(false);
        var grade4ArtifactTag = tag(MiaItemTags.ARTIFACT_GRADE_4).replace(false);
        var grade3ArtifactTag = tag(MiaItemTags.ARTIFACT_GRADE_3).replace(false);
        var grade2ArtifactTag = tag(MiaItemTags.ARTIFACT_GRADE_2).replace(false);
        var grade1ArtifactTag = tag(MiaItemTags.ARTIFACT_GRADE_1).replace(false);
        var gradeSArtifactTag = tag(MiaItemTags.ARTIFACT_GRADE_S).replace(false);
        var gradeUArtifactTag = tag(MiaItemTags.ARTIFACT_GRADE_U).replace(false);

        items.listElements().forEach(ref -> {
            Item item = ref.value();

            if (item instanceof IArtifactItem artifact) {
                allArtifactTag.add(item);
                if (item instanceof EnhanceableArtifact) {
                    enhanceableArtifactTag.add(item);
                }

                // dispatch by grade
                Grade grade = artifact.getGrade();
                switch (grade) {
                    case Grade.FOURTH:
                        grade4ArtifactTag.add(item);
                        break;
                    case Grade.THIRD:
                        grade3ArtifactTag.add(item);
                        break;
                    case Grade.SECOND:
                        grade2ArtifactTag.add(item);
                        break;
                    case Grade.FIRST:
                        grade1ArtifactTag.add(item);
                        break;
                    case Grade.SPECIAL:
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
