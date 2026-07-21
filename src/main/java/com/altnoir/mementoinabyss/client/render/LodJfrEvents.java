package com.altnoir.mementoinabyss.client.render;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import jdk.jfr.Threshold;
import jdk.jfr.Timespan;

import java.util.concurrent.atomic.AtomicLong;

/** JFR events that correlate an exact rendered frame with LOD work on every participating thread. */
public final class LodJfrEvents {
    private static final AtomicLong NEXT_CLIENT_FRAME = new AtomicLong();
    private static final EventType CLIENT_FRAME_TYPE = EventType.getEventType(ClientFrameEvent.class);
    private static final EventType FRAME_TYPE = EventType.getEventType(RenderFrameEvent.class);
    private static final EventType MESH_BUILD_TYPE = EventType.getEventType(MeshBuildEvent.class);
    private static final EventType PAGE_BUILD_TYPE = EventType.getEventType(PageBuildEvent.class);
    private static final EventType UPLOAD_TYPE = EventType.getEventType(UploadEvent.class);

    public static Event beginClientFrame(boolean rendersLevel) {
        if (!CLIENT_FRAME_TYPE.isEnabled()) return null;
        ClientFrameEvent event = new ClientFrameEvent();
        event.frame = NEXT_CLIENT_FRAME.incrementAndGet();
        event.rendersLevel = rendersLevel;
        event.begin();
        return event;
    }

    public static void endClientFrame(Event event) {
        if (event == null) return;
        event.end();
        event.commit();
    }

    static RenderFrameEvent beginFrame(long frame) {
        if (!FRAME_TYPE.isEnabled()) return null;
        RenderFrameEvent event = new RenderFrameEvent();
        event.frame = frame;
        event.begin();
        return event;
    }

    static MeshBuildEvent beginMeshBuild(int chunkX, int chunkZ, int cellSize) {
        if (!MESH_BUILD_TYPE.isEnabled()) return null;
        MeshBuildEvent event = new MeshBuildEvent();
        event.chunkX = chunkX;
        event.chunkZ = chunkZ;
        event.cellSize = cellSize;
        event.begin();
        return event;
    }

    static PageBuildEvent beginPageBuild(int pageX, int pageZ, int chunks) {
        if (!PAGE_BUILD_TYPE.isEnabled()) return null;
        PageBuildEvent event = new PageBuildEvent();
        event.pageX = pageX;
        event.pageZ = pageZ;
        event.chunks = chunks;
        event.begin();
        return event;
    }

    static UploadEvent beginUpload(String part, int x, int z, int bytes) {
        if (!UPLOAD_TYPE.isEnabled()) return null;
        UploadEvent event = new UploadEvent();
        event.part = part;
        event.x = x;
        event.z = z;
        event.bytes = bytes;
        event.begin();
        return event;
    }

    @Name("mementoinabyss.LodRenderFrame")
    @Label("LOD render frame")
    @Category({"Memento in Abyss", "Cross-dimension LOD"})
    @StackTrace(false)
    static final class RenderFrameEvent extends Event {
        @Label("Frame") public long frame;
        @Label("Receive") @Timespan(Timespan.NANOSECONDS) public long receiveNanos;
        @Label("Mesh/update") @Timespan(Timespan.NANOSECONDS) public long meshNanos;
        @Label("Page/update") @Timespan(Timespan.NANOSECONDS) public long pageNanos;
        @Label("Visibility") @Timespan(Timespan.NANOSECONDS) public long visibilityNanos;
        @Label("Draw submission") @Timespan(Timespan.NANOSECONDS) public long drawNanos;
        @Label("Visible resources") public int visibleResources;
        @Label("Draw calls") public int drawCalls;
    }

    @Name("mementoinabyss.LodMeshBuild")
    @Label("LOD mesh build")
    @Category({"Memento in Abyss", "Cross-dimension LOD"})
    @Threshold("1 ms")
    static final class MeshBuildEvent extends Event {
        @Label("Chunk X") public int chunkX;
        @Label("Chunk Z") public int chunkZ;
        @Label("Cell size") public int cellSize;
        @Label("Terrain quads") public int terrainQuads;
        @Label("Seam quads") public int seamQuads;
        @Label("Succeeded") public boolean succeeded;
    }

    @Name("mementoinabyss.LodPageBuild")
    @Label("LOD page build")
    @Category({"Memento in Abyss", "Cross-dimension LOD"})
    @Threshold("1 ms")
    static final class PageBuildEvent extends Event {
        @Label("Page X") public int pageX;
        @Label("Page Z") public int pageZ;
        @Label("Chunks") public int chunks;
        @Label("Bytes") public int bytes;
        @Label("Succeeded") public boolean succeeded;
    }

    @Name("mementoinabyss.LodGpuUpload")
    @Label("LOD GPU upload")
    @Category({"Memento in Abyss", "Cross-dimension LOD"})
    @Threshold("1 ms")
    static final class UploadEvent extends Event {
        @Label("Part") public String part;
        @Label("X") public int x;
        @Label("Z") public int z;
        @Label("Bytes") public int bytes;
        @Label("Succeeded") public boolean succeeded;
    }

    @Name("mementoinabyss.ClientRenderFrame")
    @Label("Client render frame")
    @Category({"Memento in Abyss", "Client rendering"})
    @StackTrace(false)
    static final class ClientFrameEvent extends Event {
        @Label("Frame") public long frame;
        @Label("Renders level") public boolean rendersLevel;
    }

    private LodJfrEvents() {
    }
}
