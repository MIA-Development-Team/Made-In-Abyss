package com.altnoir.mementoinabyss.content.lamptube;

import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatResource;
import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatStack;
import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatStacksResourceHandler;
import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public final class AmethystTubeBlockEntity extends BlockEntity {
    public static final int HEAT_CAPACITY = HeatType.UNIT * 8;

    public final HeatStacksResourceHandler heatHandler =
            new HeatStacksResourceHandler(1, HEAT_CAPACITY) {
                @Override
                protected void onContentsChanged(int index, HeatStack previousContents) {
                    setChanged();
                }
            };

    public AmethystTubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public HeatStack storedHeat() {
        HeatResource resource = heatHandler.getResource(0);
        return new HeatStack(resource, heatHandler.getAmountAsInt(0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        heatHandler.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        heatHandler.deserialize(input);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
