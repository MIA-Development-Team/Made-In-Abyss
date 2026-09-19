package com.altnoir.mementoinabyss.worldgen.lod;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Comparator;
import java.util.stream.Collectors;

/** Registry-only conversion. No block models, sprites or world queries. */
public final class MiaLodStateNames {
    public static String name(int id) {
        BlockState state = Block.stateById(id);
        if (Block.getId(state) != id) throw new IllegalArgumentException("Unknown LOD state ID");
        String block = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        if (state.getProperties().isEmpty()) return block;
        return block + state.getProperties().stream().sorted(Comparator.comparing(Property::getName))
                .map(property -> property.getName() + "=" + value(state, property))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static <T extends Comparable<T>> String value(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    public static int resolve(String name) {
        try {
            var reader = new StringReader(name);
            var result = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, reader, false);
            int id = Block.getId(result.blockState());
            if (reader.canRead() || !name(id).equals(name)) throw new IllegalArgumentException("Non-canonical cached block state");
            return id;
        } catch (CommandSyntaxException failure) {
            throw new IllegalArgumentException("Unavailable cached block state", failure);
        }
    }

    private MiaLodStateNames() {}
}
