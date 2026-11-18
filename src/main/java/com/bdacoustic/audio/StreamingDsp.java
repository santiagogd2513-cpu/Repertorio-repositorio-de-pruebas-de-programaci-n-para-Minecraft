package com.bdacoustic.audio;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Chunk-safe mono DSP core. Retains filter, delay and reverb memory between calls. */
public final class StreamingDsp {
    private static final int MAX_STREAMS = 128;
    private static final double MAX_SAMPLE_RATE = 96000.0;
    private static final double MAX_DELAY_SECONDS = 0.20;
    private static final double REVERB_DELAY_SECONDS = 0.037;
    private final Map<UUID, State> states = new LinkedHashMap<>(32, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<UUID, State> eldest) {
            return size() > MAX_STREAMS;
        }
    };

    private static final class State {
        final int sampleRate;
        double lowPassState;
        double highPassInput;
        double highPassOutput;
        double gainState = 1.0;
        final float[] delayLine;
        final float[] reverbLine;
        int delayIndex;
        int reverbIndex;
        State(int sampleRate) {
            this.sampleRate = sampleRate;
            delayLine = new float[Math.max(2, (int) Math.ceil(sampleRate * MAX_DELAY_SECONDS) + 2)];
            reverbLine = new float[Math.max(2, (int) Math.ceil(sampleRate * REVERB_DELAY_SECONDS) + 2)];
        }
    }

    /** Compatibility overload for the original low-pass-only API. */
    public synchronized void process(UUID streamId, float[] samples, double cutoffHz, double sampleRate) {
        process(streamId, samples, new AcousticDspParameters(1.0, cutoffHz, 0.0, 0.0, 0.0), sampleRate);
    }

    public synchronized void process(UUID streamId, float[] samples,
                                     AcousticDspParameters parameters, double sampleRate) {
        if (streamId == null || samples == null || samples.length == 0) return;
        AcousticDspParameters settings = parameters == null ? AcousticDspParameters.neutral() : parameters;
        int sr = (int) Math.round(Math.max(8000.0, Math.min(MAX_SAMPLE_RATE,
                Double.isFinite(sampleRate) ? sampleRate : 48000.0)));
        State state = states.get(streamId);
        if (state == null || state.sampleRate != sr) {
            state = new State(sr);
            states.put(streamId, state);
        }

        double lowCut = Math.max(20.0, Math.min(sr * 0.45, settings.lowPassHz()));
        double lowAlpha = 1.0 - Math.exp(-2.0 * Math.PI * lowCut / sr);
        double highCut = Math.max(0.0, Math.min(sr * 0.20, settings.highPassHz()));
        double highAlpha = highCut <= 0.0 ? 0.0 : Math.exp(-2.0 * Math.PI * highCut / sr);
        int delaySamples = (int) Math.round(settings.delaySeconds() * sr);
        delaySamples = Math.max(0, Math.min(state.delayLine.length - 1, delaySamples));
        double wet = settings.reverbMix();

        for (int i = 0; i < samples.length; i++) {
            double input = Float.isFinite(samples[i]) ? samples[i] : 0.0;
            state.lowPassState += lowAlpha * (input - state.lowPassState);
            double filtered = state.lowPassState;
            if (highCut > 0.0) {
                double hp = highAlpha * (state.highPassOutput + filtered - state.highPassInput);
                state.highPassInput = filtered;
                state.highPassOutput = hp;
                filtered = hp;
            }
            state.gainState += (settings.gain() - state.gainState) * 0.0025;
            double gained = filtered * state.gainState;

            if (delaySamples > 0) {
                int read = state.delayIndex - delaySamples;
                if (read < 0) read += state.delayLine.length;
                double delayed = state.delayLine[read];
                state.delayLine[state.delayIndex] = (float) (gained + delayed * 0.18);
                state.delayIndex = (state.delayIndex + 1) % state.delayLine.length;
                gained = gained * 0.72 + delayed * 0.28;
            } else {
                state.delayLine[state.delayIndex] = (float) gained;
                state.delayIndex = (state.delayIndex + 1) % state.delayLine.length;
            }

            float reverb = state.reverbLine[state.reverbIndex];
            state.reverbLine[state.reverbIndex] = (float) (gained + reverb * 0.31);
            state.reverbIndex = (state.reverbIndex + 1) % state.reverbLine.length;
            double output = gained * (1.0 - wet) + reverb * wet;
            samples[i] = (float) Math.max(-1.0, Math.min(1.0, output));
        }
    }

    public synchronized void forget(UUID streamId) { if (streamId != null) states.remove(streamId); }
    public synchronized int activeStreams() { return states.size(); }
}
