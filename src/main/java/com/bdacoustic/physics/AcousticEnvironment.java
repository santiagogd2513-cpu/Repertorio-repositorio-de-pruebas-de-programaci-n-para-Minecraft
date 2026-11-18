package com.bdacoustic.physics;

import net.minecraft.world.phys.Vec3;

/** Environmental inputs supplied by the game or an optional mod adapter. SI units are used. */
public record AcousticEnvironment(double temperatureC, double relativeHumidityPercent,
                                  double pressurePa, Vec3 windVelocityMetresPerSecond) {
    public AcousticEnvironment {
        temperatureC = clamp(temperatureC, -80.0, 80.0, 20.0);
        relativeHumidityPercent = clamp(relativeHumidityPercent, 0.0, 100.0, 50.0);
        pressurePa = clamp(pressurePa, 30000.0, 120000.0, 101325.0);
        windVelocityMetresPerSecond = windVelocityMetresPerSecond == null ? Vec3.ZERO : windVelocityMetresPerSecond;
    }
    public static AcousticEnvironment standard() {
        return new AcousticEnvironment(20.0, 50.0, 101325.0, Vec3.ZERO);
    }
    private static double clamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
}
