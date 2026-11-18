package com.bdacoustic.physics;

public final class AcousticMath {
    public static final double SPEED_OF_SOUND_AIR_20C = 343.2;
    private AcousticMath() {}

    public static double speedOfSound(double temperatureC, double humidityPercent) {
        double temperature = finiteClamp(temperatureC, -80.0, 80.0, 20.0);
        double humidity = finiteClamp(humidityPercent, 0.0, 100.0, 50.0);
        double dry = 331.3 + 0.606 * temperature;
        return Math.max(280.0, dry * (1.0 + 0.00012 * humidity));
    }

    public static double travelTime(double distance, double speed) {
        return Math.max(0.0, finiteOr(distance, 0.0)) / Math.max(1.0, finiteOr(speed, 343.2));
    }

    /** Geometric free-field amplitude term for adapters that do not use Minecraft's own distance rolloff. */
    public static double inverseDistanceGain(double distance) {
        double safeDistance = Math.max(0.0, finiteOr(distance, 0.0));
        return 1.0 / Math.max(1.0, 1.0 + safeDistance * 0.035);
    }

    public static double dopplerRatio(double sourceRadialVelocity, double listenerRadialVelocity, double speed) {
        double c = Math.max(1.0, finiteOr(speed, 343.2));
        double source = finiteClamp(sourceRadialVelocity, -0.8 * c, 0.8 * c, 0.0);
        double listener = finiteClamp(listenerRadialVelocity, -0.8 * c, 0.8 * c, 0.0);
        return Math.max(0.5, Math.min(2.0, (c + listener) / Math.max(1.0, c - source)));
    }

    private static double finiteOr(double value, double fallback) {
        return Double.isFinite(value) ? value : fallback;
    }
    private static double finiteClamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
}
