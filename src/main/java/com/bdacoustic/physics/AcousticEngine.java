package com.bdacoustic.physics;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class AcousticEngine {
    private AcousticEngine() {}

    /** Backwards-compatible convenience overload using standard environmental conditions. */
    public static AcousticResult evaluate(Level level, Vec3 source, Vec3 listener, double temperatureC,
                                          double humidity, double sourceRadialVelocity,
                                          double listenerRadialVelocity, int rayBudget) {
        return evaluate(level, source, listener,
                new AcousticEnvironment(temperatureC, humidity, 101325.0, Vec3.ZERO),
                sourceRadialVelocity, listenerRadialVelocity, rayBudget);
    }

    public static AcousticResult evaluate(Level level, Vec3 source, Vec3 listener,
                                          AcousticEnvironment environment,
                                          double sourceRadialVelocity, double listenerRadialVelocity,
                                          int rayBudget) {
        AcousticEnvironment env = environment == null ? AcousticEnvironment.standard() : environment;
        double distance = source.distanceTo(listener);
        Vec3 path = listener.subtract(source);
        boolean sourceWater = level.getFluidState(BlockPos.containing(source)).is(FluidTags.WATER);
        boolean listenerWater = level.getFluidState(BlockPos.containing(listener)).is(FluidTags.WATER);
        boolean bothUnderwater = sourceWater && listenerWater;
        double airSpeed = AtmosphericModel.effectiveSoundSpeed(env.temperatureC(),
                env.relativeHumidityPercent(), env.windVelocityMetresPerSecond(), path);
        double delay = Hydroacoustics.travelTime(distance, sourceWater, listenerWater, airSpeed);
        double speedForDoppler = bothUnderwater ? Hydroacoustics.WATER_SOUND_SPEED_METRES_PER_SECOND : airSpeed;
        AcousticWorldTracer.Trace trace = AcousticWorldTracer.trace(level, source, listener, rayBudget, !bothUnderwater);

        double airGain = bothUnderwater
                ? Math.pow(10.0, -Hydroacoustics.underwaterAbsorptionDbPerKm(3) * distance / 1000.0 / 20.0)
                : AtmosphericModel.atmosphericGain(distance, 3, env.temperatureC(),
                        env.relativeHumidityPercent(), env.pressurePa());
        if (!sourceWater && !listenerWater && trace.underwater()) airGain *= 0.85;
        double boundaryGain = Hydroacoustics.boundaryGain(sourceWater, listenerWater);
        boolean underwater = bothUnderwater || sourceWater || listenerWater || trace.underwater();
        return new AcousticResult(distance, delay, airGain, trace.obstruction(), trace.lowPassHz(),
                AcousticMath.dopplerRatio(sourceRadialVelocity, listenerRadialVelocity, speedForDoppler),
                trace.reflections(), trace.blocked(), underwater, trace.diffraction(),
                trace.transmission(), boundaryGain, trace.scattering(), speedForDoppler);
    }
}
