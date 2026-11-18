package com.bdacoustic.physics;

/** Sabine-style room reverberation estimate for adapters that can provide room geometry. */
public final class RoomAcoustics {
    public record Estimate(double volumeCubicMetres, double equivalentAbsorptionArea, double rt60Seconds) {}
    private RoomAcoustics() {}
    public static Estimate estimate(double volume, double totalSurfaceArea, double meanAbsorption) {
        double safeVolume = Math.max(0.1, volume);
        double alpha = Math.max(0.01, Math.min(0.99, meanAbsorption));
        double absorptionArea = Math.max(0.1, Math.max(0.1, totalSurfaceArea) * alpha);
        double rt60 = Math.max(0.05, Math.min(12.0, 0.161 * safeVolume / absorptionArea));
        return new Estimate(safeVolume, absorptionArea, rt60);
    }
}