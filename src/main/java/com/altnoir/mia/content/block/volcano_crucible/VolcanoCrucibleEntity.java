package com.altnoir.mia.content.block.volcano_crucible;

import com.altnoir.mia.content.entity.BlockEntityRegister;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class VolcanoCrucibleEntity extends BlockEntity {
    private final FluidTank fluidTank;
    private final LazyOptional<IFluidHandler> handler;
    private int capacity = 1000;
    public int FILL_LAVA = 1;

    private static final Logger LOGGER = LogUtils.getLogger();

    public VolcanoCrucibleEntity(BlockPos pPos, BlockState pBlockState){
        super(BlockEntityRegister.SEVENTH_CRUCIBLE_ENTITY.get(), pPos, pBlockState);
        this.fluidTank = new FluidTank(capacity){
            // 只允许熔岩
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().isSame(Fluids.LAVA);
            }

            @Override
            protected void onContentsChanged() {
                VolcanoCrucibleEntity.this.setChanged();
                VolcanoCrucibleEntity.this.Sync();

            }
        };
        handler = LazyOptional.of(() -> fluidTank);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if(cap == ForgeCapabilities.FLUID_HANDLER){
            return handler.cast();
        }
        return super.getCapability(cap);
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getFillLava(){
        return FILL_LAVA;
    }

    public void setCapacity(int capacity) {
        fluidTank.setCapacity(capacity);
        this.capacity = capacity;
    }

    //TODO:我写不来render，等老大抓一个幸运程序
    @Override
    public @NotNull ModelData getModelData() {
        // TODO
        return super.getModelData();
    }

    public void Sync(){
        if(!level.isClientSide){
            level.sendBlockUpdated(this.getBlockPos(),this.getBlockState(),this.getBlockState(),0);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("fluidTank",fluidTank.writeToNBT(new CompoundTag()));
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        CompoundTag tag = fluidTank.writeToNBT(new CompoundTag());
        pTag.put("fluidTank",tag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        fluidTank.readFromNBT(pTag.getCompound("fluidTank"));
    }

    public static void serverTick(Level pLevel, BlockPos pPos, BlockState pBlockState, BlockEntity pBlockEntity) {
        if (pBlockEntity instanceof VolcanoCrucibleEntity volcanoCrucibleEntity) {
            FluidStack fluidStack = new FluidStack(Fluids.LAVA, volcanoCrucibleEntity.getFillLava());
            volcanoCrucibleEntity.fluidTank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        }
    }
}
