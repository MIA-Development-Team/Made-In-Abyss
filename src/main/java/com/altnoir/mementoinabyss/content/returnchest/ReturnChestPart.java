package com.altnoir.mementoinabyss.content.returnchest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

public enum ReturnChestPart implements StringRepresentable {
    WN_DOWN(-1, 0, -1),
    N_DOWN(0, 0, -1),
    EN_DOWN(1, 0, -1),
    W_DOWN(-1, 0, 0),
    C_DOWN(0, 0, 0),
    E_DOWN(1, 0, 0),
    WS_DOWN(-1, 0, 1),
    S_DOWN(0, 0, 1),
    ES_DOWN(1, 0, 1),
    WN_MIDDLE(-1, 1, -1),
    EN_MIDDLE(1, 1, -1),
    C_MIDDLE(0, 1, 0),
    WS_MIDDLE(-1, 1, 1),
    ES_MIDDLE(1, 1, 1),
    WN_UP(-1, 2, -1),
    N_UP(0, 2, -1),
    EN_UP(1, 2, -1),
    W_UP(-1, 2, 0),
    E_UP(1, 2, 0),
    WS_UP(-1, 2, 1),
    S_UP(0, 2, 1),
    ES_UP(1, 2, 1);

    private static final Map<BlockPos, ReturnChestPart> BY_OFFSET;

    static {
        Map<BlockPos, ReturnChestPart> byOffset = new HashMap<>();
        for (ReturnChestPart part : values()) byOffset.put(part.offset, part);
        BY_OFFSET = Collections.unmodifiableMap(byOffset);
    }

    private final BlockPos offset;

    ReturnChestPart(int x, int y, int z) {
        this.offset = new BlockPos(x, y, z);
    }

    public BlockPos offset() {
        return offset;
    }

    public boolean isCore() {
        return this == C_MIDDLE;
    }

    public static @Nullable ReturnChestPart byOffset(BlockPos offset) {
        return BY_OFFSET.get(offset);
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
