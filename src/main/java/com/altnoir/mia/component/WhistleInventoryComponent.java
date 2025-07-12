package com.altnoir.mia.component;

import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.item.abs.AbstractAbilityCard;
import com.altnoir.mia.item.abs.AbstractWhistle;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WhistleInventoryComponent implements TooltipComponent {
    public static final WhistleInventoryComponent EMPTY = new WhistleInventoryComponent(List.of());
    public static final Codec<WhistleInventoryComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().fieldOf("abilitycards").forGetter(WhistleInventoryComponent::getStacks)
    ).apply(instance, WhistleInventoryComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, WhistleInventoryComponent> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), WhistleInventoryComponent::getStacks,
            WhistleInventoryComponent::new
    );

    private final List<ItemStack> stacks;
    private final int usage;

    public WhistleInventoryComponent(List<ItemStack> stacks) {
        this.stacks = stacks;
        this.usage = getUsage();
    }

    public List<ItemStack> getStacks() {
        return stacks;
    }

    private int getUsage() {
        return stacks.size();
    }

    public List<ItemStack> allItems() {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        builder.addAll(stacks);
        return builder.build();
    }

    public WhistleInventoryComponent.Mutable mutable() {
        return new Mutable(this);
    }

    public boolean isEmpty() {
        return usage <= 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stacks);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WhistleInventoryComponent that)) return false;
        return usage == that.usage
                && Objects.equals(stacks, that.stacks);
    }

    public static class Mutable{
        private final List<ItemStack> stacks;
        private int usage;

        public Mutable(WhistleInventoryComponent component) {
            this.stacks = new ArrayList<>(component.stacks);
            this.usage = component.getUsage();
        }

        public boolean tryInsert(ItemStack whistle, ItemStack itemStack) {
            if (whistle.getItem() instanceof AbstractWhistle whistleItem) {
                if (itemStack.getItem() instanceof AbstractAbilityCard item) {
                    if (usage + item.getWeight() > whistleItem.getCapacity()) return false;
                    usage += item.getWeight();
                    stacks.add(itemStack.copy());
                    return true;
                }
            }
            return false;
        }

        public ItemStack pop() {
            if (stacks.isEmpty()) return ItemStack.EMPTY;

            var stack = stacks.removeFirst();
            if (stack.getItem() instanceof AbstractAbilityCard item){
                usage -= item.getWeight();
            };
            return stack;
        }

        public WhistleInventoryComponent immutable() {
            return new WhistleInventoryComponent(ImmutableList.copyOf(stacks));
        }
    }
}
