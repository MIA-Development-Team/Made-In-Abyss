package com.altnoir.mia.component;

import com.altnoir.mia.item.abs.AArtifactBundle;
import com.altnoir.mia.item.abs.IBundleable;
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

public class ArtifactBundleInventoryComponent implements TooltipComponent {
    public static final ArtifactBundleInventoryComponent EMPTY = new ArtifactBundleInventoryComponent(List.of());
    public static final Codec<ArtifactBundleInventoryComponent> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    ItemStack.CODEC.listOf().fieldOf("artifacts")
                            .forGetter(ArtifactBundleInventoryComponent::getStacks))
            .apply(instance, ArtifactBundleInventoryComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArtifactBundleInventoryComponent> STREAM_CODEC = StreamCodec
            .composite(
                    ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), ArtifactBundleInventoryComponent::getStacks,
                    ArtifactBundleInventoryComponent::new);

    private final List<ItemStack> stacks;
    private final int usage;

    public ArtifactBundleInventoryComponent(List<ItemStack> stacks) {
        this.stacks = stacks;
        this.usage = getUsage();
    }

    public List<ItemStack> getStacks() {
        return stacks;
    }

    public int getUsage() {
        var total = 0;
        for (ItemStack stack : stacks) {
            if (stack.getItem() instanceof IBundleable artifact) {
                total += artifact.getWeight();
            }
        }
        return total;
    }

    public List<ItemStack> allItems() {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        builder.addAll(stacks);
        return builder.build();
    }

    public ArtifactBundleInventoryComponent.Mutable mutable() {
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
        if (!(o instanceof ArtifactBundleInventoryComponent that))
            return false;
        return usage == that.usage
                && Objects.equals(stacks, that.stacks);
    }

    public static class Mutable {
        private final List<ItemStack> stacks;
        private int usage;

        public Mutable(ArtifactBundleInventoryComponent component) {
            this.stacks = new ArrayList<>(component.stacks);
            this.usage = component.getUsage();
        }

        public boolean tryInsert(ItemStack artifactBundle, ItemStack itemStack) {
            if (artifactBundle.getItem() instanceof AArtifactBundle artifactBundleItem) {
                if (itemStack.getItem() instanceof IBundleable artifact
                        && !(itemStack.getItem() instanceof AArtifactBundle)) {
                    if (usage + artifact.getWeight() > artifactBundleItem.getCapacity())
                        return false;
                    usage += artifact.getWeight();
                    stacks.add(itemStack.copy());
                    return true;
                }
            }
            return false;
        }

        public ItemStack pop() {
            if (stacks.isEmpty())
                return ItemStack.EMPTY;

            var stack = stacks.removeFirst().copy();
            if (stack.getItem() instanceof IBundleable item) {
                usage -= item.getWeight();
            }

            return stack;
        }

        public ArtifactBundleInventoryComponent immutable() {
            return new ArtifactBundleInventoryComponent(ImmutableList.copyOf(stacks));
        }
    }
}
