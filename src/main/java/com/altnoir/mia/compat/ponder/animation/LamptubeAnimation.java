package com.altnoir.mia.compat.ponder.animation;

import com.altnoir.mia.common.block.abs.AbsCrystalTubeBlock;
import com.altnoir.mia.common.block.entity.PedestalBlockEntity;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class LamptubeAnimation {
    public static void amethystPressing(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("amethyst_lamptube", "Amethyst Lamptube Animation");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.DOWN);
        scene.idle(20);

        BlockPos pedestal = util.grid().at(2, 1, 2);
        BlockPos lamptube = util.grid().at(2, 3, 2);
        scene.world().showSection(util.select().position(pedestal), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(lamptube), Direction.DOWN);
        scene.idle(20);

        scene.rotateCameraY(-30);
        scene.idle(10);
        ItemStack stone = new ItemStack(Blocks.STONE);
        Class<PedestalBlockEntity> type = PedestalBlockEntity.class;
        scene.world().modifyBlockEntity(pedestal, type, b -> b.setItem(0, stone));
        scene.idle(20);
        scene.world().modifyBlock(lamptube, b -> b.setValue(AbsCrystalTubeBlock.POWERED, true), false);
        scene.idle(5);
        scene.world().modifyBlock(lamptube, b -> b.setValue(AbsCrystalTubeBlock.POWERED, false), false);
        scene.idle(5);

        Vec3 lamptubeTop = util.vector().topOf(lamptube);
        scene.overlay().showText(60).attachKeyFrame()
                .text("text.mia.ponder.amethyst_lamptube.text1")
                .colored(PonderPalette.GREEN)
                .pointAt(lamptubeTop).placeNearTarget();
        scene.idle(70);
        scene.markAsFinished();
    }

    public static void prasiolitePressing(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("prasiolite_lamptube", "Prasiolite Lamptube Animation");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.DOWN);
        scene.idle(20);

        BlockPos lamptube = util.grid().at(2, 3, 2);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 3, 2, 3), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(lamptube), Direction.DOWN);
        scene.idle(20);

        scene.rotateCameraY(-30);
        scene.idle(10);

        scene.world().modifyBlock(lamptube, b -> b.setValue(AbsCrystalTubeBlock.POWERED, true), false);
        scene.idle(5);
        scene.world().modifyBlock(lamptube, b -> b.setValue(AbsCrystalTubeBlock.POWERED, false), false);
        scene.idle(5);

        Vec3 lamptubeTop = util.vector().topOf(lamptube);
        scene.overlay().showText(60).attachKeyFrame()
                .text("text.mia.ponder.amethyst_lamptube.text1")
                .colored(PonderPalette.GREEN)
                .pointAt(lamptubeTop).placeNearTarget();
        scene.idle(70);
        scene.markAsFinished();
    }
}
