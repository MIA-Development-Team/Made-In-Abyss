package com.altnoir.mementoinabyss.content.item;

import com.altnoir.mementoinabyss.network.CompassTargetPayload;
import com.altnoir.mementoinabyss.worldgen.structure.MiaStructures;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public final class StarCompassItem extends Item {
    private static final int SEARCH_RADIUS_BLOCKS = 12_800;
    private static final int SEARCH_RADIUS_CHUNKS = SEARCH_RADIUS_BLOCKS / 16;
    private static final int COOLDOWN_TICKS = 600;
    public StarCompassItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        var structure = serverLevel.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .get(MiaStructures.ABYSS_STRONGHOLD);
        var result = structure
                .map(holder -> serverLevel.getChunkSource().getGenerator().findNearestMapStructure(
                        serverLevel,
                        HolderSet.direct(holder),
                        player.blockPosition(),
                        SEARCH_RADIUS_CHUNKS,
                        false
                ))
                .orElse(null);

        if (result != null) {
            PacketDistributor.sendToPlayer(serverPlayer, new CompassTargetPayload(result.getFirst()));
            level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            PacketDistributor.sendToPlayer(serverPlayer, new CompassTargetPayload(null));
            level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.PLAYERS, 0.5F, 1.0F);
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        return InteractionResult.SUCCESS_SERVER;
    }
}
