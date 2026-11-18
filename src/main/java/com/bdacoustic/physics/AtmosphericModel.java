package com.bdacoustic.physics;

import net.minecraft.world.phys.Vec3;

/** Lightweight outdoor propagation approximation; pressure is deliberately not conflated with sound pressure. */
public final class AtmosphericModel {
    private AtmosphericModel() {}
    public static double airAbsorptionDbPerKm(int band, double temperatureC, double humidityPercent) {
        double humidity = Math.max(0, Math.min(100, humidityPercent)) / 100.0;
        double temperature = Math.max(-40, Math.min(60, temperatureC));
        double[] base = {0.02, 0.05, 0.12, 0.35, 1.1, 3.8};
        return base[Math.max(0, Math.min(base.length - 1, band))]
                * (1.25 - humidity * 0.45) * (1.0 + Math.max(0, temperature - 20) * 0.006);
    }
    public static double atmosphericGain(double distanceBlocks, int band, double temperatureC, double humidityPercent) {
        double kilometres = Math.max(0, distanceBlocks) / 1000.0;
        double db = airAbsorptionDbPerKm(band, temperatureC, humidityPercent) * kilometres;
        return Math.pow(10.0, -db / 20.0);
    }
    /** Positive wind toward the listener modestly increases effective propagation speed. */
    public static double effectiveSoundSpeed(double temperatureC, double humidityPercent, Vec3 wind, Vec3 sourceToListener) {
        double base = AcousticMath.speedOfSound(temperatureC, humidityPercent);
        if (wind == null || sourceToListener == null || sourceToListener.lengthSqr() < 1.0e-8) return base;
        double alongPath = wind.dot(sourceToListener.normalize());
        return Math.max(base * 0.9, Math.min(base * 1.1, base + alongPath * 0.35));
    }
}