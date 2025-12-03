package com.altnoir.mia.core.spawner;

import com.altnoir.mia.MIA;
import com.altnoir.mia.core.spawner.records.AbyssTrialSpawnerPattern;
import com.altnoir.mia.core.spawner.records.EntityTableInstance;
import com.altnoir.mia.core.spawner.records.LootTableInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AbyssTrialSpawner {
    private static final int PLAYER_DETECTION_RANGE = 14;
    private static final int COOLDOWN_TICKS = 36000;
    private static final int SPAWN_DELAY_MIN = 40;
    private static final int SPAWN_DELAY_MAX = 80;
    private static final int EJECT_DELAY = 8;
    private static final int WAITING_FOR_EJECTION_TICKS = 40;
    
    private final StateAccessor stateAccessor;
    private final RandomSource random = RandomSource.create();
    
    private int spawnDelay = 20;
    private int totalMobsToSpawn = 0;
    private int mobsSpawned = 0;
    private int cooldownEndsAt = 0;
    private final Set<UUID> trackedPlayers = new HashSet<>();
    private final Set<UUID> trackedMobs = new HashSet<>();
    
    private final List<ItemStack> rewardQueue = new ArrayList<>();
    private int ejectDelay = 0;
    private int waitingForEjectionTicks = 0;
    
    @Nullable
    private Entity displayEntity;
    private double spin;
    private double oSpin;

    public AbyssTrialSpawner(StateAccessor stateAccessor) {
        this.stateAccessor = stateAccessor;
    }
    
    public void tickServer(ServerLevel level, BlockPos pos, boolean ominous) {
        var currentState = this.stateAccessor.getState();
        
        this.trackedMobs.removeIf(uuid -> {
            var entity = level.getEntity(uuid);
            return entity == null || !entity.isAlive() || entity.isRemoved();
        });
        
        switch (currentState) {
            case INACTIVE -> tickInactive(level, pos);
            case WAITING_FOR_PLAYERS -> tickWaitingForPlayers(level, pos, ominous);
            case ACTIVE -> tickActive(level, pos, ominous);
            case WAITING_FOR_REWARD_EJECTION -> tickWaitingForRewardEjection(level, pos, ominous);
            case EJECTING_REWARD -> tickEjectingReward(level, pos, ominous);
            case COOLDOWN -> tickCooldown(level, pos);
        }
    }
    
    private void tickInactive(ServerLevel level, BlockPos pos) {
        if (!hasValidPattern()) return;
        
        if (hasNearbyPlayers(level, pos)) {
            this.stateAccessor.setState(level, TrialSpawnerState.WAITING_FOR_PLAYERS);
        }
    }
    
    private void tickWaitingForPlayers(ServerLevel level, BlockPos pos, boolean ominous) {
        if (!hasValidPattern()) return;
        
        if (!hasNearbyPlayers(level, pos)) {
            this.stateAccessor.setState(level, TrialSpawnerState.INACTIVE);
            return;
        }
        
        var players = getNearbyPlayers(level, pos);
        if (!players.isEmpty()) {
            this.trackedPlayers.clear();
            players.forEach(p -> this.trackedPlayers.add(p.getUUID()));
            
            var pattern = getPattern();
            this.totalMobsToSpawn = Objects.requireNonNull(pattern).baseMobs() + (players.size() * pattern.mobsPerPlayer());
            this.mobsSpawned = 0;
            this.spawnDelay = SPAWN_DELAY_MIN;
            
            this.stateAccessor.setState(level, TrialSpawnerState.ACTIVE);
            level.playSound(null, pos, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(level.getBlockState(pos)));
        }
    }
    
    private void tickActive(ServerLevel level, BlockPos pos, boolean ominous) {
        if (!hasValidPattern()) return;
        
        var pattern = getPattern();
        
        updateTrackedPlayers(level, pos);
        
        if (this.mobsSpawned >= this.totalMobsToSpawn && this.trackedMobs.isEmpty()) {
            this.rewardQueue.clear();
            this.waitingForEjectionTicks = 0;
            this.stateAccessor.setState(level, TrialSpawnerState.WAITING_FOR_REWARD_EJECTION);
            level.playSound(null, pos, SoundEvents.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }
        
        if (this.spawnDelay > 0) {
            this.spawnDelay--;
        } else if (this.mobsSpawned < this.totalMobsToSpawn) {
            for (int i = 0; i < Objects.requireNonNull(pattern).spawnPerTick() && this.mobsSpawned < this.totalMobsToSpawn; i++) {
                if (trySpawnMob(level, pos, pattern, ominous)) {
                    this.mobsSpawned++;
                }
            }
            this.spawnDelay = this.random.nextInt(SPAWN_DELAY_MAX - SPAWN_DELAY_MIN) + SPAWN_DELAY_MIN;
        }
    }
    
    private void tickWaitingForRewardEjection(ServerLevel level, BlockPos pos, boolean ominous) {
        if (this.trackedPlayers.isEmpty()) {
            this.stateAccessor.setState(level, TrialSpawnerState.COOLDOWN);
            this.cooldownEndsAt = level.getServer().getTickCount() + COOLDOWN_TICKS;
            return;
        }
        
        if (!hasValidPattern()) {
            this.stateAccessor.setState(level, TrialSpawnerState.COOLDOWN);
            this.cooldownEndsAt = level.getServer().getTickCount() + COOLDOWN_TICKS;
            return;
        }
        
        if (this.rewardQueue.isEmpty() && this.waitingForEjectionTicks == 0) {
            var pattern = getPattern();
            for (var playerUuid : this.trackedPlayers) {
                var player = level.getPlayerByUUID(playerUuid);
                if (player != null && player.isAlive()) {
                    collectRewardsForPlayer(level, pos, player, pattern);
                }
            }
            this.waitingForEjectionTicks = WAITING_FOR_EJECTION_TICKS;
        }
        
        if (this.waitingForEjectionTicks > 0) {
            this.waitingForEjectionTicks--;
            return;
        }
        
        this.ejectDelay = 0;
        this.stateAccessor.setState(level, TrialSpawnerState.EJECTING_REWARD);
    }
    
    private void tickEjectingReward(ServerLevel level, BlockPos pos, boolean ominous) {
        if (this.rewardQueue.isEmpty()) {
            this.trackedPlayers.clear();
            this.stateAccessor.setState(level, TrialSpawnerState.COOLDOWN);
            this.cooldownEndsAt = level.getServer().getTickCount() + COOLDOWN_TICKS;
            return;
        }
        
        if (this.ejectDelay > 0) {
            this.ejectDelay--;
            return;
        }
        
        var stack = this.rewardQueue.remove(0);
        ejectItem(level, pos, stack);
        
        level.playSound(null, pos, SoundEvents.TRIAL_SPAWNER_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        this.ejectDelay = EJECT_DELAY;
    }
    
    private void tickCooldown(ServerLevel level, BlockPos pos) {
        if (level.getServer().getTickCount() >= this.cooldownEndsAt) {
            this.stateAccessor.setState(level, TrialSpawnerState.INACTIVE);
            this.totalMobsToSpawn = 0;
            this.mobsSpawned = 0;
        }
    }
    
    public void tickClient(Level level, BlockPos pos, boolean ominous) {
        var state = this.stateAccessor.getState();
        
        this.oSpin = this.spin;
        this.spin = (this.spin + (double)this.getSpinningProgress(state)) % 360.0;
        
        if (state == TrialSpawnerState.ACTIVE) {
            if (this.random.nextInt(3) == 0) {
                level.addParticle(
                    ParticleTypes.SMALL_FLAME,
                    pos.getX() + 0.5 + (this.random.nextDouble() - 0.5),
                    pos.getY() + 0.5 + (this.random.nextDouble() - 0.5),
                    pos.getZ() + 0.5 + (this.random.nextDouble() - 0.5),
                    0, 0.05, 0
                );
            }
        }
    }
    
    private int getSpinningProgress(TrialSpawnerState state) {
        return switch (state) {
            case INACTIVE -> 0;
            case WAITING_FOR_PLAYERS -> 1;
            case ACTIVE -> 4;
            case WAITING_FOR_REWARD_EJECTION, EJECTING_REWARD -> 2;
            case COOLDOWN -> 1;
        };
    }
    
    private boolean trySpawnMob(ServerLevel level, BlockPos pos, AbyssTrialSpawnerPattern pattern, boolean ominous) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        
        var selectedEntity = WeightedRandom.getRandomItem(this.random, pattern.entityTables().unwrap());
        if (selectedEntity.isEmpty()) {
            return false;
        }
        
        var entityType = selectedEntity.get().getEntityType();
        
        int range = pattern.spawnRange();
        for (int attempts = 0; attempts < 20; attempts++) {
            double x = pos.getX() + (this.random.nextDouble() - 0.5) * range * 2;
            double y = pos.getY() + this.random.nextInt(3) - 1;
            double z = pos.getZ() + (this.random.nextDouble() - 0.5) * range * 2;
            
            var spawnPos = BlockPos.containing(x, y, z);
            
            if (level.noCollision(entityType.getSpawnAABB(x, y, z))) {
                var entity = entityType.create(level, null, spawnPos, MobSpawnType.TRIAL_SPAWNER, false, false);
                if (entity != null) {
                    if (entity instanceof Mob mob) {
                        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.TRIAL_SPAWNER, null);
                    }
                    
                    level.addFreshEntity(entity);
                    this.trackedMobs.add(entity.getUUID());
                    
                    level.playSound(null, pos, SoundEvents.TRIAL_SPAWNER_SPAWN_MOB, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(entity, GameEvent.ENTITY_PLACE, spawnPos);
                    
                    level.sendParticles(
                        ParticleTypes.POOF,
                        x, y + 0.5, z,
                        10, 0.2, 0.2, 0.2, 0.02
                    );
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private void collectRewardsForPlayer(ServerLevel level, BlockPos pos, Player player, AbyssTrialSpawnerPattern pattern) {
        var selectedLoot = WeightedRandom.getRandomItem(this.random, pattern.lootTables().unwrap());
        if (selectedLoot.isEmpty()) {
            return;
        }
        
        var lootTable = level.getServer().reloadableRegistries().getLootTable(selectedLoot.get().getLootTableKey());
        if (lootTable == LootTable.EMPTY) {
            MIA.LOGGER.warn("Loot table not found: {}", selectedLoot.get().getLootTableKey().location());
            return;
        }
        
        var lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.CHEST);
        
        this.rewardQueue.addAll(lootTable.getRandomItems(lootParams));
    }
    
    private void ejectItem(ServerLevel level, BlockPos pos, ItemStack stack) {
        var x = pos.getX() + 0.5;
        var y = pos.getY() + 1.2;
        var z = pos.getZ() + 0.5;
        
        var angle = this.random.nextDouble() * Math.PI * 2;
        var speed = 0.1 + this.random.nextDouble() * 0.05;
        
        var itemEntity = new net.minecraft.world.entity.item.ItemEntity(
            level, x, y, z, stack,
            Math.cos(angle) * speed,
            0.2 + this.random.nextDouble() * 0.1,
            Math.sin(angle) * speed
        );
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
        
        level.sendParticles(
            ParticleTypes.SMALL_FLAME,
            x, y, z,
            5, 0.1, 0.1, 0.1, 0.02
        );
    }
    
    private boolean hasNearbyPlayers(ServerLevel level, BlockPos pos) {
        return !getNearbyPlayers(level, pos).isEmpty();
    }
    
    private List<ServerPlayer> getNearbyPlayers(ServerLevel level, BlockPos pos) {
        var detectionBox = new AABB(pos).inflate(PLAYER_DETECTION_RANGE);
        return level.getPlayers(player -> 
            !player.isCreative() && 
            !player.isSpectator() && 
            player.isAlive() && 
            detectionBox.contains(player.position())
        );
    }
    
    private void updateTrackedPlayers(ServerLevel level, BlockPos pos) {
        for (var player : getNearbyPlayers(level, pos)) {
            this.trackedPlayers.add(player.getUUID());
        }
        
        if (hasValidPattern()) {
            var pattern = getPattern();
            var newTotal = Objects.requireNonNull(pattern).baseMobs() + (this.trackedPlayers.size() * pattern.mobsPerPlayer());
            if (newTotal > this.totalMobsToSpawn) {
                this.totalMobsToSpawn = newTotal;
            }
        }
    }
    
    private boolean hasValidPattern() {
        return this.stateAccessor.hasValidPattern();
    }
    
    @Nullable
    private AbyssTrialSpawnerPattern getPattern() {
        return this.stateAccessor.getPattern();
    }
    
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("spawn_delay", this.spawnDelay);
        tag.putInt("total_mobs", this.totalMobsToSpawn);
        tag.putInt("mobs_spawned", this.mobsSpawned);
        tag.putInt("cooldown_ends_at", this.cooldownEndsAt);
        
        var playersTag = new CompoundTag();
        var i = 0;
        for (var uuid : this.trackedPlayers) {
            playersTag.putUUID("player_" + i, uuid);
            i++;
        }
        playersTag.putInt("count", i);
        tag.put("tracked_players", playersTag);
        
        var mobsTag = new CompoundTag();
        i = 0;
        for (var uuid : this.trackedMobs) {
            mobsTag.putUUID("mob_" + i, uuid);
            i++;
        }
        mobsTag.putInt("count", i);
        tag.put("tracked_mobs", mobsTag);
        
        return tag;
    }
    
    public void load(CompoundTag tag) {
        this.spawnDelay = tag.getInt("spawn_delay");
        this.totalMobsToSpawn = tag.getInt("total_mobs");
        this.mobsSpawned = tag.getInt("mobs_spawned");
        this.cooldownEndsAt = tag.getInt("cooldown_ends_at");
        
        this.trackedPlayers.clear();
        if (tag.contains("tracked_players", CompoundTag.TAG_COMPOUND)) {
            var playersTag = tag.getCompound("tracked_players");
            var count = playersTag.getInt("count");
            for (var i = 0; i < count; i++) {
                if (playersTag.hasUUID("player_" + i)) {
                    this.trackedPlayers.add(playersTag.getUUID("player_" + i));
                }
            }
        }
        
        this.trackedMobs.clear();
        if (tag.contains("tracked_mobs", CompoundTag.TAG_COMPOUND)) {
            var mobsTag = tag.getCompound("tracked_mobs");
            var count = mobsTag.getInt("count");
            for (var i = 0; i < count; i++) {
                if (mobsTag.hasUUID("mob_" + i)) {
                    this.trackedMobs.add(mobsTag.getUUID("mob_" + i));
                }
            }
        }
    }
    
    public double getSpin() {
        return this.spin;
    }
    
    public double getOSpin() {
        return this.oSpin;
    }
    
    @Nullable
    public Entity getOrCreateDisplayEntity(Level level, BlockPos pos) {
        if (this.displayEntity == null && hasValidPattern() && level instanceof ServerLevel serverLevel) {
            var pattern = getPattern();
            var selected = WeightedRandom.getRandomItem(this.random, pattern.entityTables().unwrap());
            if (selected.isPresent()) {
                this.displayEntity = selected.get().getEntityType().create(serverLevel, null, pos, MobSpawnType.TRIAL_SPAWNER, false, false);
            }
        }
        return this.displayEntity;
    }
    
    public interface StateAccessor {
        TrialSpawnerState getState();
        void setState(Level level, TrialSpawnerState state);
        void markUpdated();
        @Nullable ResourceLocation getPatternId();
        @Nullable AbyssTrialSpawnerPattern getPattern();
        boolean hasValidPattern();
    }
}
