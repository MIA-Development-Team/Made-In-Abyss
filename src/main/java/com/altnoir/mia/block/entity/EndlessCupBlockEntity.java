package com.altnoir.mia.block.entity;

import com.altnoir.mia.init.MiaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class EndlessCupBlockEntity extends BlockEntity {
    public FluidTank fluidTank = new FluidTank(Integer.MAX_VALUE) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level instanceof ServerLevel level) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public EndlessCupBlockEntity(BlockPos pos, BlockState blockState) {
        super(MiaBlockEntities.ENDLESS_CUP_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.fluidTank.readFromNBT(registries, tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.fluidTank.writeToNBT(registries, tag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EndlessCupBlockEntity blockEntity) {
        blockEntity.fluidTank.fill(new FluidStack(Fluids.WATER, Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE);
    }
}
