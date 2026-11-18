package com.bdacoustic.api;

import com.bdacoustic.audio.AcousticDspParameters;
import com.bdacoustic.audio.StreamingDsp;
import com.bdacoustic.physics.AcousticEngine;
import com.bdacoustic.physics.AcousticEnvironment;
import com.bdacoustic.physics.AcousticMotionTracker;
import com.bdacoustic.physics.AcousticResult;
import com.bdacoustic.physics.AerodynamicNoiseModel;
import com.bdacoustic.physics.RoomAcoustics;
import com.bdacoustic.physics.SpatialAudioModel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Stable integration entry points for Beautiful Decor and third-party audio mods. */
public final class AcousticApi {
    private static final StreamingDsp STREAMING_DSP = new StreamingDsp();
    private static final AcousticMotionTracker MOTION_TRACKER = new AcousticMotionTracker();
    private AcousticApi() {}

    /** Backwards-compatible helper for stationary listeners. */
    public static AcousticResult evaluate(Level level, AcousticSource source, Vec3 listener, int rayBudget) {
        return evaluate(level, source, listener, 0.0, rayBudget);
    }

    /** Listener radial velocity is positive when moving toward the sound source (metres/second). */
    public static AcousticResult evaluate(Level level, AcousticSource source, Vec3 listener,
                                          double listenerRadialVelocity, int rayBudget) {
        Vec3 direction = listener.subtract(source.acousticPosition()).normalize();
        double sourceRadialVelocity = source.acousticVelocity().dot(direction);
        AcousticEnvironment environment = new AcousticEnvironment(source.acousticTemperatureC(),
                source.acousticHumidityPercent(), source.acousticPressurePa(),
                source.acousticWindVelocityMetresPerSecond());
        return AcousticEngine.evaluate(level, source.acousticPosition(), listener, environment,
                sourceRadialVelocity, listenerRadialVelocity, rayBudget);
    }

    public static AcousticResult evaluate(Level level, Vec3 source, Vec3 listener,
                                          AcousticEnvironment environment, int rayBudget) {
        return AcousticEngine.evaluate(level, source, listener, environment, 0.0, 0.0, rayBudget);
    }

    public static RoomAcoustics.Estimate estimateRoom(AcousticRoom room) {
        return RoomAcoustics.estimate(room.roomVolumeCubicMetres(), room.roomSurfaceAreaSquareMetres(),
                room.roomMeanAbsorption());
    }

    public static SpatialAudioModel.StereoGains estimateStereo(Vec3 source, Vec3 listener, Vec3 listenerForward) {
        return SpatialAudioModel.estimate(source, listener, listenerForward);
    }

    public static AerodynamicNoiseModel.Estimate estimateAirflow(double relativeAirSpeedMetresPerSecond,
            double frontalAreaSquareMetres, double shapeFactor, double turbulenceFactor) {
        return AerodynamicNoiseModel.estimate(relativeAirSpeedMetresPerSecond, frontalAreaSquareMetres,
                shapeFactor, turbulenceFactor);
    }

    /** Estimate source velocity from consecutive positions. Call on the game/client thread with nanoTime timestamps. */
    public static Vec3 updateTrackedSourceVelocity(String sourceId, Vec3 position, long timestampNanos) {
        if (sourceId == null || sourceId.isBlank()) return Vec3.ZERO;
        UUID id = UUID.nameUUIDFromBytes(sourceId.getBytes(StandardCharsets.UTF_8));
        return MOTION_TRACKER.update(id, position, timestampNanos);
    }

    public static void forgetTrackedSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) return;
        MOTION_TRACKER.forget(UUID.nameUUIDFromBytes(sourceId.getBytes(StandardCharsets.UTF_8)));
    }

    /** Applies chunk-safe DSP to one channel. Reuse streamId across consecutive blocks. */
    public static void processStream(UUID streamId, float[] samples,
                                     AcousticDspParameters parameters, double sampleRate) {
        STREAMING_DSP.process(streamId, samples, parameters, sampleRate);
    }

    public static void closeStream(UUID streamId) { STREAMING_DSP.forget(streamId); }
}
