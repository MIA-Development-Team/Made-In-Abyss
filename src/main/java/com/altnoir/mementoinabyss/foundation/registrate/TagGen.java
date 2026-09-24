package com.altnoir.mementoinabyss.foundation.registrate;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import java.util.List;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Small, composable builder transforms for common block and item tag conventions. */
public final class TagGen {
    private TagGen() {}

    public static final BlockAndItemTag STAIRS =
            new BlockAndItemTag(BlockTags.STAIRS, ItemTags.STAIRS);
    public static final BlockAndItemTag WOODEN_STAIRS =
            new BlockAndItemTag(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
    public static final BlockAndItemTag SLABS =
            new BlockAndItemTag(BlockTags.SLABS, ItemTags.SLABS);
    public static final BlockAndItemTag WOODEN_SLABS =
            new BlockAndItemTag(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
    public static final BlockAndItemTag FENCES =
            new BlockAndItemTag(BlockTags.FENCES, ItemTags.FENCES);
    public static final BlockAndItemTag WOODEN_FENCES =
            new BlockAndItemTag(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
    public static final BlockAndItemTag DOORS =
            new BlockAndItemTag(BlockTags.DOORS, ItemTags.DOORS);
    public static final BlockAndItemTag WOODEN_DOORS =
            new BlockAndItemTag(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
    public static final BlockAndItemTag TRAPDOORS =
            new BlockAndItemTag(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
    public static final BlockAndItemTag WOODEN_TRAPDOORS =
            new BlockAndItemTag(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
    public static final BlockAndItemTag WALLS =
            new BlockAndItemTag(BlockTags.WALLS, ItemTags.WALLS);
    public static final BlockAndItemTag BUTTONS =
            new BlockAndItemTag(BlockTags.BUTTONS, ItemTags.BUTTONS);
    public static final BlockAndItemTag WOODEN_BUTTONS =
            new BlockAndItemTag(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
    public static final BlockAndItemTag WOODEN_PRESSURE_PLATES =
            new BlockAndItemTag(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);

    public static <T extends Block, P>
            NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOrPickaxe() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_AXE).tag(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static <T extends Block, P>
            NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOnly() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_AXE);
    }

    public static <T extends Block, P>
            NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> pickaxeOnly() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static <T extends Block, P>
            NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>>
                    tagBlockAndItem(BlockAndItemTag... tags) {
        var copy = List.of(tags);
        return b -> {
            for (var tag : copy) b.tag(tag.block());
            var item = b.item();
            for (var tag : copy) item.tag(tag.item());
            return item;
        };
    }

    public static <T extends Block, P>
            NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>>
                    tagBlockAndItem(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        return tagBlockAndItem(new BlockAndItemTag(blockTag, itemTag));
    }

    public record BlockAndItemTag(TagKey<Block> block, TagKey<Item> item) {}
}
