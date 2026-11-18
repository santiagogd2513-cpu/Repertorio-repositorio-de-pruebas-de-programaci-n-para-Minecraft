package com.bdacoustic.physics;

import net.minecraft.world.phys.Vec3;

/** Lightweight stereo/binaural-pan estimate. This is not a measured HRTF convolution. */
public final class SpatialAudioModel {
    public record StereoGains(double left, double right, double pan, double interauralDelaySeconds) {}
    private SpatialAudioModel() {}

    public static StereoGains estimate(Vec3 source, Vec3 listener, Vec3 listenerForward) {
        Vec3 toSource = source.subtract(listener);
        if (toSource.lengthSqr() < 1.0e-8) return new StereoGains(0.7071, 0.7071, 0.0, 0.0);
        Vec3 forward = listenerForward == null || listenerForward.lengthSqr() < 1.0e-8
                ? new Vec3(0.0, 0.0, 1.0) : listenerForward.normalize();
        Vec3 right = new Vec3(forward.z, 0.0, -forward.x);
        if (right.lengthSqr() < 1.0e-8) right = new Vec3(1.0, 0.0, 0.0);
        else right = right.normalize();
        Vec3 direction = toSource.normalize();
        double pan = Math.max(-1.0, Math.min(1.0, direction.dot(right)));
        double left = Math.sqrt((1.0 - pan) * 0.5);
        double rightGain = Math.sqrt((1.0 + pan) * 0.5);
        double delay = Math.abs(pan) * 0.00065;
        return new StereoGains(left, rightGain, pan, delay);
    }
}
