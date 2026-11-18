package com.bdacoustic.physics;

import net.minecraft.world.phys.Vec3;

/** Outdoor propagation estimate. Atmospheric pressure is kept separate from acoustic pressure. */
public final class AtmosphericModel {
    private static final double[] BASE_DB_PER_KM = {0.02, 0.05, 0.12, 0.35, 1.1, 3.8};
    private AtmosphericModel() {}

    public static double airAbsorptionDbPerKm(int band, double temperatureC, double humidityPercent) {
        return airAbsorptionDbPerKm(band, temperatureC, humidityPercent, 101325.0);
    }

    public static double airAbsorptionDbPerKm(int band, double temperatureC,
                                               double humidityPercent, double pressurePa) {
        int index = Math.max(0, Math.min(BASE_DB_PER_KM.length - 1, band));
        double humidity = clamp(humidityPercent, 0.0, 100.0, 50.0) / 100.0;
        double temperature = clamp(temperatureC, -80.0, 80.0, 20.0);
        double pressure = clamp(pressurePa, 30000.0, 120000.0, 101325.0);
        double pressureFactor = Math.pow(101325.0 / pressure, 0.18);
        return BASE_DB_PER_KM[index] * (1.25 - humidity * 0.45)
                * (1.0 + Math.max(0.0, temperature - 20.0) * 0.006) * pressureFactor;
    }

    public static double atmosphericGain(double distanceBlocks, int band, double temperatureC, double humidityPercent) {
        return atmosphericGain(distanceBlocks, band, temperatureC, humidityPercent, 101325.0);
    }

    public static double atmosphericGain(double distanceBlocks, int band, double temperatureC,
                                         double humidityPercent, double pressurePa) {
        double kilometres = Math.max(0.0, distanceBlocks) / 1000.0;
        double db = airAbsorptionDbPerKm(band, temperatureC, humidityPercent, pressurePa) * kilometres;
        return Math.pow(10.0, -db / 20.0);
    }

    /** Wind contributes only its component along the source-listener path. */
    public static double effectiveSoundSpeed(double temperatureC, double humidityPercent,
                                             Vec3 wind, Vec3 sourceToListener) {
        double base = AcousticMath.speedOfSound(temperatureC, humidityPercent);
        if (wind == null || sourceToListener == null || sourceToListener.lengthSqr() < 1.0e-8) return base;
        double alongPath = wind.dot(sourceToListener.normalize());
        return Math.max(base * 0.90, Math.min(base * 1.10, base + alongPath));
    }

    private static double clamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
}
