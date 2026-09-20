package com.altnoir.mementoinabyss.impl.lod.client;

import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodReceiptPayload;
import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodStreamPayload;
import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodTransfer;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;

/** Count- and byte-bounded network mailbox. No Minecraft access or feedback sends on the network thread. */
final class CrossDimensionLodIngress {
    private final int capacity;
    private final int byteCapacity, retainedCapacity;
    private final ArrayDeque<Entry> packets = new ArrayDeque<>();
    private final Map<Long, CrossDimensionLodTransfer> rejected = new LinkedHashMap<>();
    private int bytes, retainedBytes;
    private long epoch;
    private boolean reset;

    CrossDimensionLodIngress(int capacity, int byteCapacity, int retainedCapacity) {
        if (capacity <= 0 || byteCapacity <= 0 || retainedCapacity <= 0)
            throw new IllegalArgumentException("Invalid LOD mailbox capacity");
        this.capacity = capacity;
        this.byteCapacity = byteCapacity;
        this.retainedCapacity = retainedCapacity;
    }

    synchronized void offer(CrossDimensionLodStreamPayload payload) {
        var transfer = payload.transfer();
        if (transfer.streamEpoch() < epoch) return;
        if (transfer.streamEpoch() > epoch) {
            packets.clear();
            bytes = retainedBytes = 0;
            rejected.clear();
            epoch = transfer.streamEpoch();
            reset = true;
        }
        int size = payload.encodedSize();
        int retained = payload.retainedBytes();
        if (packets.size() >= capacity
                || size > byteCapacity - bytes
                || retained > retainedCapacity - retainedBytes) {
            if (rejected.size() < 64) rejected.put(transfer.transferId(), transfer);
            return; // Server ACK timeout is the safety net if the overflow mailbox also fills.
        }
        packets.addLast(new Entry(payload, size, retained));
        bytes += size;
        retainedBytes += retained;
    }

    /** Observe a stream reset before servicing the independent cache-completion lane. */
    synchronized boolean takeReset() {
        boolean value = reset;
        reset = false;
        return value;
    }

    synchronized Poll poll() {
        Entry entry = packets.pollFirst();
        if (entry != null) {
            bytes -= entry.bytes;
            retainedBytes -= entry.retainedBytes;
        }
        Poll result = new Poll(reset, entry == null ? null : entry.payload);
        reset = false;
        return result;
    }

    synchronized CrossDimensionLodReceiptPayload pollRejected() {
        var iterator = rejected.values().iterator();
        if (!iterator.hasNext()) return null;
        var transfer = iterator.next();
        iterator.remove();
        return CrossDimensionLodReceiptPayload.of(transfer, CrossDimensionLodReceiptPayload.RESYNC);
    }

    synchronized int size() {
        return packets.size();
    }

    synchronized int bytes() {
        return bytes;
    }

    synchronized int retainedBytes() {
        return retainedBytes;
    }

    synchronized void clear() {
        packets.clear();
        bytes = retainedBytes = 0;
        rejected.clear();
        epoch = 0;
        reset = false;
    }

    private record Entry(CrossDimensionLodStreamPayload payload, int bytes, int retainedBytes) {}

    record Poll(boolean reset, CrossDimensionLodStreamPayload payload) {}
}
