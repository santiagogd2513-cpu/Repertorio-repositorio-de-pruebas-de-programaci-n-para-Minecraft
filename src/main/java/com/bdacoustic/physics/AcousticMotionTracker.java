package com.bdacoustic.physics;

import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Bounded velocity history for adapters that can report a stable source identifier and positions. */
public final class AcousticMotionTracker {
    private static final int MAX_TRACKED_SOURCES = 512;
    private static final double MAX_REASONABLE_SPEED = 200.0;
    private final Map<UUID, Sample> samples = new LinkedHashMap<>(32, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<UUID, Sample> eldest) {
            return size() > MAX_TRACKED_SOURCES;
        }
    };
    private record Sample(Vec3 position, long timestampNanos) {}

    public synchronized Vec3 update(UUID sourceId, Vec3 position, long timestampNanos) {
        if (sourceId == null || !isFinite(position)) return Vec3.ZERO;
        Sample previous = samples.put(sourceId, new Sample(position, timestampNanos));
        if (previous == null) return Vec3.ZERO;
        long elapsed = timestampNanos - previous.timestampNanos();
        if (elapsed <= 0L) return Vec3.ZERO;
        double seconds = elapsed / 1_000_000_000.0;
        if (seconds < 0.001 || seconds > 5.0) return Vec3.ZERO;
        Vec3 velocity = position.subtract(previous.position()).scale(1.0 / seconds);
        if (!isFinite(velocity) || velocity.length() > MAX_REASONABLE_SPEED) return Vec3.ZERO;
        return velocity;
    }

    private static boolean isFinite(Vec3 vector) {
        return vector != null && Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }

    public synchronized void forget(UUID sourceId) {
        if (sourceId != null) samples.remove(sourceId);
    }

    public synchronized void clear() { samples.clear(); }
    public synchronized int trackedSources() { return samples.size(); }
}
