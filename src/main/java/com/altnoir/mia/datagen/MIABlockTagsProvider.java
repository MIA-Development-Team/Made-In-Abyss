package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.content.block.BlocksRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MIABlockTagsProvider extends BlockTagsProvider {
    public MIABlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var chlorophyteOreTag = TagKey.create(
                Registries.BLOCK,
                new ResourceLocation("forge", "ores/chlorophyte")
        );

        tag(chlorophyteOreTag)
                .add(BlocksRegister.CHLOROPHTRE_ORE.get());

        tag(Tags.Blocks.ORES)
                .addTag(chlorophyteOreTag);

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BlocksRegister.CHLOROPHTRE_ORE.get());

        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(BlocksRegister.CHLOROPHTRE_ORE.get());

        tag(BlockTags.DIRT)
                .add(BlocksRegister.ABYSS_GRASS_BLOCK.get())
                .add(BlocksRegister.COVERGRASS_COBBLESTONE.get())
                .add(BlocksRegister.COVERGRASS_STONE.get())
                .add(BlocksRegister.COVERGRASS_DEEPSLATE.get())
                .add(BlocksRegister.COVERGRASS_GRANITE.get())
                .add(BlocksRegister.COVERGRASS_DIORITE.get())
                .add(BlocksRegister.COVERGRASS_ANDESITE.get())
                .add(BlocksRegister.COVERGRASS_CALCITE.get())
                .add(BlocksRegister.COVERGRASS_TUFF.get());

        var scannableBlockTag = TagKey.create(
                Registries.BLOCK,
                new ResourceLocation(MIA.MODID, "scannable_block")
        );

        tag(scannableBlockTag)
                .addTag(Tags.Blocks.ORES);
    }
}
