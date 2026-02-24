package com.altnoir.mia.common.block.entity;

import com.altnoir.mia.common.block.MiaBrushableBlock;
import com.altnoir.mia.init.MiaBlockEntities;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;

public class MiaBrushableBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LOOT_TABLE_TAG = "LootTable";
    private static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
    private static final String HIT_DIRECTION_TAG = "hit_direction";
    private static final String ITEM_TAG = "item";
    private static final int BRUSH_COOLDOWN_TICKS = 10;
    private static final int BRUSH_RESET_TICKS = 40;
    private static final int REQUIRED_BRUSHES_TO_BREAK = 10;
    private int brushCount;
    private long brushCountResetsAtTick;
    private long coolDownEndsAtTick;
    private ItemStack item = ItemStack.EMPTY;
    @Nullable
    private Direction hitDirection;
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    public MiaBrushableBlockEntity(BlockPos pos, BlockState blockState) {
        super(MiaBlockEntities.BRUSHABLE_ENTITY.get(), pos, blockState);
    }

    public boolean brush(long startTick, Player player, Direction hitDirection) {
        if (this.hitDirection == null) {
            this.hitDirection = hitDirection;
        }

        this.brushCountResetsAtTick = startTick + 40L;
        if (startTick >= this.coolDownEndsAtTick && this.level instanceof ServerLevel) {
            this.coolDownEndsAtTick = startTick + 10L;
            this.unpackLootTable(player);
            int i = this.getCompletionState();
            if (++this.brushCount >= 10) {
                this.brushingCompleted(player);
                return true;
            } else {
                this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 2);
                int j = this.getCompletionState();
                if (i != j) {
                    BlockState blockstate = this.getBlockState();
                    BlockState blockstate1 = blockstate.setValue(BlockStateProperties.DUSTED, Integer.valueOf(j));
                    this.level.setBlock(this.getBlockPos(), blockstate1, 3);
                }

                return false;
            }
        } else {
            return false;
        }
    }

    public void unpackLootTable(Player player) {
        if (this.lootTable != null && this.level != null && !this.level.isClientSide() && this.level.getServer() != null) {
            LootTable loottable = this.level.getServer().reloadableRegistries().getLootTable(this.lootTable);
            if (player instanceof ServerPlayer serverplayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger(serverplayer, this.lootTable);
            }

            LootParams lootparams = new LootParams.Builder((ServerLevel) this.level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                    .withLuck(player.getLuck())
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .create(LootContextParamSets.CHEST);
            ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams, this.lootTableSeed);

            this.item = switch (objectarraylist.size()) {
                case 0 -> ItemStack.EMPTY;
                case 1 -> (ItemStack) objectarraylist.get(0);
                default -> {
                    LOGGER.warn("Expected max 1 loot from loot table {}, but got {}", this.lootTable.location(), objectarraylist.size());
                    yield objectarraylist.get(0);
                }
            };
            this.lootTable = null;
            this.setChanged();
        }
    }

    private void brushingCompleted(Player player) {
        if (this.level != null && this.level.getServer() != null) {
            this.dropContent(player);
            Block block;
            BlockState blockstate = this.getBlockState();

            if (blockstate.getBlock() instanceof MiaBrushableBlock brushable) {
                this.level.playLocalSound(this.getBlockPos(), brushable.getBrushCompletedSound(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
                block = brushable.getTurnsInto();
            } else {
                block = Blocks.AIR;
            }

            this.level.setBlock(this.worldPosition, block.defaultBlockState(), 3);
        }
    }

    private void dropContent(Player player) {
        if (this.level != null && this.level.getServer() != null) {
            this.unpackLootTable(player);
            if (!this.item.isEmpty()) {
                double d0 = (double) EntityType.ITEM.getWidth();
                double d1 = 1.0 - d0;
                double d2 = d0 / 2.0;
                Direction direction = Objects.requireNonNullElse(this.hitDirection, Direction.UP);
                BlockPos blockpos = this.worldPosition.relative(direction, 1);
                double d3 = (double) blockpos.getX() + 0.5 * d1 + d2;
                double d4 = (double) blockpos.getY() + 0.5 + (double) (EntityType.ITEM.getHeight() / 2.0F);
                double d5 = (double) blockpos.getZ() + 0.5 * d1 + d2;
                ItemEntity itementity = new ItemEntity(this.level, d3, d4, d5, this.item.split(this.level.random.nextInt(21) + 10));
                itementity.setDeltaMovement(Vec3.ZERO);
                this.level.addFreshEntity(itementity);
                this.item = ItemStack.EMPTY;
            }
        }
    }

    public void checkReset() {
        if (this.level != null) {
            if (this.brushCount != 0 && this.level.getGameTime() >= this.brushCountResetsAtTick) {
                int i = this.getCompletionState();
                this.brushCount = Math.max(0, this.brushCount - 2);
                int j = this.getCompletionState();
                if (i != j) {
                    this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(BlockStateProperties.DUSTED, Integer.valueOf(j)), 3);
                }

                int k = 4;
                this.brushCountResetsAtTick = this.level.getGameTime() + 4L;
            }

            if (this.brushCount == 0) {
                this.hitDirection = null;
                this.brushCountResetsAtTick = 0L;
                this.coolDownEndsAtTick = 0L;
            } else {
                this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 2);
            }
        }
    }

    private boolean tryLoadLootTable(CompoundTag tag) {
        if (tag.contains("LootTable", 8)) {
            this.lootTable = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(tag.getString("LootTable")));
            this.lootTableSeed = tag.getLong("LootTableSeed");
            return true;
        } else {
            return false;
        }
    }

    private boolean trySaveLootTable(CompoundTag tag) {
        if (this.lootTable == null) {
            return false;
        } else {
            tag.putString("LootTable", this.lootTable.location().toString());
            if (this.lootTableSeed != 0L) {
                tag.putLong("LootTableSeed", this.lootTableSeed);
            }

            return true;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag compoundtag = super.getUpdateTag(registries);
        if (this.hitDirection != null) {
            compoundtag.putInt("hit_direction", this.hitDirection.ordinal());
        }

        if (!this.item.isEmpty()) {
            compoundtag.put("item", this.item.save(registries));
        }

        return compoundtag;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!this.tryLoadLootTable(tag) && tag.contains("item")) {
            this.item = ItemStack.parse(registries, tag.getCompound("item")).orElse(ItemStack.EMPTY);
        } else {
            this.item = ItemStack.EMPTY;
        }

        if (tag.contains("hit_direction")) {
            this.hitDirection = Direction.values()[tag.getInt("hit_direction")];
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag) && !this.item.isEmpty()) {
            tag.put("item", this.item.save(registries));
        }
    }

    public void setLootTable(ResourceKey<LootTable> lootTable, long seed) {
        this.lootTable = lootTable;
        this.lootTableSeed = seed;
    }

    private int getCompletionState() {
        if (this.brushCount == 0) {
            return 0;
        } else if (this.brushCount < 3) {
            return 1;
        } else {
            return this.brushCount < 6 ? 2 : 3;
        }
    }

    @Nullable
    public Direction getHitDirection() {
        return this.hitDirection;
    }

    public ItemStack getItem() {
        return this.item;
    }
}
