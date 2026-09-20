package com.altnoir.mementoinabyss.content.cup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public final class EndlessCupBlockEntity extends BlockEntity {
    public static final int CAPACITY = Integer.MAX_VALUE;

    public final FluidStacksResourceHandler fluidHandler =
            new FluidStacksResourceHandler(1, CAPACITY) {
                @Override
                protected void onContentsChanged(int index, FluidStack previousContents) {
                    setChanged();
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendBlockUpdated(
                                getBlockPos(), getBlockState(), getBlockState(), 3);
                    }
                }
            };

    public EndlessCupBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fluidHandler.deserialize(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        fluidHandler.serialize(output);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public static void tick(
            Level level, BlockPos pos, BlockState state, EndlessCupBlockEntity blockEntity) {
        try (Transaction transaction = Transaction.openRoot()) {
            blockEntity.fluidHandler.insert(FluidResource.of(Fluids.WATER), CAPACITY, transaction);
            transaction.commit();
        }
    }
}
