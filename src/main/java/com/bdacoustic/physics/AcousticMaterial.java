package com.bdacoustic.physics;

/** Octave-band estimates ordered from low to high frequency (125 Hz through 4 kHz). */
public enum AcousticMaterial {
    AIR(new double[]{0.00,0.00,0.00,0.00,0.00,0.00}, new double[]{1,1,1,1,1,1}, new double[]{0,0,0,0,0,0}),
    STONE(new double[]{0.02,0.02,0.03,0.04,0.05,0.07}, new double[]{0.01,0.01,0.008,0.006,0.004,0.003}, new double[]{0.12,0.14,0.16,0.18,0.20,0.22}),
    WOOD(new double[]{0.15,0.11,0.10,0.08,0.08,0.10}, new double[]{0.12,0.10,0.08,0.06,0.04,0.03}, new double[]{0.20,0.22,0.25,0.28,0.30,0.32}),
    GLASS(new double[]{0.18,0.06,0.04,0.03,0.02,0.02}, new double[]{0.10,0.07,0.05,0.03,0.02,0.01}, new double[]{0.04,0.04,0.05,0.06,0.08,0.10}),
    SOIL(new double[]{0.10,0.18,0.28,0.38,0.48,0.55}, new double[]{0.02,0.015,0.01,0.008,0.005,0.003}, new double[]{0.28,0.32,0.36,0.40,0.44,0.48}),
    FOLIAGE(new double[]{0.12,0.20,0.32,0.45,0.58,0.65}, new double[]{0.70,0.65,0.58,0.50,0.42,0.35}, new double[]{0.35,0.40,0.45,0.50,0.55,0.60}),
    WATER(new double[]{0.02,0.03,0.05,0.08,0.12,0.18}, new double[]{0.002,0.002,0.002,0.002,0.002,0.002}, new double[]{0.02,0.03,0.04,0.05,0.06,0.08}),
    SOFT(new double[]{0.25,0.35,0.55,0.70,0.80,0.85}, new double[]{0.01,0.008,0.006,0.004,0.003,0.002}, new double[]{0.25,0.28,0.32,0.36,0.40,0.44}),
    METAL(new double[]{0.01,0.01,0.015,0.02,0.025,0.03}, new double[]{0.002,0.002,0.001,0.001,0.001,0.001}, new double[]{0.08,0.10,0.12,0.14,0.16,0.18}),
    UNKNOWN(new double[]{0.08,0.10,0.12,0.14,0.16,0.18}, new double[]{0.05,0.04,0.03,0.02,0.015,0.01}, new double[]{0.20,0.22,0.24,0.26,0.28,0.30});

    private final double[] absorption;
    private final double[] transmission;
    private final double[] scattering;
    AcousticMaterial(double[] absorption, double[] transmission, double[] scattering) {
        this.absorption = absorption;
        this.transmission = transmission;
        this.scattering = scattering;
    }
    public double absorption(int band) { return at(absorption, band); }
    public double transmission(int band) { return at(transmission, band); }
    public double scattering(int band) { return at(scattering, band); }
    public double reflectivity(int band) {
        return Math.max(0.0, Math.min(1.0, (1.0 - absorption(band)) * (1.0 - transmission(band))));
    }
    public double meanAbsorption() { return mean(absorption); }
    public double meanTransmission() { return mean(transmission); }
    public double meanScattering() { return mean(scattering); }
    private static double at(double[] values, int band) { return values[Math.max(0, Math.min(values.length - 1, band))]; }
    private static double mean(double[] values) {
        double sum = 0.0;
        for (double value : values) sum += value;
        return sum / values.length;
    }
}
