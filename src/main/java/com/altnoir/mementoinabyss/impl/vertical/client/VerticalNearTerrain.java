package com.altnoir.mementoinabyss.impl.vertical.client;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.compat.iris.IrisRenderCompat;
import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLink;
import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLinks;
import com.altnoir.mementoinabyss.impl.vertical.VerticalRaytrace;
import com.altnoir.mementoinabyss.impl.vertical.network.VerticalSectionPayload;
import com.altnoir.mementoinabyss.impl.vertical.network.VerticalSectionReceiptPayload;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.VerticalSectionRange;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.TreeSet;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

public final class VerticalNearTerrain {
    private static long boundaryPredictionStarted;
    public static final int MAX_MASKS = 96;
    private static final Map<Key, VerticalSectionPayload> DATA = new LinkedHashMap<>();
    private static final Map<Key, Mesh> MESHES = new HashMap<>();
    private static final ArrayDeque<Key> DIRTY = new ArrayDeque<>();
    private static final Map<ResourceKey<Level>, GpuBuffer> MASKS = new HashMap<>();
    private static final RenderPipeline OPAQUE = pipeline(false);
    private static final RenderPipeline TRANSLUCENT = pipeline(true);
    private static Object modelGeneration;
    private static long bridgeUntil;
    private static boolean reportedModelFailure;
    private static int receiptTicks;

    private static RenderPipeline pipeline(boolean translucent) {
        var builder =
                RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                        .withLocation(
                                MementoInAbyss.asResource(
                                        "pipeline/vertical_near_"
                                                + (translucent ? "translucent" : "opaque")))
                        .withVertexShader(MementoInAbyss.asResource("core/vertical_near"))
                        .withFragmentShader(MementoInAbyss.asResource("core/vertical_near"))
                        .withSampler("Sampler0")
                        .withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS);
        if (translucent)
            builder.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(
                            new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false));
        else builder.withDepthStencilState(DepthStencilState.DEFAULT);
        return builder.build();
    }

    public static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(OPAQUE);
        event.registerPipeline(TRANSLUCENT);
    }

    public static void accept(VerticalSectionPayload payload) {
        var level = Minecraft.getInstance().level;
        if (level == null || !payload.viewer().equals(level.dimension())) return;
        Key key = new Key(payload.source(), payload.chunkX(), payload.sectionY(), payload.chunkZ());
        var previous = DATA.get(key);
        if (previous != null && previous.revision() >= payload.revision()) return;
        if (previous == null && DATA.size() >= 192) return;
        DATA.put(key, payload);
        VerticalResidentWorlds.accept(payload);
        ClientPacketDistributor.sendToServer(
                new VerticalSectionReceiptPayload(
                        payload.source(),
                        payload.chunkX(),
                        payload.sectionY(),
                        payload.chunkZ(),
                        payload.revision()));
        dirty(key);
        for (Direction d : Direction.values()) {
            Key neighbour =
                    new Key(
                            key.source,
                            key.x + d.getStepX(),
                            key.y + d.getStepY(),
                            key.z + d.getStepZ());
            if (DATA.containsKey(neighbour)) dirty(neighbour);
        }
    }

    private static void dirty(Key key) {
        if (!DIRTY.contains(key)) DIRTY.addLast(key);
    }

    public static @Nullable RemoteHit pick() {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || IrisRenderCompat.isShaderPackInUse())
            return null;
        double reach =
                Math.min(16, mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE));
        for (var link : CrossDimensionLodLinks.forTargets(mc.level)) {
            var anchor =
                    DATA.values().stream()
                            .filter(
                                    p ->
                                            p.viewer().equals(mc.level.dimension())
                                                    && p.source().equals(link.source()))
                            .findFirst();
            if (anchor.isEmpty()) continue;
            var hit =
                    VerticalRaytrace.trace(
                            mc.level,
                            new VerticalTerrainView(DATA, anchor.get()),
                            link,
                            mc.player,
                            reach);
            if (hit == null) continue;
            var pos = hit.getBlockPos();
            if (MESHES.containsKey(
                    new Key(
                            link.source(),
                            Math.floorDiv(pos.getX(), 16),
                            Math.floorDiv(pos.getY(), 16),
                            Math.floorDiv(pos.getZ(), 16))))
                return new RemoteHit(link.source(), hit);
        }
        return null;
    }

    public static @Nullable CollisionGetter collisionView(Level level, CrossDimensionLodLink link) {
        for (var p : DATA.values())
            if (p.viewer().equals(level.dimension()) && p.source().equals(link.source()))
                return new VerticalTerrainView(DATA, p);
        return null;
    }

    public record RemoteHit(ResourceKey<Level> source, BlockHitResult hit) {}

    public static void tick() {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            clear();
            return;
        }
        if (++receiptTicks % 20 == 0)
            for (var p : DATA.values()) {
                if (p.viewer().equals(mc.level.dimension()))
                    ClientPacketDistributor.sendToServer(
                            new VerticalSectionReceiptPayload(
                                    p.source(),
                                    p.chunkX(),
                                    p.sectionY(),
                                    p.chunkZ(),
                                    p.revision()));
            }
        var iterator = DATA.entrySet().iterator();
        var window = VerticalSectionRange.around(mc.player.getY());
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var p = entry.getValue();
            boolean bridge =
                    p.source().equals(mc.level.dimension()) && System.nanoTime() < bridgeUntil;
            if ((!p.viewer().equals(mc.level.dimension()) && !bridge)
                    || Math.abs(p.chunkX() - mc.player.chunkPosition().x()) > 1
                    || Math.abs(p.chunkZ() - mc.player.chunkPosition().z()) > 1
                    || (!bridge
                            && (p.sectionY() * 16 + p.offset() >= window.maxYExclusive()
                                    || p.sectionY() * 16 + p.offset() + 16 <= window.minY()))) {
                var mesh = MESHES.remove(entry.getKey());
                if (mesh != null) mesh.close();
                iterator.remove();
                DIRTY.remove(entry.getKey());
            }
        }
        Object generation = mc.getModelManager().getBlockStateModelSet();
        if (modelGeneration != generation) {
            modelGeneration = generation;
            MESHES.values().forEach(Mesh::close);
            MESHES.clear();
            DIRTY.clear();
            DIRTY.addAll(DATA.keySet());
        }
    }

    public static void holdAtPendingBoundary() {
        var mc = Minecraft.getInstance();
        if (mc.level == null
                || mc.player == null
                || !MiaDimensions.isMiaDimension(mc.level.dimension())) return;
        double y = mc.player.getY();
        boolean below = y < mc.level.getMinY();
        boolean above = y >= mc.level.getMaxY() + 1;
        if (!below && !above) {
            boundaryPredictionStarted = 0;
            return;
        }
        var connection =
                CrossDimensionLodLinks.forTargets(mc.level).stream()
                        .filter(
                                link ->
                                        MiaDimensions.isMiaDimension(link.source())
                                                && (link.displayYOffset() < 0) == below)
                        .findFirst();
        if (connection.isEmpty()) return;
        long now = System.nanoTime();
        if (boundaryPredictionStarted == 0) boundaryPredictionStarted = now;
        double overshoot = below ? mc.level.getMinY() - y : y - mc.level.getMaxY() - 1;
        if (!mc.player.isPassenger()
                && !IrisRenderCompat.isShaderPackInUse()
                && now - boundaryPredictionStarted < 2_000_000_000L
                && overshoot < 96
                && readyToCross(connection.get())) return;
        mc.player.setPos(
                mc.player.getX(),
                below
                        ? mc.level.getMinY() + .05
                        : mc.level.getMaxY() + 1 - mc.player.getBbHeight() - .05,
                mc.player.getZ());
        var motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(motion.x, 0, motion.z);
    }

    private static boolean readyToCross(CrossDimensionLodLink link) {
        var mc = Minecraft.getInstance();
        var box = mc.player.getBoundingBox().inflate(1).move(0, -link.displayYOffset(), 0);
        int minY = Math.max(link.sourceHeight().minY(), (int) Math.floor(box.minY));
        int maxY =
                Math.min(
                        link.sourceHeight().minY() + link.sourceHeight().height() - 1,
                        (int) Math.floor(box.maxY));
        if (minY > maxY) return false;
        for (int x = (int) Math.floor(box.minX / 16); x <= (int) Math.floor(box.maxX / 16); x++)
            for (int z = (int) Math.floor(box.minZ / 16); z <= (int) Math.floor(box.maxZ / 16); z++)
                for (int y = Math.floorDiv(minY, 16); y <= Math.floorDiv(maxY, 16); y++) {
                    Key key = new Key(link.source(), x, y, z);
                    var payload = DATA.get(key);
                    if (payload == null
                            || !payload.viewer().equals(mc.level.dimension())
                            || !MESHES.containsKey(key)) return false;
                }
        return true;
    }

    public static void render(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || IrisRenderCompat.isShaderPackInUse()) return;
        long deadline = System.nanoTime() + 2_000_000;
        while (!DIRTY.isEmpty() && System.nanoTime() < deadline) {
            Key key = DIRTY.removeFirst();
            var p = DATA.get(key);
            if (p == null) continue;
            try {
                Mesh replacement = build(p);
                Mesh old = MESHES.put(key, replacement);
                if (old != null) old.close();
            } catch (RuntimeException failedModel) {
                Mesh old = MESHES.remove(key);
                if (old != null) old.close();
                if (!reportedModelFailure) {
                    reportedModelFailure = true;
                    MementoInAbyss.LOGGER.warn(
                            "Remote terrain model could not be built; retaining LOD", failedModel);
                }
            }
        }
        var camera = event.getLevelRenderState().cameraRenderState.pos;
        var ordered = new ArrayList<>(MESHES.entrySet());
        ordered.sort(
                Comparator.comparingDouble(
                        (Map.Entry<Key, Mesh> e) -> {
                            var p = DATA.get(e.getKey());
                            double dx = e.getKey().x * 16 + 8 - camera.x,
                                    dz = e.getKey().z * 16 + 8 - camera.z;
                            double dy = e.getKey().y * 16 + p.offset() + 8 - camera.y;
                            return -(dx * dx + dy * dy + dz * dz);
                        }));
        var atlas = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        var target = mc.getMainRenderTarget();
        Map<ResourceKey<Level>, Float> ambient = new HashMap<>();
        ambient.put(mc.level.dimension(), mc.level.dimensionType().ambientLight());
        for (var link : CrossDimensionLodLinks.forTargets(mc.level))
            ambient.put(link.source(), link.sourceType().ambientLight());
        var draws = new ArrayList<Draw>();
        int maxIndices = 0;
        for (var entry : ordered) {
            var p = DATA.get(entry.getKey());
            var mesh = entry.getValue();
            if (p == null
                    || (!p.viewer().equals(mc.level.dimension())
                            && !(p.source().equals(mc.level.dimension())
                                    && System.nanoTime() < bridgeUntil))) continue;
            int offset = p.source().equals(mc.level.dimension()) ? 0 : p.offset();
            var transform =
                    RenderSystem.getDynamicUniforms()
                            .writeTransform(
                                    new Matrix4f(event.getModelViewMatrix())
                                            .translate(
                                                    (float) (p.chunkX() * 16 - camera.x),
                                                    (float) (p.sectionY() * 16 + offset - camera.y),
                                                    (float) (p.chunkZ() * 16 - camera.z)),
                                    new Vector4f(1, 1, 1, ambient.getOrDefault(p.source(), .025F)),
                                    new Vector3f(),
                                    new Matrix4f());
            draws.add(new Draw(mesh, transform));
            for (var layer : mesh.layers.values()) maxIndices = Math.max(maxIndices, layer.indices);
        }
        if (maxIndices == 0) return;
        var indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        var gpuIndices = indices.getBuffer(maxIndices);
        for (boolean translucent : new boolean[] {false, true}) {
            try (var pass =
                    RenderSystem.getDevice()
                            .createCommandEncoder()
                            .createRenderPass(
                                    () -> "Vertical real terrain",
                                    target.getColorTextureView(),
                                    OptionalInt.empty(),
                                    target.getDepthTextureView(),
                                    OptionalDouble.empty())) {
                pass.setPipeline(translucent ? TRANSLUCENT : OPAQUE);
                RenderSystem.bindDefaultUniforms(pass);
                pass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
                for (var draw : draws) {
                    pass.setUniform("DynamicTransforms", draw.transform);
                    for (var layer : draw.mesh.layers.entrySet()) {
                        if (layer.getKey().translucent() != translucent) continue;
                        pass.setVertexBuffer(0, layer.getValue().vertices);
                        pass.setIndexBuffer(gpuIndices, indices.type());
                        pass.drawIndexed(0, 0, layer.getValue().indices, 1);
                    }
                }
            }
        }
    }

    private static Mesh build(VerticalSectionPayload p) {
        var mc = Minecraft.getInstance();
        var view = new VerticalTerrainView(DATA, p);
        var allocators = new EnumMap<ChunkSectionLayer, ByteBufferBuilder>(ChunkSectionLayer.class);
        var builders = new EnumMap<ChunkSectionLayer, BufferBuilder>(ChunkSectionLayer.class);
        Function<ChunkSectionLayer, BufferBuilder> builder =
                layer ->
                        builders.computeIfAbsent(
                                layer,
                                key ->
                                        new BufferBuilder(
                                                allocators.computeIfAbsent(
                                                        key,
                                                        unused -> new ByteBufferBuilder(65536)),
                                                VertexFormat.Mode.QUADS,
                                                DefaultVertexFormat.BLOCK));
        BlockQuadOutput output =
                (x, y, z, quad, instance) ->
                        builder.apply(quad.materialInfo().layer())
                                .putBlockBakedQuad(x, y, z, quad, instance);
        var blockRenderer = new ModelBlockRenderer(true, true, mc.getBlockColors());
        var fluids = new FluidRenderer(mc.getModelManager().getFluidStateModelSet());
        var layers = new EnumMap<ChunkSectionLayer, LayerMesh>(ChunkSectionLayer.class);
        try {
            var pos = new BlockPos.MutableBlockPos();
            for (int y = 0; y < 16; y++)
                for (int z = 0; z < 16; z++)
                    for (int x = 0; x < 16; x++) {
                        pos.set(p.chunkX() * 16 + x, p.sectionY() * 16 + y, p.chunkZ() * 16 + z);
                        var state = view.getBlockState(pos);
                        if (state.getRenderShape() == RenderShape.MODEL)
                            blockRenderer.tesselateBlock(
                                    output,
                                    x,
                                    y,
                                    z,
                                    view,
                                    pos,
                                    state,
                                    mc.getModelManager().getBlockStateModelSet().get(state),
                                    state.getSeed(pos));
                        if (!state.getFluidState().isEmpty())
                            fluids.tesselate(
                                    view,
                                    pos,
                                    layer -> builder.apply(layer),
                                    state,
                                    state.getFluidState());
                    }
            for (var entry : builders.entrySet()) {
                MeshData data = entry.getValue().build();
                if (data == null) continue;
                try (data) {
                    var vertices =
                            RenderSystem.getDevice()
                                    .createBuffer(
                                            () -> "Vertical section mesh",
                                            GpuBuffer.USAGE_VERTEX,
                                            data.vertexBuffer());
                    layers.put(
                            entry.getKey(), new LayerMesh(vertices, data.drawState().indexCount()));
                }
            }
            return new Mesh(layers);
        } catch (RuntimeException failure) {
            layers.values().forEach(layer -> layer.vertices.close());
            throw failure;
        } finally {
            allocators.values().forEach(ByteBufferBuilder::close);
        }
    }

    public static GpuBufferSlice lodMask(ResourceKey<Level> source) {
        ByteBuffer data = MemoryUtil.memCalloc(16 + MAX_MASKS * 32);
        try {
            int count = 0;
            if (!IrisRenderCompat.isShaderPackInUse()) {
                Map<MaskColumn, TreeSet<Integer>> columns = new HashMap<>();
                for (var key : MESHES.keySet()) {
                    if (!key.source.equals(source)) continue;
                    var p = DATA.get(key);
                    if (p != null)
                        columns.computeIfAbsent(
                                        new MaskColumn(p.chunkX(), p.chunkZ(), p.offset()),
                                        unused -> new TreeSet<>())
                                .add(p.sectionY());
                }
                for (var column : columns.entrySet()) {
                    int start = Integer.MIN_VALUE, end = start;
                    for (int section : column.getValue()) {
                        if (start != Integer.MIN_VALUE && section != end + 1) {
                            count = writeMask(data, count, column.getKey(), start, end);
                            start = section;
                        }
                        if (start == Integer.MIN_VALUE) start = section;
                        end = section;
                    }
                    count = writeMask(data, count, column.getKey(), start, end);
                }
            }
            data.putInt(0, count);
            GpuBuffer mask =
                    MASKS.computeIfAbsent(
                            source,
                            unused ->
                                    RenderSystem.getDevice()
                                            .createBuffer(
                                                    () -> "Vertical LOD replacement mask",
                                                    GpuBuffer.USAGE_UNIFORM
                                                            | GpuBuffer.USAGE_COPY_DST,
                                                    data.remaining()));
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(mask.slice(), data);
            return mask.slice();
        } finally {
            MemoryUtil.memFree(data);
        }
    }

    private static int writeMask(
            ByteBuffer data, int count, MaskColumn column, int start, int end) {
        if (start == Integer.MIN_VALUE || count == MAX_MASKS) return count;
        int index = 16 + count * 32;
        data.putFloat(index, column.x * 16)
                .putFloat(index + 4, start * 16 + column.offset)
                .putFloat(index + 8, column.z * 16);
        data.putFloat(index + 16, column.x * 16 + 16)
                .putFloat(index + 20, end * 16 + column.offset + 16)
                .putFloat(index + 24, column.z * 16 + 16);
        return count + 1;
    }

    public static void hydrate(ClientLevel level) {
        int centerX = Minecraft.getInstance().player.chunkPosition().x();
        int centerZ = Minecraft.getInstance().player.chunkPosition().z();
        level.getChunkSource().updateViewCenter(centerX, centerZ);
        for (var p : DATA.values()) if (p.source().equals(level.dimension())) install(level, p);
        bridge();
    }

    public static void bridge() {
        bridgeUntil = System.nanoTime() + 1_000_000_000L;
    }

    @SuppressWarnings("unchecked")
    public static void install(ClientLevel level, VerticalSectionPayload p) {
        if (!p.source().equals(level.dimension())
                || p.minY() != level.getMinY()
                || p.height() != level.getHeight()) return;
        LevelChunk chunk =
                level.getChunkSource().getChunk(p.chunkX(), p.chunkZ(), ChunkStatus.FULL, false);
        if (chunk == null) chunk = new LevelChunk(level, new ChunkPos(p.chunkX(), p.chunkZ()));
        var section = chunk.getSection(p.sectionY() - level.getMinSectionY());
        var biomes = (PalettedContainer<Holder<Biome>>) section.getBiomes();
        var registry = level.registryAccess().lookupOrThrow(Registries.BIOME);
        for (int i = 0; i < 64; i++) {
            var biome = registry.byId(p.biomes()[i]);
            if (biome != null)
                biomes.set(i & 3, (i >> 4) & 3, (i >> 2) & 3, registry.wrapAsHolder(biome));
        }
        for (int y = 0; y < 16; y++)
            for (int z = 0; z < 16; z++)
                for (int x = 0; x < 16; x++)
                    section.setBlockState(
                            x,
                            y,
                            z,
                            Block.stateById(
                                    p.palette()[
                                            Short.toUnsignedInt(
                                                    p.cells()[(y << 8) | (z << 4) | x])]));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (var s : chunk.getSections()) s.write(buffer);
            level.getChunkSource()
                    .replaceWithPacketData(p.chunkX(), p.chunkZ(), buffer, Map.of(), output -> {});
        } finally {
            buffer.release();
        }
        for (LightLayer layer : LightLayer.values()) {
            byte[] light = new byte[2048];
            for (int i = 0; i < 4096; i++) {
                int value = Byte.toUnsignedInt(p.light()[i]);
                int nibble = layer == LightLayer.SKY ? value >>> 4 : value & 15;
                light[i >> 1] |= (byte) (nibble << ((i & 1) * 4));
            }
            level.getLightEngine()
                    .queueSectionData(
                            layer,
                            SectionPos.of(p.chunkX(), p.sectionY(), p.chunkZ()),
                            new DataLayer(light));
        }
        level.getLightEngine().setLightEnabled(chunk.getPos(), true);
        VerticalResidentWorlds.tracked(p);
    }

    public static void clear() {
        MESHES.values().forEach(Mesh::close);
        MESHES.clear();
        DATA.clear();
        DIRTY.clear();
        MASKS.values().forEach(GpuBuffer::close);
        MASKS.clear();
        modelGeneration = null;
        bridgeUntil = 0;
        reportedModelFailure = false;
        receiptTicks = 0;
        boundaryPredictionStarted = 0;
    }

    record Key(ResourceKey<Level> source, int x, int y, int z) {}

    private record MaskColumn(int x, int z, int offset) {}

    private record LayerMesh(GpuBuffer vertices, int indices) {}

    private record Draw(Mesh mesh, GpuBufferSlice transform) {}

    private record Mesh(Map<ChunkSectionLayer, LayerMesh> layers) {
        void close() {
            layers.values().forEach(layer -> layer.vertices.close());
        }
    }

    private VerticalNearTerrain() {}
}
