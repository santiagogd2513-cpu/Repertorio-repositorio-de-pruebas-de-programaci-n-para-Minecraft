package com.bdacoustic.api;

import net.minecraft.world.phys.Vec3;

/** Adapter contract for mod-provided positional audio sources. */
public interface AcousticSource {
    String acousticSourceId();
    Vec3 acousticPosition();
    default Vec3 acousticVelocity() { return Vec3.ZERO; }
    default boolean acousticStreaming() { return false; }
    default double acousticTemperatureC() { return 20.0; }
    default double acousticHumidityPercent() { return 50.0; }
}