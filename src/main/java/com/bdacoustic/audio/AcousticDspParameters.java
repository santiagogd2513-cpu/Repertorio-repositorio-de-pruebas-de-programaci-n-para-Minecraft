package com.bdacoustic.audio;

/** Immutable per-stream DSP settings. Values are clamped to safe ranges by the constructor. */
public record AcousticDspParameters(double gain, double lowPassHz, double highPassHz,
                                    double delaySeconds, double reverbMix) {
    public AcousticDspParameters {
        gain = clamp(gain, 0.0, 4.0, 1.0);
        lowPassHz = clamp(lowPassHz, 20.0, 24000.0, 20000.0);
        highPassHz = clamp(highPassHz, 0.0, 12000.0, 0.0);
        delaySeconds = clamp(delaySeconds, 0.0, 0.20, 0.0);
        reverbMix = clamp(reverbMix, 0.0, 0.65, 0.0);
    }
    public static AcousticDspParameters neutral() {
        return new AcousticDspParameters(1.0, 20000.0, 0.0, 0.0, 0.0);
    }
    private static double clamp(double v, double min, double max, double fallback) {
        return Double.isFinite(v) ? Math.max(min, Math.min(max, v)) : fallback;
    }
}
