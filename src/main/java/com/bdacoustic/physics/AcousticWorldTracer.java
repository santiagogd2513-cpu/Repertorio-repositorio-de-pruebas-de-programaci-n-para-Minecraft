package com.bdacoustic.physics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

public final class AcousticWorldTracer {
    public record Trace(double obstruction, int blocked, int reflections, double lowPassHz, boolean underwater) {}
    private AcousticWorldTracer() {}

    public static Trace trace(Level level, Vec3 from, Vec3 to, int rays) {
        int count = Math.max(1, rays);
        int blocked = 0, reflections = 0;
        double absorption = 0;
        boolean underwater = false;
        Vec3 delta = to.subtract(from);
        for (int i = 0; i < count; i++) {
            double ox = (((i * 37) % 11) - 5) * 0.035;
            double oy = (((i * 17) % 7) - 3) * 0.045;
            double oz = (((i * 29) % 13) - 6) * 0.035;
            Vec3 start = from.add(ox, oy, oz);
            Vec3 end = to.add(ox, oy, oz);
            BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null));
            if (hit.getType() != BlockHitResult.Type.MISS) {
                blocked++;
                BlockState state = level.getBlockState(hit.getBlockPos());
                AcousticMaterial material = materialFor(state);
                absorption += material.meanAbsorption();
                if (state.getFluidState().isSource()) underwater |= state.getFluidState().getType().is(net.minecraft.tags.FluidTags.WATER);
                if (material.meanAbsorption() < 0.16) reflections++;
            }
        }
        double ratio = (double) blocked / count;
        double obstruction = Math.max(0.035, 1.0 - ratio * (0.72 + Math.min(0.22, absorption / count)));
        double cutoff = Math.max(650.0, 18000.0 * (1.0 - ratio * 0.88));
        return new Trace(obstruction, blocked, reflections, cutoff, underwater);
    }

    private static AcousticMaterial materialFor(BlockState state) {
        if (state.isAir()) return AcousticMaterial.AIR;
        if (state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) return AcousticMaterial.WATER;
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (path.contains("glass") || path.contains("pane")) return AcousticMaterial.GLASS;
        if (path.contains("leaves") || path.contains("flower") || path.contains("grass")) return AcousticMaterial.FOLIAGE;
        if (path.contains("wool") || path.contains("carpet") || path.contains("moss")) return AcousticMaterial.SOFT;
        if (path.contains("plank") || path.contains("log") || path.contains("wood")) return AcousticMaterial.WOOD;
        if (path.contains("dirt") || path.contains("sand") || path.contains("gravel")) return AcousticMaterial.SOIL;
        if (path.contains("stone") || path.contains("brick") || path.contains("concrete")) return AcousticMaterial.STONE;
        return AcousticMaterial.UNKNOWN;
    }
}