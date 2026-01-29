package com.altnoir.mia.common.item;

import com.altnoir.mia.MiaConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BlazeLeapItem extends DiggerItem {

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public BlazeLeapItem(Properties properties) {
        super(Tiers.NETHERITE, BlockTags.MINEABLE_WITH_PICKAXE, properties);
    }

    public static Properties createAttributes() {
        return new Properties()
                .attributes(DiggerItem.createAttributes(Tiers.NETHERITE, 9.0F, -3.0F));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = attacker.level();
        
        if (!level.isClientSide && attacker instanceof Player player) {
            int explosionCount = MiaConfig.BLAZE_LEAP_EXPLOSION_COUNT.get();
            double explosionRadius = MiaConfig.BLAZE_LEAP_EXPLOSION_RADIUS.get();
            MinecraftServer server = level.getServer();

            for (int i = 0; i < explosionCount; i++) {
                SCHEDULER.schedule(() -> {
                    server.execute(() -> {
                        if (target != null && target.isAlive() && !target.isRemoved()) {
                            Vec3 currentPos = target.position();
                            
                            double offsetX = (Math.random() - 0.5) * explosionRadius;
                            double offsetY = Math.random() * target.getBbHeight(); 
                            double offsetZ = (Math.random() - 0.5) * explosionRadius;

                            level.explode(
                                    player,
                                    null,
                                    null,
                                    currentPos.x + offsetX,
                                    currentPos.y + offsetY,
                                    currentPos.z + offsetZ,
                                    2.0f,
                                    false,
                                    ExplosionInteraction.NONE 
                            );
                        }
                    });
                }, i, TimeUnit.SECONDS); 
            }

            stack.hurtAndBreak(1, attacker, LivingEntity.getSlotForHand(attacker.getUsedItemHand()));
        }

        return super.hurtEnemy(stack, target, attacker);
    }
}
