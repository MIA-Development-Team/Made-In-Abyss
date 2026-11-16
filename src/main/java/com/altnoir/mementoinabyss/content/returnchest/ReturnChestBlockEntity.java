package com.altnoir.mementoinabyss.content.returnchest;

import com.altnoir.mementoinabyss.content.returnchest.client.ReturnChestAmbience;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class ReturnChestBlockEntity extends BlockEntity {
    private final ReturnChestItemAccess itemAccess = new ReturnChestItemAccess(this);
    private @Nullable BlockPos center;
    private @Nullable UUID structure;
    private int connection;

    public ReturnChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    void link(BlockPos center, UUID structure) {
        this.center = center.immutable();
        this.structure = structure;
        setChanged();
    }

    boolean belongsTo(BlockPos center, UUID structure) {
        return center.equals(this.center) && structure.equals(this.structure);
    }

    boolean matchesStructure(UUID structure) {
        return structure.equals(this.structure);
    }

    @Nullable ReturnChestBlockEntity controller(ServerLevel level) {
        if (isRemoved() || center == null || structure == null) return null;
        BlockPos base = center.below();
        if (!ReturnChestLayout.parts().contains(getBlockPos().subtract(base))) return null;
        if (!level.isLoaded(center)
                || !(level.getBlockEntity(center) instanceof ReturnChestBlockEntity core)
                || !ReturnChests.isCore(core.getBlockState())
                || !core.belongsTo(center, structure)) {
            return null;
        }
        for (BlockPos offset : ReturnChestLayout.parts()) {
            BlockPos pos = base.offset(offset);
            if (!level.isLoaded(pos)
                    || !(level.getBlockEntity(pos) instanceof ReturnChestBlockEntity part)
                    || !part.belongsTo(center, structure)
                    || ReturnChests.part(part.getBlockState())
                            != ReturnChestLayout.partOf(offset)) {
                return null;
            }
        }
        return core;
    }

    int connection() {
        return connection;
    }

    public WorldlyContainer itemAccess() {
        return itemAccess;
    }

    void bind(ServerLevel level, int connection) {
        if (this.connection == connection) return;
        this.connection = connection;
        setChanged();
        applyTint(level);
        closeMenus(level);
        if (connection != 0) {
            ReturnChests.activate(level, getBlockPos());
        }
    }

    private void applyTint(ServerLevel level) {
        BlockPos pos = getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!ReturnChests.isCore(state)) return;
        int tint = ReturnChestBlock.tintForConnection(connection);
        if (ReturnChests.tint(state) != tint) {
            level.setBlock(pos, state.setValue(ReturnChestBlock.TINT, tint), Block.UPDATE_ALL);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide()) {
            ReturnChestAmbience.stop(this);
        }
    }

    void closeMenus(ServerLevel level) {
        for (ServerPlayer viewer : level.players()) {
            if (viewer.containerMenu instanceof ReturnChestMenu menu
                    && menu.isFor(getBlockPos(), structure)) {
                viewer.closeContainer();
            }
        }
    }

    boolean mayDrop() {
        return structure != null
                && level instanceof ServerLevel server
                && !ReturnChestCleanup.get(server).isPending(getBlockPos(), structure);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel server)
                || server.restoringBlockSnapshots
                || center == null
                || structure == null) {
            return;
        }
        ReturnChestCleanup cleanup = ReturnChestCleanup.get(server);
        if (cleanup.isPending(pos, structure)) return;
        ReturnChestBlockEntity core = ReturnChests.core(server, center, structure);
        if ((core == null ? connection : core.connection) != 0) {
            ReturnChests.deactivate(server, center);
        }
        if (core != null) {
            core.closeMenus(server);
        } else if (pos.equals(center)) {
            closeMenus(server);
        }
        BlockPos base = center.below();
        for (BlockPos offset : ReturnChestLayout.parts()) {
            BlockPos member = base.offset(offset);
            if (member.equals(pos)) continue;
            if (!level.isLoaded(member)) {
                cleanup.queue(member, structure);
            } else if (level.getBlockEntity(member) instanceof ReturnChestBlockEntity part
                    && part.belongsTo(center, structure)
                    && ReturnChestCleanup.removePart(server, member)) {
                ReturnChests.breakParticles(server, member, part.getBlockState());
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        center = input.read("center", BlockPos.CODEC).orElse(null);
        structure = input.read("structure", UUIDUtil.CODEC).orElse(null);
        connection = input.getIntOr("connection", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("center", BlockPos.CODEC, center);
        output.storeNullable("structure", UUIDUtil.CODEC, structure);
        output.putInt("connection", connection);
    }
}
