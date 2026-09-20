package com.altnoir.mementoinabyss.foundation.transfer.heat;

import java.util.function.Predicate;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/** Helpers for moving heat between {@link ResourceHandler}s. No world/bucket interaction. */
public final class HeatUtil {
    public static boolean isEmpty(ResourceHandler<HeatResource> handler) {
        return ResourceHandlerUtil.isEmpty(handler);
    }

    public static boolean isFull(ResourceHandler<HeatResource> handler) {
        return ResourceHandlerUtil.isFull(handler);
    }

    public static int move(
            @Nullable ResourceHandler<HeatResource> from,
            @Nullable ResourceHandler<HeatResource> to,
            int amount,
            @Nullable TransactionContext transaction) {
        return move(from, to, resource -> true, amount, transaction);
    }

    public static int move(
            @Nullable ResourceHandler<HeatResource> from,
            @Nullable ResourceHandler<HeatResource> to,
            Predicate<HeatResource> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.move(from, to, filter, amount, transaction);
    }

    private HeatUtil() {}
}
