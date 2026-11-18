package com.bdacoustic.physics;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class AcousticEngine {
    private AcousticEngine() {}
    public static AcousticResult evaluate(Level level, Vec3 source, Vec3 listener, double temperatureC,
                                          double humidity, double sourceRadialVelocity, double listenerRadialVelocity,
                                          int rayBudget) {
        double distance = source.distanceTo(listener);
        boolean water = level.getFluidState(net.minecraft.core.BlockPos.containing(source)).is(net.minecraft.tags.FluidTags.WATER)
                || level.getFluidState(net.minecraft.core.BlockPos.containing(listener)).is(net.minecraft.tags.FluidTags.WATER);
        double speed = water ? 1480.0 : AcousticMath.speedOfSound(temperatureC, humidity);
        AcousticWorldTracer.Trace trace = AcousticWorldTracer.trace(level, source, listener, rayBudget);
        double air = AcousticMath.inverseDistanceGain(distance) * AtmosphericModel.atmosphericGain(distance, 3, temperatureC, humidity);
        if (water) air *= 0.72;
        return new AcousticResult(distance, AcousticMath.travelTime(distance, speed), air,
                trace.obstruction(), trace.lowPassHz(), AcousticMath.dopplerRatio(sourceRadialVelocity,
                listenerRadialVelocity, speed), trace.reflections(), trace.blocked(), water || trace.underwater());
    }
}