package com.bdacoustic.physics;

/** Sabine-style room reverberation estimate for adapters that can provide room geometry. */
public final class RoomAcoustics {
    public record Estimate(double volumeCubicMetres, double equivalentAbsorptionArea, double rt60Seconds) {}
    private RoomAcoustics() {}

    public static Estimate estimate(double volume, double totalSurfaceArea, double meanAbsorption) {
        double safeVolume = finiteClamp(volume, 0.1, 1.0e9, 0.1);
        double safeSurface = finiteClamp(totalSurfaceArea, 0.1, 1.0e9, 0.1);
        double alpha = finiteClamp(meanAbsorption, 0.01, 0.99, 0.25);
        double absorptionArea = Math.max(0.1, safeSurface * alpha);
        double rt60 = finiteClamp(0.161 * safeVolume / absorptionArea, 0.05, 12.0, 0.05);
        return new Estimate(safeVolume, absorptionArea, rt60);
    }

    private static double finiteClamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
}
