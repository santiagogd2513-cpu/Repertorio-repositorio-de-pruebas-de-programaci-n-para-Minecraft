package com.bdacoustic.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AcousticConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.BooleanValue APPLY_GAIN;
    public static final ForgeConfigSpec.BooleanValue APPLY_DOPPLER;
    public static final ForgeConfigSpec.DoubleValue QUALITY;
    public static final ForgeConfigSpec.IntValue MAX_RAYS;
    public static final ForgeConfigSpec.DoubleValue MAX_DISTANCE;
    public static final ForgeConfigSpec SPEC;
    static {
        BUILDER.push("acoustics");
        ENABLED = BUILDER.comment("Enable BD Acoustic Engine sound processing").define("enabled", true);
        APPLY_GAIN = BUILDER.comment("Apply material obstruction and atmospheric gain to positional sounds").define("applyGain", true);
        APPLY_DOPPLER = BUILDER.comment("Apply listener-motion Doppler pitch to positional sounds").define("applyDoppler", true);
        QUALITY = BUILDER.comment("Adaptive acoustic probe budget multiplier from 0.1 to 1.0").defineInRange("quality", 0.65, 0.1, 1.0);
        MAX_RAYS = BUILDER.comment("Maximum acoustic probe rays per evaluated sound").defineInRange("maxRays", 12, 4, 64);
        MAX_DISTANCE = BUILDER.comment("Maximum distance evaluated by the acoustic model").defineInRange("maxDistance", 512.0, 16.0, 4096.0);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    private AcousticConfig() {}
}
