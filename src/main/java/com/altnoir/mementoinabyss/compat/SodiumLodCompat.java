package com.altnoir.mementoinabyss.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Optional hooks exposed by Sodium for custom renderers. */
public final class SodiumLodCompat {
    public static boolean isLoaded() {
        return MiaMods.SODIUM.isLoaded();
    }

    public static void markSpriteActive(TextureAtlasSprite sprite) {
        if (isLoaded()) SodiumApi.markSpriteActive(sprite);
    }

    public static VertexBatch createVertexBatch(VertexConsumer consumer, PoseStack.Pose pose) {
        return isLoaded() ? SodiumApi.createVertexBatch(consumer, pose) : null;
    }

    public interface VertexBatch extends AutoCloseable {
        void write(float x, float y, float z, int argb, float u, float v,
                   int packedMinimumUv, int packedMaximumUv, float nx, float ny, float nz);

        @Override
        void close();
    }

    /** Kept separate so Sodium classes are never resolved when the mod is absent. */
    private static final class SodiumApi {
        private static void markSpriteActive(TextureAtlasSprite sprite) {
            net.caffeinemc.mods.sodium.api.texture.SpriteUtil.INSTANCE.markSpriteActive(sprite);
        }

        private static VertexBatch createVertexBatch(VertexConsumer consumer, PoseStack.Pose pose) {
            var writer = net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter.tryOf(consumer);
            return writer == null ? null : new SodiumVertexBatch(writer, pose);
        }
    }

    private static final class SodiumVertexBatch implements VertexBatch {
        private static final int CAPACITY = 4096;
        private final net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter writer;
        private final PoseStack.Pose pose;
        private final long pointer;
        private int count;

        private SodiumVertexBatch(net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter writer,
                                  PoseStack.Pose pose) {
            this.writer = writer;
            this.pose = pose;
            this.pointer = org.lwjgl.system.MemoryUtil.nmemAllocChecked(
                    (long) CAPACITY * net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex.STRIDE);
        }

        @Override
        public void write(float x, float y, float z, int argb, float u, float v,
                          int packedMinimumUv, int packedMaximumUv, float nx, float ny, float nz) {
            if (count == CAPACITY) flush();
            var matrix = pose.pose();
            float transformedX = net.caffeinemc.mods.sodium.api.math.MatrixHelper.transformPositionX(matrix, x, y, z);
            float transformedY = net.caffeinemc.mods.sodium.api.math.MatrixHelper.transformPositionY(matrix, x, y, z);
            float transformedZ = net.caffeinemc.mods.sodium.api.math.MatrixHelper.transformPositionZ(matrix, x, y, z);
            int normal = net.caffeinemc.mods.sodium.api.math.MatrixHelper.transformSafeNormal(pose.normal(), nx, ny, nz);
            long vertex = pointer + (long) count++
                    * net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex.STRIDE;
            net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex.write(vertex,
                    transformedX, transformedY, transformedZ,
                    net.caffeinemc.mods.sodium.api.util.ColorARGB.toABGR(argb), u, v,
                    packedMinimumUv, packedMaximumUv, normal);
        }

        private void flush() {
            if (count == 0) return;
            try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
                writer.push(stack, pointer, count,
                        net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex.FORMAT);
            }
            count = 0;
        }

        @Override
        public void close() {
            try {
                flush();
            } finally {
                org.lwjgl.system.MemoryUtil.nmemFree(pointer);
            }
        }
    }

    private SodiumLodCompat() {}
}
