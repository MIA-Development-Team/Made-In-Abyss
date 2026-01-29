package com.altnoir.mia.common.item;

import com.altnoir.mia.MiaConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import com.altnoir.mia.init.MiaItems;

public class BlazeReapItem extends DiggerItem {

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    
    public static final Predicate<ItemStack> BLAZE_REAP_FUEL = (stack) -> 
    stack.is(Items.GUNPOWDER) || stack.is(MiaItems.PEACE_PHOBIA.get());

    public BlazeReapItem(Properties properties) {
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
            
            ItemStack ammoStack = player.getProjectile(stack);
            
            if (ammoStack.isEmpty() || !BLAZE_REAP_FUEL.test(ammoStack)) {
                ammoStack = findAmmoInInventory(player, BLAZE_REAP_FUEL);
            }

            
            if (!ammoStack.isEmpty() || player.getAbilities().instabuild) {
                
            
		if (!player.getAbilities().instabuild) {
		    if (!ammoStack.is(MiaItems.PEACE_PHOBIA.get())) {
			ammoStack.shrink(1);
			if (ammoStack.isEmpty()) {
			    player.getInventory().removeItem(ammoStack);
			}
		    }
		}

                triggerExplosionSequence(level, player, target);
                
                
                stack.hurtAndBreak(1, attacker, LivingEntity.getSlotForHand(attacker.getUsedItemHand()));
            }
        }

        return super.hurtEnemy(stack, target, attacker);
    }

     
    private ItemStack findAmmoInInventory(Player player, Predicate<ItemStack> predicate) {
        if (predicate.test(player.getOffhandItem())) {
            return player.getOffhandItem();
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack is = player.getInventory().getItem(i);
            if (predicate.test(is)) {
                return is;
            }
        }
        return ItemStack.EMPTY;
    }

    private void triggerExplosionSequence(Level level, Player player, LivingEntity target) {
        int explosionCount = MiaConfig.BLAZE_REAP_EXPLOSION_COUNT.get();
        double explosionRadius = MiaConfig.BLAZE_REAP_EXPLOSION_RADIUS.get();
        MinecraftServer server = level.getServer();

        for (int i = 0; i < explosionCount; i++) {
            int delay = i;
            SCHEDULER.schedule(() -> {
                if (server != null) {
                    server.execute(() -> {
                        if (target != null && target.isAlive() && !target.isRemoved()) {
                            Vec3 pos = target.position();
                            
                            double x = pos.x + (Math.random() - 0.5) * explosionRadius;
                            double y = pos.y + Math.random() * target.getBbHeight();
                            double z = pos.z + (Math.random() - 0.5) * explosionRadius;

                            if (level instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 5, 0.1, 0.1, 0.1, 0.05);
                            }

                            level.explode(
                                    player,
                                    null,
                                    null,
                                    x, y, z,
                                    2.0f,
                                    false,
                                    ExplosionInteraction.NONE 
                            );
                        }
                    });
                }
            }, delay, TimeUnit.SECONDS); 
        }
    }
}
