package com.altnoir.mia.content.items;

import com.altnoir.mia.TagsRegister;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class test extends Item {
    public test(Properties pProperties) {
        super(pProperties);
    }

    private static final int SCAN_RADIUS = 4;

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            Vec3 playerPos = pPlayer.getEyePosition();

            AABB scanArea = new AABB(
                    playerPos.x - SCAN_RADIUS,
                    playerPos.y - SCAN_RADIUS,
                    playerPos.z - SCAN_RADIUS,
                    playerPos.x + SCAN_RADIUS,
                    playerPos.y + SCAN_RADIUS,
                    playerPos.z + SCAN_RADIUS
            );
            List<BlockPos> foundOres = new ArrayList<>();

            for (BlockPos pos : BlockPos.betweenClosed(
                    (int) Math.floor(scanArea.minX),
                    (int) Math.floor(scanArea.minY),
                    (int) Math.floor(scanArea.minZ),
                    (int) Math.floor(scanArea.maxX),
                    (int) Math.floor(scanArea.maxY),
                    (int) Math.floor(scanArea.maxZ)
            )) {
                BlockState state = pLevel.getBlockState(pos);
                if (isValuableBlock(state)) {
                    foundOres.add(pos.immutable());
                }
            }

            if (!foundOres.isEmpty()) {
                foundOres.forEach(pos ->
                        outputValuableCoordinates(pos, pPlayer, pLevel.getBlockState(pos).getBlock())
                );
            } else {
                pPlayer.sendSystemMessage(Component.literal("No valuable ores found"));
            }

            // 消耗耐久（根据发现数量）
            pPlayer.getItemInHand(pUsedHand).hurtAndBreak(
                    Math.max(1, foundOres.size() / 5),
                    pPlayer,
                    p -> p.broadcastBreakEvent(pUsedHand)
            );
        }

        return InteractionResultHolder.sidedSuccess(
                pPlayer.getItemInHand(pUsedHand),
                pLevel.isClientSide()
        );
    }

    private void outputValuableCoordinates(BlockPos pos, Player player, Block block) {
        player.sendSystemMessage(Component.literal(
                I18n.get(block.getDescriptionId()) + "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")"
        ));
    }

    private boolean isValuableBlock(BlockState blockState) {
        return blockState.is(TagsRegister.Blocks.SCANNABLE_BLOCK);
    }
}
