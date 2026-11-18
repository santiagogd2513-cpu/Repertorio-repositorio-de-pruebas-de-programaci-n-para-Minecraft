package com.bdacoustic.physics;

/** Separate estimate for airflow/turbulence noise; engine and exhaust audio remain separate sources. */
public final class AerodynamicNoiseModel {
    public record Estimate(double broadbandGain, double turbulence, double whistleFrequencyHz,
                           double lowFrequencyGain, double highFrequencyGain) {}
    private AerodynamicNoiseModel() {}

    public static Estimate estimate(double relativeAirSpeedMetresPerSecond, double frontalAreaSquareMetres,
                                     double shapeFactor, double turbulenceFactor) {
        double speed = finiteClamp(relativeAirSpeedMetresPerSecond, 0.0, 180.0, 0.0);
        double area = finiteClamp(frontalAreaSquareMetres, 0.01, 30.0, 1.0);
        double shape = finiteClamp(shapeFactor, 0.05, 2.0, 0.7);
        double turbulence = finiteClamp(turbulenceFactor, 0.0, 1.0, 0.25);
        double dynamic = speed * speed * area * 0.000015 * shape;
        double broadband = Math.min(1.0, dynamic * (0.25 + turbulence));
        double turbulenceLevel = Math.min(1.0, turbulence * speed / 70.0);
        double whistle = speed < 2.0 ? 0.0 : Math.max(80.0, Math.min(12000.0, speed * (180.0 + 520.0 * shape)));
        return new Estimate(broadband, turbulenceLevel, whistle,
                broadband * (0.45 + 0.25 * turbulence), broadband * (0.55 + 0.45 * turbulence));
    }

    private static double finiteClamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
}
