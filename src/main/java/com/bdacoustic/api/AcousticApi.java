package com.bdacoustic.api;

import com.bdacoustic.physics.AcousticEngine;
import com.bdacoustic.physics.AcousticResult;
import com.bdacoustic.physics.RoomAcoustics;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class AcousticApi {
    private AcousticApi() {}
    public static AcousticResult evaluate(Level level, AcousticSource source, Vec3 listener, int rayBudget) {
        Vec3 direction = listener.subtract(source.acousticPosition()).normalize();
        double sourceVelocity = source.acousticVelocity().dot(direction);
        return AcousticEngine.evaluate(level, source.acousticPosition(), listener,
                source.acousticTemperatureC(), source.acousticHumidityPercent(),
                sourceVelocity, 0.0, rayBudget);
    }
    public static RoomAcoustics.Estimate estimateRoom(AcousticRoom room) {
        return RoomAcoustics.estimate(room.roomVolumeCubicMetres(), room.roomSurfaceAreaSquareMetres(), room.roomMeanAbsorption());
    }
}