package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.compat.IrisRenderCompat;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/** An untextured sky cube that fades from the current fog color to a white floor. */
public final class EnvironmentCubeSkyboxRenderer implements CustomSkyboxRenderer {
    public static final Identifier ID = MementoInAbyss.asResource("environment_cube");
    public static final EnvironmentCubeSkyboxRenderer INSTANCE = new EnvironmentCubeSkyboxRenderer();
    private static final float EXTENT = 100.0F;
    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
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
    public boolean renderSky(LevelRenderState levelRenderState, SkyRenderState skyRenderState,
                             Matrix4fc modelViewMatrix, Runnable setupFog) {
        this.ensureBuffers();
        setupFog.run();

        GpuBufferSlice transform = RenderSystem.getDynamicUniforms().writeTransform(
                new Matrix4f(modelViewMatrix), levelRenderState.cameraRenderState.fogData.color,
                new Vector3f(), new Matrix4f());
        GpuTextureView color = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView depth = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "MIA environment cube skybox", color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
            pass.setPipeline(PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transform);
            pass.setVertexBuffer(0, this.cubeBuffer);
            pass.draw(0, 36);
        }
        return true;
    }

    private void ensureBuffers() {
        if (this.cubeBuffer == null) this.cubeBuffer = buildCube();
    }

    private static GpuBuffer buildCube() {
        return buildBuffer("MIA environment cube skybox", 36, builder -> {
            face(builder, -EXTENT, EXTENT, -EXTENT, EXTENT, EXTENT, -EXTENT,
                    EXTENT, EXTENT, EXTENT, -EXTENT, EXTENT, EXTENT); // top
            face(builder, -EXTENT, -EXTENT, -EXTENT, -EXTENT, EXTENT, -EXTENT,
                    EXTENT, EXTENT, -EXTENT, EXTENT, -EXTENT, -EXTENT); // north
            face(builder, EXTENT, -EXTENT, EXTENT, EXTENT, EXTENT, EXTENT,
                    -EXTENT, EXTENT, EXTENT, -EXTENT, -EXTENT, EXTENT); // south
            face(builder, -EXTENT, -EXTENT, EXTENT, -EXTENT, EXTENT, EXTENT,
                    -EXTENT, EXTENT, -EXTENT, -EXTENT, -EXTENT, -EXTENT); // west
            face(builder, EXTENT, -EXTENT, -EXTENT, EXTENT, EXTENT, -EXTENT,
                    EXTENT, EXTENT, EXTENT, EXTENT, -EXTENT, EXTENT); // east
            face(builder, -EXTENT, -EXTENT, EXTENT, -EXTENT, -EXTENT, -EXTENT,
                    EXTENT, -EXTENT, -EXTENT, EXTENT, -EXTENT, EXTENT); // bottom
        });
    }

    private static GpuBuffer buildBuffer(String label, int vertices, java.util.function.Consumer<BufferBuilder> writer) {
        int bytes = vertices * DefaultVertexFormat.POSITION.getVertexSize();
        try (ByteBufferBuilder storage = ByteBufferBuilder.exactlySized(bytes)) {
            BufferBuilder builder = new BufferBuilder(
                    storage, VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
            writer.accept(builder);
            try (MeshData mesh = builder.buildOrThrow()) {
                return RenderSystem.getDevice().createBuffer(() -> label, GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
            }
        }
    }

    private static void face(BufferBuilder builder,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        builder.addVertex(ax, ay, az);
        builder.addVertex(bx, by, bz);
        builder.addVertex(cx, cy, cz);
        builder.addVertex(ax, ay, az);
        builder.addVertex(cx, cy, cz);
        builder.addVertex(dx, dy, dz);
    }

    private EnvironmentCubeSkyboxRenderer() {}
}
