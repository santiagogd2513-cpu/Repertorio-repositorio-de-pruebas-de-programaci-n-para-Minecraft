package com.bdacoustic.physics;

/** Immutable propagation estimate. Minecraft's own distance attenuation is intentionally not included. */
public record AcousticResult(double distance, double delaySeconds, double airGain,
                             double obstructionGain, double lowPassHz, double dopplerRatio,
                             int reflections, int blockedRays, boolean underwater,
                             double diffractionGain, double transmissionGain,
                             double boundaryGain, double scattering, double soundSpeed) {
    /** Additional gain to apply on top of the native Minecraft distance rolloff. */
    public double totalGain() {
        double result = airGain * obstructionGain * diffractionGain * transmissionGain * boundaryGain;
        return Math.max(0.0, Math.min(1.0, result));
    }
}
