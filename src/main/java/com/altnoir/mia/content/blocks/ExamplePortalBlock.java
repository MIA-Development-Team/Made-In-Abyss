package com.altnoir.mia.content.blocks;

import com.altnoir.mia.content.worldgen.portal.MIATeleporter;
import com.altnoir.mia.content.dimension.ExampleDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ExamplePortalBlock extends MIABasePortalBlock {
    public ExamplePortalBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.STONE).noLootTable().noOcclusion().noCollission());
    }

    @Override
    public void handleMIAPortal(Entity player, BlockPos pPos) {
        if (player.level() instanceof ServerLevel serverlevel) {
            var minecraftserver = serverlevel.getServer();
            var resourcekey = player.level().dimension() == ExampleDimension.MIA_EXAMPLEDIM_LEVEL_KEY ?
                    Level.OVERWORLD : ExampleDimension.MIA_EXAMPLEDIM_LEVEL_KEY;

            var portalDimension = minecraftserver.getLevel(resourcekey);
            if (portalDimension != null && !player.isPassenger()) {
                if(resourcekey == ExampleDimension.MIA_EXAMPLEDIM_LEVEL_KEY) {
                    player.changeDimension(portalDimension, new MIATeleporter(pPos, true));
                } else {
                    player.changeDimension(portalDimension, new MIATeleporter(pPos, false));
                }
            }
        }
    }
}
