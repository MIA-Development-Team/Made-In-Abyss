package com.altnoir.mementoinabyss.content.returnchest;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber
public final class ReturnStorage extends SavedData {
    static final int SIZE = 27;
    private static final Codec<ReturnStorage> CODEC =
            Codec.unboundedMap(DyeColor.CODEC, StoredChannel.CODEC)
                    .fieldOf("channels")
                    .xmap(ReturnStorage::new, ReturnStorage::snapshot)
                    .codec();
    private static final SavedDataType<ReturnStorage> TYPE =
            new SavedDataType<>(
                    MementoInAbyss.asResource("return_storage"), ReturnStorage::new, CODEC);

    private final Map<DyeColor, Channel> channels = new EnumMap<>(DyeColor.class);

    private ReturnStorage() {}

    private ReturnStorage(Map<DyeColor, StoredChannel> saved) {
        saved.forEach(
                (color, state) -> {
                    Channel channel = channel(color);
                    for (int slot = 0; slot < state.items().size(); slot++) {
                        channel.getItems().set(slot, state.items().get(slot));
                    }
                    channel.flight = state.flight().orElse(null);
                });
    }

    static ReturnStorage get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    Channel channel(DyeColor color) {
        return channels.computeIfAbsent(color, unused -> new Channel(this::setDirty));
    }

    void depart(Channel channel, Flight flight) {
        channel.flight = flight;
        setDirty();
    }

    boolean isFlying(DyeColor color, UUID id) {
        Channel channel = channels.get(color);
        return channel != null && channel.flight != null && channel.flight.id().equals(id);
    }

    @SubscribeEvent
    public static void deliverFlights(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        ReturnStorage storage = get(overworld);
        long now = overworld.getGameTime();
        for (Channel channel : storage.channels.values()) {
            Flight flight = channel.flight;
            if (flight == null || now < flight.arrival()) continue;

            SimpleContainer packed = channel.pack(flight.items());
            if (packed == null) continue;
            for (int slot = 0; slot < SIZE; slot++) {
                channel.getItems().set(slot, packed.getItem(slot));
            }
            channel.flight = null;
            channel.setChanged();
        }
    }

    private Map<DyeColor, StoredChannel> snapshot() {
        Map<DyeColor, StoredChannel> result = new EnumMap<>(DyeColor.class);
        channels.forEach(
                (color, channel) ->
                        result.put(
                                color,
                                new StoredChannel(
                                        channel.getItems().stream().map(ItemStack::copy).toList(),
                                        Optional.ofNullable(channel.flight))));
        return result;
    }

    static final class Channel extends SimpleContainer {
        private final Runnable markDirty;
        private @Nullable Flight flight;

        private Channel(Runnable markDirty) {
            super(SIZE);
            this.markDirty = markDirty;
        }

        @Override
        public void setChanged() {
            markDirty.run();
        }

        boolean inTransit() {
            return flight != null;
        }

        @Nullable SimpleContainer pack(List<ItemStack> items) {
            SimpleContainer packed = new SimpleContainer(SIZE);
            for (int slot = 0; slot < SIZE; slot++) {
                packed.getItems().set(slot, getItem(slot).copy());
            }
            for (ItemStack item : items) {
                if (item.getCount() > packed.getMaxStackSize(item)
                        || !packed.addItem(item).isEmpty()) return null;
            }
            return packed;
        }
    }

    record Flight(UUID id, long arrival, List<ItemStack> items) {
        private static final Codec<Flight> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                                UUIDUtil.CODEC.fieldOf("id").forGetter(Flight::id),
                                                Codec.LONG
                                                        .fieldOf("arrival")
                                                        .forGetter(Flight::arrival),
                                                ItemStack.CODEC
                                                        .sizeLimitedListOf(SIZE)
                                                        .fieldOf("items")
                                                        .forGetter(Flight::items))
                                        .apply(instance, Flight::new));
    }

    private record StoredChannel(List<ItemStack> items, Optional<Flight> flight) {
        private static final Codec<StoredChannel> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                                ItemStack.OPTIONAL_CODEC
                                                        .sizeLimitedListOf(SIZE)
                                                        .fieldOf("items")
                                                        .forGetter(StoredChannel::items),
                                                Flight.CODEC
                                                        .optionalFieldOf("flight")
                                                        .forGetter(StoredChannel::flight))
                                        .apply(instance, StoredChannel::new));
    }
}
