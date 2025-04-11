package com.altnoir.mia;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class TagsRegister {
    public static class Blocks {
        public static final TagKey<Block> SCANNABLE_BLOCK = tag("scannable_block");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(MIA.MODID, name));
        }
    }
}
