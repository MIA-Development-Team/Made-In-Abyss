package com.altnoir.mementoinabyss.foundation.transfer.heat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;

/**
 * Heat type plus amount. Parallel to {@code FluidStack}; amounts use {@link HeatType#UNIT} ({@code
 * 1000}) as one portion.
 */
public record HeatStack(HeatResource resource, int amount) {
    public static final HeatStack EMPTY = new HeatStack(HeatResource.EMPTY, 0);

    public static final Codec<HeatStack> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            HeatResource.CODEC
                                                    .fieldOf("id")
                                                    .forGetter(HeatStack::resource),
                                            ExtraCodecs.POSITIVE_INT
                                                    .fieldOf("amount")
                                                    .forGetter(HeatStack::amount))
                                    .apply(instance, HeatStack::new));

    public static final Codec<HeatStack> OPTIONAL_CODEC =
            ExtraCodecs.optionalEmptyMap(CODEC)
                    .xmap(
                            optional -> optional.orElse(EMPTY),
                            stack -> {
                                assert stack != null;
                                return stack.isEmpty() ? Optional.empty() : Optional.of(stack);
                            });

    public static final StreamCodec<RegistryFriendlyByteBuf, HeatStack> STREAM_CODEC =
            StreamCodec.composite(
                    HeatResource.STREAM_CODEC,
                    HeatStack::resource,
                    ByteBufCodecs.VAR_INT,
                    HeatStack::amount,
                    HeatStack::new);

    public HeatStack(HeatResource resource, int amount) {
        if (resource.isEmpty() || amount <= 0) {
            this.resource = HeatResource.EMPTY;
            this.amount = 0;
        } else {
            this.resource = resource;
            this.amount = amount;
        }
    }

    public HeatType getHeatType() {
        return resource.getHeatType();
    }

    public boolean isEmpty() {
        return this == EMPTY || ResourceHandlerUtil.isEmpty(resource, amount);
    }

    public HeatStack copy() {
        return isEmpty() ? EMPTY : new HeatStack(resource, amount);
    }

    public HeatStack copyWithAmount(int newAmount) {
        return new HeatStack(resource, newAmount);
    }
}
