package com.altnoir.mia.core.event.common;

import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.init.MiaAttachments;
import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.TriState;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AbyssMobEvent {
    private static final short CHUNK_RADIUS = 28;
    private static final List<EntityType<? extends Mob>> riderTypes = List.of(
            EntityType.SKELETON,
            EntityType.BOGGED
    );

    public static void onCheckSpawn(Mob mob, ServerLevelAccessor level, MobSpawnType type) {
        if (!MiaConfig.abyssMobLevelSwitch) return;
        var pos = mob.blockPosition();

        long chunkX = pos.getX() >> 4, chunkZ = pos.getZ() >> 4; // 等价于 / 16
        long distance = (long) chunkX * chunkX + chunkZ * chunkZ;


        if (distance <= CHUNK_RADIUS * CHUNK_RADIUS) return;
        double euclideanDistance = Math.sqrt(distance);

        if (level instanceof ServerLevel serverLevel && serverLevel.dimension() == MiaDimensions.THE_ABYSS_LEVEL) {
            if (mob instanceof Enemy || mob instanceof Llama || mob instanceof SkeletonHorse) {
                double maxHealth = mob.getMaxHealth();

                boolean reverse = MiaConfig.abyssMobLevelIncreasingCurve != MiaConfig.AbyssMobLevelIncreasingCurve.FROM_FAST_TO_SLOW;
                int mobLevel = calculateMobLevel(euclideanDistance, reverse);

                if (mobLevel <= 0) return;

                // 从半径28个区块开始计算
                maxHealth += mobLevel;
                maxHealth = Math.min(maxHealth, 2048.0);
                Objects.requireNonNull(mob.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(maxHealth);
                mob.setHealth((float) maxHealth);

                mob.setData(MiaAttachments.ABYSS_MOB_LEVEL.get(), mobLevel);
                mob.setCustomName(Component.literal("Lv: " + mobLevel).withStyle(ChatFormatting.GREEN));

                if (serverLevel.getRandom().nextBoolean()) {
                    createRidingSkeleton(serverLevel, mob);
                }
            }
        }
    }

    public static void onLivingDrops(LivingEntity entity, Collection<ItemEntity> drops, DamageSource damageSource) {
        if (!MiaConfig.abyssMobLevelSwitch) return;
        if (entity.hasData(MiaAttachments.ABYSS_MOB_LEVEL.get())) {
            int mobLevel = entity.getData(MiaAttachments.ABYSS_MOB_LEVEL.get());
            var level = entity.level();

            for (var item : drops) {
                var originalStack = item.getItem();
                int amount = originalStack.getCount() * mobLevel;

                // 拆分物品堆叠，确保每个堆叠不超过最大限制
                while (amount > 0) {
                    int stackSize = Math.min(amount, originalStack.getMaxStackSize());
                    amount -= stackSize;

                    var newItemEntity = new ItemEntity(
                            level,
                            item.getX(), item.getY(), item.getZ(),
                            originalStack.copyWithCount(stackSize)
                    );
                    level.addFreshEntity(newItemEntity);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static TriState onRenderMobLevel(Entity entity) {
        if (entity instanceof Mob mob) {
            var name = mob.getCustomName();
            if (name != null && name.getString().startsWith("Lv")) {
                return TriState.TRUE;
            }
        }
        return null;
    }

    private static int calculateMobLevel(double euclideanDistance, boolean reverse) {
        if (!reverse) {
            int level = (int) ((Math.sqrt(8 * (euclideanDistance - CHUNK_RADIUS)) - 1) / 2) + 1;
            return Math.min(level, 1000);
        } else {
            double remaining = (499501 + CHUNK_RADIUS) - euclideanDistance;
            if (remaining <= 0) return 1000;

            int x = (int) ((Math.sqrt(8 * remaining) - 1) / 2) + 1;
            int level = 1000 - x;

            return Math.max(0, Math.min(level, 1000));
        }
    }

    private static void createRidingSkeleton(ServerLevel serverLevel, Mob mob) {
        var type = riderTypes.get(serverLevel.getRandom().nextInt(riderTypes.size()));
        var rider = type.create(serverLevel, null, mob.blockPosition(), MobSpawnType.EVENT, false, false);

        if (!(rider instanceof LivingEntity entity))
            return;

        entity.setItemSlot(EquipmentSlot.MAINHAND, Items.BOW.getDefaultInstance());
        rider.startRiding(mob, true);
    }
}
