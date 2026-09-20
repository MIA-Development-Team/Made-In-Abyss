package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.compat.iris.IrisRenderCompat;
import com.altnoir.mementoinabyss.worldgen.dimension.MiaDimensions;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** An untextured sky cube with independent ceiling/side colors and a white floor. */
public final class EnvironmentCubeSkyboxRenderer implements CustomSkyboxRenderer {
    public static final Identifier ID = MementoInAbyss.asResource("environment_cube");
    public static final EnvironmentCubeSkyboxRenderer INSTANCE =
            new EnvironmentCubeSkyboxRenderer();
    private static final float EXTENT = 100.0F;
    private static final double ABYSS_DARK_SIDE_Y = -90.0;
    private static final int DARK_SIDE_COLOR = 0xFF1C231E;
    private static final RenderPipeline PIPELINE =
            RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                    .withLocation(MementoInAbyss.asResource("pipeline/environment_cube_skybox"))
                    .withVertexShader(MementoInAbyss.asResource("core/environment_cube_skybox"))
                    .withFragmentShader(MementoInAbyss.asResource("core/environment_cube_skybox"))
                    .withCull(false)
                    .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.TRIANGLES)
                    .build();

    private GpuBuffer cubeBuffer;

    public static void registerPipeline(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
        IrisRenderCompat.assignSkyPipeline(PIPELINE);
    }

    @Override
    public boolean renderSky(
            LevelRenderState levelRenderState,
            SkyRenderState skyRenderState,
            Matrix4fc modelViewMatrix,
            Runnable setupFog) {
        this.ensureBuffers();
        setupFog.run();

        Minecraft minecraft = Minecraft.getInstance();
        var camera = levelRenderState.cameraRenderState;
        boolean darkSides =
                minecraft.level != null
                        && (minecraft.level.dimension().equals(MiaDimensions.GREAT_FAULT_LEVEL)
                                || minecraft.level.dimension().equals(MiaDimensions.THE_ABYSS_LEVEL)
                                        && camera.pos.y < ABYSS_DARK_SIDE_Y);
        Matrix4f modelView = new Matrix4f(modelViewMatrix);
        GpuBufferSlice topTransform =
                RenderSystem.getDynamicUniforms()
                        .writeTransform(
                                modelView, camera.fogData.color, new Vector3f(), new Matrix4f());
        GpuBufferSlice sideTransform =
                darkSides
                        ? RenderSystem.getDynamicUniforms()
                                .writeTransform(
                                        modelView,
                                        ARGB.vector4fFromARGB32(DARK_SIDE_COLOR),
                                        new Vector3f(),
                                        new Matrix4f())
                        : topTransform;
        GpuBufferSlice bottomTransform =
                RenderSystem.getDynamicUniforms()
                        .writeTransform(
                                modelView, new Vector4f(1.0F), new Vector3f(), new Matrix4f());
        GpuTextureView color = minecraft.getMainRenderTarget().getColorTextureView();
        GpuTextureView depth = minecraft.getMainRenderTarget().getDepthTextureView();

        try (RenderPass pass =
                RenderSystem.getDevice()
                        .createCommandEncoder()
                        .createRenderPass(
                                () -> "MIA environment cube skybox",
                                color,
                                OptionalInt.empty(),
                                depth,
                                OptionalDouble.empty())) {
            pass.setPipeline(PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setVertexBuffer(0, this.cubeBuffer);
            // buildCube emits the ceiling, four sides, then the floor. Only the side tint changes
            // with depth.
            pass.setUniform("DynamicTransforms", topTransform);
            pass.draw(0, 6);
            pass.setUniform("DynamicTransforms", sideTransform);
            pass.draw(6, 24);
            pass.setUniform("DynamicTransforms", bottomTransform);
            pass.draw(30, 6);
        }
        return true;
    }

    private void ensureBuffers() {
        if (this.cubeBuffer == null) this.cubeBuffer = buildCube();
    }

    private static GpuBuffer buildCube() {
        return buildBuffer(
                "MIA environment cube skybox",
                36,
                builder -> {
                    face(
                            builder, -EXTENT, EXTENT, -EXTENT, EXTENT, EXTENT, -EXTENT, EXTENT,
                            EXTENT, EXTENT, -EXTENT, EXTENT, EXTENT); // top
                    face(
                            builder, -EXTENT, -EXTENT, -EXTENT, -EXTENT, EXTENT, -EXTENT, EXTENT,
                            EXTENT, -EXTENT, EXTENT, -EXTENT, -EXTENT); // north
                    face(
                            builder, EXTENT, -EXTENT, EXTENT, EXTENT, EXTENT, EXTENT, -EXTENT,
                            EXTENT, EXTENT, -EXTENT, -EXTENT, EXTENT); // south
                    face(
                            builder, -EXTENT, -EXTENT, EXTENT, -EXTENT, EXTENT, EXTENT, -EXTENT,
                            EXTENT, -EXTENT, -EXTENT, -EXTENT, -EXTENT); // west
                    face(
                            builder, EXTENT, -EXTENT, -EXTENT, EXTENT, EXTENT, -EXTENT, EXTENT,
                            EXTENT, EXTENT, EXTENT, -EXTENT, EXTENT); // east
                    face(
                            builder, -EXTENT, -EXTENT, EXTENT, -EXTENT, -EXTENT, -EXTENT, EXTENT,
                            -EXTENT, -EXTENT, EXTENT, -EXTENT, EXTENT); // bottom
                });
    }

    private static GpuBuffer buildBuffer(
            String label, int vertices, java.util.function.Consumer<BufferBuilder> writer) {
        int bytes = vertices * DefaultVertexFormat.POSITION.getVertexSize();
        try (ByteBufferBuilder storage = ByteBufferBuilder.exactlySized(bytes)) {
            BufferBuilder builder =
                    new BufferBuilder(
                            storage, VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
            writer.accept(builder);
            try (MeshData mesh = builder.buildOrThrow()) {
                return RenderSystem.getDevice()
                        .createBuffer(() -> label, GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
            }
        }
    }

    private static void face(
            BufferBuilder builder,
            float ax,
            float ay,
            float az,
            float bx,
            float by,
            float bz,
            float cx,
            float cy,
            float cz,
            float dx,
            float dy,
            float dz) {
        builder.addVertex(ax, ay, az);
        builder.addVertex(bx, by, bz);
        builder.addVertex(cx, cy, cz);
        builder.addVertex(ax, ay, az);
        builder.addVertex(cx, cy, cz);
        builder.addVertex(dx, dy, dz);
    }

    private EnvironmentCubeSkyboxRenderer() {}
}
