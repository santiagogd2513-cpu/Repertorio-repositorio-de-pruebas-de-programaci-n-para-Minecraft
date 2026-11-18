package com.bdacoustic.physics;

public record AcousticResult(double distance, double delaySeconds, double airGain,
                             double obstructionGain, double lowPassHz, double dopplerRatio,
                             int reflections, int blockedRays, boolean underwater) {
    public double totalGain() { return Math.max(0.0, Math.min(1.0, airGain * obstructionGain)); }
}