package com.bdacoustic.physics;

/** First-order air/water boundary model using acoustic impedances (not a full fluid simulation). */
public final class Hydroacoustics {
    public static final double AIR_IMPEDANCE_RAYL = 413.0;
    public static final double WATER_IMPEDANCE_RAYL = 1_480_000.0;
    public static final double WATER_SOUND_SPEED_METRES_PER_SECOND = 1480.0;
    private Hydroacoustics() {}

    /** Normal-incidence power transmission coefficient at one ideal air/water boundary. */
    public static double boundaryPowerTransmission(boolean airToWater) {
        double a = AIR_IMPEDANCE_RAYL;
        double b = WATER_IMPEDANCE_RAYL;
        double coefficient = (4.0 * a * b) / ((a + b) * (a + b));
        return Math.max(0.0001, Math.min(1.0, coefficient));
    }

    public static double boundaryAmplitudeGain(boolean airToWater) {
        return Math.sqrt(boundaryPowerTransmission(airToWater));
    }

    public static double travelTime(double distanceMetres, boolean sourceUnderwater, boolean listenerUnderwater,
                                    double airSpeedMetresPerSecond) {
        double distance = Math.max(0.0, distanceMetres);
        double airSpeed = Math.max(250.0, airSpeedMetresPerSecond);
        if (sourceUnderwater && listenerUnderwater) return distance / WATER_SOUND_SPEED_METRES_PER_SECOND;
        if (!sourceUnderwater && !listenerUnderwater) return distance / airSpeed;
        // The actual interface location depends on terrain and water geometry; split the path evenly
        // until an adapter can provide the boundary intersection.
        return 0.5 * distance / airSpeed + 0.5 * distance / WATER_SOUND_SPEED_METRES_PER_SECOND;
    }

    public static double boundaryGain(boolean sourceUnderwater, boolean listenerUnderwater) {
        return sourceUnderwater == listenerUnderwater ? 1.0 : boundaryAmplitudeGain(true);
    }

    public static double underwaterAbsorptionDbPerKm(int band) {
        double[] values = {0.05, 0.12, 0.35, 1.0, 3.5, 12.0};
        return values[Math.max(0, Math.min(values.length - 1, band))];
    }
}
