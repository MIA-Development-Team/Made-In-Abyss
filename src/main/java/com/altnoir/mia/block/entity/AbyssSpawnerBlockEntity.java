package com.altnoir.mia.block.entity;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.AbyssSpawnerBlock;
import com.altnoir.mia.init.MiaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AbyssSpawnerBlockEntity extends BlockEntity implements Spawner, TrialSpawner.StateAccessor {
    private TrialSpawner abyssSpawner;

    public AbyssSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(MiaBlockEntities.ABYSS_SPAWNER.get(), pos, state);
        PlayerDetector playerdetector = PlayerDetector.NO_CREATIVE_PLAYERS;
        PlayerDetector.EntitySelector playerdetector$entityselector = PlayerDetector.EntitySelector.SELECT_FROM_LEVEL;
        this.abyssSpawner = new TrialSpawner(this, playerdetector, playerdetector$entityselector);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("normal_config")) {
            CompoundTag compoundtag = tag.getCompound("normal_config").copy();
            tag.put("ominous_config", compoundtag.merge(tag.getCompound("ominous_config")));
        }

        this.abyssSpawner.codec().parse(NbtOps.INSTANCE, tag).resultOrPartial(MIA.LOGGER::error).ifPresent(p_311911_ -> this.abyssSpawner = p_311911_);
        if (this.level != null) {
            this.markUpdated();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.abyssSpawner
                .codec()
                .encodeStart(NbtOps.INSTANCE, this.abyssSpawner)
                .ifSuccess(tag1 -> tag.merge((CompoundTag) tag1))
                .ifError(error -> MIA.LOGGER.warn("Failed to encode TrialSpawner {}", error.message()));
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.abyssSpawner.getData().getUpdateTag(this.getBlockState().getValue(AbyssSpawnerBlock.STATE));
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public void setEntityId(EntityType<?> entityType, RandomSource random) {
        this.abyssSpawner.getData().setEntityId(this.abyssSpawner, random, entityType);
        this.setChanged();
    }

    public TrialSpawner getAbyssSpawner() {
        return this.abyssSpawner;
    }

    @Override
    public TrialSpawnerState getState() {
        return !this.getBlockState().hasProperty(BlockStateProperties.TRIAL_SPAWNER_STATE)
                ? TrialSpawnerState.INACTIVE
                : this.getBlockState().getValue(BlockStateProperties.TRIAL_SPAWNER_STATE);
    }

    @Override
    public void setState(Level level, TrialSpawnerState state) {
        this.setChanged();
        level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(BlockStateProperties.TRIAL_SPAWNER_STATE, state));
    }

    @Override
    public void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
}
