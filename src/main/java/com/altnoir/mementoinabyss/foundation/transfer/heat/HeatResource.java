package com.altnoir.mementoinabyss.foundation.transfer.heat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.transfer.resource.RegisteredResource;

/**
 * Immutable heat type without an amount. Parallel to {@code FluidResource}; there is no world
 * fluid, entity, or bucket form.
 */
public final class HeatResource implements RegisteredResource<HeatType> {
    public static final HeatResource EMPTY = new HeatResource(HeatType.EMPTY);

    /**
     * Codec for a heat resource. Does <b>not</b> accept empty resources.
     */
    public static final Codec<HeatResource> CODEC =
            Codec.lazyInitialized(
                    () ->
                            HeatType.registry()
                                    .holderByNameCodec()
                                    .comapFlatMap(
                                            holder ->
                                                    holder.value().isEmpty()
                                                            ? DataResult.error(
                                                                    () ->
                                                                            "Heat resource cannot"
                                                                                    + " be empty")
                                                            : DataResult.success(of(holder)),
                                            HeatResource::typeHolder));

    /**
     * Codec for a heat resource. Same format as {@link #CODEC}, and also accepts empty resources.
     */
    public static final Codec<HeatResource> OPTIONAL_CODEC =
            ExtraCodecs.optionalEmptyMap(CODEC)
                    .xmap(
                            optional -> optional.orElse(EMPTY),
                            resource ->
                                    resource.isEmpty() ? Optional.empty() : Optional.of(resource));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeatResource> STREAM_CODEC =
            ByteBufCodecs.holderRegistry(HeatType.REGISTRY_KEY)
                    .map(HeatResource::of, HeatResource::typeHolder);

    private final HeatType type;

    private HeatResource(HeatType type) {
        this.type = type;
    }

    public static HeatResource of(HeatType type) {
        if (type.isEmpty()) {
            return EMPTY;
        }
        return new HeatResource(type);
    }

    public static HeatResource of(Holder<HeatType> holder) {
        return of(holder.value());
    }

    @Override
    public HeatType value() {
        return type;
    }

    public HeatType getHeatType() {
        return type;
    }

    @Override
    public Holder<HeatType> typeHolder() {
        return HeatType.registry().wrapAsHolder(type);
    }

    @Override
    public boolean isEmpty() {
        return type.isEmpty();
    }

    public HeatStack toStack(int amount) {
        return new HeatStack(this, amount);
    }

    public boolean matches(HeatStack stack) {
        return !isEmpty() && equals(stack.resource());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof HeatResource other && type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    @Override
    public String toString() {
        var id = HeatType.registry().getKey(type);
        return id != null ? id.toString() : "mementoinabyss:empty";
    }
}
