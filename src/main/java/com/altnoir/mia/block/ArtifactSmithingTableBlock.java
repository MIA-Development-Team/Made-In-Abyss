package com.altnoir.mia.block;

import com.altnoir.mia.init.MiaStats;
import com.altnoir.mia.inventory.ArtifactSmithingTableMenu;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ArtifactSmithingTableBlock extends CraftingTableBlock {
    public static final String TITLE = "container.mia.artifact.smithing.title";
    public static final MapCodec<ArtifactSmithingTableBlock> CODEC = simpleCodec(ArtifactSmithingTableBlock::new);

    private static final Component CONTAINER_TITLE = Component.translatable(TITLE);

    public MapCodec<ArtifactSmithingTableBlock> codec() {
        return CODEC;
    }

    public ArtifactSmithingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((containerId, playerInventory, player) ->
                new ArtifactSmithingTableMenu(containerId, playerInventory,
                        ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(level, pos));
            player.awardStat(MiaStats.INTERACT_WITH_ARTIFACT_SMITHING_TABLE.get());
            return InteractionResult.CONSUME;
        }
    }

}