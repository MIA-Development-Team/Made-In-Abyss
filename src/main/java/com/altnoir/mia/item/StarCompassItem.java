package com.altnoir.mia.item;

import com.altnoir.mia.item.abs.IMiaTooltip;
import com.altnoir.mia.network.CompassTargetPayload;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class StarCompassItem extends Item implements IMiaTooltip {
    private static final int SEARCH_RADIUS = 6400;
    private static final ResourceKey<Structure> TARGET_STRUCTURE = BuiltinStructures.DESERT_PYRAMID;

    public StarCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip){
        IMiaTooltip.super.appendTooltip(stack, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            var serverPlayer = (ServerPlayer) player;

            var structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
            var structureHolder = structureRegistry.getHolder(TARGET_STRUCTURE);

            if (structureHolder.isPresent()) {
                var structures = HolderSet.direct(structureHolder.get());
                var result = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(
                        serverLevel,
                        structures,
                        player.blockPosition(),
                        SEARCH_RADIUS,
                        false
                );

                if (result != null) {
                    var structurePos = result.getFirst();
                    PacketDistributor.sendToPlayer(serverPlayer, new CompassTargetPayload(
                            structurePos.getX(),
                            structurePos.getY(),
                            structurePos.getZ(),
                            true
                    ));
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                } else {
                    PacketDistributor.sendToPlayer(serverPlayer, new CompassTargetPayload(0, 0, 0, false));
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.0F);
                }
            }

            player.getCooldowns().addCooldown(this, 600);
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
