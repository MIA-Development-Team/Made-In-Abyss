package com.altnoir.mementoinabyss.content.returnchest;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber
public final class ReturnStorage extends SavedData {
    private static final Codec<ItemStack> FULL_STACK_CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            Item.CODEC_WITH_BOUND_COMPONENTS
                                                    .fieldOf("id")
                                                    .forGetter(ItemStack::typeHolder),
                                            Codec.intRange(1, Integer.MAX_VALUE)
                                                    .fieldOf("count")
                                                    .orElse(1)
                                                    .forGetter(ItemStack::getCount),
                                            DataComponentPatch.CODEC
                                                    .optionalFieldOf(
                                                            "components", DataComponentPatch.EMPTY)
                                                    .forGetter(ItemStack::getComponentsPatch))
                                    .apply(
                                            instance,
                                            (holder, count, patch) ->
                                                    new ItemStack(holder, count, patch)));
    private static final Codec<ItemStack> STACK_CODEC =
            Codec.either(ItemStack.OPTIONAL_CODEC, FULL_STACK_CODEC)
                    .xmap(
                            either -> either.map(stack -> stack, stack -> stack),
                            stack -> Either.right(stack));
    private static final Codec<ReturnStorage> CODEC =
            Codec.unboundedMap(DyeColor.CODEC, StoredChannel.CODEC)
                    .fieldOf("channels")
                    .xmap(ReturnStorage::new, ReturnStorage::snapshot)
                    .codec();
    private static final SavedDataType<ReturnStorage> TYPE =
            new SavedDataType<>(
                    MementoInAbyss.asResource("return_storage"), ReturnStorage::new, CODEC);

    private static final int FLIGHTS_PER_TICK = 1;

    private final Map<DyeColor, Channel> channels = new EnumMap<>(DyeColor.class);

    private ReturnStorage() {}

    private ReturnStorage(Map<DyeColor, StoredChannel> saved) {
        saved.forEach(
                (color, state) -> {
                    Channel channel = channel(color);
                    channel.restore(state.items());
                    channel.flights.addAll(state.flights());
                });
    }

    static ReturnStorage get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    Channel channel(DyeColor color) {
        return channels.computeIfAbsent(color, unused -> new Channel(this::setDirty));
    }

    void depart(Channel channel, Flight flight) {
        channel.flights.add(flight);
        setDirty();
    }

    boolean isFlying(DyeColor color, UUID id) {
        Channel channel = channels.get(color);
        if (channel == null) return false;
        for (Flight flight : channel.flights) {
            if (flight.id().equals(id)) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void deliverFlights(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        ReturnStorage storage = get(overworld);
        long now = overworld.getGameTime();
        for (Channel channel : storage.channels.values()) {
            for (int count = 0; count < FLIGHTS_PER_TICK; count++) {
                Flight flight = earliestDue(channel, now);
                if (flight == null) break;
                channel.flights.remove(flight);
                channel.deliver(flight.items());
            }
        }
    }

    private static @Nullable Flight earliestDue(Channel channel, long now) {
        Flight earliest = null;
        for (Flight flight : channel.flights) {
            if (now < flight.arrival()) continue;
            if (earliest == null || flight.arrival() < earliest.arrival()) earliest = flight;
        }
        return earliest;
    }

    private Map<DyeColor, StoredChannel> snapshot() {
        Map<DyeColor, StoredChannel> result = new EnumMap<>(DyeColor.class);
        channels.forEach(
                (color, channel) ->
                        result.put(
                                color,
                                new StoredChannel(
                                        channel.snapshotItems(), List.copyOf(channel.flights))));
        return result;
    }

    static final class Channel implements Container {
        private final Runnable markDirty;
        private final List<Runnable> listeners = new ArrayList<>();
        private final List<ItemStack> items = new ArrayList<>();
        private final List<Flight> flights = new ArrayList<>();

        private Channel(Runnable markDirty) {
            this.markDirty = markDirty;
        }

        void restore(List<ItemStack> saved) {
            items.clear();
            for (ItemStack stack : saved) {
                items.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            compact();
            mergeTypes();
        }

        List<ItemStack> snapshotItems() {
            return items.stream().map(ItemStack::copy).toList();
        }

        int occupied() {
            int count = 0;
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) count++;
            }
            return count;
        }

        void deliver(List<ItemStack> incoming) {
            for (ItemStack stack : incoming) {
                if (stack.isEmpty()) continue;
                ItemStack existing = findSame(stack);
                if (existing == null) {
                    items.add(stack.copy());
                } else {
                    existing.setCount(mergeCounts(existing.getCount(), stack.getCount()));
                }
            }
            compact();
            setChanged();
        }

        private @Nullable ItemStack findSame(ItemStack stack) {
            for (ItemStack existing : items) {
                if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                    return existing;
                }
            }
            return null;
        }

        private void mergeTypes() {
            List<ItemStack> merged = new ArrayList<>();
            for (ItemStack stack : items) {
                if (stack.isEmpty()) continue;
                ItemStack existing = null;
                for (ItemStack candidate : merged) {
                    if (ItemStack.isSameItemSameComponents(candidate, stack)) {
                        existing = candidate;
                        break;
                    }
                }
                if (existing == null) {
                    merged.add(stack.copy());
                } else {
                    existing.setCount(mergeCounts(existing.getCount(), stack.getCount()));
                }
            }
            items.clear();
            items.addAll(merged);
        }

        private static int mergeCounts(int first, int second) {
            return (int) Math.min(Integer.MAX_VALUE, (long) first + second);
        }

        void addListener(Runnable listener) {
            if (!listeners.contains(listener)) listeners.add(listener);
        }

        void removeListener(Runnable listener) {
            listeners.remove(listener);
        }

        void compact() {
            items.removeIf(ItemStack::isEmpty);
        }

        private void trim() {
            for (int index = items.size() - 1; index >= 0; index--) {
                if (!items.get(index).isEmpty()) return;
                items.remove(index);
            }
        }

        @Override
        public int getContainerSize() {
            return items.size();
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) return ItemStack.EMPTY;
            ItemStack taken = stack.split(amount);
            if (stack.isEmpty()) trim();
            setChanged();
            return taken;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return removeItem(slot, Integer.MAX_VALUE);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot < 0) return;
            while (items.size() <= slot) {
                items.add(ItemStack.EMPTY);
            }
            items.set(slot, stack);
            trim();
            setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public void setChanged() {
            markDirty.run();
            if (listeners.isEmpty()) compact();
            for (Runnable listener : List.copyOf(listeners)) listener.run();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            items.clear();
            setChanged();
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return false;
        }
    }

    private static List<Flight> mergeFlights(List<Flight> flights, Optional<Flight> legacy) {
        if (legacy.isEmpty()) return flights;
        List<Flight> merged = new ArrayList<>(flights);
        merged.add(legacy.get());
        return merged;
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
                                                STACK_CODEC
                                                        .listOf()
                                                        .fieldOf("items")
                                                        .forGetter(Flight::items))
                                        .apply(instance, Flight::new));
    }

    private record StoredChannel(List<ItemStack> items, List<Flight> flights) {
        private static final Codec<StoredChannel> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                                STACK_CODEC
                                                        .listOf()
                                                        .fieldOf("items")
                                                        .forGetter(StoredChannel::items),
                                                Flight.CODEC
                                                        .listOf()
                                                        .optionalFieldOf("flights", List.of())
                                                        .forGetter(StoredChannel::flights),
                                                Flight.CODEC
                                                        .optionalFieldOf("flight")
                                                        .forGetter(state -> Optional.empty()))
                                        .apply(
                                                instance,
                                                (items, flights, legacy) ->
                                                        new StoredChannel(
                                                                items,
                                                                mergeFlights(flights, legacy))));
    }
}
