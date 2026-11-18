package com.bdacoustic.physics;

/** Coherent two-path interference and single-edge diffraction approximations. */
public final class WaveInterferenceModel {
    public record Interference(double phaseRadians, double amplitudeGain, double decibels) {}
    private WaveInterferenceModel() {}

    /**
     * Relative amplitude for two coherent paths with equal direct amplitudes.
     * Path lengths are metres; frequency is Hz; sound speed is metres/second.
     */
    public static Interference twoPath(double pathA, double pathB, double frequencyHz, double soundSpeed) {
        double a = finiteClamp(pathA, 0.0, 1.0e7, 0.0);
        double b = finiteClamp(pathB, 0.0, 1.0e7, 0.0);
        double frequency = finiteClamp(frequencyHz, 1.0, 24000.0, 1000.0);
        double speed = finiteClamp(soundSpeed, 1.0, 2000.0, 343.2);
        double phase = 2.0 * Math.PI * (Math.abs(a - b) * frequency / speed);
        double gain = Math.abs(Math.cos(phase * 0.5));
        gain = Math.max(0.0001, Math.min(1.0, gain));
        return new Interference(phase, gain, 20.0 * Math.log10(gain));
    }

    /** Approximate knife-edge diffraction loss from the dimensionless Fresnel parameter v. */
    public static double knifeEdgeGain(double fresnelV) {
        double v = finiteClamp(fresnelV, -10.0, 20.0, 0.0);
        if (v <= -0.78) return 1.0;
        double lossDb = 6.9 + 20.0 * Math.log10(Math.sqrt((v - 0.1) * (v - 0.1) + 1.0) + v - 0.1);
        return Math.pow(10.0, -Math.max(0.0, lossDb) / 20.0);
    }

    private static double finiteClamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
}
