package com.bdacoustic.api;

import net.minecraft.world.phys.Vec3;

/** Adapter contract for any mod-provided positional sound source, including streaming audio. */
public interface AcousticSource {
    String acousticSourceId();
    Vec3 acousticPosition();
    default Vec3 acousticVelocity() { return Vec3.ZERO; }
    default boolean acousticStreaming() { return false; }
    default double acousticTemperatureC() { return 20.0; }
    default double acousticHumidityPercent() { return 50.0; }
    default double acousticPressurePa() { return 101325.0; }
    default Vec3 acousticWindVelocityMetresPerSecond() { return Vec3.ZERO; }
}
