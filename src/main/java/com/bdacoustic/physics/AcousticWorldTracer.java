package com.bdacoustic.physics;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Multi-ray voxel-world probe. Material values are empirical game approximations, not measured block data. */
public final class AcousticWorldTracer {
    public record Trace(double obstruction, int blocked, int reflections, double lowPassHz,
                        boolean underwater, double diffraction, double transmission,
                        double scattering, double meanAbsorption) {}
    private AcousticWorldTracer() {}

    public static Trace trace(Level level, Vec3 from, Vec3 to, int rays) {
        return trace(level, from, to, rays, true);
    }

    /** includeFluids=false when both endpoints are underwater, so the medium itself is not treated as a wall. */
    public static Trace trace(Level level, Vec3 from, Vec3 to, int rays, boolean includeFluids) {
        int count = Math.max(1, Math.min(64, rays));
        int blocked = 0;
        int reflections = 0;
        int waterHits = 0;
        double absorption = 0.0;
        double materialTransmission = 0.0;
        double scattering = 0.0;
        ClipContext.Fluid fluidMode = includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        for (int i = 0; i < count; i++) {
            double scale = 0.12 + (i % 5) * 0.075;
            double ox = (((i * 37) % 11) - 5) * scale * 0.16;
            double oy = (((i * 17) % 7) - 3) * scale * 0.20;
            double oz = (((i * 29) % 13) - 6) * scale * 0.16;
            Vec3 offset = new Vec3(ox, oy, oz);
            BlockHitResult hit = level.clip(new ClipContext(from.add(offset), to.add(offset),
                    ClipContext.Block.COLLIDER, fluidMode, null));
            if (hit.getType() == BlockHitResult.Type.MISS) continue;
            blocked++;
            BlockState state = level.getBlockState(hit.getBlockPos());
            AcousticMaterial material = materialFor(state);
            absorption += material.meanAbsorption();
            materialTransmission += material.meanTransmission();
            scattering += material.meanScattering();
            if (material.reflectivity(2) > 0.55) reflections++;
            if (state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) waterHits++;
        }
        double blockedRatio = (double) blocked / count;
        double avgAbsorption = blocked == 0 ? 0.0 : absorption / blocked;
        double avgMaterialTransmission = blocked == 0 ? 1.0 : materialTransmission / blocked;
        double avgScattering = blocked == 0 ? 0.0 : scattering / blocked;
        double obstruction = clamp(1.0 - blockedRatio * 0.55, 0.20, 1.0);
        double diffraction = blocked == 0 ? 1.0 : clamp(0.30 + 0.25 * (1.0 - blockedRatio), 0.30, 0.55);
        double transmission = blocked == 0 ? 1.0 : clamp(0.45 + avgMaterialTransmission * 0.55, 0.45, 0.98);
        double cutoff = Math.max(550.0, 19000.0 * (1.0 - blockedRatio * (0.82 + avgAbsorption * 0.12)));
        return new Trace(obstruction, blocked, reflections, cutoff, waterHits > 0,
                diffraction, transmission, avgScattering, avgAbsorption);
    }

    private static AcousticMaterial materialFor(BlockState state) {
        if (state.isAir()) return AcousticMaterial.AIR;
        if (state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) return AcousticMaterial.WATER;
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (path.contains("glass") || path.contains("pane")) return AcousticMaterial.GLASS;
        if (path.contains("leaves") || path.contains("flower") || path.contains("grass") || path.contains("vine")) return AcousticMaterial.FOLIAGE;
        if (path.contains("wool") || path.contains("carpet") || path.contains("moss") || path.contains("sponge")) return AcousticMaterial.SOFT;
        if (path.contains("plank") || path.contains("log") || path.contains("wood") || path.contains("stem")) return AcousticMaterial.WOOD;
        if (path.contains("dirt") || path.contains("sand") || path.contains("gravel") || path.contains("mud") || path.contains("clay")) return AcousticMaterial.SOIL;
        if (path.contains("iron") || path.contains("copper") || path.contains("gold") || path.contains("anvil") || path.contains("metal")) return AcousticMaterial.METAL;
        if (path.contains("stone") || path.contains("brick") || path.contains("concrete") || path.contains("deepslate") || path.contains("blackstone")) return AcousticMaterial.STONE;
        return AcousticMaterial.UNKNOWN;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
