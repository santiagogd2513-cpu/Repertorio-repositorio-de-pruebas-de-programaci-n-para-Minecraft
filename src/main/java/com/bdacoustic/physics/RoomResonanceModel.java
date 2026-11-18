package com.bdacoustic.physics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Rectangular-room modal frequency estimate (axial, tangential and oblique modes). */
public final class RoomResonanceModel {
    public record Mode(int lengthOrder, int widthOrder, int heightOrder,
                       double frequencyHz, double estimatedDecaySeconds) {}
    private RoomResonanceModel() {}

    /**
     * Estimates the lowest modes of a rectangular room.
     * Dimensions are metres. The result is bounded to avoid pathological allocations.
     */
    public static List<Mode> estimate(double lengthMetres, double widthMetres, double heightMetres,
                                      double soundSpeed, double meanAbsorption, int maximumModes) {
        double length = finiteClamp(lengthMetres, 0.5, 2000.0, 5.0);
        double width = finiteClamp(widthMetres, 0.5, 2000.0, 5.0);
        double height = finiteClamp(heightMetres, 0.5, 500.0, 3.0);
        double speed = finiteClamp(soundSpeed, 250.0, 2000.0, 343.2);
        double absorption = finiteClamp(meanAbsorption, 0.01, 0.99, 0.25);
        int limit = Math.max(1, Math.min(128, maximumModes));
        int maxOrder = Math.min(12, Math.max(3, (int) Math.ceil(Math.cbrt(limit) + 2)));
        List<Mode> modes = new ArrayList<>();
        for (int nx = 0; nx <= maxOrder; nx++) {
            for (int ny = 0; ny <= maxOrder; ny++) {
                for (int nz = 0; nz <= maxOrder; nz++) {
                    if (nx == 0 && ny == 0 && nz == 0) continue;
                    double squared = square(nx / length) + square(ny / width) + square(nz / height);
                    double frequency = (speed * 0.5) * Math.sqrt(squared);
                    if (frequency < 20.0 || frequency > 20000.0) continue;
                    // A coarse modal decay estimate; it is a design aid, not a full wave solver.
                    double decay = Math.max(0.03, Math.min(8.0, 0.18 / absorption));
                    modes.add(new Mode(nx, ny, nz, frequency, decay));
                }
            }
        }
        modes.sort(Comparator.comparingDouble(Mode::frequencyHz));
        if (modes.size() > limit) return List.copyOf(modes.subList(0, limit));
        return List.copyOf(modes);
    }

    private static double square(double value) { return value * value; }
    private static double finiteClamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
}
