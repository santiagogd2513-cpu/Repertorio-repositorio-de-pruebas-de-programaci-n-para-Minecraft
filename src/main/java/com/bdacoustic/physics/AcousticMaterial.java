package com.bdacoustic.physics;

/** Approximate octave-band absorption coefficients, from low to high frequency. */
public enum AcousticMaterial {
    AIR(0.00,0.00,0.00,0.00,0.00,0.00),
    STONE(0.02,0.02,0.03,0.04,0.05,0.07),
    WOOD(0.15,0.11,0.10,0.08,0.08,0.10),
    GLASS(0.18,0.06,0.04,0.03,0.02,0.02),
    SOIL(0.10,0.18,0.28,0.38,0.48,0.55),
    FOLIAGE(0.12,0.20,0.32,0.45,0.58,0.65),
    WATER(0.02,0.03,0.05,0.08,0.12,0.18),
    SOFT(0.25,0.35,0.55,0.70,0.80,0.85),
    UNKNOWN(0.08,0.10,0.12,0.14,0.16,0.18);

    private final double[] absorption;
    AcousticMaterial(double... absorption) { this.absorption = absorption; }
    public double absorption(int band) { return absorption[Math.max(0, Math.min(absorption.length - 1, band))]; }
    public double meanAbsorption() {
        double sum = 0;
        for (double value : absorption) sum += value;
        return sum / absorption.length;
    }
}