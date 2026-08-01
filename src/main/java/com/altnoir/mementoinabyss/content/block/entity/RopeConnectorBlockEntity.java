package com.altnoir.mementoinabyss.content.block.entity;

import com.altnoir.mementoinabyss.impl.rope.RopeAnchor;
import com.altnoir.mementoinabyss.impl.rope.RopeParameters;
import com.altnoir.mementoinabyss.impl.rope.RopeSimulation;
import com.altnoir.mementoinabyss.impl.rope.minecraft.MinecraftBlockCollisionResolver;
import com.altnoir.mementoinabyss.network.RopeSnapshotPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.UUID;

/**
 * Stores one persistent rope connection. Both endpoints store the peer, while
 * only the endpoint with the lower packed position owns simulation/rendering.
 */
public final class RopeConnectorBlockEntity extends BlockEntity {
    private static final double GRAB_INTERACTION_DISTANCE_SQUARED = 25.0;
    private static final Set<RopeConnectorBlockEntity> CLIENT_INSTANCES =
            Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<RopeConnectorBlockEntity> SERVER_INSTANCES =
            Collections.newSetFromMap(new WeakHashMap<>());
    private static final String CONNECTED_POS_TAG = "connected_pos";
    private static final String FREE_END_X_TAG = "free_end_x";
    private static final String FREE_END_Y_TAG = "free_end_y";
    private static final String FREE_END_Z_TAG = "free_end_z";
    private static final String ROPE_LENGTH_TAG = "rope_length";
    private static final String ROPE_POINTS_TAG = "rope_points";
    private static final RopeParameters SERVER_PARAMETERS = RopeParameters.DEFAULT
            .withSegmentLength(1.0)
            .withCollisionRadius(2.25 / 16.0)
            .withSolver(3, 16);
    private static final RopeParameters CLIENT_PARAMETERS = RopeParameters.DEFAULT
            .withSegmentLength(1.0)
            .withCollisionRadius(2.25 / 16.0)
            .withSolver(2, 6);

    private @Nullable BlockPos connectedPos;
    private @Nullable Vec3 freeEnd;
    private @Nullable double[] synchronizedPoints;
    private double ropeLength;
    private @Nullable RopeSimulation clientRope;
    private @Nullable RopeSimulation serverRope;
    private int clientGrabbedPoint = -1;
    private @Nullable double[] clientCorrectionTarget;
    private @Nullable double[] lastBroadcastPoints;
    private @Nullable UUID serverGrabber;
    private int serverGrabbedPoint = -1;
    private @Nullable Vec3 serverGrabPosition;
    private @Nullable Vec3 serverGrabTarget;
    private @Nullable BlockPos simulatedConnectedPos;
    private @Nullable Vec3 simulatedFreeEnd;
    private double simulatedLength;

    public RopeConnectorBlockEntity(
            BlockEntityType<RopeConnectorBlockEntity> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
    }

    public static boolean connect(Level level, BlockPos first, BlockPos second, double length) {
        if (first.equals(second)
                || !(level.getBlockEntity(first) instanceof RopeConnectorBlockEntity firstConnector)
                || !(level.getBlockEntity(second) instanceof RopeConnectorBlockEntity secondConnector)) {
            return false;
        }

        firstConnector.disconnect();
        secondConnector.disconnect();
        firstConnector.setConnectionInternal(second, length);
        secondConnector.setConnectionInternal(first, length);
        return true;
    }

    public static boolean connectFree(
            Level level,
            BlockPos connectorPos,
            Vec3 freeEnd,
            double length,
            Vec3 throwVelocity
    ) {
        if (!(level.getBlockEntity(connectorPos) instanceof RopeConnectorBlockEntity connector)
                || !isFinite(freeEnd)
                || !isFinite(throwVelocity)
                || !Double.isFinite(length)
                || length <= 0.0) {
            return false;
        }
        connector.disconnect();
        connector.connectedPos = null;
        connector.freeEnd = freeEnd;
        connector.ropeLength = length;
        connector.initializeStraightSnapshot();
        connector.invalidateRopes();
        if (!level.isClientSide()) {
            connector.serverRope = connector.createSimulation(SERVER_PARAMETERS, true);
            if (connector.serverRope != null) {
                int lastPoint = connector.serverRope.pointCount() - 1;
                for (int point = 1; point <= lastPoint; point++) {
                    double fraction = (double) point / lastPoint;
                    connector.serverRope.setVelocity(
                            point,
                            throwVelocity.x() * fraction,
                            throwVelocity.y() * fraction,
                            throwVelocity.z() * fraction
                    );
                }
            }
        }
        connector.syncChanged();
        return true;
    }

    public void disconnect() {
        BlockPos oldConnection = this.connectedPos;
        this.clearConnectionInternal();
        if (oldConnection != null
                && this.level != null
                && this.level.getBlockEntity(oldConnection) instanceof RopeConnectorBlockEntity other
                && this.worldPosition.equals(other.connectedPos)) {
            other.clearConnectionInternal();
        }
    }

    private void setConnectionInternal(BlockPos other, double length) {
        this.connectedPos = other.immutable();
        this.freeEnd = null;
        this.ropeLength = length;
        this.initializeStraightSnapshot();
        this.invalidateRopes();
        this.syncChanged();
    }

    private void clearConnectionInternal() {
        if (this.connectedPos == null && this.freeEnd == null) {
            return;
        }
        this.connectedPos = null;
        this.freeEnd = null;
        this.synchronizedPoints = null;
        this.ropeLength = 0.0;
        this.invalidateRopes();
        this.syncChanged();
    }

    private void syncChanged() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RopeConnectorBlockEntity connector) {
        if (level.isClientSide()) {
            connector.clientTick();
        } else {
            connector.serverTick((ServerLevel) level);
            if (level.getGameTime() % 20L == Math.floorMod(pos.asLong(), 20L)) {
                connector.validateConnection();
            }
        }
    }

    private void serverTick(ServerLevel level) {
        SERVER_INSTANCES.add(this);
        if (!this.ownsConnection()) {
            this.serverRope = null;
            this.clearServerGrab();
            return;
        }
        if (this.serverRope == null) {
            this.serverRope = this.createSimulation(SERVER_PARAMETERS, true);
        }
        if (this.serverRope == null) {
            return;
        }
        this.updateServerGrab(level);
        this.serverRope.tick(1.0 / 20.0);
        if ((level.getGameTime() & 1L) == 0L) {
            this.synchronizedPoints = this.serverRope.copyPoints();
            if (this.freeEnd != null) {
                int last = this.synchronizedPoints.length - 3;
                this.freeEnd = new Vec3(
                        this.synchronizedPoints[last],
                        this.synchronizedPoints[last + 1],
                        this.synchronizedPoints[last + 2]
                );
            }
            if (this.snapshotChanged() || level.getGameTime() % 20L == 0L) {
                PacketDistributor.sendToPlayersTrackingChunk(
                        level,
                        new ChunkPos(
                                this.worldPosition.getX() >> 4,
                                this.worldPosition.getZ() >> 4
                        ),
                        new RopeSnapshotPayload(this.worldPosition, this.synchronizedPoints)
                );
                this.lastBroadcastPoints = this.synchronizedPoints.clone();
            }
            if (level.getGameTime() % 20L == 0L) {
                this.setChanged();
            }
        }
    }

    private void validateConnection() {
        if (this.connectedPos == null || this.level == null || !this.level.isLoaded(this.connectedPos)) {
            return;
        }
        if (!(this.level.getBlockEntity(this.connectedPos) instanceof RopeConnectorBlockEntity other)
                || !this.worldPosition.equals(other.connectedPos)) {
            this.clearConnectionInternal();
        }
    }

    private void clientTick() {
        CLIENT_INSTANCES.add(this);
        if (!this.ownsConnection()) {
            this.invalidateClientRope();
            return;
        }

        if (this.clientRope == null
                || !java.util.Objects.equals(this.connectedPos, this.simulatedConnectedPos)
                || !java.util.Objects.equals(this.freeEnd, this.simulatedFreeEnd)
                || Double.compare(this.ropeLength, this.simulatedLength) != 0) {
            this.createClientRope();
        }
        if (this.clientRope != null) {
            this.clientRope.tick(1.0 / 20.0);
            this.applyClientCorrection();
        }
    }

    private void createClientRope() {
        this.invalidateClientRope();
        this.clientRope = this.createSimulation(CLIENT_PARAMETERS, true);
        this.clientCorrectionTarget = null;
        this.simulatedConnectedPos = this.connectedPos;
        this.simulatedFreeEnd = this.freeEnd;
        this.simulatedLength = this.ropeLength;
    }

    private @Nullable RopeSimulation createSimulation(
            RopeParameters parameters,
            boolean collisions
    ) {
        if (this.level == null || this.connectedPos == null && this.freeEnd == null) {
            return null;
        }

        double startX = this.worldPosition.getX() + 0.5;
        double startY = this.worldPosition.getY() + 0.5;
        double startZ = this.worldPosition.getZ() + 0.5;
        double endX = this.connectedPos != null ? this.connectedPos.getX() + 0.5 : this.freeEnd.x();
        double endY = this.connectedPos != null ? this.connectedPos.getY() + 0.5 : this.freeEnd.y();
        double endZ = this.connectedPos != null ? this.connectedPos.getZ() + 0.5 : this.freeEnd.z();
        double directDistance = Math.sqrt(
                square(endX - startX) + square(endY - startY) + square(endZ - startZ)
        );
        double safeLength = Math.max(this.ropeLength, directDistance);

        RopeSimulation simulation;
        if (this.synchronizedPoints != null) {
            try {
                simulation = RopeSimulation.fromPoints(
                        this.synchronizedPoints, safeLength, parameters);
            } catch (IllegalArgumentException ignored) {
                this.synchronizedPoints = null;
                simulation = RopeSimulation.withLength(
                        startX, startY, startZ, endX, endY, endZ,
                        safeLength, parameters);
            }
        } else {
            simulation = RopeSimulation.withLength(
                    startX, startY, startZ, endX, endY, endZ,
                    safeLength, parameters);
        }
        simulation.attachStart(out -> out.set(startX, startY, startZ));
        if (this.connectedPos != null) {
            simulation.attachEnd(out -> out.set(endX, endY, endZ));
            if (collisions) {
                simulation.collisionResolver(new MinecraftBlockCollisionResolver(
                        this.level, 1.0, this.worldPosition, this.connectedPos));
            }
        } else if (collisions) {
            simulation.collisionResolver(new MinecraftBlockCollisionResolver(
                    this.level, 1.0, this.worldPosition));
        }
        return simulation;
    }

    private void invalidateRopes() {
        this.invalidateClientRope();
        this.serverRope = null;
        this.lastBroadcastPoints = null;
        this.clearServerGrab();
    }

    private boolean snapshotChanged() {
        if (this.synchronizedPoints == null
                || this.lastBroadcastPoints == null
                || this.synchronizedPoints.length != this.lastBroadcastPoints.length) {
            return true;
        }
        for (int index = 0; index < this.synchronizedPoints.length; index++) {
            if (Math.abs(this.synchronizedPoints[index] - this.lastBroadcastPoints[index]) > 0.0025) {
                return true;
            }
        }
        return false;
    }

    private void invalidateClientRope() {
        this.clientRope = null;
        this.clientCorrectionTarget = null;
        this.simulatedConnectedPos = null;
        this.simulatedFreeEnd = null;
        this.simulatedLength = 0.0;
    }

    private boolean ownsConnection() {
        return this.freeEnd != null
                || this.connectedPos != null && this.worldPosition.asLong() < this.connectedPos.asLong();
    }

    public @Nullable RopeSimulation getClientRope() {
        return this.clientRope;
    }

    public @Nullable RopeSimulation getActiveRope() {
        return this.level != null && this.level.isClientSide()
                ? this.clientRope
                : this.serverRope;
    }

    public void acceptServerSnapshot(double[] points) {
        if (points == null || points.length < 6 || points.length % 3 != 0) {
            return;
        }
        this.synchronizedPoints = points.clone();
        if (this.freeEnd != null) {
            int last = points.length - 3;
            this.freeEnd = new Vec3(points[last], points[last + 1], points[last + 2]);
        }
        this.clientCorrectionTarget = points.clone();
        this.simulatedConnectedPos = this.connectedPos;
        this.simulatedFreeEnd = this.freeEnd;
        this.simulatedLength = this.ropeLength;
    }

    private void applyClientCorrection() {
        if (this.clientRope == null
                || this.clientCorrectionTarget == null
                || this.clientGrabbedPoint >= 0
                || this.clientCorrectionTarget.length != this.clientRope.pointCount() * 3) {
            return;
        }
        double maximumErrorSquared = 0.0;
        for (int point = 0; point < this.clientRope.pointCount(); point++) {
            int offset = point * 3;
            maximumErrorSquared = Math.max(maximumErrorSquared,
                    square(this.clientCorrectionTarget[offset] - this.clientRope.x(point))
                            + square(this.clientCorrectionTarget[offset + 1] - this.clientRope.y(point))
                            + square(this.clientCorrectionTarget[offset + 2] - this.clientRope.z(point)));
        }
        double blend = maximumErrorSquared > 4.0 ? 0.35
                : maximumErrorSquared > 0.25 ? 0.2
                : 0.1;
        this.clientRope.applyPoints(this.clientCorrectionTarget, blend);
        if (maximumErrorSquared < 1.0E-5) {
            this.clientCorrectionTarget = null;
        }
    }

    public static Iterable<RopeConnectorBlockEntity> clientInstances() {
        return CLIENT_INSTANCES;
    }

    public static Iterable<RopeConnectorBlockEntity> serverInstances() {
        return SERVER_INSTANCES;
    }

    public @Nullable BlockPos getConnectedPos() {
        return this.connectedPos;
    }

    public boolean isConnected() {
        return this.connectedPos != null || this.freeEnd != null;
    }

    public boolean hasFreeEnd() {
        return this.freeEnd != null;
    }

    public double getRopeLength() {
        return this.ropeLength;
    }

    public boolean grabClientPoint(int point, RopeAnchor anchor) {
        if (this.freeEnd == null || this.clientRope == null) {
            return false;
        }
        if (point <= 0 || point >= this.clientRope.pointCount()) {
            return false;
        }
        this.clientGrabbedPoint = point;
        this.clientRope.attachPoint(point, anchor);
        return true;
    }

    public void releaseClientPoint(int point) {
        if (this.clientRope != null && this.freeEnd != null) {
            this.clientRope.detachPoint(point);
        }
        if (this.clientGrabbedPoint == point) {
            this.clientGrabbedPoint = -1;
        }
    }

    public boolean beginServerGrab(ServerPlayer player, int point, Vec3 target) {
        if (this.freeEnd == null || !isFinite(target)
                || !player.getMainHandItem().isEmpty() && !player.getOffhandItem().isEmpty()) {
            return false;
        }
        if (this.serverRope == null) {
            this.serverRope = this.createSimulation(SERVER_PARAMETERS, true);
        }
        if (this.serverRope == null || point <= 0 || point >= this.serverRope.pointCount()) {
            return false;
        }
        Vec3 node = new Vec3(
                this.serverRope.x(point),
                this.serverRope.y(point),
                this.serverRope.z(point)
        );
        if (player.getEyePosition().distanceToSqr(node) > 36.0
                || player.getEyePosition().distanceToSqr(target) > 9.0) {
            return false;
        }
        this.clearServerGrab();
        this.serverGrabber = player.getUUID();
        this.serverGrabbedPoint = point;
        this.serverGrabPosition = this.clampGrabPosition(this.serverRope, point, node);
        this.serverGrabTarget = target;
        this.serverRope.attachPoint(point, out -> out.set(
                this.serverGrabPosition.x(),
                this.serverGrabPosition.y(),
                this.serverGrabPosition.z()
        ));
        return true;
    }

    public boolean updateServerGrab(ServerPlayer player, int point, Vec3 target) {
        if (!player.getUUID().equals(this.serverGrabber)
                || point != this.serverGrabbedPoint
                || !isFinite(target)
                || player.getEyePosition().distanceToSqr(target) > 9.0) {
            return false;
        }
        this.serverGrabTarget = target;
        return true;
    }

    public void releaseServerGrab(ServerPlayer player, int point) {
        if (player.getUUID().equals(this.serverGrabber) && point == this.serverGrabbedPoint) {
            this.clearServerGrab();
        }
    }

    private void updateServerGrab(ServerLevel level) {
        if (this.serverGrabber == null || this.serverRope == null
                || this.serverGrabPosition == null || this.serverGrabTarget == null) {
            return;
        }
        if (!(level.getPlayerByUUID(this.serverGrabber) instanceof ServerPlayer player)
                || !player.getMainHandItem().isEmpty() && !player.getOffhandItem().isEmpty()
                || player.position().distanceTo(Vec3.atCenterOf(this.worldPosition))
                > this.ropeLength + 6.0) {
            this.clearServerGrab();
            return;
        }
        Vec3 movement = this.serverGrabTarget.subtract(this.serverGrabPosition);
        double distance = movement.length();
        Vec3 nextPosition = distance > 0.2
                ? this.serverGrabPosition.add(movement.scale(0.2 / distance))
                : this.serverGrabTarget;
        this.serverGrabPosition = this.clampGrabPosition(
                this.serverRope, this.serverGrabbedPoint, nextPosition);
        if (player.getEyePosition().distanceToSqr(this.serverGrabPosition)
                > GRAB_INTERACTION_DISTANCE_SQUARED) {
            this.clearServerGrab();
        }
    }

    public Vec3 clampClientGrabPosition(int point, Vec3 target) {
        return this.clientRope == null
                ? target
                : this.clampGrabPosition(this.clientRope, point, target);
    }

    private Vec3 clampGrabPosition(RopeSimulation rope, int point, Vec3 target) {
        Vec3 origin = Vec3.atCenterOf(this.worldPosition);
        Vec3 offset = target.subtract(origin);
        double maximumDistance = rope.lengthToPoint(point);
        double distanceSquared = offset.lengthSqr();
        if (distanceSquared <= maximumDistance * maximumDistance
                || distanceSquared < 1.0E-12) {
            return target;
        }
        return origin.add(offset.scale(maximumDistance / Math.sqrt(distanceSquared)));
    }

    private void clearServerGrab() {
        if (this.serverRope != null && this.serverGrabbedPoint >= 0) {
            this.serverRope.detachPoint(this.serverGrabbedPoint);
        }
        this.serverGrabber = null;
        this.serverGrabbedPoint = -1;
        this.serverGrabPosition = null;
        this.serverGrabTarget = null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.connectedPos != null) {
            output.putLong(CONNECTED_POS_TAG, this.connectedPos.asLong());
        } else if (this.freeEnd != null) {
            output.putDouble(FREE_END_X_TAG, this.freeEnd.x());
            output.putDouble(FREE_END_Y_TAG, this.freeEnd.y());
            output.putDouble(FREE_END_Z_TAG, this.freeEnd.z());
        }
        if (this.isConnected()) {
            output.putDouble(ROPE_LENGTH_TAG, this.ropeLength);
        }
        if (this.synchronizedPoints != null) {
            int[] encoded = new int[this.synchronizedPoints.length];
            for (int index = 0; index < encoded.length; index++) {
                encoded[index] = Float.floatToIntBits((float) this.synchronizedPoints[index]);
            }
            output.putIntArray(ROPE_POINTS_TAG, encoded);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.connectedPos = input.getLong(CONNECTED_POS_TAG).map(BlockPos::of).orElse(null);
        double freeEndX = input.getDoubleOr(FREE_END_X_TAG, Double.NaN);
        double freeEndY = input.getDoubleOr(FREE_END_Y_TAG, Double.NaN);
        double freeEndZ = input.getDoubleOr(FREE_END_Z_TAG, Double.NaN);
        if (this.connectedPos == null
                && Double.isFinite(freeEndX)
                && Double.isFinite(freeEndY)
                && Double.isFinite(freeEndZ)) {
            this.freeEnd = new Vec3(freeEndX, freeEndY, freeEndZ);
        } else {
            this.freeEnd = null;
        }
        this.ropeLength = input.getDoubleOr(ROPE_LENGTH_TAG, 0.0);
        int[] encodedPoints = input.getIntArray(ROPE_POINTS_TAG).orElse(null);
        if (encodedPoints != null && encodedPoints.length >= 6 && encodedPoints.length % 3 == 0) {
            this.synchronizedPoints = new double[encodedPoints.length];
            for (int index = 0; index < encodedPoints.length; index++) {
                this.synchronizedPoints[index] = Float.intBitsToFloat(encodedPoints[index]);
            }
        } else {
            this.synchronizedPoints = null;
        }
        if (this.clientRope != null
                && this.synchronizedPoints != null
                && this.clientRope.applyPoints(this.synchronizedPoints, 0.3)) {
            this.simulatedConnectedPos = this.connectedPos;
            this.simulatedFreeEnd = this.freeEnd;
            this.simulatedLength = this.ropeLength;
            return;
        }
        this.invalidateClientRope();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.disconnect();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    private static double square(double value) {
        return value * value;
    }

    private static boolean isFinite(Vec3 position) {
        return Double.isFinite(position.x())
                && Double.isFinite(position.y())
                && Double.isFinite(position.z());
    }

    private void initializeStraightSnapshot() {
        if (this.connectedPos == null && this.freeEnd == null) {
            this.synchronizedPoints = null;
            return;
        }
        Vec3 start = Vec3.atCenterOf(this.worldPosition);
        Vec3 end = this.connectedPos != null
                ? Vec3.atCenterOf(this.connectedPos)
                : this.freeEnd;
        double directDistance = start.distanceTo(end);
        double safeLength = Math.max(this.ropeLength, directDistance);
        this.synchronizedPoints = RopeSimulation.withLength(
                start.x(), start.y(), start.z(),
                end.x(), end.y(), end.z(),
                safeLength, SERVER_PARAMETERS
        ).copyPoints();
    }
}
