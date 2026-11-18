package com.bdacoustic.physics;

public final class AcousticMath {
    public static final double SPEED_OF_SOUND_AIR_20C = 343.2;
    private AcousticMath() {}
    public static double speedOfSound(double temperatureC, double humidityPercent) {
        double dry = 331.3 + 0.606 * temperatureC;
        return Math.max(300.0, dry * (1.0 + 0.00012 * Math.max(0, Math.min(100, humidityPercent))));
    }
    public static double travelTime(double distance, double speed) {
        return Math.max(0, distance) / Math.max(1, speed);
    }
    public static double inverseDistanceGain(double distance) {
        return 1.0 / Math.max(1.0, 1.0 + distance * 0.035);
    }
    public static double dopplerRatio(double sourceRadialVelocity, double listenerRadialVelocity, double speed) {
        double c = Math.max(1, speed);
        return Math.max(0.5, Math.min(2.0, (c + listenerRadialVelocity) / Math.max(1, c - sourceRadialVelocity)));
    }
}