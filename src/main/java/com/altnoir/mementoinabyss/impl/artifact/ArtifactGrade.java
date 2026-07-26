package com.altnoir.mementoinabyss.impl.artifact;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

public enum ArtifactGrade implements StringRepresentable {
    D("d", 0, ChatFormatting.DARK_GRAY),
    C("c", 1, ChatFormatting.DARK_AQUA),
    B("b", 2, ChatFormatting.DARK_PURPLE),
    A("a", 4, ChatFormatting.YELLOW),
    S("s", 5, ChatFormatting.DARK_RED),
    UNKNOWN("unknown", 0, ChatFormatting.BLACK);

    public static final Codec<ArtifactGrade> CODEC = StringRepresentable.fromEnum(ArtifactGrade::values);
    private static final IntFunction<ArtifactGrade> BY_ID = ByIdMap.continuous(
            ArtifactGrade::ordinal,
            values(),
            ByIdMap.OutOfBoundsStrategy.ZERO
    );
    public static final StreamCodec<io.netty.buffer.ByteBuf, ArtifactGrade> STREAM_CODEC =
            ByteBufCodecs.idMapper(BY_ID, ArtifactGrade::ordinal);

    private final String serializedName;
    private final int defaultMaxEnhancementLevel;
    private final ChatFormatting color;

    ArtifactGrade(String serializedName, int defaultMaxEnhancementLevel, ChatFormatting color) {
        this.serializedName = serializedName;
        this.defaultMaxEnhancementLevel = defaultMaxEnhancementLevel;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public int defaultMaxEnhancementLevel() {
        return defaultMaxEnhancementLevel;
    }

    public UnaryOperator<Style> style() {
        return style -> style.withColor(color);
    }
}
