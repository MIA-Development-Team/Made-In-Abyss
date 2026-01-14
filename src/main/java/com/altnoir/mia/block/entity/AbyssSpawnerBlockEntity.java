package com.altnoir.mia.block.entity;

import com.altnoir.mia.MIA;
import com.altnoir.mia.core.spawner.AbyssTrialSpawner;
import com.altnoir.mia.core.spawner.records.AbyssTrialSpawnerPattern;
import com.altnoir.mia.init.MiaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class AbyssSpawnerBlockEntity extends BlockEntity implements AbyssTrialSpawner.StateAccessor {
    private final AbyssTrialSpawner abyssSpawner;
    @Nullable
    private ResourceLocation patternId;
    @Nullable
    private AbyssTrialSpawnerPattern cachedPattern;

    public AbyssSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(MiaBlockEntities.ABYSS_SPAWNER_BLOCK_ENTITY.get(), pos, state);
        this.abyssSpawner = new AbyssTrialSpawner(this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("pattern_id", CompoundTag.TAG_STRING)) {
            this.patternId = ResourceLocation.parse(tag.getString("pattern_id"));
            this.refreshPattern();
        }
        
        if (tag.contains("abyss_spawner", CompoundTag.TAG_COMPOUND)) {
            this.abyssSpawner.load(tag.getCompound("abyss_spawner"));
        }
        
        if (this.level != null) {
            this.markUpdated();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (this.patternId != null) {
            tag.putString("pattern_id", this.patternId.toString());
        }
        
        tag.put("abyss_spawner", this.abyssSpawner.save(new CompoundTag()));
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        if (this.patternId != null) {
            tag.putString("pattern_id", this.patternId.toString());
        }
        tag.put("abyss_spawner", this.abyssSpawner.save(new CompoundTag()));
        return tag;
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public AbyssTrialSpawner getAbyssSpawner() {
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
    
    public void setPatternId(@Nullable ResourceLocation patternId) {
        this.patternId = patternId;
        this.refreshPattern();
        this.setChanged();
    }
    
    @Nullable
    @Override
    public ResourceLocation getPatternId() {
        return this.patternId;
    }
    
    public void refreshPattern() {
        if (this.patternId != null) {
            this.cachedPattern = MIA.SPAWNER_MANAGER.getPattern(this.patternId).orElse(null);
            if (this.cachedPattern == null) {
                MIA.LOGGER.warn("Unknown spawner pattern: {}", this.patternId);
            }
        } else {
            this.cachedPattern = null;
        }
    }
    
    @Nullable
    @Override
    public AbyssTrialSpawnerPattern getPattern() {
        return this.cachedPattern;
    }
    
    @Override
    public boolean hasValidPattern() {
        return this.cachedPattern != null;
    }
}
