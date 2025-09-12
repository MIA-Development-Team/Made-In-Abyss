package com.altnoir.mia.item;

import com.altnoir.mia.init.MiaBlockTags;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
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
        super(tier, MiaBlockTags.MINEABLE_WITH_COMPOSITE, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && this.calculateHitResult(player).getType() == HitResult.Type.BLOCK) {
            player.startUsingItem(context.getHand());
        }

        return InteractionResult.CONSUME;
    }

    private HitResult calculateHitResult(Player player) {
        return ProjectileUtil.getHitResultOnViewVector(
                player, entity -> !entity.isSpectator() && entity.isPickable(), player.blockInteractionRange()
        );
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 200;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (remainingUseDuration >= 0 && livingEntity instanceof Player player) {
            HitResult hitresult = this.calculateHitResult(player);

            if (hitresult instanceof BlockHitResult blockhitresult && hitresult.getType() == HitResult.Type.BLOCK) {
                int i = this.getUseDuration(stack, livingEntity) - remainingUseDuration + 1;
                boolean flag = i % 10 == 5;
                if (flag) {
                    BlockPos blockpos = blockhitresult.getBlockPos();
                    BlockState blockstate = level.getBlockState(blockpos);
                    HumanoidArm humanoidarm = livingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND
                            ? player.getMainArm()
                            : player.getMainArm().getOpposite();

                    if (blockstate.shouldSpawnTerrainParticles() && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                        this.spawnDustParticles(level, blockhitresult, blockstate, livingEntity.getViewVector(0.0F), humanoidarm);
                    }

                    SoundEvent soundevent;
                    if (blockstate.getBlock() instanceof BrushableBlock brushableblock) {
                        soundevent = brushableblock.getBrushSound();
                    } else {
                        soundevent = SoundEvents.AMETHYST_BLOCK_HIT;
                    }

                    level.playSound(player, blockpos, soundevent, SoundSource.BLOCKS);
                    if (!level.isClientSide() && level.getBlockEntity(blockpos) instanceof BrushableBlockEntity brushableblockentity) {
                        boolean flag1 = brushableblockentity.brush(level.getGameTime(), player, blockhitresult.getDirection());
                        if (flag1) {
                            EquipmentSlot equipmentslot = stack.equals(player.getItemBySlot(EquipmentSlot.OFFHAND))
                                    ? EquipmentSlot.OFFHAND
                                    : EquipmentSlot.MAINHAND;
                            stack.hurtAndBreak(1, livingEntity, equipmentslot);
                        }
                    }
                    if (level.isClientSide()) {
                        player.swing(InteractionHand.MAIN_HAND);
                    }
                }

                return;
            }

            livingEntity.releaseUsingItem();
        } else {
            livingEntity.releaseUsingItem();
        }
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