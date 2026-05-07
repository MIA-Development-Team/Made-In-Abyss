package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MiaTags {
    public enum NameSpaces {
        MOD(MementoInAbyss.ID)
        ;

        public final String id;

        NameSpaces(String id) {
            this.id = id;
        }

        public Identifier id(String path) {
            return Identifier.fromNamespaceAndPath(this.id, path);
        }

        public Identifier id(Enum<?> entry, @Nullable String pathOverride) {
            return this.id(pathOverride != null ? pathOverride : entry.name().toLowerCase(Locale.ROOT));
        }
    }

    public enum BlockTags {
        BASE_STONE_ABYSS,
        ABYSS_ANDESITE_ORE_REPLACEABLE,
        ABYSS_MUD_ORE_REPLACEABLE,
        COVERGRASS,
        ABYSS_DRIPSTONE_UNREPLACEABLE,
        NEED_PRASIOLITE_TOOL,
        INCORRECT_FOR_PRASIOLITE_TOOL,
        MINEABLE_WITH_COMPOSITE
        ;

        public final TagKey<Block> tag;

        BlockTags() {
			this(NameSpaces.MOD);
        }

        BlockTags(NameSpaces namespace) {
            this(namespace, null);
        }

        BlockTags(NameSpaces namespace, @Nullable String pathOverride) {
            this.tag = TagKey.create(Registries.BLOCK, namespace.id(this, pathOverride));
        }
    }

    public enum ItemTags {
        ;

        public final TagKey<Item> tag;

        ItemTags() {
            this(NameSpaces.MOD);
        }

        ItemTags(NameSpaces namespace) {
            this(namespace, null);
        }

        ItemTags(NameSpaces namespace, @Nullable String pathOverride) {
            this.tag = TagKey.create(Registries.ITEM, namespace.id(this, pathOverride));
        }
    }

    public enum BiomeTags {
        HAS_ISLAND,
        THE_ABYSS_CLEAR,
        HAS_STAR_COMPASS_TEMPLE
        ;

        public final TagKey<Biome> tag;

        BiomeTags() {
            this(NameSpaces.MOD);
        }

        BiomeTags(NameSpaces namespace) {
            this(namespace, null);
        }

        BiomeTags(NameSpaces namespace, @Nullable String pathOverride) {
            this.tag = TagKey.create(Registries.BIOME, namespace.id(this, pathOverride));
        }
    }
}
