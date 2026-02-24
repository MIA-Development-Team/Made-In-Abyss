package com.altnoir.mia.common.item;

import com.altnoir.mia.common.block.MiaBrushableBlock;
import com.altnoir.mia.common.block.entity.MiaBrushableBlockEntity;
import com.altnoir.mia.init.MiaTags;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositeItem extends DiggerItem {
    protected static final Set<ItemAbility> DEFAULT_COMPOSITE_ACTIONS = of(
            ItemAbilities.PICKAXE_DIG,
            ItemAbilities.AXE_DIG,
            ItemAbilities.SHOVEL_DIG,
            ItemAbilities.BRUSH_BRUSH
    );

    public CompositeItem(Tier tier, Properties properties) {
        super(tier, MiaTags.Blocks.MINEABLE_WITH_COMPOSITE, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        HitResult hitresult = this.calculateHitResult(player);
        if (player != null && hitresult.getType() == HitResult.Type.BLOCK) {
            player.swing(context.getHand());

            if (hitresult instanceof BlockHitResult blockhitresult) {
                BlockPos blockpos = blockhitresult.getBlockPos();
                BlockState blockstate = player.level().getBlockState(blockpos);
                Direction direction = blockhitresult.getDirection();
                HumanoidArm humanoidarm = player.getMainArm();

                if (blockstate.shouldSpawnTerrainParticles() && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                    this.spawnDustParticles(player.level(), blockhitresult, blockstate, player.getViewVector(0.0F), humanoidarm);
                }

                SoundEvent soundevent;
                if (blockstate.getBlock() instanceof MiaBrushableBlock brushable) {
                    soundevent = brushable.getBrushSound();
                } else {
                    soundevent = SoundEvents.BRUSH_GENERIC;
                }

                player.level().playSound(player, blockpos, soundevent, SoundSource.BLOCKS);
                if (!player.level().isClientSide() && player.level().getBlockEntity(blockpos) instanceof MiaBrushableBlockEntity brushable) {
                    boolean success = brushable.brush(player.level().getGameTime(), player, direction);
                    if (success) {
                        EquipmentSlot equipmentslot = context.getItemInHand().equals(player.getItemBySlot(EquipmentSlot.OFFHAND))
                                ? EquipmentSlot.OFFHAND
                                : EquipmentSlot.MAINHAND;
                        context.getItemInHand().hurtAndBreak(1, player, equipmentslot);
                    }
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    private HitResult calculateHitResult(Player player) {
        return ProjectileUtil.getHitResultOnViewVector(
                player, entity -> !entity.isSpectator() && entity.isPickable(), player.blockInteractionRange()
        );
    }

    private void spawnDustParticles(Level level, BlockHitResult hitResult, BlockState state, Vec3 pos, HumanoidArm arm) {
        double d0 = 3.0;
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        int j = level.getRandom().nextInt(7, 12);
        BlockParticleOption blockparticleoption = new BlockParticleOption(ParticleTypes.BLOCK, state);
        Direction direction = hitResult.getDirection();
        DustParticlesDelta brushitem$dustparticlesdelta = DustParticlesDelta.fromDirection(pos, direction);
        Vec3 vec3 = hitResult.getLocation();

        for (int k = 0; k < j; k++) {
            level.addParticle(
                    blockparticleoption,
                    vec3.x - (double) (direction == Direction.WEST ? 1.0E-6F : 0.0F),
                    vec3.y,
                    vec3.z - (double) (direction == Direction.NORTH ? 1.0E-6F : 0.0F),
                    brushitem$dustparticlesdelta.xd() * (double) i * 3.0 * level.getRandom().nextDouble(),
                    0.0,
                    brushitem$dustparticlesdelta.zd() * (double) i * 3.0 * level.getRandom().nextDouble()
            );
        }
    }

    static record DustParticlesDelta(double xd, double yd, double zd) {
        public static DustParticlesDelta fromDirection(Vec3 pos, Direction direction) {
            return switch (direction) {
                case DOWN, UP -> new DustParticlesDelta(pos.z(), 0.0, -pos.x());
                case NORTH -> new DustParticlesDelta(1.0, 0.0, -0.1);
                case SOUTH -> new DustParticlesDelta(-1.0, 0.0, 0.1);
                case WEST -> new DustParticlesDelta(-0.1, 0.0, -1.0);
                case EAST -> new DustParticlesDelta(0.1, 0.0, 1.0);
            };
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return DEFAULT_COMPOSITE_ACTIONS.contains(itemAbility);
    }

    private static Set<ItemAbility> of(ItemAbility... actions) {
        return Stream.of(actions).collect(Collectors.toCollection(Sets::newIdentityHashSet));
    }
}