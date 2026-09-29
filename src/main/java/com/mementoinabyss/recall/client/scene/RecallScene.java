package com.mementoinabyss.recall.client.scene;

import com.mementoinabyss.recall.client.render.outliner.AABBOutline;
import com.mementoinabyss.recall.data.scene.SceneDefinition;
import com.mementoinabyss.recall.data.scene.SceneStructure;
import com.mementoinabyss.recall.data.scene.SceneTimeline;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Resource diorama; Ponder's transform and cuboid outline ports require no simulated level. */
final class RecallScene {
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final List<BlockModel> models;
    private final Map<SceneStructure.Position, BlockModel> modelsByPosition;
    private final AABB bounds;
    private final float centerY;
    private final AABBOutline outline = new AABBOutline();

    RecallScene(SceneDefinition definition)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Map<BlockPos, BlockState> states = new HashMap<>();
        Map<String, AABB> groups = new HashMap<>();
        AABB all = null;
        for (var block : definition.blocks()) {
            var p = block.position();
            BlockPos pos = new BlockPos(p.x(), p.y(), p.z());
            states.put(
                    pos,
                    BlockStateParser.parseForBlock(
                                    BuiltInRegistries.BLOCK, block.state().commandString(), false)
                            .blockState());
            AABB box = new AABB(pos);
            groups.merge(block.group(), box, AABB::minmax);
            all = all == null ? box : all.minmax(box);
        }
        if (all == null) throw new IllegalArgumentException("Scene has no blocks");
        bounds = all;
        centerY = definition.zeroLayer() + .5F;
        BlockGetter blockGetter = new SceneBlocks(Map.copyOf(states));
        var resolver = new BlockModelResolver(Minecraft.getInstance().getModelManager());
        List<BlockModel> built = new ArrayList<>();
        for (var block : definition.blocks()) {
            var p = block.position();
            BlockPos pos = new BlockPos(p.x(), p.y(), p.z());
            BlockState state = states.get(pos);
            BlockModelRenderState model = new BlockModelRenderState();
            resolver.update(model, state, DISPLAY_CONTEXT);
            built.add(
                    new BlockModel(
                            p,
                            model,
                            block.group(),
                            groups.get(block.group()).getCenter(),
                            state.getBlock().getName(),
                            state.getShape(blockGetter, pos)));
        }
        models = List.copyOf(built);
        modelsByPosition =
                built.stream()
                        .collect(
                                java.util.stream.Collectors.toUnmodifiableMap(
                                        BlockModel::position, block -> block));
    }

    float fitScale(int width, int height) {
        float diameter =
                (float)
                        Math.sqrt(
                                bounds.getXsize() * bounds.getXsize()
                                        + bounds.getYsize() * bounds.getYsize()
                                        + bounds.getZsize() * bounds.getZsize());
        return Math.min(width, height) / (diameter * 1.08F);
    }

    SceneTransform transform(
            SceneTimeline.Frame frame,
            int width,
            int height,
            int guiScale,
            float yawOffset,
            float pitchOffset,
            float zoom) {
        Vec3 center = bounds.getCenter();
        return new SceneTransform(
                width,
                height,
                guiScale,
                frame.yaw() + yawOffset,
                Math.clamp(frame.pitch() + pitchOffset, 10, 80),
                zoom,
                (float) center.x,
                centerY,
                (float) center.z);
    }

    void renderScene(SceneTimeline.Frame frame, SubmitNodeStorage queue, PoseStack pose) {
        for (BlockModel block : models) {
            var group = frame.groups().get(block.group());
            if (group == null || !group.visible()) continue;
            pose.pushPose();
            pose.mulPose(block.matrix(group));
            block.model().submit(pose, queue, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }

    void renderOutline(
            SceneTimeline.Frame frame,
            @Nullable Selection selection,
            float alpha,
            MultiBufferSource buffer,
            PoseStack pose) {
        if (selection == null || alpha <= 0) return;
        var block = selection.block();
        var group = frame.groups().get(block.group());
        if (group == null || !group.visible() || block.shape().isEmpty()) return;
        pose.pushPose();
        pose.mulPose(block.matrix(group));
        outline.render(pose, buffer, block.shape().bounds(), alpha);
        pose.popPose();
    }

    @Nullable Selection pick(SceneTimeline.Frame frame, SceneTransform transform, float x, float y) {
        // As in Ponder: transform a near/far ray into each animated section's local coordinates.
        Vector3f near = transform.screenToScene(x, y, 1000);
        Vector3f far = transform.screenToScene(x, y, -1000);
        Selection selected = null;
        double best = Double.POSITIVE_INFINITY;
        for (BlockModel block : models) {
            var group = frame.groups().get(block.group());
            if (group == null || !group.visible() || block.shape().isEmpty()) continue;
            Matrix4f matrix = block.matrix(group);
            Matrix4f inverse = new Matrix4f(matrix).invert();
            var hit =
                    block.shape()
                            .clip(
                                    vec(inverse.transformPosition(new Vector3f(near))),
                                    vec(inverse.transformPosition(new Vector3f(far))),
                                    BlockPos.ZERO);
            if (hit == null) continue;
            Vec3 location = hit.getLocation();
            Vector3f scenePoint =
                    matrix.transformPosition(
                            new Vector3f(
                                    (float) location.x, (float) location.y, (float) location.z));
            double distance = scenePoint.distanceSquared(near);
            if (distance >= best) continue;
            best = distance;
            selected = new Selection(block, transform.sceneToScreen(scenePoint));
        }
        return selected;
    }

    @Nullable Vector3f tooltipAnchor(
            SceneTimeline.Frame frame, SceneTransform transform, SceneStructure.Position position) {
        BlockModel block = modelsByPosition.get(position);
        if (block == null) return null;
        var group = frame.groups().get(block.group());
        if (group == null || !group.visible()) return null;
        Vector3f point = block.matrix(group).transformPosition(new Vector3f(.5F, .65F, .5F));
        return transform.sceneToScreen(point);
    }

    private static Vec3 vec(Vector3f value) {
        return new Vec3(value.x, value.y, value.z);
    }

    record Selection(BlockModel block, Vector3f screenPoint) {}

    record BlockModel(
            SceneStructure.Position position,
            BlockModelRenderState model,
            String group,
            Vec3 pivot,
            Component name,
            VoxelShape shape) {
        Matrix4f matrix(SceneTimeline.GroupPose groupPose) {
            return SceneTransform.blockMatrix(
                    groupPose, (float) pivot.x, (float) pivot.y, (float) pivot.z, position);
        }
    }

    /** Read-only neighbors for model shapes; never delegates mutations to the player's level. */
    private record SceneBlocks(Map<BlockPos, BlockState> states) implements BlockGetter {
        @Override
        public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return states.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public int getHeight() {
            return SceneStructure.MAX_SIZE;
        }

        @Override
        public int getMinY() {
            return 0;
        }
    }
}
