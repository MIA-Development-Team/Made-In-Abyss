/*
package com.altnoir.mia.content.blocks;

import com.altnoir.mia.content.worldgen.portal.MIATeleporter;
import com.altnoir.mia.content.worldgen.AbyssBrinkDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ExamplePortalBlock extends MIABasePortalBlock {
    public ExamplePortalBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.NETHER_PORTAL)
                .noLootTable()
                .noOcclusion()
                .noCollission()
                .lightLevel(state -> 11));
    }

    @Override
    public void handleMIAPortal(Entity entity, BlockPos pPos) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            var server = serverLevel.getServer();
            // 判断当前是否在深渊边缘维度
            boolean isInAbyss = entity.level().dimension() == AbyssBrinkDimension.ABYSS_BRINK_LEVEL_KEY;

            // 确定目标维度
            ResourceKey<Level> targetDimension = isInAbyss ?
                    Level.OVERWORLD :
                    AbyssBrinkDimension.ABYSS_BRINK_LEVEL_KEY;

            ServerLevel targetLevel = server.getLevel(targetDimension);
            if (targetLevel != null && !entity.isPassenger()) {
                // 根据方向设置teleporter参数
                MIATeleporter teleporter = new MIATeleporter(
                        pPos,
                        !isInAbyss // 进入深渊维度时为true
                );
                entity.changeDimension(targetLevel, teleporter);
            }
        }
    }
}
*/
