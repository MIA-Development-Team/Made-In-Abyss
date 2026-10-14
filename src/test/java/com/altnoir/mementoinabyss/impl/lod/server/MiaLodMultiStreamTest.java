package com.altnoir.mementoinabyss.impl.lod.server;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.MiaHeight;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class MiaLodMultiStreamTest {
    @Test
    @SuppressWarnings("unchecked")
    void distinctLinksRetainIndependentCurrentTasksEpochsAndSourceNotifications() throws Exception {
        var taskType = Class.forName(MiaLodSampler.class.getName() + "$Task");
        var constructor =
                taskType.getDeclaredConstructor(ServerPlayer.class, CrossDimensionLodLink.class);
        constructor.setAccessible(true);
        var current = MiaLodSampler.class.getDeclaredMethod("isCurrent", taskType);
        current.setAccessible(true);
        var tasks = (Map<Object, Object>) field(MiaLodSampler.class, "TASKS").get(null);
        var saved = new HashMap<>(tasks);
        var height = new MiaHeight(-64, 384, 320);
        var upper =
                new CrossDimensionLodLink(
                        Level.OVERWORLD.identifier(),
                        Level.OVERWORLD,
                        MiaDimensions.THE_ABYSS_LEVEL,
                        height,
                        448);
        var lower =
                new CrossDimensionLodLink(
                        MiaDimensions.GREAT_FAULT_LEVEL.identifier(),
                        MiaDimensions.GREAT_FAULT_LEVEL,
                        MiaDimensions.THE_ABYSS_LEVEL,
                        height,
                        -704);
        var upperTask = constructor.newInstance(null, upper);
        var lowerTask = constructor.newInstance(null, lower);
        var replacement = constructor.newInstance(null, upper);
        var keyField = field(taskType, "key");
        var upperKey = keyField.get(upperTask);
        var lowerKey = keyField.get(lowerTask);
        long chunkKey = CrossDimensionLodKey.pack(3, -4);
        try {
            tasks.clear();
            tasks.put(upperKey, upperTask);
            tasks.put(lowerKey, lowerTask);
            assertEquals(2, tasks.size());
            assertNotEquals(upperKey, lowerKey);
            assertEquals(upperKey, keyField.get(replacement));
            assertNotEquals(
                    field(taskType, "streamEpoch").get(upperTask),
                    field(taskType, "streamEpoch").get(lowerTask));
            assertEquals(true, current.invoke(null, upperTask));
            assertEquals(true, current.invoke(null, lowerTask));
            for (var task : new Object[] {upperTask, lowerTask}) {
                var demands = new HashMap<Long, Object>();
                demands.put(chunkKey, null);
                field(taskType, "demands").set(task, demands);
            }
            MiaLodSampler.notifyReplaced(upper, new ChunkPos(3, -4), 17);
            var upperRevisions =
                    (Map<Long, Long>) field(taskType, "pendingRevisions").get(upperTask);
            var lowerRevisions =
                    (Map<Long, Long>) field(taskType, "pendingRevisions").get(lowerTask);
            assertEquals(17L, upperRevisions.get(chunkKey));
            assertFalse(lowerRevisions.containsKey(chunkKey));
            tasks.put(upperKey, replacement);
            assertEquals(false, current.invoke(null, upperTask));
            assertEquals(true, current.invoke(null, replacement));
            assertEquals(true, current.invoke(null, lowerTask));
            field(taskType, "active").setBoolean(replacement, false);
            assertEquals(false, current.invoke(null, replacement));
            MiaLodSampler.clearTasks();
            assertTrue(tasks.isEmpty());
            assertEquals(false, current.invoke(null, lowerTask));
        } finally {
            tasks.clear();
            tasks.putAll(saved);
        }
    }

    private static Field field(Class<?> type, String name) throws Exception {
        var field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
